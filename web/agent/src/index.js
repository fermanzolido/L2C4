/**
 * The bridge between the public website and the game database.
 *
 * Runs on the machine that hosts the server, makes outbound connections only, and
 * is the single place where anything from the internet is allowed to touch MySQL.
 *
 *   npm install && npm run keygen && node src/index.js
 */
import { appendFileSync, readFileSync, renameSync, statSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { constants, privateDecrypt } from 'node:crypto';
import { connect } from 'node:net';

import { Firestore } from './firestore.js';
import { GameDatabase, hashGamePassword } from './mysql.js';

const config = JSON.parse(readFileSync(new URL('../config.json', import.meta.url), 'utf8'));
const privateKey = readFileSync(new URL(`../${config.privateKeyFile.replace('./', '')}`, import.meta.url), 'utf8');
const accountNamePattern = new RegExp(config.accountNamePattern);

/**
 * Where log lines go besides the console.
 *
 * Task Scheduler discards stdout, so on the machine this actually runs on the
 * console goes nowhere and a failure leaves no trace. Rotation is one file deep:
 * enough that a crash loop cannot fill the disk, without keeping history nobody
 * reads.
 */
const logPath = fileURLToPath(new URL(`../${config.logFile.replace('./', '')}`, import.meta.url));
const rotatedLogPath = `${logPath}.1`;

const firestore = new Firestore(config.firestore);
const database = new GameDatabase(config);

let running = true;

/**
 * Undoes the Worker's RSA-OAEP encryption. Parameters must match the WebCrypto call
 * on the other side exactly: RSA-OAEP with SHA-256.
 */
function decryptPassword(ciphertextBase64) {
  return privateDecrypt(
    { key: privateKey, padding: constants.RSA_PKCS1_OAEP_PADDING, oaepHash: 'sha256' },
    Buffer.from(ciphertextBase64, 'base64')
  ).toString('utf8');
}

const handlers = {
  /**
   * Creates the game account. The plaintext exists only inside this function, and
   * the ciphertext is cleared from the job on the way out so it does not sit in
   * Firestore after it has been used.
   */
  async create_account(job) {
    const login = String(job.login ?? '').toLowerCase();
    if (!accountNamePattern.test(login)) {
      return { ok: false, reason: 'invalid_login', clearCiphertext: true };
    }

    let plaintext;
    try {
      plaintext = decryptPassword(job.passwordCiphertext);
    } catch {
      // A key rotation orphans jobs queued under the old key. Retrying cannot fix
      // that, so it is parked immediately rather than burning every attempt.
      return { ok: false, reason: 'undecryptable_password', clearCiphertext: true };
    }

    const result = await database.createAccount({
      login,
      passwordHash: hashGamePassword(plaintext),
      email: job.email,
    });

    if (!result.created) {
      return { ok: false, reason: result.reason, clearCiphertext: true };
    }

    log(`created game account ${login}`);
    return { ok: true, clearCiphertext: true };
  },

  /**
   * Replaces the game password after the website changed its own.
   *
   * Keeping the two together is the point. The in-game `.changepassword` reaches
   * only MySQL, so a player who uses it can still play but is left signed out of
   * the website until they change it here as well.
   */
  async change_password(job) {
    const login = String(job.login ?? '').toLowerCase();
    if (!accountNamePattern.test(login)) {
      return { ok: false, reason: 'invalid_login', clearCiphertext: true };
    }

    let plaintext;
    try {
      plaintext = decryptPassword(job.passwordCiphertext);
    } catch {
      return { ok: false, reason: 'undecryptable_password', clearCiphertext: true };
    }

    const updated = await database.updateAccountPassword({
      login,
      passwordHash: hashGamePassword(plaintext),
    });
    if (!updated) {
      return { ok: false, reason: 'account_not_found', clearCiphertext: true };
    }

    log(`changed the game password of ${login}`);
    return { ok: true, clearCiphertext: true };
  },

  /**
   * Adds premium days. The job id is derived from the Mercado Pago payment id, so a
   * webhook delivered twice writes the same document and cannot grant twice.
   */
  async grant_premium(job) {
    const accountName = String(job.accountName ?? '').toLowerCase();
    const days = Number(job.days);

    if (!accountNamePattern.test(accountName) || !Number.isInteger(days) || days < 1 || days > 365) {
      return { ok: false, reason: 'invalid_grant' };
    }

    // A payment for an account that no longer exists should be visible, not
    // silently written into a table nothing reads.
    if (!(await database.accountExists(accountName))) {
      return { ok: false, reason: 'account_not_found' };
    }

    const expiresAt = await database.grantPremium({ accountName, days });
    log(`granted ${days}d premium to ${accountName}, expires ${new Date(expiresAt).toISOString()}`);
    return { ok: true, expiresAt };
  },
};

async function processJob(job) {
  const handler = handlers[job.type];
  if (!handler) {
    return { ok: false, reason: `unknown_type:${job.type}` };
  }
  return handler(job);
}

async function drainJobs() {
  const jobs = await firestore.pendingJobs();
  for (const job of jobs) {
    if (!running) return;

    const attempts = (job.attempts ?? 0) + 1;
    let outcome;
    try {
      outcome = await processJob(job);
    } catch (error) {
      // An unexpected failure is worth retrying: the database may simply have been
      // restarting. A rejection the handler returned deliberately is not.
      log(`job ${job.id} threw: ${error.message}`);
      outcome = { ok: false, reason: 'error', retry: true, detail: error.message };
    }

    const parked = !outcome.ok && outcome.retry && attempts < config.maxAttempts;
    const fields = {
      status: outcome.ok ? 'done' : parked ? 'pending' : 'failed',
      attempts,
      result: outcome.reason ?? 'ok',
      finishedAt: new Date(),
    };
    if (outcome.detail) fields.detail = String(outcome.detail).slice(0, 500);
    if (outcome.expiresAt) fields.expiresAt = outcome.expiresAt;
    if (outcome.clearCiphertext) fields.passwordCiphertext = null;

    try {
      // The precondition is what makes a second agent harmless: whoever writes
      // first wins, and the other gets a 400 instead of repeating the work.
      await firestore.patch(job.name, fields, { ifUnchangedSince: job.updateTime });
    } catch (error) {
      log(`could not record the outcome of job ${job.id}: ${error.message}`);
    }
  }
}

/**
 * Whether a port on the server machine accepts a connection.
 *
 * Resolves rather than rejects: a refused connection is the ordinary answer when
 * the server is down, not an error to handle.
 */
function portOpen(host, port, timeoutMs) {
  return new Promise((resolve) => {
    const socket = connect({ host, port });
    const settle = (open) => {
      socket.destroy();
      resolve(open);
    };
    socket.setTimeout(timeoutMs);
    socket.once('connect', () => settle(true));
    socket.once('timeout', () => settle(false));
    socket.once('error', () => settle(false));
  });
}

/**
 * Publishes what the website shows as server status.
 *
 * Availability is read from the ports the client itself connects to, not from
 * whether MySQL answers -- the database runs whether or not the server does, and
 * reporting "online" from it told players the server was up while it was down.
 *
 * Both ports must answer. With the login server down nobody gets in even if the
 * game server is fine, and with the game server down the player is stranded after
 * picking a character; either way there is no playing.
 *
 * The player count is only read when the server is up. The game server sets
 * characters.online and clears it on a clean shutdown, so after a crash those flags
 * stay set and the table would report a crowd inside a server nobody is in.
 */
async function publishStatus() {
  const { host, loginPort, gamePort, probeTimeoutMs } = config.gameServer;
  let status;
  try {
    const [login, game] = await Promise.all([
      portOpen(host, loginPort, probeTimeoutMs),
      portOpen(host, gamePort, probeTimeoutMs),
    ]);
    const online = login && game;
    status = {
      online,
      players: online ? await database.onlinePlayers() : 0,
      // Which half is down, so a partial outage is diagnosable from the outside.
      loginServer: login,
      gameServer: game,
    };
  } catch (error) {
    status = { online: false, players: 0, error: error.code ?? 'unreachable' };
  }
  try {
    await firestore.setDocument('status', 'server', { ...status, checkedAt: new Date() });
  } catch (error) {
    log(`could not publish status: ${error.message}`);
  }
}

function log(message) {
  const line = `[${new Date().toISOString()}] ${message}`;
  console.log(line);

  try {
    if (statSync(logPath).size >= config.logMaxBytes) renameSync(logPath, rotatedLogPath);
  } catch {
    // Not existing yet is the ordinary case on a first run, and a rotation that
    // fails is not worth losing the line that triggered it.
  }

  try {
    appendFileSync(logPath, line + '\n');
  } catch {
    // A log that cannot be written must not take the agent down with it.
  }
}

/** Keeps a slow pass from overlapping the next one, which polling with setInterval would allow. */
async function loop(name, task, intervalMs) {
  while (running) {
    try {
      await task();
    } catch (error) {
      log(`${name} failed: ${error.message}`);
    }
    await new Promise((resolve) => setTimeout(resolve, intervalMs));
  }
}

async function shutdown(signal) {
  if (!running) return;
  running = false;
  log(`${signal} received, finishing the current pass`);
  await database.close();
  process.exit(0);
}

process.on('SIGINT', () => shutdown('SIGINT'));
process.on('SIGTERM', () => shutdown('SIGTERM'));

// Without this an uncaught error kills the agent silently, and the only evidence
// is that jobs stopped being applied. Exiting non-zero is deliberate: the
// scheduled task treats it as a failure and starts the agent again.
for (const event of ['uncaughtException', 'unhandledRejection']) {
  process.on(event, (error) => {
    log(`${event}: ${error?.stack ?? error}`);
    process.exit(1);
  });
}

log(`agent started, polling every ${config.pollIntervalMs}ms`);
await Promise.all([
  loop('jobs', drainJobs, config.pollIntervalMs),
  loop('status', publishStatus, config.statusIntervalMs),
]);

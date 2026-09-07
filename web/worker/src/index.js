/**
 * L2C4 public API.
 *
 * Nothing here touches the game database. Registrations and premium grants are
 * written as jobs into Firestore, which the agent on the server machine picks up.
 */
import { Firestore } from './firestore.js';
import { createPreference, fetchPayment, verifyWebhookSignature } from './mercadopago.js';
import {
  ABSENT_USER_VERIFIER,
  clearedSessionCookie,
  cookieValue,
  createSession,
  encryptForAgent,
  hashPassword,
  readSession,
  sessionCookie,
  verifyPassword,
} from './auth.js';

/** Must agree with accountNamePattern in the agent's config. */
const LOGIN_PATTERN = /^[a-z0-9]{4,14}$/;
const EMAIL_PATTERN = /^[^@\s]+@[^@\s.]+\.[^@\s]+$/;
const MIN_PASSWORD_LENGTH = 8;
/**
 * Not a policy choice: the client's own account field stops at 16 characters, so a
 * longer password can be stored and then never be typed at the login screen. The
 * in-game .changepassword command refuses past the same limit for the same reason.
 */
const MAX_PASSWORD_LENGTH = 16;

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    if (request.method === 'OPTIONS') {
      return new Response(null, { status: 204, headers: corsHeaders(env) });
    }

    try {
      const response = await route(request, env, url);
      for (const [key, value] of Object.entries(corsHeaders(env))) {
        response.headers.set(key, value);
      }
      return response;
    } catch (error) {
      // The message may name internal hosts, so it is logged and not returned.
      console.error(`${request.method} ${url.pathname}: ${error.stack ?? error.message}`);
      return json({ error: 'internal_error' }, 500, corsHeaders(env));
    }
  },
};

function route(request, env, url) {
  const post = request.method === 'POST';
  switch (url.pathname) {
    case '/api/register':
      return post ? register(request, env) : methodNotAllowed();
    case '/api/login':
      return post ? login(request, env) : methodNotAllowed();
    case '/api/logout':
      return post ? logout() : methodNotAllowed();
    case '/api/change-password':
      return post ? changePassword(request, env) : methodNotAllowed();
    case '/api/me':
      return me(request, env);
    case '/api/status':
      return status(env);
    case '/api/plans':
      return json({ plans: plans(env), currency: env.CURRENCY_ID });
    case '/api/checkout':
      return post ? checkout(request, env) : methodNotAllowed();
    case '/api/webhook/mercadopago':
      return post ? webhook(request, env) : methodNotAllowed();
    default:
      return json({ error: 'not_found' }, 404);
  }
}

async function register(request, env) {
  const body = await request.json().catch(() => ({}));
  const login = String(body.login ?? '').trim().toLowerCase();
  const email = String(body.email ?? '').trim();
  const password = String(body.password ?? '');

  if (!LOGIN_PATTERN.test(login)) return json({ error: 'invalid_login' }, 400);
  if (!EMAIL_PATTERN.test(email)) return json({ error: 'invalid_email' }, 400);
  if (password.length < MIN_PASSWORD_LENGTH || password.length > MAX_PASSWORD_LENGTH) {
    return json({ error: 'invalid_password' }, 400);
  }

  const firestore = new Firestore(env);

  // The login is the document id, so Firestore itself enforces uniqueness: a
  // second registration of the same name is a 409, with no read-then-write race.
  try {
    await firestore.create('users', login, {
      login,
      email,
      passwordHash: await hashPassword(password),
      createdAt: new Date(),
      gameAccountReady: false,
    });
  } catch (error) {
    if (error.status === 409) return json({ error: 'login_taken' }, 409);
    throw error;
  }

  // The plaintext leaves here encrypted to the agent's key and is never stored.
  await firestore.create('jobs', `create_${login}`, {
    type: 'create_account',
    login,
    email,
    passwordCiphertext: await encryptForAgent(password, env.AGENT_PUBLIC_KEY),
    status: 'pending',
    attempts: 0,
    createdAt: new Date(),
  });

  return json({ ok: true, login }, 201, {
    'Set-Cookie': sessionCookie(await createSession(login, login, env.SESSION_SECRET)),
  });
}

async function login(request, env) {
  const body = await request.json().catch(() => ({}));
  const name = String(body.login ?? '').trim().toLowerCase();
  const password = String(body.password ?? '');

  const user = await new Firestore(env).get('users', name);

  // Verify even when the user is missing, so a wrong name and a wrong password
  // take the same time and cannot be told apart.
  const valid = await verifyPassword(password, user?.passwordHash ?? ABSENT_USER_VERIFIER);
  if (!user || !valid) return json({ error: 'invalid_credentials' }, 401);

  return json({ ok: true, login: user.login }, 200, {
    'Set-Cookie': sessionCookie(await createSession(user.login, user.login, env.SESSION_SECRET)),
  });
}

function logout() {
  return json({ ok: true }, 200, { 'Set-Cookie': clearedSessionCookie });
}

async function currentUser(request, env) {
  const claims = await readSession(cookieValue(request, 'session'), env.SESSION_SECRET);
  if (!claims) return null;
  return new Firestore(env).get('users', claims.uid);
}

async function me(request, env) {
  const user = await currentUser(request, env);
  if (!user) return json({ error: 'unauthenticated' }, 401);

  const firestore = new Firestore(env);
  const [job, orders] = await Promise.all([
    firestore.get('jobs', `create_${user.login}`),
    firestore.queryEqual('orders', 'login', user.login, { limit: 10, orderBy: 'createdAt' }),
  ]);

  return json({
    login: user.login,
    email: user.email,
    // The game account is real only once the agent has written it.
    gameAccountReady: job?.status === 'done',
    gameAccountError: job?.status === 'failed' ? job.result : null,
    orders: orders.map((order) => ({
      id: order.id,
      months: order.months,
      amount: order.amount,
      status: order.status,
      createdAt: order.createdAt,
    })),
  });
}

/**
 * Changes both passwords at once.
 *
 * The site and the game hold separate verifiers of the same password, so changing
 * one alone splits them. Doing it here keeps them together; the in-game
 * .changepassword only reaches MySQL, and leaves this site on the old password.
 */
async function changePassword(request, env) {
  const user = await currentUser(request, env);
  if (!user) return json({ error: 'unauthenticated' }, 401);

  const body = await request.json().catch(() => ({}));
  const currentPassword = String(body.currentPassword ?? '');
  const newPassword = String(body.newPassword ?? '');

  if (newPassword.length < MIN_PASSWORD_LENGTH || newPassword.length > MAX_PASSWORD_LENGTH) {
    return json({ error: 'invalid_password' }, 400);
  }
  if (!(await verifyPassword(currentPassword, user.passwordHash))) {
    return json({ error: 'wrong_current_password' }, 401);
  }

  const firestore = new Firestore(env);

  // The game password is queued before the site one is replaced. If the agent were
  // to fail, the account is still reachable with the password the player knows,
  // rather than left with a site that moved on and a game that did not.
  await firestore.patch('jobs', `chpw_${user.login}_${Date.now()}`, {
    type: 'change_password',
    login: user.login,
    passwordCiphertext: await encryptForAgent(newPassword, env.AGENT_PUBLIC_KEY),
    status: 'pending',
    attempts: 0,
    createdAt: new Date(),
  });

  await firestore.patch('users', user.login, { passwordHash: await hashPassword(newPassword) });
  return json({ ok: true });
}

async function status(env) {
  const server = await new Firestore(env).get('status', 'server');
  if (!server) return json({ online: false, players: 0, checkedAt: null });

  // The agent publishes on a timer; if it stopped, the last reading is stale and
  // saying "online" from it would be a guess.
  const age = Date.now() - new Date(server.checkedAt).getTime();
  const stale = age > 5 * 60 * 1000;
  return json({
    online: stale ? false : server.online,
    players: stale ? 0 : server.players,
    checkedAt: server.checkedAt,
    stale,
  });
}

function plans(env) {
  return JSON.parse(env.PREMIUM_PLANS);
}

async function checkout(request, env) {
  const user = await currentUser(request, env);
  if (!user) return json({ error: 'unauthenticated' }, 401);

  const body = await request.json().catch(() => ({}));
  const plan = plans(env).find((candidate) => candidate.months === Number(body.months));
  if (!plan) return json({ error: 'unknown_plan' }, 400);

  const firestore = new Firestore(env);
  const orderId = crypto.randomUUID();

  await firestore.create('orders', orderId, {
    login: user.login,
    months: plan.months,
    // Days is what the agent grants with; months is what the buyer was sold.
    days: plan.days,
    amount: plan.price,
    currency: env.CURRENCY_ID,
    status: 'pending',
    createdAt: new Date(),
  });

  const preference = await createPreference(env, {
    orderId,
    months: plan.months,
    price: plan.price,
    login: user.login,
  });

  await firestore.patch('orders', orderId, { preferenceId: preference.id });
  return json({ orderId, checkoutUrl: preference.checkoutUrl });
}

/**
 * Mercado Pago calls this after a payment. It is unauthenticated by nature, so the
 * signature is the only thing standing between it and free premium.
 */
async function webhook(request, env) {
  const body = await request.json().catch(() => ({}));
  const paymentId = body?.data?.id;
  if (!paymentId) return json({ ok: true, ignored: 'no_payment_id' });

  if (!(await verifyWebhookSignature(request, env, paymentId))) {
    console.warn(`rejected a webhook with a bad signature for payment ${paymentId}`);
    return json({ error: 'bad_signature' }, 401);
  }

  const payment = await fetchPayment(env, paymentId);
  const orderId = payment.external_reference;
  if (!orderId) return json({ ok: true, ignored: 'no_reference' });

  const firestore = new Firestore(env);
  const order = await firestore.get('orders', orderId);
  if (!order) return json({ ok: true, ignored: 'unknown_order' });

  if (payment.status !== 'approved') {
    await firestore.patch('orders', orderId, { status: payment.status, mpPaymentId: String(paymentId) });
    return json({ ok: true, status: payment.status });
  }

  // What was actually paid, from Mercado Pago, against what the order asked for.
  // Trusting the order alone would let a tampered preference buy 30 days for the
  // price of 7.
  if (Number(payment.transaction_amount) < Number(order.amount)) {
    await firestore.patch('orders', orderId, { status: 'underpaid', mpPaymentId: String(paymentId) });
    console.warn(`payment ${paymentId} paid ${payment.transaction_amount} for an order of ${order.amount}`);
    return json({ ok: true, status: 'underpaid' });
  }

  // Mercado Pago retries. The job id is derived from the payment id, so a repeat
  // delivery collides on the document instead of granting premium a second time.
  try {
    await firestore.create('jobs', `grant_${paymentId}`, {
      type: 'grant_premium',
      accountName: order.login,
      days: order.days,
      orderId,
      status: 'pending',
      attempts: 0,
      createdAt: new Date(),
    });
  } catch (error) {
    if (error.status !== 409) throw error;
    return json({ ok: true, status: 'already_granted' });
  }

  await firestore.patch('orders', orderId, { status: 'paid', mpPaymentId: String(paymentId), paidAt: new Date() });
  return json({ ok: true, status: 'granted' });
}

function corsHeaders(env) {
  return {
    'Access-Control-Allow-Origin': env.SITE_ORIGIN,
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Allow-Credentials': 'true',
  };
}

function methodNotAllowed() {
  return json({ error: 'method_not_allowed' }, 405);
}

function json(body, statusCode = 200, headers = {}) {
  return new Response(JSON.stringify(body), {
    status: statusCode,
    headers: { 'Content-Type': 'application/json', ...headers },
  });
}

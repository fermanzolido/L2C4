/**
 * Everything this agent writes into the game databases.
 *
 * Two pools because the login server and the game server keep separate schemas:
 * `accounts` lives in the login database, `account_premium` and `characters` in the
 * game one. They are usually the same MySQL instance.
 */
import mysql from 'mysql2/promise';
import bcrypt from 'bcryptjs';

/**
 * The server's own cost factor, from GENSALT_DEFAULT_LOG2_ROUNDS in
 * commons/util/BCrypt.java.
 */
const BCRYPT_ROUNDS = 10;

/**
 * commons/util/BCrypt.java accepts `$2$` and `$2a$` and throws
 * "Invalid salt revision" on anything else, so a `$2b$` hash -- what most current
 * libraries emit -- produces an account that exists and can never log in. bcryptjs
 * emits `$2a$`, and this asserts it rather than trusting the dependency to keep
 * doing so across a major version.
 */
export function hashGamePassword(plaintext) {
  const hash = bcrypt.hashSync(plaintext, bcrypt.genSaltSync(BCRYPT_ROUNDS));
  if (!hash.startsWith('$2a$')) {
    throw new Error(
      `bcrypt produced "${hash.slice(0, 4)}" but the login server only reads $2$ and $2a$. ` +
        'Refusing to write an account that could never log in.'
    );
  }
  return hash;
}

export class GameDatabase {
  constructor({ loginDb, gameDb }) {
    const common = { waitForConnections: true, connectionLimit: 4, timezone: 'Z' };
    this.login = mysql.createPool({ ...loginDb, ...common });
    this.game = mysql.createPool({ ...gameDb, ...common });
  }

  async close() {
    await Promise.allSettled([this.login.end(), this.game.end()]);
  }

  /**
   * @returns {Promise<{created: boolean, reason?: string}>} created:false when the
   * login is already taken, which is an ordinary outcome and not an error.
   */
  async createAccount({ login, passwordHash, email }) {
    try {
      await this.login.execute(
        'INSERT INTO accounts (login, password, email, lastactive, accessLevel, lastIP) VALUES (?, ?, ?, 0, 0, NULL)',
        [login, passwordHash, email ?? null]
      );
      return { created: true };
    } catch (error) {
      if (error.code === 'ER_DUP_ENTRY') {
        return { created: false, reason: 'account_exists' };
      }
      throw error;
    }
  }

  /**
   * Replaces the bcrypt hash the login server checks against. Who is asking was
   * settled by the website before the job was queued; this is only the write.
   *
   * @returns {Promise<boolean>} false when there is no such account
   */
  async updateAccountPassword({ login, passwordHash }) {
    const [result] = await this.login.execute('UPDATE accounts SET password = ? WHERE login = ?', [
      passwordHash,
      login,
    ]);
    return result.affectedRows > 0;
  }

  async accountExists(login) {
    const [rows] = await this.login.execute('SELECT 1 FROM accounts WHERE login = ? LIMIT 1', [login]);
    return rows.length > 0;
  }

  /**
   * Extends premium rather than replacing it, from whichever is later: now, or the
   * time already on the account. This mirrors PremiumManager.addPremiumTime, so a
   * purchase stacks on top of a GM grant instead of erasing it.
   *
   * The row is locked for the read because the game server writes it too.
   *
   * @returns {Promise<number>} the new expiration, epoch milliseconds
   */
  async grantPremium({ accountName, days }) {
    const connection = await this.game.getConnection();
    try {
      await connection.beginTransaction();

      const [rows] = await connection.execute(
        'SELECT enddate FROM account_premium WHERE account_name = ? FOR UPDATE',
        [accountName]
      );

      const now = Date.now();
      const current = rows.length > 0 ? Number(rows[0].enddate) : 0;
      const newEnd = Math.max(now, current) + days * 24 * 60 * 60 * 1000;

      await connection.execute(
        'INSERT INTO account_premium (account_name, enddate) VALUES (?, ?) ON DUPLICATE KEY UPDATE enddate = VALUES(enddate)',
        [accountName, newEnd]
      );

      await connection.commit();
      return newEnd;
    } catch (error) {
      await connection.rollback().catch(() => {});
      throw error;
    } finally {
      connection.release();
    }
  }

  /**
   * What the website shows as "server status". Reaching the game database is the
   * signal: the game server is what sets `online`, so if it is down the number is
   * stale rather than wrong, and staleness is reported by checkedAt.
   */
  async serverStatus() {
    const [rows] = await this.game.execute('SELECT COUNT(*) AS players FROM characters WHERE online > 0');
    return { online: true, players: Number(rows[0].players) };
  }
}

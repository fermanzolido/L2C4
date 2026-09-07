/**
 * Password verification for the website, and the session cookie.
 *
 * The website cannot check the bcrypt hash the game uses -- that lives in MySQL,
 * which a Worker cannot reach -- so registration derives a second verifier from the
 * same password and keeps it in Firestore. PBKDF2 is used rather than bcrypt
 * because WebCrypto implements it natively; a JS bcrypt would burn Worker CPU on
 * every login for no security gain at these parameters.
 */

const PBKDF2_ITERATIONS = 210_000;
const SALT_BYTES = 16;
const KEY_BITS = 256;
const SESSION_TTL_SECONDS = 60 * 60 * 24 * 14;

const encoder = new TextEncoder();

function toBase64(bytes) {
  return btoa(String.fromCharCode(...new Uint8Array(bytes)));
}

function fromBase64(text) {
  return Uint8Array.from(atob(text), (character) => character.charCodeAt(0));
}

function base64Url(bytes) {
  return toBase64(bytes).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

async function pbkdf2(password, salt) {
  const key = await crypto.subtle.importKey('raw', encoder.encode(password), 'PBKDF2', false, ['deriveBits']);
  const bits = await crypto.subtle.deriveBits(
    { name: 'PBKDF2', hash: 'SHA-256', salt, iterations: PBKDF2_ITERATIONS },
    key,
    KEY_BITS
  );
  return new Uint8Array(bits);
}

/** @returns {Promise<string>} `pbkdf2$<iterations>$<salt>$<hash>`, all base64. */
export async function hashPassword(password) {
  const salt = crypto.getRandomValues(new Uint8Array(SALT_BYTES));
  const hash = await pbkdf2(password, salt);
  return `pbkdf2$${PBKDF2_ITERATIONS}$${toBase64(salt)}$${toBase64(hash)}`;
}

export async function verifyPassword(password, stored) {
  const [scheme, iterations, saltB64, hashB64] = String(stored ?? '').split('$');
  if (scheme !== 'pbkdf2' || !saltB64 || !hashB64) return false;

  const salt = fromBase64(saltB64);
  const expected = fromBase64(hashB64);
  const key = await crypto.subtle.importKey('raw', encoder.encode(password), 'PBKDF2', false, ['deriveBits']);
  const bits = new Uint8Array(
    await crypto.subtle.deriveBits(
      { name: 'PBKDF2', hash: 'SHA-256', salt, iterations: Number(iterations) },
      key,
      expected.length * 8
    )
  );

  // Constant time: a length check short-circuits, but both are our own output.
  if (bits.length !== expected.length) return false;
  let difference = 0;
  for (let i = 0; i < bits.length; i++) difference |= bits[i] ^ expected[i];
  return difference === 0;
}

async function hmacKey(secret) {
  return crypto.subtle.importKey('raw', encoder.encode(secret), { name: 'HMAC', hash: 'SHA-256' }, false, [
    'sign',
    'verify',
  ]);
}

/** A compact signed token: `<payload>.<signature>`, both base64url. */
export async function createSession(uid, login, secret) {
  const payload = base64Url(
    encoder.encode(JSON.stringify({ uid, login, exp: Math.floor(Date.now() / 1000) + SESSION_TTL_SECONDS }))
  );
  const signature = await crypto.subtle.sign('HMAC', await hmacKey(secret), encoder.encode(payload));
  return `${payload}.${base64Url(signature)}`;
}

export async function readSession(token, secret) {
  const [payload, signature] = String(token ?? '').split('.');
  if (!payload || !signature) return null;

  const valid = await crypto.subtle.verify(
    'HMAC',
    await hmacKey(secret),
    fromBase64(signature.replace(/-/g, '+').replace(/_/g, '/')),
    encoder.encode(payload)
  );
  if (!valid) return null;

  try {
    const claims = JSON.parse(new TextDecoder().decode(fromBase64(payload.replace(/-/g, '+').replace(/_/g, '/'))));
    return claims.exp > Math.floor(Date.now() / 1000) ? claims : null;
  } catch {
    return null;
  }
}

export function sessionCookie(token) {
  return `session=${token}; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=${SESSION_TTL_SECONDS}`;
}

export const clearedSessionCookie = 'session=; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=0';

export function cookieValue(request, name) {
  const header = request.headers.get('Cookie') ?? '';
  const match = header.match(new RegExp(`(?:^|;\\s*)${name}=([^;]*)`));
  return match ? match[1] : null;
}

/**
 * Encrypts the password for the agent, which holds the only private key.
 *
 * This is what keeps a plaintext password out of Firestore: the job carries
 * ciphertext that neither this Worker nor the database it writes to can undo.
 */
export async function encryptForAgent(password, publicKeyPem) {
  const body = publicKeyPem
    .replace(/-----BEGIN PUBLIC KEY-----/, '')
    .replace(/-----END PUBLIC KEY-----/, '')
    .replace(/\s+/g, '');

  const key = await crypto.subtle.importKey(
    'spki',
    fromBase64(body),
    { name: 'RSA-OAEP', hash: 'SHA-256' },
    false,
    ['encrypt']
  );

  return toBase64(await crypto.subtle.encrypt({ name: 'RSA-OAEP' }, key, encoder.encode(password)));
}

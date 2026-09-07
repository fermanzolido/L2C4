/**
 * Firestore from a Worker.
 *
 * There is no Google SDK here: the service account JWT is signed with WebCrypto and
 * exchanged for an access token, which is then cached for the life of the isolate.
 * A Worker isolate is short-lived, so this costs roughly one token exchange per cold
 * start rather than one per request.
 */

const SCOPE = 'https://www.googleapis.com/auth/datastore';
const TOKEN_URL = 'https://oauth2.googleapis.com/token';

const encoder = new TextEncoder();

let cachedToken = null;

function base64Url(input) {
  const bytes = typeof input === 'string' ? encoder.encode(input) : new Uint8Array(input);
  return btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
}

async function importServiceAccountKey(pem) {
  const body = pem
    .replace(/-----BEGIN PRIVATE KEY-----/, '')
    .replace(/-----END PRIVATE KEY-----/, '')
    .replace(/\s+/g, '');
  return crypto.subtle.importKey(
    'pkcs8',
    Uint8Array.from(atob(body), (character) => character.charCodeAt(0)),
    { name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-256' },
    false,
    ['sign']
  );
}

async function accessToken(serviceAccount) {
  // A minute of slack so a token is never used in the last moments of its life.
  if (cachedToken && cachedToken.expiresAt > Date.now() + 60_000) {
    return cachedToken.value;
  }

  const now = Math.floor(Date.now() / 1000);
  const claims = base64Url(
    JSON.stringify({
      iss: serviceAccount.client_email,
      scope: SCOPE,
      aud: TOKEN_URL,
      exp: now + 3600,
      iat: now,
    })
  );
  const header = base64Url(JSON.stringify({ alg: 'RS256', typ: 'JWT' }));
  const signature = await crypto.subtle.sign(
    'RSASSA-PKCS1-v1_5',
    await importServiceAccountKey(serviceAccount.private_key),
    encoder.encode(`${header}.${claims}`)
  );

  const response = await fetch(TOKEN_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      assertion: `${header}.${claims}.${base64Url(signature)}`,
    }),
  });

  if (!response.ok) {
    throw new Error(`Google token exchange failed: ${response.status} ${await response.text()}`);
  }

  const token = await response.json();
  cachedToken = { value: token.access_token, expiresAt: Date.now() + token.expires_in * 1000 };
  return cachedToken.value;
}

export class Firestore {
  constructor(env) {
    this.serviceAccount = JSON.parse(env.GCP_SERVICE_ACCOUNT);
    this.projectId = env.FIRESTORE_PROJECT_ID;
    this.base = `https://firestore.googleapis.com/v1/projects/${this.projectId}/databases/(default)/documents`;
  }

  async #request(url, options = {}) {
    const response = await fetch(url, {
      ...options,
      headers: {
        Authorization: `Bearer ${await accessToken(this.serviceAccount)}`,
        'Content-Type': 'application/json',
        ...(options.headers ?? {}),
      },
    });

    if (response.status === 404) return null;
    if (!response.ok) {
      const error = new Error(`Firestore ${response.status}: ${await response.text()}`);
      error.status = response.status;
      throw error;
    }
    return response.json();
  }

  async get(collection, id) {
    const document = await this.#request(`${this.base}/${collection}/${id}`);
    return document ? { id, ...decodeFields(document.fields) } : null;
  }

  /**
   * Creates a document at a chosen id and fails with 409 if it is already there.
   * That conflict is the mechanism behind two guarantees: a login can only be
   * claimed once, and a webhook delivered twice cannot grant premium twice.
   */
  async create(collection, id, fields) {
    return this.#request(`${this.base}/${collection}?documentId=${encodeURIComponent(id)}`, {
      method: 'POST',
      body: JSON.stringify({ fields: encodeFields(fields) }),
    });
  }

  async patch(collection, id, fields) {
    const mask = Object.keys(fields)
      .map((key) => `updateMask.fieldPaths=${encodeURIComponent(key)}`)
      .join('&');
    return this.#request(`${this.base}/${collection}/${id}?${mask}`, {
      method: 'PATCH',
      body: JSON.stringify({ fields: encodeFields(fields) }),
    });
  }

  async queryEqual(collection, field, value, { limit = 20, orderBy } = {}) {
    const structuredQuery = {
      from: [{ collectionId: collection }],
      where: { fieldFilter: { field: { fieldPath: field }, op: 'EQUAL', value: encodeValue(value) } },
      limit,
    };
    if (orderBy) {
      structuredQuery.orderBy = [{ field: { fieldPath: orderBy }, direction: 'DESCENDING' }];
    }

    const rows = await this.#request(`${this.base}:runQuery`, {
      method: 'POST',
      body: JSON.stringify({ structuredQuery }),
    });

    return (rows ?? [])
      .filter((row) => row.document)
      .map((row) => ({ id: row.document.name.split('/').pop(), ...decodeFields(row.document.fields) }));
  }
}

function encodeValue(value) {
  if (value === null || value === undefined) return { nullValue: null };
  if (typeof value === 'boolean') return { booleanValue: value };
  if (typeof value === 'number') {
    return Number.isInteger(value) ? { integerValue: String(value) } : { doubleValue: value };
  }
  if (value instanceof Date) return { timestampValue: value.toISOString() };
  if (Array.isArray(value)) return { arrayValue: { values: value.map(encodeValue) } };
  if (typeof value === 'object') return { mapValue: { fields: encodeFields(value) } };
  return { stringValue: String(value) };
}

function encodeFields(object) {
  return Object.fromEntries(Object.entries(object).map(([key, value]) => [key, encodeValue(value)]));
}

function decodeValue(value) {
  if ('nullValue' in value) return null;
  if ('booleanValue' in value) return value.booleanValue;
  if ('integerValue' in value) return Number(value.integerValue);
  if ('doubleValue' in value) return value.doubleValue;
  if ('timestampValue' in value) return value.timestampValue;
  if ('arrayValue' in value) return (value.arrayValue.values ?? []).map(decodeValue);
  if ('mapValue' in value) return decodeFields(value.mapValue.fields ?? {});
  return value.stringValue;
}

function decodeFields(fields = {}) {
  return Object.fromEntries(Object.entries(fields).map(([key, value]) => [key, decodeValue(value)]));
}

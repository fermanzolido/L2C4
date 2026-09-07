/**
 * Minimal Firestore REST client.
 *
 * The full SDK pulls in gRPC and a large dependency tree for the four operations
 * this agent needs, so this speaks the REST API directly and converts Firestore's
 * typed values at the edges.
 */
import { GoogleAuth } from 'google-auth-library';

const SCOPE = 'https://www.googleapis.com/auth/datastore';

export class Firestore {
  constructor({ projectId, serviceAccountKeyFile }) {
    this.projectId = projectId;
    this.root = `projects/${projectId}/databases/(default)/documents`;
    this.base = `https://firestore.googleapis.com/v1/${this.root}`;
    this.auth = new GoogleAuth({ keyFile: serviceAccountKeyFile, scopes: [SCOPE] });
    this.client = null;
  }

  async #request(url, options = {}) {
    if (!this.client) {
      this.client = await this.auth.getClient();
    }
    const { token } = await this.client.getAccessToken();
    const response = await fetch(url, {
      ...options,
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
        ...(options.headers ?? {}),
      },
    });

    if (!response.ok) {
      const body = await response.text();
      const error = new Error(`Firestore ${response.status}: ${body}`);
      error.status = response.status;
      throw error;
    }

    return response.status === 204 ? null : response.json();
  }

  /**
   * Pending jobs, oldest first. The limit keeps one slow job from starving the
   * rest and bounds how much a single pass can hold in memory.
   */
  async pendingJobs(limit = 20) {
    const body = {
      structuredQuery: {
        from: [{ collectionId: 'jobs' }],
        where: {
          fieldFilter: {
            field: { fieldPath: 'status' },
            op: 'EQUAL',
            value: { stringValue: 'pending' },
          },
        },
        orderBy: [{ field: { fieldPath: 'createdAt' }, direction: 'ASCENDING' }],
        limit,
      },
    };

    const rows = await this.#request(`${this.base}:runQuery`, {
      method: 'POST',
      body: JSON.stringify(body),
    });

    return (rows ?? [])
      .filter((row) => row.document)
      .map((row) => ({
        id: row.document.name.split('/').pop(),
        name: row.document.name,
        updateTime: row.document.updateTime,
        ...decodeFields(row.document.fields),
      }));
  }

  /**
   * Writes only the named fields, and only if the document has not changed since
   * it was read. Two agents racing on the same job means the loser gets a 400 and
   * moves on rather than applying the job twice.
   */
  async patch(name, fields, { ifUnchangedSince } = {}) {
    const mask = Object.keys(fields)
      .map((key) => `updateMask.fieldPaths=${encodeURIComponent(key)}`)
      .join('&');
    const precondition = ifUnchangedSince
      ? `&currentDocument.updateTime=${encodeURIComponent(ifUnchangedSince)}`
      : '';

    return this.#request(`https://firestore.googleapis.com/v1/${name}?${mask}${precondition}`, {
      method: 'PATCH',
      body: JSON.stringify({ fields: encodeFields(fields) }),
    });
  }

  async setDocument(collection, id, fields) {
    const mask = Object.keys(fields)
      .map((key) => `updateMask.fieldPaths=${encodeURIComponent(key)}`)
      .join('&');
    return this.#request(`${this.base}/${collection}/${id}?${mask}`, {
      method: 'PATCH',
      body: JSON.stringify({ fields: encodeFields(fields) }),
    });
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
  // Firestore returns 64-bit integers as strings so they survive JSON. Epoch
  // milliseconds fit in a double, so Number is safe for what this agent reads.
  if ('integerValue' in value) return Number(value.integerValue);
  if ('doubleValue' in value) return value.doubleValue;
  if ('timestampValue' in value) return new Date(value.timestampValue);
  if ('arrayValue' in value) return (value.arrayValue.values ?? []).map(decodeValue);
  if ('mapValue' in value) return decodeFields(value.mapValue.fields ?? {});
  return value.stringValue;
}

function decodeFields(fields = {}) {
  return Object.fromEntries(Object.entries(fields).map(([key, value]) => [key, decodeValue(value)]));
}

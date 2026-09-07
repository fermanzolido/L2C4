/**
 * Generates the RSA keypair that carries registration passwords from the Worker to
 * this agent.
 *
 * The Worker gets the public half and can only encrypt. The private half never
 * leaves this machine, so neither Cloudflare nor Firestore can read a password even
 * if both were fully compromised.
 *
 *   npm run keygen
 */
import { generateKeyPairSync } from 'node:crypto';
import { writeFileSync, existsSync } from 'node:fs';

const PRIVATE_FILE = './agent-private.pem';
const PUBLIC_FILE = './agent-public.pem';

if (existsSync(PRIVATE_FILE)) {
  console.error(
    `${PRIVATE_FILE} already exists. Delete it deliberately if you mean to rotate the key.\n` +
      'Rotating invalidates any registration job already queued with the old key.'
  );
  process.exit(1);
}

// 2048 with OAEP/SHA-256 matches what WebCrypto in a Worker imports without fuss.
const { publicKey, privateKey } = generateKeyPairSync('rsa', {
  modulusLength: 2048,
  publicKeyEncoding: { type: 'spki', format: 'pem' },
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' },
});

writeFileSync(PRIVATE_FILE, privateKey, { mode: 0o600 });
writeFileSync(PUBLIC_FILE, publicKey);

console.log(`Wrote ${PRIVATE_FILE} (keep it here, mode 0600) and ${PUBLIC_FILE}.`);
console.log('\nGive the public key to the Worker as a secret, in one line:\n');
console.log('  cd ../worker');
console.log(`  npx wrangler secret put AGENT_PUBLIC_KEY < ../agent/${PUBLIC_FILE.replace('./', '')}`);

/**
 * Mercado Pago: hosted checkout, and verification of the webhook that follows.
 *
 * No card data passes through here. The buyer is sent to Mercado Pago's own
 * checkout, and what comes back is a payment id this Worker looks up over the API.
 */

const API = 'https://api.mercadopago.com';

const encoder = new TextEncoder();

/**
 * Creates a checkout preference and returns where to send the buyer.
 *
 * `external_reference` carries our order id, so the webhook can be tied back to the
 * order without trusting anything the browser reports.
 */
export async function createPreference(env, { orderId, months, price, login }) {
  const response = await fetch(`${API}/checkout/preferences`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${env.MP_ACCESS_TOKEN}`,
      'Content-Type': 'application/json',
      // Makes a retried create return the same preference instead of a second one.
      'X-Idempotency-Key': orderId,
    },
    body: JSON.stringify({
      items: [
        {
          id: `premium-${months}m`,
          title: months === 1 ? '1 month of premium' : `${months} months of premium`,
          description: `Premium account for ${login}`,
          quantity: 1,
          currency_id: env.CURRENCY_ID,
          unit_price: price,
        },
      ],
      external_reference: orderId,
      // Without the extension, matching the site's cleanUrls: sending a buyer to
      // /account.html would bounce them through a redirect on the way back.
      back_urls: {
        success: `${env.SITE_ORIGIN}/account?paid=1`,
        pending: `${env.SITE_ORIGIN}/account?pending=1`,
        failure: `${env.SITE_ORIGIN}/account?failed=1`,
      },
      auto_return: 'approved',
      statement_descriptor: 'L2C4',
    }),
  });

  if (!response.ok) {
    throw new Error(`Mercado Pago preference failed: ${response.status} ${await response.text()}`);
  }

  const preference = await response.json();
  return { id: preference.id, checkoutUrl: preference.init_point };
}

/**
 * Verifies the `x-signature` header.
 *
 * Without this, anyone who found the webhook URL could post a payment id and be
 * granted premium. The manifest format and the header layout are Mercado Pago's.
 */
export async function verifyWebhookSignature(request, env, dataId) {
  const signatureHeader = request.headers.get('x-signature');
  const requestId = request.headers.get('x-request-id');
  if (!signatureHeader || !requestId) return false;

  const parts = Object.fromEntries(
    signatureHeader.split(',').map((piece) => piece.split('=').map((value) => value.trim()))
  );
  if (!parts.ts || !parts.v1) return false;

  const manifest = `id:${String(dataId).toLowerCase()};request-id:${requestId};ts:${parts.ts};`;
  const key = await crypto.subtle.importKey(
    'raw',
    encoder.encode(env.MP_WEBHOOK_SECRET),
    { name: 'HMAC', hash: 'SHA-256' },
    false,
    ['sign']
  );
  const signature = await crypto.subtle.sign('HMAC', key, encoder.encode(manifest));
  const expected = [...new Uint8Array(signature)].map((byte) => byte.toString(16).padStart(2, '0')).join('');

  if (expected.length !== parts.v1.length) return false;
  let difference = 0;
  for (let i = 0; i < expected.length; i++) difference |= expected.charCodeAt(i) ^ parts.v1.charCodeAt(i);
  return difference === 0;
}

/**
 * The payment as Mercado Pago holds it. The webhook body is only a pointer; the
 * amount and the status are read from here, never from what was posted to us.
 */
export async function fetchPayment(env, paymentId) {
  const response = await fetch(`${API}/v1/payments/${encodeURIComponent(paymentId)}`, {
    headers: { Authorization: `Bearer ${env.MP_ACCESS_TOKEN}` },
  });
  if (!response.ok) {
    throw new Error(`Mercado Pago payment lookup failed: ${response.status}`);
  }
  return response.json();
}

# Worker

The public API. Deploy after the agent, because one of the secrets comes from it.

## 1. Edit `wrangler.toml`

Set `SITE_ORIGIN`, `FIRESTORE_PROJECT_ID`, `CURRENCY_ID` and `PREMIUM_PLANS`.

`PREMIUM_PLANS` sets the price of each term. The terms themselves are 1, 2 and 3
months, matching what the server grants through `//premium_add1/2/3` at 30 days to
the month; only the prices are yours to move.

```toml
PREMIUM_PLANS = '[{"months":1,"days":30,"price":8000},{"months":2,"days":60,"price":15000},{"months":3,"days":90,"price":21000}]'
```

`days` is what the agent writes into `account_premium`; `months` is what the buyer
is shown and what `/api/checkout` is called with. Keep them at `months * 30` so a
purchase and a GM grant mean the same thing.

Prices are in whatever currency the Mercado Pago account settles in. Changing a
price does not disturb orders already placed — each order stores the amount it was
created with, and the webhook compares the payment against that.

## 2. Secrets

Never in `wrangler.toml` — that file is committed.

```bash
npx wrangler secret put SESSION_SECRET        # openssl rand -base64 32
npx wrangler secret put AGENT_PUBLIC_KEY < ../agent/agent-public.pem
npx wrangler secret put GCP_SERVICE_ACCOUNT   # paste the whole service account JSON
npx wrangler secret put MP_ACCESS_TOKEN
npx wrangler secret put MP_WEBHOOK_SECRET
```

Changing `SESSION_SECRET` logs everybody out. That is the way to end all sessions
at once if you ever need to.

## 3. Deploy

```bash
npm install
npx wrangler deploy
```

`wrangler.toml` claims `api.l2jsaked.com.ar` as a custom domain. The zone has to be
on Cloudflare for that to resolve; Wrangler creates the DNS record itself.

**Do not fall back to the workers.dev address.** The session cookie is
`SameSite=Lax`, and browsers do not send a Lax cookie on a cross-site fetch. The
site on `l2jsaked.com.ar` calling an API on `workers.dev` is cross-site by the
registrable-domain rule, so login would appear to succeed — the Worker sets the
cookie, the response is 200 — and every request after it would arrive
unauthenticated. A subdomain of the site's own domain is same-site, and the cookie
travels normally.

Relaxing this with `SameSite=None` is not the fix. That works until a browser
blocks third-party cookies, which Safari already does and Chrome is phasing in, and
then it breaks for those users only.

## 4. Mercado Pago

In the Mercado Pago developer panel, create the application and then, under
**Webhooks**, register:

```
https://<your-worker>.workers.dev/api/webhook/mercadopago
```

Subscribe to **payment** events. Mercado Pago shows a signing secret when you save
it — that is `MP_WEBHOOK_SECRET`.

The signature is checked on every call. Without it, anybody who found the URL could
post a payment id and be granted premium, so a deployment with the wrong secret
fails closed: every webhook is rejected with 401 and no premium is granted.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/register` | — | Creates the site user and queues the game account |
| POST | `/api/login` | — | Sets the session cookie |
| POST | `/api/logout` | — | Clears it |
| GET | `/api/me` | cookie | Account, game login state, recent orders |
| GET | `/api/status` | — | Server up, players online |
| GET | `/api/plans` | — | Plans and currency |
| POST | `/api/checkout` | cookie | Creates the order, returns the Mercado Pago URL |
| POST | `/api/webhook/mercadopago` | signature | Grants premium |

## Two things worth knowing

**Sessions are stateless.** A signed token in an httpOnly cookie, valid 14 days.
There is no session store, so an individual session cannot be revoked — only all of
them, by rotating `SESSION_SECRET`.

**The site never learns anyone's premium status.** That lives in MySQL, which this
Worker cannot reach. `/api/me` reports what was *ordered*; `.premium` in game
reports what is actually *active*. Keeping them separate means the site can never
show a confident number that the server disagrees with.

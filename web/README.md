# L2C4 Web

Public site for the server: information, account registration, and premium sales
through Mercado Pago.

## Why it is shaped like this

The game database is MySQL on a home machine. Cloudflare Workers cannot reach it,
and exposing MySQL to the internet to make them able to would be the worst
available trade. So the flow is inverted: nothing outside ever connects in. An
agent running next to the database polls Firestore for work and applies it
locally, using outbound connections only. **No inbound port is opened, and the
database is never reachable from outside the house.**

```
Player ──HTTPS──► Cloudflare Pages (site/)
                        │
                 Cloudflare Worker (worker/)
                   │              │
            Firestore ◄──── Mercado Pago webhook
                   │
             (jobs collection)
                   │
        Agent (agent/) ──localhost──► MySQL
```

## Four compatibility facts that constrain the code

These were read out of the server source, not assumed. Changing any of them
breaks logins silently, so they are stated here rather than buried.

**1. Passwords in `accounts` are bcrypt, revision `$2a$` only.**
`commons/util/BCrypt.java` rejects any other revision with `Invalid salt revision`
— see the `minor != 'a'` check in `hashpw`. Node's `bcrypt` and most modern
libraries emit `$2b$` by default, which this server cannot read. The agent pins
`$2a$` with cost 10 (the server's own `GENSALT_DEFAULT_LOG2_ROUNDS`). A hash with
the wrong revision produces an account that exists and can never log in.

**2. `account_premium.enddate` is epoch milliseconds** in a `decimal(20,0)`, and
premium is granted by extending from `max(now, current_enddate)` — never by
overwriting. The agent does this inside a transaction with `SELECT ... FOR UPDATE`,
because the game server writes the same row when a GM grants premium.

**3. The game server caches premium in memory and reads it once at login.**
A write from here is invisible to a player already online. `PremiumSystemConfig`
gained `PremiumRefreshInterval` (default 60s) so the server re-reads the table for
online accounts and applies purchases without a relog. Set it to 0 and buyers have
to reconnect.

**4. Passwords cannot be longer than 16 characters.** Not a policy: the client's own
account field stops there, so a longer password can be stored and then never typed
at the login screen. `PasswordChange.ini` says the same thing about the in-game
command, and the registration form here enforces 8–16 for the same reason.

## Passwords: two verifiers, one password

The site needs to authenticate people, and the game needs a bcrypt hash the login
server accepts. The Worker cannot compute bcrypt cheaply (it is deliberately slow,
and Workers bill CPU time), and the plaintext must not sit in Firestore.

So registration produces two things from one password:

- **Firestore** stores a PBKDF2-SHA256 verifier (100k iterations, WebCrypto native
  and fast in a Worker). This is what the website logs you in against. 100k is the
  Workers ceiling, not a preference: the runtime throws `NotSupportedError` above
  it, so the 210k OWASP suggests for PBKDF2-SHA256 is out of reach here.
- **MySQL** gets the bcrypt `$2a$` hash the game requires, computed by the agent.

The password reaches the agent encrypted with **RSA-OAEP** using a public key the
Worker holds; only the agent has the private key. A full compromise of the Worker
and Firestore together still does not reveal any password. The ciphertext is
deleted from the job as soon as the account is created.

Two verifiers can drift apart, so the account page changes **both** at once. The
in-game `.changepassword` reaches only MySQL: a player who uses it keeps playing
and can no longer sign in here. Two ways out, and they are a choice rather than a
bug to fix — leave `AllowChangePassword = False` in `PasswordChange.ini`, which is
where it currently sits, and the site is the only way to change a password; or turn
it on and tell players that changing it in game signs them out of the site.

## Layout

| Path | Runs on | Purpose |
|---|---|---|
| `site/` | Cloudflare Pages | Static frontend |
| `worker/` | Cloudflare Workers | API, sessions, Mercado Pago |
| `agent/` | The home server | Applies jobs to MySQL, publishes status |

## Firestore model

```
users/{uid}              login, email, pbkdf2 verifier, createdAt, gameAccountReady
orders/{orderId}         uid, login, days, amount, status, mpPaymentId, createdAt
jobs/{jobId}             type, payload, status, attempts, result, createdAt
status/server            online, players, checkedAt
```

`jobs` ids are deterministic where they can be: a premium grant uses
`grant_<mercadoPagoPaymentId>`, so a webhook delivered twice writes the same
document instead of granting twice. Mercado Pago retries webhooks, so this is not
hypothetical.

## Where this runs

`l2jsaked.com.ar` serves `site/` from Cloudflare Pages, project `l2c4-site`. The API
answers on `api.l2jsaked.com.ar`, and the agent runs on the server machine as a
scheduled task. `www` is a 301 to the apex.

**Pages rather than Firebase Hosting, even though `firebase.json` still configures
Hosting.** That block is dead; the `firestore` block in the same file is not, and is
how rules and indexes are deployed. The reason is not preference: this domain had its
free Hosting quota consumed by an attack. Pages bills no bandwidth on the free plan,
so a flood of traffic costs nothing and takes nothing down.

**The `www` redirect is a Redirect Rule on the zone, not a `_redirects` file and not a
Pages Function.** `_redirects` cannot do it — Pages matches those on path alone and
ignores a rule with a hostname in the source, silently. A `functions/_middleware.js`
can, but it intercepts every visit to the site and spends a Workers invocation on each
one, from the same free allowance the API draws on. A zone rule is evaluated at the
edge for free.

One origin is deliberate too. The Worker answers `Access-Control-Allow-Origin` with
`SITE_ORIGIN` exactly, so a page served from `www` would have every call to the API
blocked by the browser. Redirecting rather than serving both keeps the site, the CORS
header and the URLs Mercado Pago returns buyers to all naming the same origin.

### What the cutover was actually like

The apex used to serve a High Five server's site from Firebase Hosting. A warning
stood here saying the deny-all rules would break it, because that site read Firestore
from the browser. It did not: the database held no collections at all, so the old site
never touched it, and the rules were published without disturbing anything.

The DNS is what bit. Attaching the apex to Pages removed the record pointing at
Hosting before the replacement existed, and the domain answered nothing for the
minutes the custom domain spent activating. Note the record before replacing it — the
apex pointed at `199.36.158.100` — so a rollback is restoring one A record rather than
a reconstruction.

Expect the domain to look broken from your own machine for a while afterwards, and do
not chase it: a resolver that asked during the gap caches the empty answer and keeps
serving `NXDOMAIN` until that negative TTL expires. Check against the authoritative
nameservers, or from a phone on mobile data, before believing the site is down.

## Setup

Each part has its own README. Order matters:

1. `agent/` — generate the RSA keypair and a Firestore service account first;
   the Worker needs the public key and cannot be configured without it.
2. `worker/` — deploy with secrets set.
3. `site/` — `npx wrangler pages deploy site --project-name=l2c4-site`, then
   attach the apex as a custom domain from the dashboard and add the `www`
   Redirect Rule. Rules and indexes go up separately, with
   `firebase deploy --only firestore:rules` and `--only firestore:indexes`.

## What is not handled here

- **Chargebacks.** Mercado Pago can reverse a payment after premium is granted.
  Nothing revokes it automatically; `orders` keeps the payment id so it can be
  found and revoked by hand with `//premium_remove`.
- **A forgotten password.** Changing a password you know is covered twice over —
  here on the account page, and in game with `.changepassword`. Recovering one you
  have forgotten is not: that needs an email sender, which is a separate decision.
  Until then it is a manual job, and the account page is the only place a player
  can change both credentials at once.
- **Rate limiting** beyond what Cloudflare does by default. If registration gets
  abused, add a Turnstile challenge to the register form.

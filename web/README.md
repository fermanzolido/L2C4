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

## Replacing what is on the domain now

This is not a deployment onto empty space. `l2jsaked.com.ar` currently serves a
High Five server's site from Firebase Hosting on the same project, and that site
reads Firestore from the browser. Two steps here take it down, so their order is
what keeps the old site alive until the new one is ready rather than during a gap:

**Publishing the deny-all rules breaks the old site immediately.** The rules are
what stop a stranger writing a `jobs` document and granting themselves premium, so
they are not optional — but they also end the old site's Firestore access the
moment they land. Publish them at the cutover, not while preparing.

**Pointing the apex at Pages is what actually swaps the site.** Until the DNS
record changes, the old site keeps serving no matter what else is deployed.

So: build everything else first — Firestore database, indexes, agent, Worker on
`api.l2jsaked.com.ar` — and verify the API answers. Only then publish the rules and
move the apex, in that order and close together.

One thing to check in the console before any of it: this project already holds the
old site's collections. If any of them is named `users`, `orders`, `jobs` or
`status`, the two applications would be writing into the same place, and the
collections here need a prefix first.

## Setup

Each part has its own README. Order matters:

1. `agent/` — generate the RSA keypair and a Firestore service account first;
   the Worker needs the public key and cannot be configured without it.
2. `worker/` — deploy with secrets set.
3. `site/` — point Cloudflare Pages at this directory.

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

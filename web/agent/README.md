# Agent

Runs on the machine hosting the game server. Makes outbound connections only.

Set this up **first** — the Worker needs the public key produced here.

## 1. Install and generate the keypair

```bash
cd web/agent
npm install
npm run keygen
```

That writes `agent-private.pem` (stays here, mode 0600) and `agent-public.pem`
(goes to the Worker). Both are gitignored.

Rotating the key later invalidates any registration job still queued under the old
one; those jobs park as `undecryptable_password` and the person has to register
again.

## 2. Firebase service account

In the Firebase console: **Project settings → Service accounts → Generate new
private key**. Save the JSON as `agent/service-account.json`.

The agent needs read and write on `jobs` and `status`. If you lock Firestore down
with rules, note that a service account bypasses them — the restriction has to be
an IAM role on the service account itself.

## 3. Configure

```bash
cp config.example.json config.json
```

Fill in both database blocks. They are usually the same MySQL host with different
schema names — the login server owns `accounts`, the game server owns
`account_premium` and `characters`.

Give it a MySQL user that can do only what it needs:

```sql
CREATE USER 'l2c4_web'@'localhost' IDENTIFIED BY 'something long';
GRANT SELECT, INSERT, UPDATE ON l2c4_login.accounts TO 'l2c4_web'@'localhost';
GRANT SELECT, INSERT, UPDATE ON l2c4_game.account_premium TO 'l2c4_web'@'localhost';
GRANT SELECT ON l2c4_game.characters TO 'l2c4_web'@'localhost';
FLUSH PRIVILEGES;
```

UPDATE on `accounts` is what lets the site change a password. No DELETE and no
DROP: a mistake here should not be able to remove an account or a premium row.

## 4. Run

```bash
npm start
```

To keep it running, on Windows use Task Scheduler with "run whether user is logged
on or not"; on Linux a systemd unit with `Restart=always`.

## What it does

| Job type | Effect |
|---|---|
| `create_account` | Decrypts the password, bcrypts it as `$2a$` cost 10, inserts into `accounts` |
| `change_password` | Same hashing, but updates the row — keeps the game password level with the site one |
| `grant_premium` | Extends `account_premium.enddate` from `max(now, current)` inside a transaction |

It also writes `status/server` every 30 seconds so the website can show whether the
server is up and how many people are on.

## Checking it works

```bash
mysql -e "SELECT login, LEFT(password, 4) AS bcrypt_rev, created_time FROM accounts ORDER BY created_time DESC LIMIT 5" l2c4_login
```

`bcrypt_rev` must read `$2a$`. Anything else and that account can never log in —
the server's BCrypt rejects other revisions outright.

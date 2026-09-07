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

Fill in both database blocks. The login server owns `accounts` and the game server
owns `account_premium` and `characters`, and the two blocks exist because those can
live in separate schemas — but they are read from the servers' own `Database.ini`,
not chosen here, and a stock L2J Mobius install points both at one database. On this
server that is `l2jmobiusc4`, so both blocks name it and the agent simply opens two
pools against the same place.

`gameServer` is separate from either: those are the ports the client connects to,
checked over TCP to decide whether the server is up. They have to agree with
`LoginserverPort` in `login/config/Server.ini` and `GameserverPort` in
`game/config/Server.ini`. Asking MySQL instead would not work — the database answers
whether or not the game server is running.

Give it a MySQL user that can do only what it needs. Against a single schema:

```sql
CREATE USER 'l2c4_web'@'localhost'  IDENTIFIED BY 'something long';
CREATE USER 'l2c4_web'@'127.0.0.1'  IDENTIFIED BY 'the same thing';

GRANT SELECT, INSERT, UPDATE ON l2jmobiusc4.accounts        TO 'l2c4_web'@'localhost';
GRANT SELECT, INSERT, UPDATE ON l2jmobiusc4.account_premium TO 'l2c4_web'@'localhost';
GRANT SELECT                  ON l2jmobiusc4.characters     TO 'l2c4_web'@'localhost';

GRANT SELECT, INSERT, UPDATE ON l2jmobiusc4.accounts        TO 'l2c4_web'@'127.0.0.1';
GRANT SELECT, INSERT, UPDATE ON l2jmobiusc4.account_premium TO 'l2c4_web'@'127.0.0.1';
GRANT SELECT                  ON l2jmobiusc4.characters     TO 'l2c4_web'@'127.0.0.1';

FLUSH PRIVILEGES;
```

Both hosts, because the agent connects to `127.0.0.1` and whether MySQL matches that
against `localhost` or against the literal address depends on whether name resolution
is on. Granting one and not the other produces an access-denied that looks like a
wrong password.

Split schemas instead? Then name each database where it belongs, and set the two
config blocks to match.

UPDATE on `accounts` is what lets the site change a password. No DELETE and no DROP:
a mistake here should not be able to remove an account or a premium row.

## 4. Run

```bash
npm start
```

To keep it running, on Linux use a systemd unit with `Restart=always`. On Windows,
Task Scheduler, with three things that are not obvious:

**Point the action at `node` and the script, not at `npm start`.** npm launches node
as a child and does not pass a stop along to it, so stopping the task leaves the
agent running. Start it again and two agents write the same status document, each
overwriting fields the other just set.

**"Run whether user is logged on or not" needs an administrator.** Registering the
task with that principal — S4U, which stores no password — fails with access denied
from an ordinary console, and so does editing the task afterwards. Without it the
agent only runs while someone is signed in.

**Add a trigger that repeats every minute, and set multiple instances to "ignore the
new instance".** The "restart if the task fails" setting does not cover a process
killed from outside, which is what a crash looks like; the task simply ends and
nothing brings it back until the machine reboots. A repeating trigger does: the
attempt is discarded while the agent is running and starts it when it is not.

The agent writes `agent.log` beside this file, rotating one generation deep, because
Task Scheduler discards stdout and a failure would otherwise leave nothing to read.

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
mysql -e "SELECT login, LEFT(password, 4) AS bcrypt_rev, created_time FROM accounts ORDER BY created_time DESC LIMIT 5" l2jmobiusc4
```

`bcrypt_rev` must read `$2a$`. Anything else and that account can never log in —
the server's BCrypt rejects other revisions outright.

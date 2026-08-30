# L2C4 — Chronicle 4: Scions of Destiny

**English** · [Español](README.es.md)

A Lineage 2 Chronicle 4 server, built on [L2J Mobius](http://www.l2jmobius.org/), running on **Java 25**.

What separates this fork from the pack is not a feature list. It is that **every claim below was measured, with a denominator**, and the measurement is in the commit history where you can check it.

---

## Why this one

Most datapacks tell you what they added. This one can tell you what it *verified*, and how many things it looked at to say so.

Every fix in this repository came out of the same method: **enumerate the whole population, then fix only the ones missing the guard the rest already carry.** A lock that 6 of 7 call sites take. A `try-with-resources` that 354 of 354 connections use. A null check that 198 of 198 kill handlers perform. When the outlier is a handful against hundreds, that outlier is a bug. When the proportion turns over — 37 against 6 — there is no bug, there is a habit, and the sweep gets thrown away instead of dressed up as findings.

That discipline cuts both ways, and the record keeps the failures too. Four separate sweeps over quest logic produced nothing usable and were abandoned; one sweep had to be rewritten four times and still could not discriminate, so it was dropped. **A zero only means something when you know how many things were examined to reach it.**

---

## What has been verified

Each of these is a count taken by parsing the actual data or source, not an estimate.

| Area | Result |
|---|---|
| XML data files validating against their XSD | **1,448 / 1,448** |
| NPC ids defined more than once | **0** of 5,784 |
| Database connections outside `try-with-resources` | **0** of 354 |
| SQL calls fully parameterised | **468** of 479 — the other 11 paste compile-time constants or identifiers, which JDBC cannot bind |
| Path from remote input to unparameterised SQL | **none** |
| OS handles (files, directories, jars) properly scoped | **25** of 26 — the last is a socket field closed in a `finally` |
| `InterruptedException` handlers that answer the interrupt | **16 / 16** |
| Classes defining `equals` and `hashCode` consistently | **15 / 15** |
| Recurring tasks protected against a throwing body | **69 / 69** |
| String comparisons made by reference instead of value | **0** of 5,146, against 1,244 by value |
| Spawn points inside the world grid | **10,167 / 10,167** |
| Class-change dialogue pages present for C4 NPCs | **222 / 222** |

Plus **54 unit tests** across 15 classes, and a CI workflow that compiles the core, runs the tests, **and separately compiles all 784 datapack scripts** — because `build.xml` only builds `java/`, so a core change that breaks a script would otherwise pass a green build.

---

## What was fixed

### Concurrency and data integrity

- **Item duplication under concurrent deposits.** Both `addItem` overloads merged into an existing stack without holding its monitor, while `destroyItem` and `transferItem` did. A test with 8 threads × 2,000 additions lost roughly half the deposits against the unlocked version — 8,519 of an expected 17,000. `transferItem` now takes both monitors in object-id order.
- **`WorldObject` compared by object id and inherited the identity hash.** Equal objects landed in different hash buckets, and the repository declares around a hundred sets and maps that hold world objects directly. Covered by five tests, four of which fail against the old code.
- **Variable saves discarded their changes on failure.** The `finally` cleared the change tracking even when the write had failed, so pending changes were never retried and memory diverged from the database in silence. Fixing it required making the insert idempotent, which required giving `character_variables` and `item_variables` the primary key that `account_gsdata` already had — without it those tables can hold two rows for the same variable, and which one a player reads depends on row order.
- **A failed id-space build no longer hands out ids.** Each of the five queries collecting used object ids swallowed its own failure, so one query that did not finish silently removed a whole table's ids and the server reissued ids that live rows already held. Irreversible damage, quietly. The extraction now reports incompleteness and the manager refuses rather than guess.

### Resource leaks

Ten file handles were released only on the happy path, with the `close()` as the last line of a `try` whose `catch` merely logged — including a directory held open once per backup, the server's own jar whenever a manifest lacked a build date, and a `Files.walk` opened inside another `Files.walk`'s try block and never closed.

### Failures that could not be seen

- **Task exceptions went to stderr, never to the log.** The thread pool contained them correctly, but no thread carried a handler and none was installed globally, so every task failure in the server landed outside the log the rest of it writes to.
- **Six threads ignored their own interrupt**, including one non-daemon thread in a loop whose only other exit was every olympiad game reporting itself finished — one stuck game could hold the JVM open with no way to ask it to stop.

### Content and data

- **An NPC id claimed by two features at once.** `900103` was declared both as Core's teleport cube and as the Race Manager. The loader merges rather than replaces, so the surviving NPC was a hybrid with one feature broken — and *which* one was not fixed, because NPC files are parsed on a thread pool. The cube moved to a free id; both work now.
- **Dead links repaired rather than deleted.** A "Quest" button wired to a bypass nothing implemented, when 72 other pages use the one that exists. Dialogue options pointing at a convention no handler reads, when the page that literally answers the question was already a handled event. A reward page misspelled by one character, so a second click did nothing at all.
- **A drop chance that could not do what it said.** `getRandom(1000) < 20.200001` rolls 21 in a thousand, not 20.2 — `getRandom` returns an int. One of 398 such comparisons; the other 397 use integers.
- **Crafting charged short.** The per-pass HP and MP cost of a recipe divided an int by an int into a double, so a cost of 3 spread over 5 passes charged nothing.

---

## Server configuration

The rates are set as **one curve, not independent knobs.**

**Experience is x1** — retail pace, including skill points, party rates, quest experience and pet rates. Drops and spoil sit at **x2**, raid drops at x2, adena at **x3**, quest rewards at x2 with quest adena at x3. The reason: at x1 a character stays at each level about five times longer, so leaving drops at x5 would mean five times the loot across five times the kills, and gear would run far ahead of the level, trivialising the grind x1 exists to preserve.

What is already right for x1 is left alone — autoloot on, raid autoloot off, retail vitality, retail delevel and death penalty, and champion monsters, which at five percent frequency and five times the health are close to neutral per unit of time.

### The scheme buffer

A buffer NPC stands in **all fifteen towns**, positioned from each town's own gatekeeper.

- **Free for everyone to level 30.** Above that it serves premium accounts only, and tells anyone else to go find a Prophet or a Swordsinger.
- **No third-class buffs.** Which seven those are was computed from the class skill trees — the lowest tier that learns each buff, derived from the parent chain in `PlayerClass` — not from memory. Elemental, Divine and Arcane Protection, Siren's Dance, and the Songs of Renewal, Meditation and Champion are learned only by a Cardinal, an Eva's Saint, a Shillien Saint, a Hierophant, a Sword Muse or a Spectral Dancer. Leaving them out is what gives those classes something the NPC cannot do.
- **43 buffs, each at the lowest level of its skill.**
- **One hour duration, applied globally** rather than to the NPC alone — so a real Prophet's Might is never the worse option than the cat's.

---

## Building

Requires **JDK 25** and Apache Ant.

```bash
ant
```

The build compiles `java/`, produces `LoginServer.jar`, `GameServer.jar` and `DatabaseInstaller.jar`, and assembles `L2J_Mobius_C4_ScionsOfDestiny.zip` in `../build` alongside the datapack.

To run the tests as CI does:

```bash
ant test
```

Then set up the database with `db_installer/DatabaseInstaller`, and start `login/LoginServer` followed by `game/GameServer`.

---

## What has *not* been verified

Stated plainly, because the numbers above are only worth what their limits are worth.

**The server has never been started.** Everything here is compilation, XSD validation, cross-referenced data, and unit tests. Nothing has been observed with a client connected. The changes that would most benefit from a live pass are the buffer NPC positions, which are placed beside each gatekeeper but not eyeballed in game, and the primary key added to `character_variables`, which on a database that already holds duplicate rows needs the one-time migration written into that schema file.

Balance beyond the rate curve is untouched: no skill damage, no drop tables, no NPC stats were rebalanced. This is a Chronicle 4 server with C4 numbers.

---

## Credits

Built on [L2J Mobius](http://www.l2jmobius.org/) by Mobius and contributors. Chronicle 4 content list in [`readme.txt`](readme.txt).

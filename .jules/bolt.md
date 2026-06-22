## 2026-06-20 - [Optimize target list collection in Creature.java]
**Learning:** `LinkedList` was used in `Creature.onMagicLaunchedTimer`, which is a high-frequency combat path for target filtering. Replacing it with `ArrayList` and pre-allocating capacity based on the input `targets` list reduces `Node` object allocations and improves CPU cache locality.
**Action:** Always check for `LinkedList` usage in hot combat paths (like `onMagicLaunchedTimer`, `doCast`, etc.) and refactor to `ArrayList` with appropriate initial capacity.

## 2026-06-20 - [Compilation Workaround for Creature.java]
**Learning:** In this environment, `ant compile` may fail due to target release mismatches. When manually compiling `Creature.java` using `javac`, you must also include `java/org/l2jmobius/gameserver/config/PlayerConfig.java` to resolve symbols like `ANTI_CHEAT_RANGE_ENABLE`.
**Action:** If `ant` fails, use `javac -cp "dist/libs/*" -d build/bin ...` and include necessary dependency source files.

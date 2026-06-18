## 2026-06-18 - [Hot-path Collection Discrepancy]
**Learning:** Even when project memory suggests an optimization (like LinkedList to ArrayList migration) is complete, core hot-paths in `World.java` and `InstanceWorld.java` may still contain legacy collection types. Cache locality in visibility checks is a recurring bottleneck in this architecture.
**Action:** Always manually grep for `LinkedList` in `org.l2jmobius.gameserver.model` and `network.serverpackets` regardless of what historical logs state.

## 2026-06-18 - [Build Environment Target Mismatch]
**Learning:** The sandbox environment may report `javac 25` but fail to recognize `target="25"` in Ant `build.xml`.
**Action:** If `ant compile` fails with "invalid target release: 25", temporarily downgrade to `target="21"` for local verification and ensure `build/bin` is created manually if Ant fails to do so.

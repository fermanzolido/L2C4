## 2026-06-04 - Optimize NPC collection in InstanceWorld
**Learning:** In high-frequency game loop paths, prefer `ArrayList` for entity collections returned by getters; pre-allocate capacity if the source size is known and the source collection is not an O(n) size() type like `ConcurrentLinkedQueue`.
**Action:** Replaced `LinkedList` with pre-allocated `ArrayList` in `InstanceWorld.java` NPC retrieval methods. This reduces object allocation and improves cache locality for iterated results.

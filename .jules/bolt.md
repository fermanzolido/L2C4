## 2026-07-04 - Collection Anti-patterns and Type Safety in Hot Paths
**Learning:** `LinkedList` is an anti-pattern for visibility and NPC lookup hot paths in this codebase due to poor CPU cache locality and memory overhead. However, when refactoring to `ArrayList`, special care must be taken with dependent logic (like `Skill.getTargetList`) that performs `instanceof` checks on mutable list implementations for monster buff filtering.

**Action:** Always verify dependent `instanceof` checks when changing core collection types. Prefer `ArrayList` for iteration-heavy methods and pre-allocate capacity using `size()` where available (e.g., `WorldRegion` or `Instance`).

## 2026-07-04 - JDBC Batching vs. Data Integrity
**Learning:** Naive JDBC batching in a best-effort persistence loop (like `OfflineTraderTable.storeOffliners`) can compromise data integrity if exceptions are swallowed per-iteration. Moving `executeBatch()` outside the loop without atomic transactions or proper batch state management can lead to orphaned records if child table inserts succeed while parent table inserts fail (or are skipped).

**Action:** Prioritize data integrity over micro-optimizations in persistence layers. Only use JDBC batching if the entire operation can be made atomic or if state consistency is guaranteed across batches.

## 2026-07-04 - LinkedList is an anti-pattern for hot paths
**Learning:** LinkedList is an anti-pattern for visibility hot paths and NPC collections in this codebase due to O(N) access and high allocation overhead. Migrating to ArrayList improves cache locality and iteration performance. However, care must be taken with pre-allocation logic; avoids double-iteration of source data (e.g., getting a count first) as the overhead of two passes can outweigh resizing benefits.
**Action:** Favor ArrayList for all collection-returning methods in World and InstanceWorld. When refactoring, always verify dependent logic like Skill.java that might perform `instanceof` checks on the returned list type to maintain functional correctness (e.g., monster buff filtering).

## 2026-07-04 - JDBC Batching Caveat in Shutdown
**Learning:** Attempting to batch OfflineTraderTable.storeOffliners by removing intermediate clearParameters() and executeUpdate() calls in favor of a single final batch execution can compromise data persistence during server shutdown. If the loop is large or an error occurs late, no data may be saved.
**Action:** Prioritize data integrity on shutdown paths over micro-optimizations that significantly alter the persistence pattern.

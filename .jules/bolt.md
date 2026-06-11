## 2026-06-10 - Optimized GrandBossManager with JDBC Batching
**Learning:** Found an N+1 query bottleneck in `GrandBossManager.storeMe()` where `INSERT` statements for player lists were being executed individually within a nested loop. This pattern scales poorly as the number of players in boss zones increases.
**Action:** Applied JDBC batching using `addBatch()` and `executeBatch()` to consolidate multiple inserts into a single database roundtrip. Always audit persistence loops for `executeUpdate()` calls.

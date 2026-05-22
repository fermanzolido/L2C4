## 2026-05-21 - [JDBC Batching in GrandBossManager and Quest]
**Learning:** Found N+1 query patterns in GrandBossManager and Quest script handlers where multiple database updates were performed sequentially in loops.
**Action:** Implemented JDBC batching (addBatch/executeBatch) and added defensive empty-list checks to reduce database round-trips and connection overhead. Improved resource management by including PreparedStatement in try-with-resources.

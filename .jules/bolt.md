## 2025-05-15 - JDBC Batching for Quest Data
**Learning:** Found an N+1 query bottleneck in `Quest.setQuestToOfflineMembers` where each offline clan member's quest state was inserted individually.
**Action:** Implement JDBC batching using `addBatch()` and `executeBatch()` to group multiple inserts into a single database round-trip. This pattern should be applied wherever loops contain `executeUpdate()` for repetitive operations.

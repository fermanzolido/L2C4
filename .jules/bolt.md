## 2025-05-23 - [JDBC Batching for N+1 Query in Quest.java]
**Learning:** Found a classic N+1 performance bottleneck in `Quest.setQuestToOfflineMembers` where database updates were performed in a loop. Implementing JDBC batching significantly reduces database round-trips from O(N) to O(1).
**Action:** Always look for loops containing `executeUpdate()` and replace them with `addBatch()`/`executeBatch()` for collection-based persistence.

## 2025-05-15 - [JDBC Batching for RaidBoss Points]
**Learning:** Distributing raid points to party members upon boss death caused an N+1 query bottleneck because each player's points were updated with a separate database round-trip.
**Action:** Implement JDBC batching for any loop-based database updates, especially in high-frequency events like boss deaths or player logins/logouts. Use `addBatch()` inside the loop and `executeBatch()` outside to significantly reduce overhead.

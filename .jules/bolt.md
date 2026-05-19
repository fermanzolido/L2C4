## 2026-05-19 - JDBC Batching for GrandBoss allowed players
**Learning:** Nested loops performing individual `executeUpdate()` calls (N+1 query problem) are a significant performance bottleneck during bulk data persistence. Implementing JDBC batching with `addBatch()` and `executeBatch()` significantly reduces database round-trips.
**Action:** Always look for loops containing database updates and convert them to batched operations, especially in managers that store large amounts of data periodically.

## 2025-05-14 - Batching Grand Boss Zone Persistence
**Learning:** The `GrandBossManager.storeMe()` method, which runs every 5 minutes, was performing individual `executeUpdate()` calls for each allowed player in every boss zone. This created a classic N+1 query bottleneck, especially when multiple boss zones have large lists of allowed players.
**Action:** Use JDBC `addBatch()` and `executeBatch()` for the `INSERT_GRAND_BOSS_LIST` operation to consolidate these into a single database transaction, significantly reducing network round-trips.

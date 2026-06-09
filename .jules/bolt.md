## 2026-06-09 - N+1 Query Bottleneck in Quest Persistence
**Learning:** Bulk database operations such as assigning quests to multiple clan members (online or offline) were previously performed using individual `executeUpdate()` calls within a loop. This creates an N+1 query bottleneck, where each member requires a separate round-trip to the database, significantly increasing overhead during bulk updates.

**Action:** Implement JDBC batching (`addBatch()` and `executeBatch()`) for all bulk persistence tasks. Always include defensive null/empty checks on input collections to avoid unnecessary connection overhead, and use try-with-resources for robust resource management.

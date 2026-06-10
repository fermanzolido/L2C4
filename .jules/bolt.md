## 2026-06-10 - JDBC N+1 Bottleneck in Quest Offline Member Management
**Learning:** Found a recurring performance anti-pattern where legacy database operations (e.g., `Quest.setQuestToOfflineMembers`) perform individual `executeUpdate()` calls within a loop. This significantly increases database latency due to multiple round-trips.
**Action:** Always implement JDBC batching (`addBatch()`/`executeBatch()`) when performing bulk database operations in loops. Use try-with-resources for `PreparedStatement` to ensure resource safety and improve code readability.

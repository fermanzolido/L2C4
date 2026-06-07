## 2026-06-04 - JDBC Batching for Quest Updates
**Learning:** Bulk database operations in the quest system, specifically for clan-wide updates, suffer from N+1 query bottlenecks when implemented using individual `executeUpdate` calls in a loop.
**Action:** Always implement JDBC batching (`addBatch`, `executeBatch`) for bulk persistence logic to reduce database network round-trips from O(N) to O(1). Combine this with try-with-resources and defensive empty-list checks to maximize efficiency.

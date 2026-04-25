## 2026-04-25 - Bulk Data Persistence Optimization
**Learning:** Bulk data persistence in this codebase (e.g., storing offline traders or quest states) often uses iterative `executeUpdate()` calls, leading to N+1 database round-trip performance bottlenecks.
**Action:** Always implement JDBC batching using `addBatch()` inside loops and `executeBatch()` outside the loop for bulk operations to reduce overhead from O(N) to O(1).

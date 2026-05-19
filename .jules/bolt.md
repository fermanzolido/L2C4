## 2025-05-19 - [JDBC Batching Optimization]
**Learning:** N+1 query bottlenecks are common in bulk persistence methods (like server shutdown or clan-wide updates). Implementing JDBC batching with `addBatch()` and `executeBatch()` significantly reduces network overhead.
**Action:** Always check for `executeUpdate()` inside loops for database persistence and convert them to batched operations. Use try-with-resources to manage `PreparedStatement` and avoid manual `.close()` calls.

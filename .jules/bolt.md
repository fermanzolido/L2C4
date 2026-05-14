## 2025-05-15 - JDBC Batching for Bulk Operations
**Learning:** Performing JDBC `executeUpdate()` or `executeBatch()` inside a loop over a collection of entities (like players or items) leads to an N+1 query bottleneck, significantly increasing database round-trips and decreasing performance during server shutdown or bulk updates.
**Action:** Always prefer global batching by calling `addBatch()` inside the loop and `executeBatch()` once outside the loop for the entire collection to maximize throughput and minimize network latency.

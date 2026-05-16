## 2025-05-16 - JDBC Batching and Caching for Raid Points
**Learning:** High-frequency game events (like boss deaths) involving multiple players (parties) cause N+1 query bottlenecks if handled sequentially. Caching aggregated results (like total points) reduces O(N*M) ranking calculations to O(N).
**Action:** Always prefer batched updates for party-wide rewards and maintain cached aggregates for frequently accessed global rankings.

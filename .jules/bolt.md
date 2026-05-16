## 2026-05-16 - JDBC Batching and Caching for Raid Points
**Learning:** High-frequency events like Raid Boss deaths involving multiple players (party members) can cause N+1 query bottlenecks if database updates are performed sequentially. Additionally, calculating total points for rankings by iterating over all entries is O(M) and slow. Combining JDBC batching with an aggregated ConcurrentHashMap cache resolves both issues.
**Action:** Identify loops performing database updates and repetitive aggregation logic; replace with JDBC batching and O(1) cache lookups.

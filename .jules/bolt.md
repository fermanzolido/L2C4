## 2025-05-15 - Optimizing RaidBossPointsManager
**Learning:** High-frequency operations like boss deaths involve multiple players and can cause N+1 query overhead. Caching aggregated results (like total points) avoids repeated O(M) scans, and JDBC batching reduces database round-trips from O(PartySize) to O(1).
**Action:** Always look for loops containing database updates to apply JDBC batching, and use ConcurrentHashMap for O(1) lookups of frequently accessed aggregated data in the world model.

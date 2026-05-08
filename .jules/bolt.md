## 2026-05-08 - Optimized RaidBossPointsManager with caching and JDBC batching
**Learning:** High-frequency game events like boss deaths triggered N+1 query bottlenecks and O(N) ranking calculations in RaidBossPointsManager.
**Action:** Implemented a `_totalPoints` cache (ConcurrentHashMap) for O(1) point retrieval and optimized `getRankList` to O(N). Introduced JDBC batching for adding points to party members, significantly reducing database roundtrips during raid completions.

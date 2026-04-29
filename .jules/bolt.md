## 2026-04-29 - Optimize RaidBossPointsManager Ranking
**Learning:** RaidBossPointsManager.calculateRanking was O(N*M + N log N) because it re-aggregated points for all players from a Map<Integer, Map<Integer, Integer>> and sorted them for every call.
**Action:** Implement a cached total points map (_totalPoints) to allow O(1) total point retrieval and O(N) ranking calculation via a single pass counting higher scores. Ensure rank consistency by using standard competition ranking (1, 1, 3) in both calculateRanking and getRankList.

## 2026-05-12 - Optimized RaidBoss points awarding with JDBC batching
**Learning:** Awarding raid points to a full party upon boss death was an N+1 query bottleneck, performing one DB update per member.
**Action:** Implement JDBC batching in `RaidBossPointsManager` to consolidate multiple updates into a single round-trip, and refactor `RaidBoss`/`GrandBoss` to collect and send points in bulk.

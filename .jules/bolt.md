## 2026-05-12 - JDBC Batching in Quest.setQuestToOfflineMembers
**Learning:** Refactoring N+1 query bottlenecks in quest state persistence for offline clan members using JDBC batching significantly reduces database round-trips.
**Action:** Always check for iterative executeUpdate() calls within loops that handle bulk data persistence and refactor them into batched operations.

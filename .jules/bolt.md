# Bolt's Performance Journal

## 2026-06-02 - Initial Entry
**Learning:** Identified N+1 query performance bottleneck in `Quest.setQuestToOfflineMembers`. Sequential database updates within a loop can be significantly optimized using JDBC batching.
**Action:** Implement JDBC batching for bulk quest state updates to offline clan members.

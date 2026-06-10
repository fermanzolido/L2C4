# Bolt's Journal - Performance Learnings

## 2026-06-09 - JDBC Batching in Quest.java
**Learning:** Found an N+1 query pattern in `Quest.setQuestToOfflineMembers` where `executeUpdate()` was called inside a loop for each clan member. This can be significantly optimized using JDBC batching.
**Action:** Use `PreparedStatement.addBatch()` and `executeBatch()` for bulk database operations in legacy code paths.

## 2026-06-09 - SQL Subqueries vs Batching
**Learning:** Some "N+1" patterns are already efficiently handled by SQL subqueries (e.g., `Quest.deleteQuestToOfflineMembers` using `charId IN (SELECT ...)`). These do not require Java-side batching and are often faster as they stay entirely within the DB engine.
**Action:** Verify if a loop can be replaced by a single SQL statement with a subquery before implementing JDBC batching.

## 2026-07-01 - JDBC Batching for Offline Traders
**Learning:** In the L2J Mobius codebase, `OfflineTraderTable.storeOffliners()` originally performed N+1 database roundtrips by executing an update for each trader and a separate batch execution for each trader's items within a loop. This pattern scales poorly as the number of offline traders increases.
**Action:** Use JDBC `addBatch()` within loops and a single `executeBatch()` after the loop to reduce roundtrips from $O(N)$ to $O(1)$. This is particularly critical for shutdown procedures where many traders are saved simultaneously.

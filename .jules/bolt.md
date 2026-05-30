## 2026-05-28 - CharInfoTable and CharSelectionInfo Optimization
**Learning:** `CharInfoTable.getIdByName` performed an $O(N)$ linear scan for name-to-ID lookups. Introducing a secondary `ConcurrentHashMap` index for lowercase names reduces this to $O(1)$. Additionally, `CharSelectionInfo` used a `LinkedList` with indexed access in its packet writing loop, causing $O(N^2)$ performance.
**Action:** Implement secondary indexes for reverse map lookups and prefer `ArrayList` for indexed iteration to avoid $O(N)$ access costs.

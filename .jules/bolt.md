## 2026-05-28 - Optimized CharInfoTable name lookups
**Learning:** CharInfoTable.getIdByName was performing an O(N) linear search over the entire character name cache for every lookup. This is a significant bottleneck in L2J-based servers as the number of characters grows.
**Action:** Implemented a secondary ConcurrentHashMap index (_namesLower) to provide O(1) case-insensitive lookups, synchronized with the primary name cache.

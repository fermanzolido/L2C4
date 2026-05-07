## 2025-05-15 - CharInfoTable O(N) lookup optimization
**Learning:** `CharInfoTable.getIdByName` performs a linear scan over all character names, resulting in $O(N)$ complexity. This is a significant bottleneck on high-population servers for common operations like retrieving a player by name.
**Action:** Implement a reverse lookup map (`ConcurrentHashMap<String, Integer>`) to achieve $O(1)$ lookup performance. Use lowercase keys for case-insensitive matching.

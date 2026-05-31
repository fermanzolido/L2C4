## 2026-05-31 - [O(1) Case-Insensitive Character Lookup]
**Learning:** CharInfoTable.getIdByName performed a linear scan (O(n)) over all cached character names to find a case-insensitive match. In a production environment with thousands of characters, this high-frequency lookup becomes a significant CPU bottleneck.
**Action:** Implement a secondary index _namesLower (ConcurrentHashMap mapping lowercase names to IDs) to provide O(1) lookups. Ensure all entry points (constructor, addName, removeName) maintain this index.

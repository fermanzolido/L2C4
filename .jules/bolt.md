## 2025-05-15 - Optimize CharInfoTable name lookup
**Learning:** CharInfoTable.getIdByName performed a linear O(N) scan over all cached character names for every lookup, which scales poorly as the player base grows.
**Action:** Implement a reverse Map (ConcurrentHashMap<String, Integer>) using lowercase name keys to provide O(1) case-insensitive lookups, ensuring it is kept in sync during character creation, rename, and deletion.

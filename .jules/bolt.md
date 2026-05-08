## 2025-05-15 - Optimize ClanTable.getClanByName lookup
**Learning:** ClanTable.getClanByName was performing a linear O(N) scan over all clans for every name-based lookup, which is inefficient as the number of clans grows.
**Action:** Implement a secondary case-insensitive ConcurrentHashMap index for clan names to provide O(1) lookups. Ensure the index is synchronized during clan restoration, creation, and destruction.

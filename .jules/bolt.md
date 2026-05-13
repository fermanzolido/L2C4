## 2026-05-13 - [CharInfoTable Optimization]
**Learning:** O(N) linear search in core singleton caches (like character name lookups) can be a significant bottleneck as the server population grows. Iterating over a `ConcurrentHashMap.entrySet()` for case-insensitive matching is (N)$.
**Action:** Implement a secondary index (`_namesLower`) mapping lowercase names to IDs for (1)$ lookups. Ensure all cache update methods (`addName`, `removeName`, etc.) maintain synchronization between the primary and secondary maps using `Locale.ENGLISH` for consistent case-insensitivity.

## 2026-05-04 - CharInfoTable getIdByName Optimization
**Learning:** The `CharInfoTable` used a linear scan over all character names for case-insensitive lookup by name, which is an O(N) operation. In a server with many characters, this becomes a significant bottleneck for common operations like friend/block management or looking up players by name.
**Action:** Use a case-insensitive reverse mapping (`ConcurrentHashMap<String, Integer>`) to provide O(1) lookups, ensuring it is kept in sync with the primary ID-to-name map.

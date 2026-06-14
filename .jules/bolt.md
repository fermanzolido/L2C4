## 2026-06-14 - [O(1) Character Name Lookup]
**Learning:** `CharInfoTable.getIdByName` performed an O(N) linear search over the entire `_names` map using `equalsIgnoreCase`. In servers with thousands of characters, this is a major bottleneck for any logic resolving names to IDs (e.g., friend list, mail, PMs).
**Action:** Implement a secondary `_namesLower` index (ConcurrentHashMap<String, Integer>) to allow O(1) case-insensitive lookups. Ensure `addName` and `removeName` keep both maps in sync. Use the index as a fast-path in `doesCharNameExist` to avoid redundant DB queries.

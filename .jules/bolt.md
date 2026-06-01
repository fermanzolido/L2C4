## 2026-05-31 - [O(1) Char Name Lookup]
**Learning:** Character name lookups in CharInfoTable were O(N) due to iterating over the entire character name map. Adding a secondary ConcurrentHashMap index for lowercase names makes this O(1) and significantly reduces CPU usage during frequent lookups (PMs, friend additions, commands).
**Action:** Always consider secondary indexes for frequent lookups by non-primary keys in large maps.

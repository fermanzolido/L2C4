## 2026-06-04 - [O(1) Char Name Lookup]
**Learning:** `CharInfoTable.getIdByName` was performing a linear scan ($O(N)$) over all character names for every lookup. In large servers, this becomes a significant bottleneck during player login and admin commands.
**Action:** Introduced a secondary `ConcurrentHashMap` index (`_namesLower`) to store lowercase character names mapped to IDs, enabling constant time ($O(1)$) lookups.

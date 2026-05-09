
## 2025-05-15 - Optimized CharInfoTable lookups
**Learning:** High-frequency character name lookups (getIdByName) were performing O(N) linear scans on the primary ID-to-name map, causing significant CPU overhead and unnecessary database queries in doesCharNameExist.
**Action:** Implement a secondary ConcurrentHashMap index for lowercase name-to-ID mappings to achieve O(1) lookups and reduce database pressure for name existence checks.

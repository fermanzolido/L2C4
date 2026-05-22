## 2026-05-21 - regex and lookup optimizations
**Learning:** High-frequency methods like character name lookups and name validation (pet/clan) were performing O(N) operations or repeated regex compilations. Converting these to O(1) using secondary indexes and pre-compiled patterns provides a measurable performance boost.
**Action:** Always check for linear scans in large collections and repeated `Pattern.compile` calls in validation logic.

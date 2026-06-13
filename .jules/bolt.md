## 2026-06-13 - [Character Name Indexing]
**Learning:** Character name lookups via `getIdByName` were O(N) due to iterating over a map's entry set. This is a common performance bottleneck in L2J-based servers when handling commands or social interactions involving many characters.
**Action:** Always provide a secondary lowercase index for string-to-ID lookups to ensure O(1) complexity and case-insensitivity.

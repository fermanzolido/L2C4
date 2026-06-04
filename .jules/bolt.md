## 2026-06-04 - [Optimize CharSelectionInfo performance]
**Learning:** `CharSelectionInfo` was using `LinkedList` for `characterList`, which led to O(N^2) complexity in `writeImpl` when iterating with `get(i)`. Although the number of characters per account is small, using `ArrayList` is better for cache locality and avoids the overhead of `Node` allocations.
**Action:** Always prefer `ArrayList` over `LinkedList` for collections that are mostly appended to and then iterated, especially if indexed access is used.

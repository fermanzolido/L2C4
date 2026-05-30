## 2026-05-30 - [O(n²) bottleneck in CharSelectionInfo]
**Learning:** Using `LinkedList` for a list that is frequently accessed by index via `get(i)` leads to O(n²) complexity. In `CharSelectionInfo.writeImpl`, the character list is iterated and multiple `get(i)` calls are made per character, which is very inefficient for `LinkedList`.
**Action:** Prefer `ArrayList` for collections that require random access or are primarily used for iteration after being populated.

## 2026-06-04 - [ArrayList Optimization for Hot Paths]
**Learning:** Identifying O(N^2) complexity in `CharSelectionInfo.writeImpl` due to `LinkedList.get(i)` calls. Even small collections benefit from `ArrayList`'s O(1) access and better cache locality. Pre-allocating `ArrayList` capacity (as done in `SkillCoolTime`) further reduces resizing overhead.
**Action:** Always prefer `ArrayList` for entity collections that are iterated by index or returned to other components, especially in network packet generation paths. Pre-allocate capacity if source size is known.

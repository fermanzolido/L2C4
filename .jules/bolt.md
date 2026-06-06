## 2026-06-04 - [ArrayList Optimization in InstanceWorld]
**Learning:** In high-frequency game loop paths, prefer `ArrayList` for entity collections returned by getters; pre-allocate capacity if the source size is known and the source collection is not an O(n) size() type like `ConcurrentLinkedQueue`.
**Action:** Replace `LinkedList` with `ArrayList` in collection gathering methods and use `.size()` for initial capacity when safe (e.g., `ConcurrentHashMap.newKeySet().size()` is O(1)).

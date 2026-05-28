# Bolt's Performance Journal

## 2026-05-28 - [ArrayList vs LinkedList for entity collection]
**Learning:** In high-frequency game loop paths, using `ArrayList` instead of `LinkedList` for collecting entities reduces garbage collection pressure and improves cache locality.
**Action:** Always prefer `ArrayList` for temporary collections returned by "getter" methods, and pre-allocate capacity if the source size is known and the source collection is not an O(n) size() type (like `ConcurrentLinkedQueue`).

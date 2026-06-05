# Bolt's Performance Journal

## 2026-06-04 - Optimize NPC collection in InstanceWorld
**Learning:** In high-frequency game loop paths (like instance management), using `LinkedList` for entity collection is a common anti-pattern. `ArrayList` is superior due to better cache locality and reduced object allocation (avoiding `Node` objects). Pre-allocating the `ArrayList` capacity using the source collection's size (when safe, like `ConcurrentHashMap.newKeySet().size()` or a known upper bound) further optimizes performance by avoiding internal array resizing and copying.
**Action:** Always prefer `ArrayList` over `LinkedList` for temporary entity collections returned by getters, and pre-allocate capacity if the source size is known.

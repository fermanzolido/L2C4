## 2026-06-02 - Entity Collection Optimization
**Learning:** In high-frequency game loop paths (visibility checks, NPC filtering, inventory lookups), the use of `LinkedList` for temporary collection of entities is a significant anti-pattern due to O(n) allocations and poor cache locality. `ArrayList` is significantly faster.
**Action:** Always prefer `ArrayList` for methods returning a list of entities. Pre-allocate capacity if the source collection size is known (e.g., using `source.size()` if it's O(1)).

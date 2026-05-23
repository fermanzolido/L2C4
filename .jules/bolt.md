## 2026-05-23 - [Entity Collection Optimization]
**Learning:** `LinkedList` in Java is inefficient for high-frequency entity collection because it allocates a `Node` object for every element, increasing GC pressure and reducing cache locality. `ArrayList` is significantly more efficient for these transient result lists.
**Action:** Always prefer `ArrayList` over `LinkedList` for collecting entities in high-frequency paths (like `getVisibleObjects`, `getNpcs`, `getPaperdollItems`), and pre-allocate initial capacity when the size is known or can be estimated.

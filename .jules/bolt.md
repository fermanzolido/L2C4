## 2026-07-01 - Optimized World.java visibility methods
**Learning:** Using `LinkedList` for temporary collections in visibility hot paths (`getVisibleObjects`) creates unnecessary GC pressure and poor cache locality. Switching to `ArrayList` with pre-allocation based on surrounding region counts significantly improves performance.
**Action:** Always prefer `ArrayList` for hot-path collections and use O(1) size lookups (like `WorldRegion.getVisibleObjects().size()`) to pre-allocate capacity.

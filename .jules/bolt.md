## 2026-06-20 - [World Visibility Optimization]
**Learning:** Core visibility methods in `World.java` (`getVisibleObjects`, `getVisibleObjectsInRange`) are highly hot paths that benefit from switching from `LinkedList` to `ArrayList`.
**Action:** Use `ArrayList` with a pre-allocated capacity based on surrounding objects count to improve cache locality and reduce allocation pressure. Always include null checks for surrounding regions and cap the estimated capacity to avoid over-allocation in dense areas.

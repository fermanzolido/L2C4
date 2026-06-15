## 2026-06-14 - [Codebase Performance Anti-pattern: LinkedList in Hot Paths]
**Learning:** Pervasive use of `LinkedList` in core visibility and collection methods (e.g., `World.getVisibleObjects`, `InstanceWorld.getNpcs`) creates unnecessary memory pressure and reduces CPU cache efficiency. In Java, `ArrayList` is almost always superior for these "collect-and-iterate" patterns.
**Action:** Prioritize replacing `LinkedList` with `ArrayList` in any method that returns a list of objects for iteration, especially in high-frequency game loop paths.

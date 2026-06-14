## 2026-06-14 - World visibility optimization
**Learning:** Core world visibility methods in `World.java` were using `LinkedList` for collecting visible objects. In high-frequency game loop paths, this causes significant GC pressure and lacks CPU cache locality.
**Action:** Always prefer `ArrayList` for transient entity collections in hot paths.

## 2026-06-19 - [Optimized Visibility and NPC Collection]
**Learning:** Found that core visibility methods in `World.java` and NPC collection in `InstanceWorld.java` were using `LinkedList`, causing high object allocation and poor cache locality. Pre-allocating `ArrayList` capacity based on known upper bounds (surrounding objects count or total NPCs) further reduces resizing overhead.
**Action:** Always prefer `ArrayList` with pre-allocated capacity for collecting objects from world regions or instances.

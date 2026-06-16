## 2026-06-16 - World Visibility Collection Optimization
**Learning:** Core world visibility methods (`getVisibleObjects`, `getVisibleObjectsInRange`) were using `LinkedList`, which incurs higher memory overhead and poorer cache locality during frequent object lookups compared to `ArrayList`.
**Action:** Migrated these methods to `ArrayList` to improve CPU cache efficiency and reduce allocation overhead in high-frequency visibility checks.

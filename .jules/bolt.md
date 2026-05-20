## 2026-05-20 - Batching N+1 Queries in Persistence Logic
**Learning:** Core manager classes like `GrandBossManager` and the `Quest` engine frequently use individual `executeUpdate()` calls within loops for persistence, leading to N+1 query bottlenecks that degrade performance during bulk operations or periodic saves.
**Action:** Always check for `executeUpdate()` inside loops when reviewing persistence logic. Replace with `addBatch()` inside the loop and `executeBatch()` outside, while ensuring proper resource management using try-with-resources.

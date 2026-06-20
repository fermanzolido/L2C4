## 2026-06-20 - [World Visibility Optimization]
**Learning:** Using LinkedList in high-frequency visibility paths like World.getVisibleObjects is a major performance bottleneck due to lack of cache locality and high allocation overhead. Pre-allocating ArrayList based on surrounding objects count significantly reduces GC pressure and improves CPU efficiency.
**Action:** Always prefer ArrayList with pre-allocated capacity for entity collections in game server hot paths.

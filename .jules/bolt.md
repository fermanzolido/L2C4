## 2026-05-01 - LinkedList vs ArrayList in Hot Paths
**Learning:** This codebase frequently uses LinkedList for gathering entities (VisibleObjects, NPCs, PaperdollItems) in high-frequency game loop paths. LinkedList's per-element Node allocation significantly increases GC pressure compared to ArrayList.
**Action:** Always prefer ArrayList for collecting and returning lists of entities in hot paths. For fixed-size collections like Paperdoll items, provide an initial capacity (e.g., PAPERDOLL_TOTALSLOTS) to avoid internal array resizing.

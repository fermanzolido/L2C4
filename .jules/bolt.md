
## 2026-05-18 - [Anti-pattern: LinkedList for entity gathering]
**Learning:** Using LinkedList in high-frequency methods that gather entities (like visible objects or NPCs) is a performance anti-pattern. Each insertion in a LinkedList allocates a new Node object, increasing GC pressure, and lacks cache locality.
**Action:** Always prefer ArrayList for gathering entities. When the maximum possible size is known (e.g., total NPCs in an instance or paperdoll slots), set the initial capacity of the ArrayList to avoid internal array reallocations.

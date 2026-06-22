## 2026-06-20 - [World visibility collection optimization]
**Learning:** The World visibility methods are a major hot path in L2J servers. Using LinkedList here causes significant allocation overhead and poor cache locality. Pre-allocating ArrayList based on surrounding regions' object counts (which is O(1) in this codebase) effectively eliminates array resizing during the collection phase.
**Action:** Always prefer ArrayList with pre-allocated capacity for transient collection lists in combat hot paths.

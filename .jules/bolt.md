## 2026-06-20 - [Hot-Path LinkedList Anti-Pattern]
**Learning:** Found widespread use of LinkedList in performance-critical methods like World visibility and InstanceWorld NPC collection. This leads to high allocation pressure and poor CPU cache locality compared to ArrayList. Pre-allocating ArrayList with estimated capacity (using surrounding regions or source collection size) further reduces resizing overhead.
**Action:** Prioritize refactoring LinkedList to pre-allocated ArrayList in any method that is part of the game loop, AI logic, or visibility scanning.

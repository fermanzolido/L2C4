## 2026-06-20 - Hot-path collection optimization
**Learning:** Core visibility methods in World.java and NPC collection in InstanceWorld.java were using LinkedList, causing excessive allocations and poor cache locality.
**Action:** Migrated to ArrayList with pre-allocated capacity based on surrounding object counts or source collection size. Updated Skill.java to handle ArrayList in monster buff filtering.

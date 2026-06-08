
## 2026-06-04 - Target Handler Collection Optimization
**Learning:** High-frequency combat paths in the gameserver often used LinkedList for target collection, causing excessive Node allocations and poor cache locality. Additionally, Skill.java had an explicit 'instanceof LinkedList' check for filtering, creating a fragile dependency on the collection implementation.
**Action:** Migrated target handlers to ArrayList with size-based pre-allocation using skill.getAffectLimit(). Updated Skill.java filtering logic to support ArrayList, ensuring performance gains without breaking monster AI behavior.

## 2026-07-01 - Core World Visibility Optimization
**Learning:** `LinkedList` is an anti-pattern for hot-path iteration in this codebase. Replacing it with `ArrayList` and using pre-allocation (via summing `size()` of surrounding regions' `ConcurrentHashMap.KeySetView` collections) significantly improves CPU cache efficiency and reduces allocation pressure. Pre-allocation in visibility methods is safe as `ConcurrentHashMap.size()` is O(1).
**Action:** Always prefer `ArrayList` for collection return types in hot paths. When return collections are derived from existing collections, use their combined `size()` to pre-allocate the `ArrayList` capacity.

## 2026-07-01 - Monster Buff Filtering Safety
**Learning:** Core visibility results are often filtered in `Skill.getTargetList` for monster-to-playable buffs. The existing logic relies on `instanceof LinkedList` to ensure mutability before calling `removeIf`.
**Action:** When changing return types of core visibility methods to `ArrayList`, the `instanceof` check in `Skill.java` must be updated to include `ArrayList` to prevent AI regressions.

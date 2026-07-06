
## 2026-07-04 - [ArrayList Optimization for Collections]
**Learning:** LinkedList is an anti-pattern for hot-path iteration and collection construction in this codebase due to CPU cache misses and overhead. However, when refactoring to ArrayList, ensure dependent logic using `instanceof LinkedList` (e.g., in `Skill.java`) is updated to maintain functional correctness.
**Action:** Replace LinkedList with ArrayList in visibility and collection methods. Pre-allocate ArrayList capacity when the size of the source collection is known (O(1)). Update instanceof checks in `Skill.getTargetList` to include ArrayList.

## 2026-05-23 - [Optimization of entity collection in World, Inventory, and InstanceWorld]
**Learning:** Replaced `LinkedList` with `ArrayList` in high-frequency methods. `ArrayList` is faster for these use cases due to better cache locality and lower memory overhead compared to `LinkedList`.
**Action:** Always prefer `ArrayList` over `LinkedList` when only adding and iterating through elements, and pre-allocate the initial capacity if the size is known or can be estimated to avoid internal array resizing.

## 2025-05-15 - Optimize String concatenation with pre-allocated arrays
**Learning:** Using `LinkedList` in variadic utility methods like `StringUtil.concat(Object... args)` introduces unnecessary `Node` allocations. Since the number of elements is known from `args.length`, a pre-allocated array is more efficient and reduces GC pressure.
**Action:** Prefer fixed-size arrays over `LinkedList` or `ArrayList` when the number of elements is known upfront, especially in high-frequency utility methods.

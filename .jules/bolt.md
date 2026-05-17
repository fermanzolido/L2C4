## 2025-05-17 - [Optimized StringUtil concatenation]
**Learning:** Using `LinkedList<String>` for temporary storage of varargs representations in utility methods like `append` and `concat` causes unnecessary object allocations and GC pressure. Since the size is known from the varargs array, a pre-allocated `String[]` is more efficient.
**Action:** Always prefer primitive arrays or pre-allocated collections when the size of intermediate results is known at the start of a multi-step operation.

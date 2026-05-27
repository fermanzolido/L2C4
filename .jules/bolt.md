## 2026-05-27 - ArrayList vs LinkedList in Model Collections
**Learning:** High-frequency methods in L2J Mobius (like getVisibleObjects, getNpcs, and inventory lookups) often accumulate results in a LinkedList. For these write-once, read-once scenarios, ArrayList is significantly more efficient as it avoids the overhead of Node object allocations and provides superior cache locality during subsequent iteration or packet serialization.
**Action:** Always prefer ArrayList for local accumulation of entities unless constant-time removals from the middle/head are required.

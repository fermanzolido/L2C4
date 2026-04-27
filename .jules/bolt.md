## 2026-04-27 - N+1 query optimization in FriendListExtended
**Learning:** In the L2J Mobius codebase, several packets and data tables suffer from N+1 query patterns during bulk data retrieval or initialization. Using a single `SELECT ... IN` query with a `Map` to preserve ordering is an effective and safe pattern to optimize these bottlenecks.
**Action:** Always check if online objects can be retrieved from `World.getInstance()` before querying the database, and use batched `IN` queries when iterating over a collection of IDs for offline data.

## 2026-05-11 - [N+1 Query in FriendListExtended]
**Learning:** The `FriendListExtended` packet constructor was executing a synchronous database query for every offline friend in a player's list, leading to O(N) database round-trips where N is the number of offline friends.
**Action:** Replace individual queries with a single bulk `SELECT ... IN (...)` query. Always check if the input collection is empty before querying and use a `Map` to map results back to the original order.

## 2026-05-21 - [JDBC Batching for Raid Points]
**Learning:** High-frequency events like party-wide raid point rewards (7-9 players) create N+1 query bottlenecks if handled sequentially. JDBC batching reduces this to a single round-trip.
**Action:** Always prefer batched updates for events that affect multiple entities (parties, clans, world-wide updates) using the Map<Entity, Value> pattern and executeBatch().

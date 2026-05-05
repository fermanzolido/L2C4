## 2025-05-15 - [CharInfoTable O(1) Lookup]
**Learning:** The `CharInfoTable` class used an O(N) linear scan for `getIdByName`, which is a common bottleneck in legacy servers when handling friend requests, blocking, or contact lists. Introducing a reverse map significantly improves performance for these frequent operations.
**Action:** Always check if name-to-ID lookups in data tables are backed by a reverse map. If not, implement one using a `ConcurrentHashMap` with lowercase keys for case-insensitive O(1) access.

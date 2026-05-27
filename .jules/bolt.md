## 2026-05-23 - Character Name Lookup Optimization
**Learning:** Character name lookups by name (getIdByName) was O(N) due to iterating over a ConcurrentHashMap's entrySet. In large servers with thousands of characters, this is a significant bottleneck.
**Action:** Implement a secondary Map<String, Integer> using lowercase names as keys to achieve O(1) lookup. Ensure synchronization between both maps in add/remove operations.

## 2026-06-02 - Entity collection optimization with ArrayList
**Learning:** LinkedList is a significant performance anti-pattern in high-frequency game loop paths (like visible object collection or NPC queries) and in cases where indexed access (get(i)) is used (like CharSelectionInfo.writeImpl). Replacing it with ArrayList reduces memory overhead (no Node objects), improves cache locality, and fixes O(N^2) complexity issues.
**Action:** Always prefer ArrayList for collecting entities. Use source collection size or constants for pre-allocation when possible.

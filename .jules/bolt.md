## 2026-06-19 - [World visibility optimization]
**Learning:** ArrayList with pre-allocated capacity is significantly more efficient than LinkedList for visibility gathering in high-density areas. Pre-allocation using a helper to sum surrounding region sizes minimizes array resizing.
**Action:** Always prefer ArrayList for transient entity collections in hot paths, and use a reasonable initial capacity estimate if available.

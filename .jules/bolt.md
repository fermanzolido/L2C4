
## 2026-06-15 - Discrepancy between Memory and Codebase State
**Learning:** Historical memory entries indicated that certain hot paths (like InstanceWorld NPC collection) were already optimized to use ArrayList, but manual inspection revealed they still utilized LinkedList. This suggests that previous optimization attempts might have failed to commit or were regressed.
**Action:** Always verify the actual codebase state using read_file or grep before assuming a previous optimization is in place, especially for critical performance paths.


## 2026-05-23 - [Regex Pre-compilation Optimization]
**Learning:** Frequent use of String.matches() and String.split() in high-frequency paths (like XML validation or cron parsing) leads to redundant regex compilation. Pre-compiling these into static final Pattern instances significantly reduces CPU overhead. Replacing toLowerCase().matches() with a Pattern using Pattern.CASE_INSENSITIVE also avoids unnecessary string allocations.
**Action:** Always check for repeated regex operations in loops or high-frequency utility methods and refactor them to use pre-compiled Pattern objects.

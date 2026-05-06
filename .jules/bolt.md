# Bolt's Journal - Critical Learnings

## 2025-05-15 - Regex Optimization in Hot Paths
**Learning:** Using `String.matches()` in frequently called methods (like file filtering during data loading) is inefficient because it compiles the regex on every call. Furthermore, using `String.toLowerCase().matches()` adds unnecessary string allocation.
**Action:** Use a pre-compiled `static final Pattern` with `Pattern.CASE_INSENSITIVE` flag. Benchmarking showed this is ~80% faster than `toLowerCase().matches()` and ~75% faster than `matches()` with a pre-compiled pattern but still calling `toLowerCase()`.

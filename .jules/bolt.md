## 2026-05-03 - [Regex Optimization: Pre-compiled Patterns]
**Learning:** Using `String.matches()` or `Pattern.compile()` inside frequently called methods (like file validation or name validation) introduces significant overhead due to repeated regex compilation. Pre-compiling `Pattern` objects, especially with `Pattern.CASE_INSENSITIVE` instead of calling `toLowerCase().matches()`, can improve performance by ~67% for regex-heavy operations.
**Action:** Always pre-compile `Pattern` objects as `static final` members or in a configuration singleton for frequently used regex.

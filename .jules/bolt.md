## 2025-05-15 - [XML File Validation Optimization]
**Learning:** Using a pre-compiled `static final Pattern` instead of `String.matches()` for file validation in data loaders (like `MultisellData` and `BuyListData`) significantly improves performance and reduces object allocation.
**Action:** Replace `String.matches()` with pre-compiled `Pattern` in hot paths or repeatedly called validation methods.

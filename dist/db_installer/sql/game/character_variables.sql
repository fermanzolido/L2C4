CREATE TABLE IF NOT EXISTS `character_variables` (
  `charId` int(10) UNSIGNED NOT NULL,
  `var` varchar(255) NOT NULL,
  `val` text NOT NULL,
  PRIMARY KEY (`charId`,`var`)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- idx_charId would duplicate the leftmost column of the primary key above.
CREATE INDEX idx_var ON character_variables (var);

-- A database created before this key exists can hold more than one row for the
-- same (charId, var), which makes the value a player reads depend on row order
-- and leaves ON DUPLICATE KEY UPDATE inert. CREATE TABLE IF NOT EXISTS will not
-- alter such a table, so run this once by hand, with the server stopped:
--
--   CREATE TABLE character_variables_new LIKE character_variables;
--   ALTER TABLE character_variables_new ADD PRIMARY KEY (charId, var);
--   INSERT IGNORE INTO character_variables_new SELECT * FROM character_variables;
--   RENAME TABLE character_variables TO character_variables_old,
--                character_variables_new TO character_variables;
--
-- Check character_variables_old for anything unexpected, then drop it.

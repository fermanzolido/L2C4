DROP TABLE IF EXISTS `item_variables`;
CREATE TABLE IF NOT EXISTS `item_variables` (
  `id` int(10) UNSIGNED NOT NULL,
  `var` varchar(255) NOT NULL,
  `val` text NOT NULL,
  PRIMARY KEY (`id`,`var`)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- The old KEY on id alone, and idx_id, both duplicated the leftmost column of
-- the primary key above. This table is dropped and recreated on install, so no
-- migration is needed for it.

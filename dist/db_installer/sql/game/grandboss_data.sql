CREATE TABLE IF NOT EXISTS `grandboss_data` (
  `boss_id` smallint(5) unsigned NOT NULL,
  `loc_x` mediumint(6) NOT NULL,
  `loc_y` mediumint(6) NOT NULL,
  `loc_z` mediumint(6) NOT NULL,
  `heading` mediumint(6) NOT NULL DEFAULT '0',
  `respawn_time` bigint(13) unsigned NOT NULL DEFAULT '0',
  `currentHP` decimal(30,15) NOT NULL,
  `currentMP` decimal(30,15) NOT NULL,
  `status` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`boss_id`)
) DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

INSERT IGNORE INTO `grandboss_data` (`boss_id`,`loc_x`,`loc_y`,`loc_z`,`heading`,`currentHP`,`currentMP`) VALUES
(29001, -21610, 181594, -5734, 0, 229898.48, 667.776), -- Queen Ant (40)
(29006, 17726, 108915, -6480, 0, 622493.58388, 3793.536), -- Core (50)
(29014, 55024, 17368, -5412, 10126, 622493.58388, 3793.536), -- Orfen (50)
(29019, 185708, 114298, -8221, 32768, 17850000, 39960), -- Antharas (79)
(29020, 116033, 17447, 10107, -25348, 4068372, 39960), -- Baium (75)
(29022, 55312, 219168, -3223, 0, 858518.36, 399600), -- Zaken (60)
-- (29025, 0, 0, 0, 0, 0, 0), -- Baium (Stone) (75)
(29028, -105200, -253104, -15264, 0, 62041918, 2248572); -- Valakas (85)

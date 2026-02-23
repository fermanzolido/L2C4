CREATE TABLE IF NOT EXISTS `olympiad_seasons_history` (
    `season_id` INT NOT NULL,
    `charId` INT NOT NULL,
    `char_name` VARCHAR(35) NOT NULL,
    `class_id` INT NOT NULL,
    `olympiad_points` INT NOT NULL,
    `elo` INT NOT NULL,
    `division` VARCHAR(20) NOT NULL,
    PRIMARY KEY (`season_id`, `charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

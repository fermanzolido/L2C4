CREATE TABLE IF NOT EXISTS `donations_log` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `admin_name` VARCHAR(35) NOT NULL,
    `target_name` VARCHAR(35) NOT NULL,
    `target_id` INT NOT NULL,
    `item_id` INT NOT NULL,
    `amount` BIGINT NOT NULL,
    `enchant` INT NOT NULL DEFAULT 0,
    `date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `delivered` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

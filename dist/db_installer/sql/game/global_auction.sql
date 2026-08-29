CREATE TABLE IF NOT EXISTS `global_auctions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `seller_id` int(11) NOT NULL,
  `item_object_id` int(11) NOT NULL,
  `price` bigint(20) NOT NULL,
  `end_time` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `item_object_id` (`item_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `global_auction_funds` (
  `player_id` int(11) NOT NULL,
  `adena` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

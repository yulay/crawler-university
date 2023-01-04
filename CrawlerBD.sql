--
-- Create schema crawler
--

CREATE DATABASE IF NOT EXISTS crawler;
USE crawler;

--
-- Definition of table `crawler`.`universidades`
--

DROP TABLE IF EXISTS `crawler`.`universidades`;
CREATE TABLE  `crawler`.`universidades` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `nombre` text COLLATE utf8_spanish_ci NOT NULL,
  `url` text COLLATE utf8_spanish_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci ROW_FORMAT=DYNAMIC;

--
-- Definition of table `crawler`.`palabras`
--

DROP TABLE IF EXISTS `crawler`.`palabras`;
CREATE TABLE  `crawler`.`palabras` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `id_universidad` int(10) unsigned NOT NULL,
  `palabra` text COLLATE utf8_spanish_ci NOT NULL,
  `cantidad` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `new_fk_constraint` (`id_universidad`),
  CONSTRAINT `new_fk_constraint` FOREIGN KEY (`id_universidad`) REFERENCES `universidades` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;



--
-- Definition of table `crawler`.`urls`
--

DROP TABLE IF EXISTS `crawler`.`urls`;
CREATE TABLE  `crawler`.`urls` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `id_universidad` int(10) unsigned NOT NULL,
  `url` text COLLATE utf8_spanish_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `new_fk_constraint_urls_universidades` (`id_universidad`),
  CONSTRAINT `new_fk_constraint_urls_universidades` FOREIGN KEY (`id_universidad`) REFERENCES `universidades` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;

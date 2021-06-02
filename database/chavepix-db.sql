-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema pix_db
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `pix_db` DEFAULT CHARACTER SET utf8 ;
USE `pix_db` ;

-- -----------------------------------------------------
-- Table `pix_db`.`chave_pix`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `pix_db`.`chave_pix` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `chave` VARCHAR(255) NOT NULL,
  `erp_cliente_id` VARCHAR(255) NOT NULL,
  `registrada_no_bcb_em` DATETIME(6) NOT NULL,
  `tipo_chave` VARCHAR(255) NOT NULL,
  `tipo_conta` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_2rc2bn1uhdfjiqgc3guec2xkv` (`chave` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 9
DEFAULT CHARACTER SET = utf8;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

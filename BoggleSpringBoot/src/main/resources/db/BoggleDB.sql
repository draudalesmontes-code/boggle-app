-- MySQL dump 10.13  Distrib 8.0.45, for Linux (aarch64)
-- Host: localhost    Database: boggle
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `boards`
--

DROP TABLE IF EXISTS `boards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `boards` (
                          `board_id` varchar(50) NOT NULL,
                          `board_string` text NOT NULL,
                          PRIMARY KEY (`board_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dictionary`
--

DROP TABLE IF EXISTS `dictionary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dictionary` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `word` varchar(100) NOT NULL,
                              `point_value` int NOT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `games`
--

DROP TABLE IF EXISTS `games`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `games` (
                         `id` int NOT NULL AUTO_INCREMENT,
                         `player1_id` int NOT NULL,
                         `player2_id` INT DEFAULT NULL,
                         `board_id` varchar(50) NOT NULL,
                         `winner_player_id` int DEFAULT NULL,
                         `status` enum('WAITING','IN_PROGRESS','FINISHED') NOT NULL DEFAULT 'WAITING',
                         `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                         `started_at` datetime DEFAULT NULL,
                         `finished_at` datetime DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         KEY `fk_game_player1` (`player1_id`),
                         KEY `fk_game_player2` (`player2_id`),
                         KEY `fk_game_board` (`board_id`),
                         KEY `fk_game_winner` (`winner_player_id`),
                         CONSTRAINT `fk_game_board` FOREIGN KEY (`board_id`) REFERENCES `boards` (`board_id`) ON DELETE CASCADE,
                         CONSTRAINT `fk_game_player1` FOREIGN KEY (`player1_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                         CONSTRAINT `fk_game_player2` FOREIGN KEY (`player2_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                         CONSTRAINT `fk_game_winner` FOREIGN KEY (`winner_player_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
                         CONSTRAINT `chk_different_players` CHECK ((`player1_id` <> `player2_id`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `found_words`
--

DROP TABLE IF EXISTS `found_words`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `found_words` (
                               `id` int NOT NULL AUTO_INCREMENT,
                               `player_id` int NOT NULL,
                               `game_id` int NOT NULL,
                               `dictionary_word_id` int NOT NULL,
                               `found_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,                               PRIMARY KEY (`id`),
                               UNIQUE KEY `unique_word_per_player_per_game` (`player_id`,`game_id`,`dictionary_word_id`),
                               KEY `fk_found_game` (`game_id`),
                               KEY `fk_found_dictionary` (`dictionary_word_id`),
                               CONSTRAINT `fk_found_dictionary` FOREIGN KEY (`dictionary_word_id`) REFERENCES `dictionary` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `fk_found_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `fk_found_player` FOREIGN KEY (`player_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
                         `id` int NOT NULL AUTO_INCREMENT,
                         `username` varchar(50) NOT NULL,
                         `email` varchar(100) NOT NULL,
                         `password_hash` varchar(255) NOT NULL,
                         `is_guest` tinyint(1) NOT NULL DEFAULT 0,
                         `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `username` (`username`),
                         UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sessions`
--

DROP TABLE IF EXISTS `sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sessions` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `session_code` varchar(50) NOT NULL,
                            `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_sessions_session_code` (`session_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `session_users`
--

DROP TABLE IF EXISTS `session_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `session_users` (
                                 `session_id` bigint NOT NULL,
                                 `username` varchar(50) NOT NULL,
                                 KEY `idx_session_users_session_id` (`session_id`),
                                 CONSTRAINT `fk_session_users_session` FOREIGN KEY (`session_id`) REFERENCES `sessions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-23 21:34:49

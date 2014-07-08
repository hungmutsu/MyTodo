-- phpMyAdmin SQL Dump
-- version 4.1.12
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Jul 08, 2014 at 10:16 AM
-- Server version: 5.5.36
-- PHP Version: 5.4.27

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `mytodo`
--

-- --------------------------------------------------------

--
-- Table structure for table `tasks`
--

CREATE TABLE IF NOT EXISTS `tasks` (
  `taskId` int(11) NOT NULL AUTO_INCREMENT,
  `userId` int(11) DEFAULT NULL,
  `name` varchar(500) NOT NULL,
  `description` varchar(4000) DEFAULT NULL,
  `reminderDate` timestamp NULL DEFAULT NULL,
  `createdDate` timestamp NULL DEFAULT NULL,
  `updatedDate` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`taskId`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=23 ;

--
-- Dumping data for table `tasks`
--

INSERT INTO `tasks` (`taskId`, `userId`, `name`, `description`, `reminderDate`, `createdDate`, `updatedDate`) VALUES
(17, 1, 'task1', 'task1', '0000-00-00 00:00:00', '2014-07-07 02:14:27', '2014-07-07 02:14:27'),
(18, 1, 'task2', 'task2', '0000-00-00 00:00:00', '2014-07-07 02:15:18', '2014-07-07 02:15:18'),
(19, 1, 'local task 3', 'local task', '2014-07-07 20:50:09', '2014-07-07 20:50:09', '2014-07-07 20:50:19'),
(20, 1, 'local task 3', 'local task', '2014-07-07 20:50:09', '2014-07-07 20:50:09', '2014-07-07 20:50:19'),
(21, 1, 'kfkfgk', 'ôugukfkygggvg', '2014-07-07 19:21:06', '2014-07-07 19:21:05', '2014-07-07 19:21:11'),
(22, 1, 'fity?ivigigivj', 'givyiviigvi', '2014-07-07 19:50:32', '2014-07-07 19:49:32', '2014-07-07 19:49:43');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `userId` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(200) NOT NULL,
  `password` varchar(200) NOT NULL,
  `fullname` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`userId`),
  KEY `uid` (`userId`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`userId`, `username`, `password`, `fullname`) VALUES
(1, 'admin', 'admin', 'administrator');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

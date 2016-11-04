--Copyright 2016 ANI Technologies Pvt. Ltd.
--
--Licensed under the Apache License, Version 2.0 (the "License");
--you may not use this file except in compliance with the License.
--You may obtain a copy of the License at
--
--http://www.apache.org/licenses/LICENSE-2.0
--
--Unless required by applicable law or agreed to in writing, software
--distributed under the License is distributed on an "AS IS" BASIS,
--WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--See the License for the specific language governing permissions and
--limitations under the License.

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `applications`
--

DROP TABLE IF EXISTS `applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `applications` (
  `internalId` int(11) NOT NULL AUTO_INCREMENT,
  `createdAt` datetime DEFAULT NULL,
  `createdBy` varchar(255) DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `updatedBy` varchar(255) DEFAULT NULL,
  `tenant` varchar(255) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `executorConfig` varchar(255) DEFAULT NULL,
  `instances` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `computationId` int(11) DEFAULT NULL,
  `runtimeOptions` text,
  PRIMARY KEY (`internalId`),
  KEY `FK_eurgh9se1wmg3t8sec838ktg4` (`computationId`),
  CONSTRAINT `FK_eurgh9se1wmg3t8sec838ktg4` FOREIGN KEY (`computationId`) REFERENCES `computations` (`internalId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `component_instance_properties`
--

DROP TABLE IF EXISTS `component_instance_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `component_instance_properties` (
  `componentInstanceId` int(11) NOT NULL,
  `id` varchar(255) NOT NULL,
  `value` longtext,
  PRIMARY KEY (`componentInstanceId`,`id`),
  CONSTRAINT `FK_115flepnf4udbgfnfoysdv6r0` FOREIGN KEY (`componentInstanceId`) REFERENCES `component_instances` (`internalId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `component_instances`
--

DROP TABLE IF EXISTS `component_instances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `component_instances` (
  `internalId` int(11) NOT NULL AUTO_INCREMENT,
  `createdAt` datetime DEFAULT NULL,
  `createdBy` varchar(255) DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `updatedBy` varchar(255) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `componentId` int(11) DEFAULT NULL,
  `computationId` int(11) DEFAULT NULL,
  PRIMARY KEY (`internalId`),
  KEY `FK_4clo6td5n91vlffjkbf8uth00` (`componentId`),
  KEY `FK_30989sg2syeh2kv2sbmdwqloe` (`computationId`),
  CONSTRAINT `FK_30989sg2syeh2kv2sbmdwqloe` FOREIGN KEY (`computationId`) REFERENCES `computations` (`internalId`),
  CONSTRAINT `FK_4clo6td5n91vlffjkbf8uth00` FOREIGN KEY (`componentId`) REFERENCES `components` (`internalId`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `components`
--

DROP TABLE IF EXISTS `components`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `components` (
  `internalId` int(11) NOT NULL AUTO_INCREMENT,
  `createdAt` datetime DEFAULT NULL,
  `createdBy` varchar(255) DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `updatedBy` varchar(255) DEFAULT NULL,
  `cpu` double NOT NULL,
  `deleted` bit(1) NOT NULL,
  `memory` double NOT NULL,
  `name` varchar(255) NOT NULL,
  `namespace` varchar(255) NOT NULL,
  `processorType` varchar(255) DEFAULT NULL,
  `source` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `version` varchar(255) NOT NULL,
  `description` longtext,
  `optionalProperties` longtext,
  `requiredProperties` longtext,
  PRIMARY KEY (`internalId`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `computation_attributes`
--

DROP TABLE IF EXISTS `computation_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `computation_attributes` (
  `computationId` int(11) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  PRIMARY KEY (`computationId`,`id`),
  CONSTRAINT `FK_b8u41lxfyicp4ba0oys6o23pt` FOREIGN KEY (`computationId`) REFERENCES `computations` (`internalId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `computation_properties`
--

DROP TABLE IF EXISTS `computation_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `computation_properties` (
  `computationId` int(11) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  PRIMARY KEY (`computationId`,`id`),
  CONSTRAINT `FK_8nbtsd50dcmaba7rjw4wikkui` FOREIGN KEY (`computationId`) REFERENCES `computations` (`internalId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `computations`
--

DROP TABLE IF EXISTS `computations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `computations` (
  `internalId` int(11) NOT NULL AUTO_INCREMENT,
  `createdAt` datetime DEFAULT NULL,
  `createdBy` varchar(255) DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `updatedBy` varchar(255) DEFAULT NULL,
  `tenant` varchar(255) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `ownerEmail` varchar(255) NOT NULL,
  `description` longtext,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`internalId`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `connections`
--

DROP TABLE IF EXISTS `connections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `connections` (
  `internalId` int(11) NOT NULL AUTO_INCREMENT,
  `createdAt` datetime DEFAULT NULL,
  `createdBy` varchar(255) DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `updatedBy` varchar(255) DEFAULT NULL,
  `fromLink` varchar(255) DEFAULT NULL,
  `fromType` varchar(255) NOT NULL,
  `toLink` varchar(255) DEFAULT NULL,
  `computationId` int(11) DEFAULT NULL,
  PRIMARY KEY (`internalId`),
  KEY `FK_k9c6jc8kf6xqym2gmtpswgnpu` (`computationId`),
  CONSTRAINT `FK_k9c6jc8kf6xqym2gmtpswgnpu` FOREIGN KEY (`computationId`) REFERENCES `computations` (`internalId`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `global_properties`
--

DROP TABLE IF EXISTS `global_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `global_properties` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_tqqwe6s7avhd581j89iro56j5` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-11-03 17:09:46

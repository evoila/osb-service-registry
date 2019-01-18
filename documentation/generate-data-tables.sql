-- SQL file for creating the datamodel of the service registry for a mariadb


CREATE DATABASE IF NOT EXISTS `service_registry2` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `service_registry2`;

CREATE TABLE IF NOT EXISTS `cloud_site` (
  `id` varchar(255) NOT NULL,
  `host` varchar(255) DEFAULT NULL,
  `platform` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `companies` (
  `id` varchar(255) NOT NULL,
  `basic_auth_token` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `service_broker` (
  `id` varchar(255) NOT NULL,
  `api_version` varchar(255) DEFAULT NULL,
  `basic_auth_token` varchar(255) DEFAULT NULL,
  `cloud_foundry_allowed` bit(1) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `host` varchar(255) DEFAULT NULL,
  `kubernetes_allowed` bit(1) NOT NULL,
  `port` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `broker_site` (
  `broker_id` varchar(255) NOT NULL,
  `site_id` varchar(255) NOT NULL,
  KEY `FK1vhiw7g35gj01t7vtaomb5hbl` (`site_id`),
  KEY `FK5p3iu6m8w88u9rc1s3p0il14` (`broker_id`),
  CONSTRAINT `FK1vhiw7g35gj01t7vtaomb5hbl` FOREIGN KEY (`site_id`) REFERENCES `service_broker` (`id`),
  CONSTRAINT `FK5p3iu6m8w88u9rc1s3p0il14` FOREIGN KEY (`broker_id`) REFERENCES `cloud_site` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `cloud_context` (
  `id` varchar(255) NOT NULL,
  `auth_token` varchar(255) DEFAULT NULL,
  `namespace` varchar(255) DEFAULT NULL,
  `organization` varchar(255) DEFAULT NULL,
  `space` varchar(255) DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `cloud_site` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkubpeisx76ldp61igh1n6oqjo` (`company`),
  KEY `FKgsrb6yansjfnh9dgw6l3jnjp3` (`cloud_site`),
  CONSTRAINT `FKgsrb6yansjfnh9dgw6l3jnjp3` FOREIGN KEY (`cloud_site`) REFERENCES `cloud_site` (`id`),
  CONSTRAINT `FKkubpeisx76ldp61igh1n6oqjo` FOREIGN KEY (`company`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `service_instance` (
  `id` varchar(255) NOT NULL,
  `creation_in_progress` bit(1) NOT NULL,
  `dashboard_url` varchar(255) DEFAULT NULL,
  `deletion_in_progress` bit(1) NOT NULL,
  `name_space` varchar(255) DEFAULT NULL,
  `organization_guid` varchar(255) DEFAULT NULL,
  `original_instance` bit(1) NOT NULL,
  `plan_id` varchar(255) DEFAULT NULL,
  `service_definition_id` varchar(255) DEFAULT NULL,
  `space_guid` varchar(255) DEFAULT NULL,
  `broker` varchar(255) DEFAULT NULL,
  `shared_context_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfygcl5rmhjw5yr58hfbue1hw3` (`broker`),
  KEY `FKdc7vfg29i5pn43a4lw4byd16n` (`shared_context_id`),
 -- CONSTRAINT `FKdc7vfg29i5pn43a4lw4byd16n` FOREIGN KEY (`shared_context_id`) REFERENCES `shared_context` (`id`),
  CONSTRAINT `FKfygcl5rmhjw5yr58hfbue1hw3` FOREIGN KEY (`broker`) REFERENCES `service_broker` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `shared_context` (
  `id` varchar(255) NOT NULL,
  `name_space` varchar(255) DEFAULT NULL,
  `organization` varchar(255) DEFAULT NULL,
  `plan_id` varchar(255) DEFAULT NULL,
  `service_definition_id` varchar(255) DEFAULT NULL,
  `service_instance_id` varchar(255) DEFAULT NULL,
  `shared` bit(1) NOT NULL,
  `space` varchar(255) DEFAULT NULL,
  `instance_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4y5p1xou9etoavfgnifwxbllb` (`instance_id`),
  CONSTRAINT `FK4y5p1xou9etoavfgnifwxbllb` FOREIGN KEY (`instance_id`) REFERENCES `service_instance` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE `service_instance` ADD CONSTRAINT `FKdc7vfg29i5pn43a4lw4byd16n` FOREIGN KEY (`shared_context_id`) REFERENCES `shared_context` (`id`);

CREATE TABLE IF NOT EXISTS `service_binding` (
  `id` varchar(255) NOT NULL,
  `creation_in_progress` bit(1) NOT NULL,
  `delete_in_progress` bit(1) NOT NULL,
  `service_instance` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr0k5ow9sldoxmmn6tgmq3mxqp` (`service_instance`),
  CONSTRAINT `FKr0k5ow9sldoxmmn6tgmq3mxqp` FOREIGN KEY (`service_instance`) REFERENCES `service_instance` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

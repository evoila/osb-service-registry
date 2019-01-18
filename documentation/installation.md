# Table of Contents
1. [OSB Service Registry](../README.md)
2. [Requirements](./requirements.md)
3. [Installation](#installation)
    * 3.1 [Configuration](#configuration)
    * 3.2 [Building the project](#building-the-project)
4. [API](./api.md)
5. [Shadow Service Broker](./shadowservicebroker.md)
6. [Service Broker Communication](./servicebrokercommunication.md)
7. [Tests](./tests.md)  
---

# Installation

The sources in this repository have to be build first and supplied with a proper configuration file. The generated jar file can be run without the need of additional parameters.

## Configuration
 
Using the Spring Framework, configuration of the application can be done via for example a .yml or .properties file. The resource path is `{project-root}/service-registry/src/main/resources` and contains an example configuration file. Furthermore the directory contains the logback.xml file for changing the logging levels of the application.

Following configuration parameters exist:
| Parameter | Type | Description |
|----|----|----|
| registry.update_thread_number | integer | Maximum thread pool size when updating catalogs |
| registry.timeout_connection | integer | Timeout for connection with service brokers |
| registry.timeout_read | integer | Timeout for reading responses of service brokers |
| spring.application.name<span></span> | String | Name of the spring application|
| spring.jpa.hibernate.ddl-auto | String | Option for hibernate DDL validation and export (see [community documentation])|
| spring.jpa.hibernate.dialect | String | SQL dialect of the underlying database |
| spring.datasource.url | String | Connection string for the database |
| spring.datasource.username | String | Username for the database |
| spring.datasource.password | String | Password for the user |
| login.username | String | Basic Auth name |
| login.password | String | Basic Auth password |

## Building the project

This project uses Apache Maven as its build management tool. Make sure to initialize the submodules and have access to the dependencies (either via remote or local maven repositories). Run `mvn clean install` to build the sources and get an executable fat jar. 

---
<p align="center">
    <span ><a href="./requirements.md"><- Previous</a></span>
	    <span>&nbsp; | &nbsp;</span> 
    <span><a href="./api.md">Next -></a></span>
</p>

[community documentation]: https://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#configurations-hbmddl
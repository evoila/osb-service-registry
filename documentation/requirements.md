# Table of Contents
1. [OSB Service Registry](../README.md)
2. [Requirements](#requirements)
    * 2.1 [Submodules](#submodules)
    * 2.2 [Data Storage](#data-storage)
3. [Installation](./installation.md)
4. [API](./shadowservicebroker.md)
5. [Shadow Service Broker](./shadowservicebroker.md)
6. [Service Broker Communication](./servicebrokercommunication.md)
7. [Tests](./tests.md)  
---

# Requirements

To build and run this application, some requirements have to be met first.

## Submodules

This project uses git submodules for direct access to other evoila repositories and source code. Make sure to initialize the submodules before trying to build the project.

The repository [osb-core](https://github.com/evoila/osb-core) holds three different modules from which the security and model modules are used in the service registry.

## Data Storage

The service registry uses a mysql database as its data storage and will not start without a proper connection. The tables necessary can be either generated via auto generation configuring hibernate or spring data to do so or use the file [generate-data-tables.sql].

The data model is designed as following:
```
   _______________                _________________                  _____________
  |               | 1          n |                 | n            1 |             |
  |  Cloud Site   |--------------|  Cloud Context  |----------------|   Company   |
  |_______________|              |_________________|                |_____________|
          | n
          |
          |
          | m
   ________________               __________________               ___________________
  |                | 1         n |                  | 1         n |                   |
  | Service Broker |-------------| Service Instance |-------------|   Service Binding |
  |________________|             |__________________|             |___________________|
          | 1
          |
          |
          | n
 --------------------
¦                    ¦
¦ Service Definition ¦  <-- cached, not in storage
¦                    ¦  
 --------------------
```

---
<p align="center">
    <span ><a href="../README.md"><- Previous</a></span>
	    <span>&nbsp; | &nbsp;</span> 
    <span><a href="./installation.md">Next -></a></span>
</p>

[generate-data-tables.sql]: ./generate-data-tables.sql
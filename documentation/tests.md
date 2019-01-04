# Table of Contents
1. [OSB Service Registry](../README.md)
2. [Requirements](./requirements.md)
3. [Installation](./installation.md)
4. [API](./shadowservicebroker.md)
5. [Shadow Service Broker](./shadowservicebroker.md)
6. [Service Broker Communication](./servicebrokercommunication.md)
7. [Tests](#tests)  
---

# Tests

## JUnit Tests

The JUnit tests cover basic parts of the application and guarantee a proper functionality of these components.

JUnit tests that create a spring application context are executed using the 'test' profile.

### Manager Tests

The manager classes of the service registry are tested using the SpringBootTest annotation and therefore start a H2 in-memory database upon executing.

## Shadow Service Broker Tests

The Shadow Service Broker functionality is tested by using the [osb-checker-kotlin] application.

---
<p align="center">
    <span ><a href="./servicebrokercommunication.md"><- Previous</a></span>
	    <span>&nbsp; | &nbsp;</span> 
    <span><a href="./tests.md">Next -></a></span>
</p>

[osb-checker-kotlin]: https://github.com/evoila/osb-checker-kotlin 
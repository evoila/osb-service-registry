# Table of Contents
1. [OSB Service Registry](../README.md)
2. [Requirements](./requirements.md)
3. [Installation](./installation.md)
4. [API](./api.md)
5. [Shadow Service Broker](./shadowservicebroker.md)
6. [Service Broker Communication](#service-broker-communication)
    * 6.1 [Catalogs](#catalogs)
7. [Tests](./tests.md)    
---

# Service Broker Communication

Generally speaking, the service registry forwards incoming calls for the service brokers to the targeted service broker. Additionally the service registry has an own data storage about existing resources created by the service brokers to verify existence, allow management of targeted resources and check authorization of incoming requests before forwarding them to the service broker. Due to this additional layer further error codes are returned, that are not directly specified by the osb-API, for example blocking a binding call to the service broker if the service instance does not exist in the data storage of the service registry, to prevent desynchronization between the service registry and the service brokers. 

Furthermore requests to delete a service instance with still active bindings are blocked by the service registry with a 412 Precondition Failed error to ensure no creation of orphaned bindings.

Shared instances have an additional protection. Delete service instance calls on a shared instance with more than one reference will not result in an actual deletion of the service instance at the service broker, but remove the entry from the service registry (if all other requirements are met). Trying to unshare a shared service with more than one reference is not allowed and will be answered with a 400 error code.

## Catalogs

The service registry caches the service definitions found in the catalogs of a service broker in its memory (not data storage). The service definitions are saved via soft or forced updates. A soft update does an existence check for an already existing entry first and will not perform an actual request to the service broker, when finding a matching entry in the cache. A forced update will always request the service broker's catalog and clear the service definitions cache first. Actions based on service definitions will execute a soft cache update and a forced update is performed scheduled every 15 minutes.

The update of all service definitions is done in parallel to prevent timeouts by unresponsive service brokers or by sheer amount of necessary catalog requests. The number of threads in the used thread pool and the timeouts can be configured via the configuration file.

To perform the service definition updates on schedule, registering a service broker at the service registry requires a basic auth token for communication with the service broker.

---
<p align="center">
    <span ><a href="./shadowservicebroker.md"><- Previous</a></span>
	    <span>&nbsp; | &nbsp;</span> 
    <span><a href="./tests.md">Next -></a></span>
</p>
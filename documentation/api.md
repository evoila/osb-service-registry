# Table of Contents
1. [OSB Service Registry](../README.md)
2. [Submodules](./submodules.md)
3. [Installation](./installation.md)
4. [API](#api)
5. [Shadow Service Broker](./shadowservicebroker.md)
---

# API

The API of the service registry consists of multiple parts:
* Spring REST API
* Shadow Service Broker API
* Sharing Endpoint
* Health Endpoint

## Security

The service registry is currently protected by basic auth and enforces propers credentials on all endpoints. This will be changed in the near future to a more detailed security approach. The credentials can be set in the configuration file of the service registry.

## Spring Rest API

Currently the basic CRUD operations on the datamodel of the service registry are handles via Spring Rest API. An oversight can be obtained by calling the root endpoint.

## Shadow Service Broker API

To allow the masking of the service registry as a service broker, the service registry implements the osb-API 2.14. For further information about the osb-API or the Shadow Service Broker feature, see [osb-API] and [Shadow Service Broker].

**Note:** Although the service registry does implement the osb-API, it is possible for API tests like the [osb-checker] or the [osb-checker-kotlin] to fail, because the service registry has to return a different error code, when two or more error cases occur at the same time. An example would be calling a synchronous instance deletion on a not existing service instance will be answered with a 410 by the service registry, but the expected error by the osb-checker is 422. This deviation from the specification is necessary, because the service registry needs to allow the access to the service broker, based on its known instances and binding, before actually sending a request, and will not compromise the usage since the result stays the same.

## Sharing Endpoint

To toggle sharing for an existing service instance use the following endpoint:
```PATCH /service_instance/{instance_id}/shareable```
To set the shared status a query parameter named `sharing` (defaults to false) is needed as following:
```PATCH /service_instance/{instance_id}/shareable?sharing=[true/false]```

Service Instances that are shared will occur in the dedicated shared instances service definition and can not be unshared while other service instances refer to the same physical instance.

## Health Endpoint
A simple endpoint to check whether the service registry is running and will return a simple response:

GET /status
```json
{
    "message": "Service Registry is running."
}
```


---
<p align="center">
    <span ><a href="./installation.md"><- Previous</a></span>
	    <span>&nbsp; | &nbsp;</span> 
    <span><a href="./shadowservicebroker.md">Next -></a></span>
</p>

[osb-API]: https://github.com/openservicebrokerapi/servicebroker
[Shadow Service Broker]: ./shadowservicebroker.md
[osb-checker]: https://github.com/openservicebrokerapi/osb-checker
[osb-checker-kotlin]: https://github.com/evoila/osb-checker-kotlin
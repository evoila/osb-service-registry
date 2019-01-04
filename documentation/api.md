# Table of Contents
1. [OSB Service Registry](../README.md)
2. [Requirements](./requirements.md)
3. [Installation](./installation.md)
4. [API](#api)
    * 4.1 [Security](#security)
    * 4.2 [Spring Data Rest API](#spring-data-rest-api)
    * 4.3 [Shadow Service Broker API](#shadow-service-broker-api)
    * 4.4 [Sharing Endpoint](#sharing-endpoint)
    * 4.5 [Health Endpoint](#health-endpoint)
5. [Shadow Service Broker](./shadowservicebroker.md)
6. [Service Broker Communication](./servicebrokercommunication.md)
7. [Tests](./tests.md)  
---

# API

The API of the service registry consists of multiple parts:
* Spring REST API
* Shadow Service Broker API
* Sharing Endpoint
* Health Endpoint


## Security

The service registry is currently protected by basic auth and enforces proper credentials on all endpoints. This will be changed in the near future to a more detailed security approach. The credentials can be set in the configuration file of the service registry.

## Spring Data Rest API

Currently the basic CRUD operations on the data model of the service registry are handles via Spring Data Rest API. An oversight can be obtained by calling the root endpoint.

## Shadow Service Broker API

To allow the masking of the service registry as a service broker, the service registry implements the osb-API 2.14. For further information about the osb-API, the Shadow Service Broker feature and the communication with the service brokers, see [osb-API], [Shadow Service Broker] and [Service Broker Communication]. 

## Sharing Endpoint

To toggle sharing for an existing service instance use the following endpoint:
`PATCH /service_instance/{instance_id}/shareable`
To set the shared status a query parameter named `sharing` (defaults to false) is needed as following:
`PATCH /service_instance/{instance_id}/shareable?sharing=[true/false]`

Service Instances that are shared will occur in the dedicated shared instances service definition of the Shadow Service Broker and can not be unshared while other service instances refer to the same physical instance.

## Health Endpoint
A simple endpoint to check whether the service registry is running and will return a simple response:

```json
GET /status

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
[Service Broker Communication]: ./servicebrokercommunication.md

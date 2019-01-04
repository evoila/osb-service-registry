# Table of Contents
1. [OSB Service Registry](#osb-service-registry)
    * 1.1 [Challenges with multiple PaaS platforms and service brokers](#Challenges-with-multiple-PaaS-platforms-and-service-brokers)
        * 1.1.1 [Number of references](#number-of-references)
        * 1.1.2 [Instance Sharing](#instance-sharing)
        * 1.1.3 [Limitations of the osb-api](#limitations-of-the-osb-api)
        * 1.1.4 [Authentication and Authorization](#Authentication-and-Authorization)
    * 2.1 [Goals of this project](#Goals-of-this-project)
2. [Requirements](documentation/requirements.md)
3. [Installation](documentation/installation.md)
4. [API](documentation/api.md)
5. [Shadow Service Broker](documentation/shadowservicebroker.md)
6. [Service Broker Communication](documentation/servicebrokercommunication.md)
7. [Tests](documentation/tests.md)  
---

# OSB Service Registry

The Service Registry is an application build to handle the challenges when working with multiple PaaS platforms and service brokers.



## Challenges with multiple PaaS platforms and service brokers

### Number of references

For a PaaS Platform to use a service broker, the broker has to be registered at the platform.  When only using one or two platforms and few brokers, this does not pose a problem, but upon adding more and more brokers and platforms the number of cross-references becomes a nuisance. Therefore the service registry provides a single central point for platforms to get their brokers and on the other hand for brokers to be registered.

### Instance Sharing

Using the same service instance for multiple bindings is a common use case but is obstructed when using more than one platform. The service registry supports the flagging of service instances as shared and the creation of shared service instances on other platforms.

### Limitations of the osb-api

Understandably the open service broker api currently does not hold the tools the service registry needs to work as intended, since it was not designed with this idea in mind. Therefore the service registry will be using an extended osb-api for its services, which means an alteration on the platform's side is necessary. For unaltered platforms, the service registry has a "shadow service broker" feature. For further information about the shadow service broker see [Shadow Service Broker](documentation/shadowservicebroker.md).

### Authentication and Authorization

Keeping track of who has access to which service brokers gets more and more confusing and generates massive overhead, when handling several platforms and brokers. The service registry will be supporting a centralized authorization for accessing service brokers and shared instances.

## Goals of this project

* Centralize management of service brokers
* Enable sharing and access to shared instances
* Automatic update of service broker catalogs at the platforms
* Centralize authorization for service broker and instance access

---
<p align="center">
	<span>&nbsp; || &nbsp;</span> 
    <span><a href="documentation/requirements.md">Next -></a></span>
</p>
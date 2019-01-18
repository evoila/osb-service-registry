# Table of Contents
1. [OSB Service Registry](../README.md)
2. [Requirements](./requirements.md)
3. [Installation](./installation.md)
4. [API](./api.md)
5. [Shadow Service Broker](#shadow-service-broker)
6. [Service Broker Communication](./servicebrokercommunication.md)
7. [Tests](./tests.md)  
---

# Shadow Service Broker

The shadow service broker is a feature of the service registry to be compatible with unaltered platforms. The service registry masks itself as a service broker and implements the standard osb-api 2.14. The service registry will gather every catalog of all its service brokers and provides the platform with a list of all service definitions.

**Note:** Although the service registry does implement the osb-API, it is possible for API tests like the [osb-checker] or the [osb-checker-kotlin] to fail, because the service registry might have to return a different error code, when two or more error cases occur at the same time. An example would be calling a synchronous instance deletion on a not existing service instance will be answered with a 410 by the service registry, but the expected error by the osb-checker is 422. This deviation from the specification is necessary, because the service registry needs to allow the access to the service broker, based on its known instances and binding, before actually sending a request, and will not compromise the usage since the result stays the same.

Sharing instances is possible with the shadow service broker feature, but loses comfort in usage. The shared instances will occur as an additional service definition in the catalog for the user to create a service instance, which will be handled similar to an existing service creation in the background of the service registry. But values like organization, space and plan id have to be moved further down the tree as well as the actual plan id field is used for the service instance id.

Following json is an example body for /v2/catalog call that holds two service definitions from service brokers and two shared instances:
```json
{
    "services": [
        {
            "id": "8db65104-37a2-4b0a-bfa4-346b4b6f9b75",
            "name": "osb-example",
            "description": "Example service broker",
            "bindable": true,
            "plans": [
                {
                    "id": "a038ef3c-da86-424f-9b89-341491a52bac",
                    "name": "s",
                    "description": "The standard example plan",
                    "metadata": {
                        "connections": 4,
                        "instanceGroupConfig": [],
                        "customParameters": {}
                    },
                    "free": false
                }
            ],
            "tags": [],
            "requires": [],
            "dashboard": {
                "url": "https://osb-example.cf.host/custom/v2/authentication",
                "auth_endpoint": "https://uaa.cf.host/oauth"
            },
            "instances_retrievable": true,
            "bindings_retrievable": true,
            "dashboard_client": {
                "id": "osb-example.cf.host",
                "secret": "super-secretly-secured-secret",
                "redirect_uri": "https://osb-example.cf.host/custom/v2/authentication"
            },
            "plan_updateable": false
        },
        {
            "id": "a563099b-7b5c-4f09-8587-abb6de18e439",
            "name": "another-osb",
            "description": "Another example service broker",
            "bindable": false,
            "plans": [
                {
                    "id": "8bb50268-b8ea-47b9-b5bd-e36db462453c",
                    "name": "s",
                    "description": "An other standard example plan",
                    "metadata": {
                        "connections": 2,
                        "instanceGroupConfig": [],
                        "customParameters": {}
                    },
                    "free": true
                }
            ],
            "tags": [],
            "requires": [],
            "dashboard": {
                "url": "https://another-osb.cf.host/custom/v2/authentication",
                "auth_endpoint": "https://uaa.cf.host/oauth"
            },
            "instances_retrievable": true,
            "bindings_retrievable": true,
            "dashboard_client": {
                "id": "another-osb.cf.dev.eu-de-central.msh.host",
                "secret": "super-secretly-secured-secret",
                "redirect_uri": "https://another-osb.cf.host/custom/v2/authentication"
            },
            "plan_updateable": false
        },
        {
            "id": "shared-instances-id",
            "name": "shared-instances",
            "description": "This service definition represents the shared service instances.",
            "bindable": true,
            "plans": [
                {
                    "id": "f01a88ee-0fd4-44a3-924a-87e259b2c225",
                    "name": "si-of-osb-example",
                    "description": "Org: example-org, Space: example-space",
                    "metadata": {
                        "instanceGroupConfig": [],
                        "customParameters": {}
                    },
                    "free": false
                },
                {
                    "id": "3130cc80-823c-4186-91e4-7b31815acb3b",
                    "name": "si-of-osb-example",
                    "description": "Org: example-org, Space: example-space-2",
                    "metadata": {
                        "instanceGroupConfig": [],
                        "customParameters": {}
                    },
                    "free": false
                }
            ],
            "tags": [],
            "requires": [],
            "instances_retrievable": false,
            "bindings_retrievable": false,
            "plan_updateable": false
        }
    ]
}
```       

---
<p align="center">
    <span ><a href="./api.md"><- Previous</a></span>
	    <span>&nbsp; | &nbsp;</span> 
    <span><a href="./servicebrokercommunication.md">Next -></a></span>
</p>


[osb-checker]: https://github.com/openservicebrokerapi/osb-checker
[osb-checker-kotlin]: https://github.com/evoila/osb-checker-kotlin 
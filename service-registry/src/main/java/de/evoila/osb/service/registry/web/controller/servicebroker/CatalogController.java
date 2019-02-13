package de.evoila.osb.service.registry.web.controller.servicebroker;


import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.osb.service.registry.exceptions.InvalidFieldException;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.manager.ServiceBrokerManager;
import de.evoila.osb.service.registry.manager.ServiceDefinitionCacheManager;
import de.evoila.osb.service.registry.manager.SharedInstancesManager;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.web.bodies.CatalogResponse;
import de.evoila.osb.service.registry.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@RestController
public class CatalogController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(CatalogController.class);

    private ServiceBrokerManager sbManager;
    private ServiceDefinitionCacheManager cacheManager;
    private SharedInstancesManager sharedInstancesManager;

    public CatalogController(ServiceBrokerManager sbManager, ServiceDefinitionCacheManager cacheManager, SharedInstancesManager sharedInstancesManager) {
        this.sbManager = sbManager;
        this.cacheManager = cacheManager;
        this.sharedInstancesManager = sharedInstancesManager;
    }

    @GetMapping(value = "/brokers/{brokerId}/v2/catalog")
    public ResponseEntity<?> getCatalogOfBroker(@PathVariable String brokerId) throws InvalidFieldException, ResourceNotFoundException {
        log.info("Catalog request received for: " + brokerId);
        ServiceBroker serviceBroker = sbManager.getServiceBrokerWithExistenceCheck(brokerId);

        sbManager.updateServiceBrokerCatalog(serviceBroker, false);

        List<ServiceDefinition> services = cacheManager.getUnmodifiableDefinitions(brokerId);
        if (services == null) {
            log.error("Could not get service definitions for service broker '" + brokerId + "'.");
            throw new ResourceNotFoundException("service definitions");
        }

        log.info("Catalog prepared for: " + brokerId);
        return new ResponseEntity<CatalogResponse>(new CatalogResponse(services), HttpStatus.OK);
    }

    @GetMapping(value = "/brokers/{brokerId}/v2/catalog/shared")
    public ResponseEntity<?> getSharedCatalogOfBroker(@PathVariable String brokerId) throws InvalidFieldException, ResourceNotFoundException {
        log.info("Shared catalog request received for: " + brokerId);
        ServiceBroker serviceBroker = sbManager.getServiceBrokerWithExistenceCheck(brokerId);

        List<ServiceDefinition> services = new LinkedList<>();
        Optional<ServiceDefinition> sharedDefinition = sharedInstancesManager.getSharedServiceDefinition(serviceBroker);
        if (sharedDefinition.isPresent() && sharedDefinition.get().getPlans() != null && sharedDefinition.get().getPlans().size() > 0)
            services.add(sharedDefinition.get());
        log.info("Catalog prepared for: " + brokerId);
        return new ResponseEntity<CatalogResponse>(new CatalogResponse(services), HttpStatus.OK);
    }

    @GetMapping(value = "/v2/catalog/shared")
    public ResponseEntity<?> getSharedCatalog() {
        log.info("Received shared instances catalog request.");
        List<ServiceDefinition> definitions = new LinkedList<>();
        ServiceDefinition sharedDefinition = sharedInstancesManager.getSharedServiceDefinition();
        if (sharedDefinition.getPlans() != null && sharedDefinition.getPlans().size() > 0)
            definitions.add(sharedDefinition);
        CatalogResponse response = new CatalogResponse(definitions);
        log.debug("Built new catalog response for shared instances: " + response.toString());
        return new ResponseEntity<CatalogResponse>(response, HttpStatus.OK);
    }
}

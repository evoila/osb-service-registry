package de.evoila.osb.service.registry.web.controller.servicebroker;


import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.osb.service.registry.exceptions.InvalidFieldException;
import de.evoila.osb.service.registry.exceptions.NotAuthorizedException;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.manager.ServiceBrokerManager;
import de.evoila.osb.service.registry.manager.ServiceDefinitionCacheManager;
import de.evoila.osb.service.registry.manager.SharedInstancesManager;
import de.evoila.osb.service.registry.manager.VisibilityManager;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.web.bodies.CatalogResponse;
import de.evoila.osb.service.registry.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CatalogController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(CatalogController.class);

    private ServiceBrokerManager sbManager;
    private ServiceDefinitionCacheManager cacheManager;
    private SharedInstancesManager sharedInstancesManager;
    private VisibilityManager visibilityManager;

    public CatalogController(ServiceBrokerManager sbManager, ServiceDefinitionCacheManager cacheManager, SharedInstancesManager sharedInstancesManager, VisibilityManager visibilityManager) {
        this.sbManager = sbManager;
        this.cacheManager = cacheManager;
        this.sharedInstancesManager = sharedInstancesManager;
        this.visibilityManager = visibilityManager;
    }

    @GetMapping(value = "/brokers/{brokerId}/v2/catalog")
    public ResponseEntity<?> getCatalogOfBroker(@PathVariable String brokerId) throws InvalidFieldException, ResourceNotFoundException, NotAuthorizedException {
        log.info("Catalog request received for broker: " + brokerId);
        ServiceBroker serviceBroker = sbManager.getServiceBrokerWithExistenceCheck(brokerId);

        sbManager.updateServiceBrokerCatalog(serviceBroker, false);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!visibilityManager.hasAccessTo(authentication, serviceBroker)) throw new NotAuthorizedException(authentication.getName());

        List<ServiceDefinition> services = cacheManager.getUnmodifiableDefinitions(brokerId);
        if (services == null) {
            log.error("Could not get service definitions for service broker '" + brokerId + "'.");
            throw new ResourceNotFoundException("service definitions");
        }

        log.info("Catalog prepared for: " + brokerId);
        return new ResponseEntity<CatalogResponse>(new CatalogResponse(services), HttpStatus.OK);
    }
}

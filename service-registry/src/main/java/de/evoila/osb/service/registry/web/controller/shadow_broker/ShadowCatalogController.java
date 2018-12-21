package de.evoila.osb.service.registry.web.controller.shadow_broker;

import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.osb.service.registry.manager.RegistryServiceInstanceManager;
import de.evoila.osb.service.registry.manager.ServiceBrokerManager;
import de.evoila.osb.service.registry.manager.SharedInstancesManager;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.web.bodies.CatalogResponse;
import de.evoila.osb.service.registry.manager.ServiceDefinitionCacheManager;
import de.evoila.osb.service.registry.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
public class ShadowCatalogController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(ShadowCatalogController.class);

    private ServiceBrokerManager sbManager;
    private ServiceDefinitionCacheManager cacheManager;
    private RegistryServiceInstanceManager serviceInstanceManager;
    private SharedInstancesManager sharedInstancesManager;

    public ShadowCatalogController(ServiceBrokerManager sbManager, ServiceDefinitionCacheManager cacheManager, RegistryServiceInstanceManager serviceInstanceManager, SharedInstancesManager sharedInstancesManager) {
        this.sbManager = sbManager;
        this.cacheManager = cacheManager;
        this.serviceInstanceManager = serviceInstanceManager;
        this.sharedInstancesManager = sharedInstancesManager;
    }

    @GetMapping(value = "/v2/catalog")
    public ResponseEntity<?> getCatalog() {
        log.info("Received shadow service broker catalog request");
        List<ServiceDefinition> allDefinitions = new LinkedList<>();

        sbManager.updateAllServiceBrokerCatalogs(false);

        for (ServiceBroker serviceBroker : sbManager.getAll()) {
            List<ServiceDefinition> services = cacheManager.getUnmodifiableDefinitions(serviceBroker.getId());
            if (services == null) {
                log.error("Could not get service definitions for service broker '" + serviceBroker.getId() + "'.");
            } else {
                allDefinitions.addAll(services);
            }
        }

        ServiceDefinition sharedDefinition = sharedInstancesManager.getSharedServiceDefinition();
        if (sharedDefinition.getPlans() != null && sharedDefinition.getPlans().size() > 0)
            allDefinitions.add(sharedDefinition);

        CatalogResponse response = new CatalogResponse(allDefinitions);
        log.debug("Built new catalog response: " + response.toString());
        return new ResponseEntity<CatalogResponse>(response, HttpStatus.OK);
    }
}

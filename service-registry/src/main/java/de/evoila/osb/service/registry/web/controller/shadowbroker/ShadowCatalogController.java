package de.evoila.osb.service.registry.web.controller.shadowbroker;

import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.osb.service.registry.exceptions.NotAuthorizedException;
import de.evoila.osb.service.registry.manager.ServiceBrokerManager;
import de.evoila.osb.service.registry.manager.ServiceDefinitionCacheManager;
import de.evoila.osb.service.registry.manager.SharedInstancesManager;
import de.evoila.osb.service.registry.manager.VisibilityManager;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import de.evoila.osb.service.registry.web.bodies.CatalogResponse;
import de.evoila.osb.service.registry.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
public class ShadowCatalogController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(ShadowCatalogController.class);

    private ServiceBrokerManager sbManager;
    private ServiceDefinitionCacheManager cacheManager;
    private SharedInstancesManager sharedInstancesManager;
    private VisibilityManager visibilityManager;

    public ShadowCatalogController(ServiceBrokerManager sbManager, ServiceDefinitionCacheManager cacheManager, SharedInstancesManager sharedInstancesManager, VisibilityManager visibilityManager) {
        this.sbManager = sbManager;
        this.cacheManager = cacheManager;
        this.sharedInstancesManager = sharedInstancesManager;
        this.visibilityManager = visibilityManager;
    }

    @GetMapping(value = "/v2/catalog")
    public ResponseEntity<?> getCatalog() throws NotAuthorizedException {
        log.info("Received shadow service broker catalog request");
        List<ServiceDefinition> returnedDefinitions = new LinkedList<>();

        sbManager.updateAllServiceBrokerCatalogs(false);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<ServiceDefinition> visibleDefinitions = visibilityManager.getVisibleServiceDefinitionsForUser(authentication);
        returnedDefinitions.addAll(visibleDefinitions);

        List<RegistryServiceInstance> sharedInstances = visibilityManager.getVisibleSharedRegistryServiceInstances(authentication);
        ServiceDefinition sharedDefinition = sharedInstancesManager.getSharedServiceDefinition(sharedInstances);
        if (sharedDefinition.getPlans() != null && sharedDefinition.getPlans().size() > 0)
            returnedDefinitions.add(sharedDefinition);

        if (returnedDefinitions.isEmpty()) {
            log.debug("No visible service definitions for the user "+authentication.getName()+". Returning a dummy service definition to prevent blockage of the response by the plattform.");
            returnedDefinitions.add(cacheManager.getDummyServiceDefinition());
        }


        CatalogResponse response = new CatalogResponse(returnedDefinitions);
        log.debug("Built new catalog response: " + response.toString());
        return new ResponseEntity<CatalogResponse>(response, HttpStatus.OK);
    }
}

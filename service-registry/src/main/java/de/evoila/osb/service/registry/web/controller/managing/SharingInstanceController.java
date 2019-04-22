package de.evoila.osb.service.registry.web.controller.managing;

import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.osb.service.registry.exceptions.InvalidFieldException;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.manager.RegistryServiceInstanceManager;
import de.evoila.osb.service.registry.manager.ServiceBrokerManager;
import de.evoila.osb.service.registry.manager.ServiceDefinitionCacheManager;
import de.evoila.osb.service.registry.manager.SharedInstancesManager;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.model.service.broker.SharedContext;
import de.evoila.osb.service.registry.web.bodies.CatalogResponse;
import de.evoila.osb.service.registry.web.bodies.ErrorResponse;
import de.evoila.osb.service.registry.web.bodies.ShareResponse;
import de.evoila.osb.service.registry.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@RestController
public class SharingInstanceController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(SharingInstanceController.class);

    private ServiceBrokerManager sbManager;
    private RegistryServiceInstanceManager instanceManager;
    private SharedInstancesManager sharedInstancesManager;
    private ServiceDefinitionCacheManager cacheManager;

    public SharingInstanceController(ServiceBrokerManager sbManager, RegistryServiceInstanceManager instanceManager, SharedInstancesManager sharedInstancesManager, ServiceDefinitionCacheManager cacheManager) {
        this.sbManager = sbManager;
        this.instanceManager = instanceManager;
        this.sharedInstancesManager = sharedInstancesManager;
        this.cacheManager = cacheManager;
    }

    @PatchMapping(value = "/service_instance/{instanceId}/shareable")
    public ResponseEntity<?> shareable(
            @PathVariable("instanceId") String serviceInstanceId,
            @RequestParam(value = "sharing", defaultValue = "false") boolean sharing,
            @RequestParam(value = "displayname", defaultValue = "") String displayName) throws ResourceNotFoundException {
        log.info("Received sharing managing request");

        ServiceBroker serviceBroker = sbManager.searchForServiceBrokerWithServiceInstanceId(serviceInstanceId, HttpStatus.NOT_FOUND);
        RegistryServiceInstance serviceInstance = serviceBroker.getServiceInstance(serviceInstanceId).get();
        ServiceDefinition definition = cacheManager.getDefinition(serviceInstance.getBroker().getId()
                , serviceInstance.getServiceDefinitionIdForServiceBroker());

        SharedContext sharedContext = serviceInstance.getSharedContext();
        if (sharedContext == null) sharedContext = new SharedContext();

        if (sharing) {
            log.info("Initiating shared context for " + serviceInstance.getId());
            sharedContext.initShared(serviceInstance, definition);
            if (displayName != null && !displayName.isEmpty()) sharedContext.setDisplayName(displayName);
        } else {
            if (!sharedInstancesManager.isTheOnlySharedInstance(serviceInstance))
                return new ResponseEntity<ErrorResponse>(new ErrorResponse("There are still other service instances referring to this instance, therefor it can not be unshared."), HttpStatus.BAD_REQUEST);
            log.info("Setting shared to false for " + serviceInstance.getId());
            sharedContext.setShared(false);
        }
        log.info("Updating service instance " + serviceInstance.getId() + " in storage.");
        instanceManager.update(serviceInstance);

        return new ResponseEntity<ShareResponse>(new ShareResponse(serviceInstance.isShared(), serviceInstance.getSharedContext().getDisplayNameOrDefaultName()), HttpStatus.OK);
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

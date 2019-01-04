package de.evoila.osb.service.registry.web.controller;

import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.manager.RegistryServiceInstanceManager;
import de.evoila.osb.service.registry.manager.ServiceBrokerManager;
import de.evoila.osb.service.registry.manager.SharedInstancesManager;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.model.service.broker.SharedContext;
import de.evoila.osb.service.registry.web.bodies.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SharingInstanceController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(SharingInstanceController.class);

    private ServiceBrokerManager sbManager;
    private RegistryServiceInstanceManager instanceManager;
    private SharedInstancesManager sharedInstancesManager;

    public SharingInstanceController(ServiceBrokerManager sbManager, RegistryServiceInstanceManager instanceManager, SharedInstancesManager sharedInstancesManager) {
        this.sbManager = sbManager;
        this.instanceManager = instanceManager;
        this.sharedInstancesManager = sharedInstancesManager;
    }

    @PatchMapping(value = "/service_instance/{instanceId}/shareable")
    public ResponseEntity<?> shareable(
            @PathVariable("instanceId") String serviceInstanceId,
            @RequestParam(value = "sharing", defaultValue = "false") boolean sharing) throws ResourceNotFoundException {
        log.info("Received sharing managing request");

        ServiceBroker serviceBroker = sbManager.searchForServiceBrokerWithServiceInstanceId(serviceInstanceId, HttpStatus.NOT_FOUND);
        RegistryServiceInstance serviceInstance = serviceBroker.getServiceInstance(serviceInstanceId);
        SharedContext sharedContext = serviceInstance.getSharedContext();
        if (sharing) {
            log.info("Initiating shared context for " + serviceInstance.getId());
            sharedContext.initShared();
        } else {
            if (!sharedInstancesManager.isTheOnlySharedInstance(serviceInstance))
                return new ResponseEntity<ErrorResponse>(new ErrorResponse("There are still other service instances referring to this instance, therefor it can not be unshared."), HttpStatus.BAD_REQUEST);
            log.info("Setting shared to false for " + serviceInstance.getId());
            sharedContext.setShared(false);
        }
        log.info("Updating service instance " + serviceInstance.getId() + " in storage.");
        instanceManager.update(serviceInstance);

        return new ResponseEntity<String>("", HttpStatus.OK);
    }
}

package de.evoila.osb.service.registry.web.controller.shadow_broker;

import de.evoila.cf.broker.model.*;
import de.evoila.osb.service.registry.exceptions.NotSharedException;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.manager.*;
import de.evoila.osb.service.registry.model.ResponseWithHttpStatus;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.model.service.broker.SharedContext;
import de.evoila.osb.service.registry.web.bodies.ErrorResponse;
import de.evoila.osb.service.registry.web.controller.BaseController;
import de.evoila.osb.service.registry.web.request.services.ShadowServiceInstanceRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@RestController
public class ShadowServiceInstanceController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(ShadowServiceInstanceController.class);

    private ServiceBrokerManager sbManager;
    private ServiceDefinitionCacheManager cacheManager;
    private RegistryServiceInstanceManager serviceInstanceManager;
    private SharedInstancesManager sharedInstancesManager;
    private SharedContextManager sharedContextManager;

    public ShadowServiceInstanceController(ServiceBrokerManager sbManager, ServiceDefinitionCacheManager cacheManager, RegistryServiceInstanceManager serviceInstanceManager, SharedInstancesManager sharedInstancesManager, SharedContextManager sharedContextManager) {
        this.sbManager = sbManager;
        this.cacheManager = cacheManager;
        this.serviceInstanceManager = serviceInstanceManager;
        this.sharedInstancesManager = sharedInstancesManager;
        this.sharedContextManager = sharedContextManager;
    }

    @GetMapping(value = "/v2/service_instances/{instanceId}/last_operation")
    public ResponseEntity<?> lastOperationOfServiceInstance(
            @PathVariable("instanceId") String serviceInstanceId,
            @RequestHeader("X-Broker-API-Version") String apiVersion,
            @RequestParam(value = "service_id", required = false) String serviceDefinitionId,
            @RequestParam(value = "plan_id", required = false) String planId,
            @RequestParam(value = "operation", required = false) String operation) throws ResourceNotFoundException {

        log.info("Received shadow service broker service instance last operation request.");

        RegistryServiceInstance serviceInstance = serviceInstanceManager.searchServiceInstance(serviceInstanceId);
        ServiceBroker sb = sbManager.searchForServiceBrokerWithServiceInstanceId(serviceInstanceId);
        Map<String, String> queryParams = new HashMap<String, String>();
        if (serviceDefinitionId != null) queryParams.put("service_id", serviceDefinitionId);
        if (planId != null) queryParams.put("plan_id", planId);
        if (operation != null) queryParams.put("operation", operation);

        ResponseWithHttpStatus<JobProgressResponse> response = null;
        try {
            response = ShadowServiceInstanceRequestService.pollServiceInstance(sb, serviceInstance.getIdForServiceBroker(), apiVersion, queryParams);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() != HttpStatus.GONE)
                throw ex;

            if (serviceInstance.getBindings().size() == 0) {
                log.debug("Last operation returned with 410 (GONE) and no bindings exist -> deleting the service instance.");
                serviceInstanceManager.remove(serviceInstance);
            } else {
                log.debug("Last operation returned with 410 (GONE) but there are still " + serviceInstance.getBindings().size() + " bindings in existence -> NOT deleting the service instance.");
            }

            return new ResponseEntity<String>("", HttpStatus.GONE);
        }

        if (response != null && response.getBody() != null && response.getBody().getState() != null) {
            String state = response.getBody().getState();
            if (state.equals("succeeded")) {
                if (serviceInstance.isCreationInProgress()) {
                    log.debug("Operation state is 'succeeded' and creation is in progress -> toggling creation flag.");
                    serviceInstance.setCreationInProgress(false);
                    serviceInstanceManager.update(serviceInstance);
                }
                // case of succeeded and isDeletionInProgress can never occur
            } else if (state.equals("failed")) {
                if (serviceInstance.isCreationInProgress()) {
                    log.debug("Operation state is 'failed' and creation is in progress -> removing service instance from storage.");
                    serviceInstanceManager.remove(serviceInstance);
                } else if (serviceInstance.isDeletionInProgress()) {
                    log.debug("Operation state is 'failed' and deletion is in progress -> toggling deletion flag.");
                    serviceInstance.setDeletionInProgress(false);
                }
            }
        }

        return new ResponseEntity<>(response.getBody(), response.getStatus());
    }

    @GetMapping(value = "/v2/service_instances/{instanceId}")
    public ResponseEntity<?> fetchServiceInstance(
            @PathVariable("instanceId") String serviceInstanceId,
            @RequestHeader("X-Broker-API-Version") String apiVersion) throws ResourceNotFoundException {

        log.info("Received shadow service broker service instance fetch request.");
        RegistryServiceInstance serviceInstance = serviceInstanceManager.searchServiceInstance(serviceInstanceId);
        ServiceBroker serviceBroker = sbManager.searchForServiceBrokerWithServiceInstanceId(serviceInstanceId, HttpStatus.NOT_FOUND);
        ResponseWithHttpStatus<ServiceInstance> response = ShadowServiceInstanceRequestService.fetchServiceInstance(serviceBroker, serviceInstance.getIdForServiceBroker(), apiVersion);
        return new ResponseEntity<ServiceInstance>(response.getBody(), response.getStatus());
    }

    @PutMapping(value = "/v2/service_instances/{instanceId}")
    public ResponseEntity<?> createServiceInstance(
            @PathVariable("instanceId") String serviceInstanceId,
            @RequestParam(value = "accepts_incomplete", required = false, defaultValue = "false") Boolean acceptsIncomplete,
            @RequestHeader("X-Broker-API-Version") String apiVersion,
            @RequestHeader(value = "X-Broker-API-Originating-Identity", required = false) String originatingIdentity,
            @Valid @RequestBody ServiceInstanceRequest request) throws ResourceNotFoundException, NotSharedException {

        log.info("Received shadow service broker create service instance request.");

        if (request.getServiceDefinitionId().equals(SharedInstancesManager.SHARED_DEFINITIONS_ID))
            return handleSharedInstance(serviceInstanceId, acceptsIncomplete, apiVersion, originatingIdentity, request);

        ServiceBroker sb = sbManager.searchForServiceBrokerWithServiceDefinitionId(request.getServiceDefinitionId());

        ResponseWithHttpStatus<ServiceInstanceResponse> response = null;
        try {
            response = ShadowServiceInstanceRequestService.createServiceInstance(sb, serviceInstanceId, apiVersion, originatingIdentity, acceptsIncomplete, request);
        } catch (HttpClientErrorException ex) {
            log.error("Received a error when trying to provision an instance at the service broker " + sb.getHost(), ex);
            throw ex;
        }

        // Check that the instance does not exist already, otherwise do not add a new instance.
        // This can be caused by an additional async create call for an already existing instance in creation progress (See OSB specification for async instance provisioning).
        if (!serviceInstanceManager.exists(serviceInstanceId)) {
            RegistryServiceInstance registryServiceInstance = new RegistryServiceInstance(
                    serviceInstanceId, request.getServiceDefinitionId(), request.getPlanId(), request.getOrganizationGuid(),
                    request.getSpaceGuid(), "", false, false, true,
                    response.getBody().getDashboardUrl(), sb, new LinkedList<>(), new SharedContext());

            if (acceptsIncomplete && response.getStatus() == HttpStatus.ACCEPTED) {
                log.debug("Setting creation progress to true for service instance: " + serviceInstanceId);
                registryServiceInstance.setCreationInProgress(true);
            }
            log.debug("Saving service instance in the storage: " + serviceInstanceId);
            serviceInstanceManager.add(registryServiceInstance);
        }

        return new ResponseEntity<ServiceInstanceResponse>(response.getBody(), response.getStatus());
    }

    private ResponseEntity<?> handleSharedInstance(
            String serviceInstanceId,
            Boolean acceptsIncomplete,
            String apiVersion,
            String originatingIdentity,
            ServiceInstanceRequest request) throws ResourceNotFoundException, NotSharedException {

        if (serviceInstanceManager.exists(serviceInstanceId))
            return new ResponseEntity<String>("", HttpStatus.CONFLICT);

        RegistryServiceInstance sharedOfInstance = serviceInstanceManager.searchServiceInstance(request.getPlanId());
        if (!sharedOfInstance.isShared())
            throw new NotSharedException();

        RegistryServiceInstance registryServiceInstance = new RegistryServiceInstance(serviceInstanceId,
                request.getServiceDefinitionId(),
                request.getPlanId(),
                request.getOrganizationGuid(),
                request.getSpaceGuid(),
                "",
                false,
                false,
                false,
                sharedOfInstance.getDashboardUrl(),
                sharedOfInstance.getBroker(),
                new LinkedList<>(),
                new SharedContext(sharedOfInstance.getSharedContext()));

        serviceInstanceManager.add(registryServiceInstance);

        return new ResponseEntity<ServiceInstanceResponse>(
                new ServiceInstanceResponse(registryServiceInstance.getDashboardUrl()), HttpStatus.CREATED);
    }

    @PatchMapping(value = "/v2/service_instances/{instance_id}")
    public ResponseEntity<?> updateServiceInstance(
            @PathVariable("instance_id") String serviceInstanceId,
            @RequestHeader("X-Broker-API-Version") String apiVersion,
            @RequestHeader(value = "X-Broker-API-Originating-Identity", required = false) String originatingIdentity,
            @RequestParam(value = "accepts_incomplete", required = false, defaultValue = "false") Boolean acceptsIncomplete,
            @Valid @RequestBody ServiceInstanceUpdateRequest request) throws ResourceNotFoundException {

        log.info("Received shadow service broker update service instance request.");

        RegistryServiceInstance serviceInstance = serviceInstanceManager.searchServiceInstance(serviceInstanceId);
        ServiceBroker sb = sbManager.searchForServiceBrokerWithServiceInstanceId(serviceInstanceId);

        ResponseWithHttpStatus<ServiceInstanceUpdateResponse> response = ShadowServiceInstanceRequestService.updateServiceInstance(
                sb, serviceInstance.getIdForServiceBroker(), apiVersion, originatingIdentity, acceptsIncomplete, request);

        return new ResponseEntity<>(response.getBody(), response.getStatus());
    }

    @DeleteMapping(value = "/v2/service_instances/{instance_id}")
    public ResponseEntity<?> deleteServiceInstance(
            @PathVariable("instance_id") String instanceId,
            @RequestHeader("X-Broker-API-Version") String apiVersion,
            @RequestHeader(value = "X-Broker-API-Originating-Identity", required = false) String originatingIdentity,
            @RequestParam(value = "accepts_incomplete", required = false, defaultValue = "false") Boolean acceptsIncomplete,
            @RequestParam("service_id") String serviceId,
            @RequestParam("plan_id") String planId) throws ResourceNotFoundException {

        log.info("Received shadow service broker delete service instance request.");

        RegistryServiceInstance serviceInstance = serviceInstanceManager.searchServiceInstance(instanceId);
        if (serviceInstance.getBindings().size() > 0)
            return new ResponseEntity<ErrorResponse>(new ErrorResponse("There are still active binding in existence. Unbind them before deprovisioning the service instance."), HttpStatus.PRECONDITION_FAILED);
        if (!sharedInstancesManager.isTheOnlySharedInstance(serviceInstance)) {
            log.info("This service instance is not the only existing shared instance -> only deleting the registry entry");
            serviceInstanceManager.remove(serviceInstance);
            return new ResponseEntity<String>("{}", HttpStatus.OK);
        }

        ServiceBroker sb = sbManager.searchForServiceBrokerWithServiceDefinitionId(serviceId);
        ResponseWithHttpStatus<String> response = null;
        try {
            response = ShadowServiceInstanceRequestService.deleteServiceInstance(sb, serviceInstance.getIdForServiceBroker(), serviceId, planId, apiVersion, originatingIdentity, acceptsIncomplete);
        } catch (HttpClientErrorException ex) {
            log.error("Received a error when trying to delete an instance at the service broker " + sb.getHost(), ex);
            throw ex;
        }
        log.debug("Request to the service broker returned successful with code " + response.getStatus());

        if (acceptsIncomplete && response.getStatus() == HttpStatus.ACCEPTED) {
            log.debug("Setting deletion progress to true for service instance: " + instanceId);
            serviceInstance.setDeletionInProgress(true);
            log.debug("Updating binding in the storage: " + instanceId);
            serviceInstanceManager.update(serviceInstance);
            // actual removal from storage needs to be done by last_operation endpoint
        } else if (response.getStatus() == HttpStatus.OK) {
            log.debug("Removing binding from storage: " + instanceId);
            serviceInstanceManager.remove(serviceInstance);
        }

        return new ResponseEntity<>(response.getBody(), response.getStatus());
    }
}

package de.evoila.osb.service.registry.web.controller.shadowbroker;

import de.evoila.cf.broker.model.JobProgressResponse;
import de.evoila.cf.broker.model.ServiceInstanceBinding;
import de.evoila.cf.broker.model.ServiceInstanceBindingRequest;
import de.evoila.cf.broker.model.ServiceInstanceBindingResponse;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.exceptions.SharedContextInvalidException;
import de.evoila.osb.service.registry.manager.*;
import de.evoila.osb.service.registry.model.ResponseWithHttpStatus;
import de.evoila.osb.service.registry.model.service.broker.RegistryBinding;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.web.controller.BaseController;
import de.evoila.osb.service.registry.web.request.services.ServiceBindingRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ShadowServiceBindingController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(ShadowServiceBindingController.class);

    private ServiceBrokerManager sbManager;
    private ServiceDefinitionCacheManager cacheManager;
    private RegistryServiceInstanceManager serviceInstanceManager;
    private RegistryBindingManager bindingManager;
    private SharedInstancesManager sharedInstancesManager;

    public ShadowServiceBindingController(ServiceBrokerManager sbManager, ServiceDefinitionCacheManager cacheManager, RegistryServiceInstanceManager serviceInstanceManager, RegistryBindingManager bindingManager, SharedInstancesManager sharedInstancesManager) {
        this.sbManager = sbManager;
        this.cacheManager = cacheManager;
        this.serviceInstanceManager = serviceInstanceManager;
        this.bindingManager = bindingManager;
        this.sharedInstancesManager = sharedInstancesManager;
    }

    @GetMapping(value = "/v2/service_instances/{instanceId}/service_bindings/{bindingId}")
    public ResponseEntity<?> fetchBinding(
            @PathVariable("instanceId") String serviceInstanceId,
            @PathVariable("bindingId") String serviceBindingId,
            @RequestHeader("X-Broker-API-Version") String apiVersion) throws ResourceNotFoundException {

        log.info("Received shadow service broker fetch service binding request.");

        RegistryServiceInstance serviceInstance = serviceInstanceManager.searchServiceInstance(serviceInstanceId);
        RegistryBinding binding = bindingManager.searchRegistryBinding(serviceBindingId, HttpStatus.NOT_FOUND);
        if (!binding.isBindingOf(serviceInstance))
            throw new ResourceNotFoundException("binding", HttpStatus.BAD_REQUEST);

        ServiceBroker sb = sbManager.searchForServiceBrokerWithServiceInstanceId(serviceInstanceId);
        ResponseWithHttpStatus<ServiceInstanceBinding> response = ServiceBindingRequestService.fetchServiceBinding(sb, serviceInstance.getIdForServiceBroker(), serviceBindingId, apiVersion);

        return new ResponseEntity<>(response.getBody(), response.getStatus());
    }

    @GetMapping(value = "/v2/service_instances/{instanceId}/service_bindings/{bindingId}/last_operation")
    public ResponseEntity<?> lastOperationOfServiceBinding(
            @PathVariable("instanceId") String serviceInstanceId,
            @PathVariable("bindingId") String serviceBindingId,
            @RequestHeader("X-Broker-API-Version") String apiVersion,
            @RequestParam(value = "service_id", required = false) String serviceDefinitionId,
            @RequestParam(value = "plan_id", required = false) String planId,
            @RequestParam(value = "operation", required = false) String operation) throws ResourceNotFoundException {

        log.info("Received shadow service broker service binding last operation request.");

        RegistryServiceInstance serviceInstance = serviceInstanceManager.searchServiceInstance(serviceInstanceId);
        RegistryBinding binding = bindingManager.searchRegistryBinding(serviceBindingId);
        if (!binding.isBindingOf(serviceInstance))
            throw new ResourceNotFoundException("binding", HttpStatus.BAD_REQUEST);

        ServiceBroker sb = sbManager.searchForServiceBrokerWithServiceInstanceId(serviceInstanceId);
        Map<String, String> queryParams = new HashMap<String, String>();
        if (serviceDefinitionId != null) queryParams.put("service_id", serviceDefinitionId);
        if (planId != null) queryParams.put("plan_id", planId);
        if (operation != null) queryParams.put("operation", operation);

        ResponseWithHttpStatus<JobProgressResponse> response = null;
        try {
            response = ServiceBindingRequestService.pollServiceBinding(sb, serviceInstance.getIdForServiceBroker(),
                    serviceBindingId, apiVersion, queryParams);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() != HttpStatus.GONE)
                throw ex;

            log.debug("Last operation returned with 410 (GONE) -> deleting the binding.");
            bindingManager.remove(binding);
            return new ResponseEntity<String>("", HttpStatus.GONE);
        }

        if (response != null && response.getBody() != null && response.getBody().getState() != null) {
            String state = response.getBody().getState();
            if (state.equals("succeeded")) {
                if (binding.isCreationInProgress()) {
                    log.debug("Operation state is 'succeeded' and creation is in progress -> toggling creation flag.");
                    binding.setCreationInProgress(false);
                    bindingManager.update(binding);
                }
                // case of succeeded and isDeletionInProgress can never occur
            } else if (state.equals("failed")) {
                if (binding.isCreationInProgress()) {
                    log.debug("Operation state is 'failed' and creation is in progress -> removing binding from storage.");
                    bindingManager.remove(binding);
                } else if (binding.isDeleteInProgress()) {
                    log.debug("Operation state is 'failed' and deletion is in progress -> toggling deletion flag.");
                    binding.setDeleteInProgress(false);
                }
            }
        }

        return new ResponseEntity<>(response.getBody(), response.getStatus());
    }

    @PutMapping(value = "/v2/service_instances/{instanceId}/service_bindings/{bindingId}")
    public ResponseEntity<?> createBinding(
            @PathVariable("instanceId") String serviceInstanceId,
            @PathVariable("bindingId") String serviceBindingId,
            @RequestHeader("X-Broker-API-Version") String apiVersion,
            @RequestHeader(value = "X-Broker-API-Originating-Identity", required = false) String originatingIdentity,
            @RequestParam(value = "accepts_incomplete", required = false, defaultValue = "false") Boolean acceptsIncomplete,
            @Valid @RequestBody ServiceInstanceBindingRequest request
    ) throws ResourceNotFoundException, SharedContextInvalidException {

        log.info("Received shadow service broker create service binding request.");

        RegistryServiceInstance serviceInstance = serviceInstanceManager.searchServiceInstance(serviceInstanceId);
        if (!serviceInstance.isOriginalInstance()) {
            if (serviceInstance.getSharedContext() == null)
                throw new SharedContextInvalidException();
            serviceInstance.getSharedContext().validate();

            request.setServiceDefinitionId(serviceInstance.getSharedContext().getServiceDefinitionId());
            request.setPlanId(serviceInstance.getSharedContext().getPlanId());
        }

        ServiceBroker sb = sbManager.searchForServiceBrokerWithServiceInstanceId(serviceInstanceId);
        log.debug("Using following service broker for the create binding request: " + sb.getLoggingNameString());
        ResponseWithHttpStatus<ServiceInstanceBindingResponse> response = ServiceBindingRequestService.createServiceBinding(sb,
                serviceInstance.getIdForServiceBroker(), serviceBindingId, apiVersion, originatingIdentity, acceptsIncomplete, request);
        log.debug("Request to the service broker returned successful with code " + response.getStatus());

        // Check that the binding does not exist already, otherwise do not add a new binding.
        // This can be caused by an additional async create call for an already existing binding in creation progress (See OSB specification for async binding creation).
        if (!bindingManager.exists(serviceBindingId)) {
            String appGuid = request.getAppGuid() == null ? request.getBindResource().getAppGuid() : request.getAppGuid();
            String route = "";
            if (request.getBindResource() != null && request.getBindResource().getRoute() != null) route = request.getBindResource().getRoute();
            RegistryBinding binding = new RegistryBinding(serviceBindingId, appGuid, route, false, false, serviceInstance);
            if (acceptsIncomplete && response.getStatus() == HttpStatus.ACCEPTED) {
                log.debug("Setting creation progress to true for binding: " + serviceBindingId);
                binding.setCreationInProgress(true);
            }
            log.debug("Saving binding in the storage: " + serviceBindingId);
            bindingManager.add(binding);
        }

        return new ResponseEntity<ServiceInstanceBindingResponse>(response.getBody(), response.getStatus());
    }

    @DeleteMapping(value = "/v2/service_instances/{instance_id}/service_bindings/{bindingId}")
    public ResponseEntity<?> deleteServiceInstance(
            @PathVariable("instance_id") String serviceInstanceId,
            @PathVariable("bindingId") String serviceBindingId,
            @RequestHeader("X-Broker-API-Version") String apiVersion,
            @RequestHeader(value = "X-Broker-API-Originating-Identity", required = false) String originatingIdentity,
            @RequestParam(value = "accepts_incomplete", required = false, defaultValue = "false") Boolean acceptsIncomplete,
            @RequestParam("service_id") String serviceId,
            @RequestParam("plan_id") String planId) throws ResourceNotFoundException, SharedContextInvalidException {

        log.info("Received shadow service broker delete service binding request.");

        RegistryServiceInstance serviceInstance = serviceInstanceManager.searchServiceInstance(serviceInstanceId);
        RegistryBinding binding = bindingManager.searchRegistryBinding(serviceBindingId);

        if (serviceId != binding.getServiceInstance().getServiceDefinitionId() || planId != binding.getServiceInstance().getPlanId())
            throw new ResourceNotFoundException("service binding", HttpStatus.GONE);

        if (!binding.isBindingOf(serviceInstance))
            throw new ResourceNotFoundException("binding", HttpStatus.BAD_REQUEST);

        if (!serviceInstance.isOriginalInstance()) {
            if (serviceInstance.getSharedContext() == null)
                throw new SharedContextInvalidException();
            serviceInstance.getSharedContext().validate();

            serviceId = serviceInstance.getSharedContext().getServiceDefinitionId();
            planId = serviceInstance.getSharedContext().getPlanId();
        }

        ServiceBroker sb = sbManager.searchForServiceBrokerWithServiceInstanceId(serviceInstanceId);
        log.debug("Using following service broker for the delete binding request: " + sb.getLoggingNameString());
        ResponseWithHttpStatus<String> response = null;
        try {
            response = ServiceBindingRequestService.deleteBinding(sb, serviceInstance.getIdForServiceBroker(), serviceId, serviceBindingId, planId, apiVersion, originatingIdentity, acceptsIncomplete);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.GONE) {
                log.debug("Delete request to service broker returned with a 410 GONE -> deleting the binding in the service registry.");
                bindingManager.remove(binding);
                return new ResponseEntity<>(null, HttpStatus.GONE);
            }

            log.error("Received an error when trying to delete binding at the service broker " + sb.getLoggingNameString(), ex);
            throw ex;

        }
        log.debug("Request to the service broker returned successful with code " + response.getStatus());

        if (acceptsIncomplete && response.getStatus() == HttpStatus.ACCEPTED) {
            log.debug("Setting deletion progress to true for binding: " + serviceBindingId);
            binding.setDeleteInProgress(true);
            log.debug("Updating binding in the storage: " + serviceBindingId);
            bindingManager.update(binding);
            // actual removal from storage needs to be done by last_operation endpoint
        } else if (response.getStatus() == HttpStatus.OK) {
            log.debug("Removing binding from storage: " + serviceBindingId);
            bindingManager.remove(binding);
        }

        return new ResponseEntity<>(response.getBody(), response.getStatus());
    }
}

package de.evoila.osb.service.registry;

import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.cf.broker.model.catalog.plan.Plan;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.exceptions.SharedContextInvalidException;
import de.evoila.osb.service.registry.manager.*;
import de.evoila.osb.service.registry.model.service.broker.RegistryBinding;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.model.service.broker.SharedContext;
import de.evoila.osb.service.registry.util.Cryptor;
import de.evoila.osb.service.registry.util.MockServer;
import de.evoila.osb.service.registry.util.servicebroker.ServiceBrokerMockClient;
import de.evoila.osb.service.registry.util.servicebroker.TestRequestService;
import de.evoila.osb.service.registry.web.bodies.CatalogResponse;
import de.evoila.osb.service.registry.web.bodies.ServiceBrokerCreate;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@EnableAutoConfiguration
public class EndToEndTest {

    private static MockServer mockServer;
    private static ServiceBrokerMockClient sbMock;

    @Autowired
    private ServiceBrokerManager sbManager;
    @Autowired
    private SharedInstancesManager sharedInstancesManager;
    @Autowired
    private RegistryServiceInstanceManager instanceManager;
    @Autowired
    private RegistryBindingManager bindingManager;
    @Autowired
    private ServiceDefinitionCacheManager definitionCacheManager;
    @Autowired
    private SharedContextManager sharedContextManager;
    @Autowired
    private Cryptor cryptor;


    @BeforeClass
    public static void startMockServer() throws IOException {
        TestRequestService.initHeaders();
        mockServer = new MockServer(MockServer.SHARE, 8081);
        mockServer.startServer();
        ServiceBrokerMockClient.mockServiceBroker(ServiceBrokerMockClient.CATALOG_FILE_PATH);
        sbMock = ServiceBrokerMockClient.getServiceBrokerMock();
    }

    @AfterClass
    public static void stopServer() {
        mockServer.stopServer();
    }

    /**
     * This test fulfills a end-to-end test for sharing instances.
     * In it following steps are done and checked in order:
     * <ul>
     * <li>Used managers and services autowiring</li>
     * <li>Pre Test condition</li>
     * <li>Service Broker registration</li>
     * <li>Service instance provisioning</li>
     * <li>Service binding creation</li>
     * <li>Sharing a service instance</li>
     * <li>Catalog after sharing a service instance</li>
     * <li>Shared service instance provisioning</li>
     * <li>Service binding on shared instance creation</li>
     * <li>Catalog after shared service instance provisioning</li>
     * <li>Blocked service instance unprovisioning</li>
     * <li>Blocked unsharing of service instance</li>
     * <li>Binding deletion of original instance</li>
     * <li>Original service instance unprovisioning</li>
     * <li>Catalog after deprovisioning of original instance</li>
     * <li>Service Binding creation on shared service instance after </li>
     * <li>Unsharing the shared instance</li>
     * <li>Binding deletion of shared instance</li>
     * <li>Shared service instance unprovisioning</li>
     * <li>Post Test condition</li>
     *
     * </ul>
     */
    @Test
    public void runTest() throws ResourceNotFoundException {
        ServiceBrokerCreate serviceBrokerCreate = ServiceBrokerMockClient.getServiceBrokerCreate();
        ServiceInstanceRequest instanceRequest = ServiceBrokerMockClient.getServiceInstanceRequest(sbMock);
        ServiceInstanceRequest sharedInstanceRequest = ServiceBrokerMockClient.getSharedServiceInstanceRequest(sbMock, ServiceBrokerMockClient.TEST_INSTANCE_1);
        ServiceInstanceBindingRequest bindingRequest = ServiceBrokerMockClient.getBindingRequest(sbMock);
        String serviceDefinitionId = instanceRequest.getServiceDefinitionId();
        String planId = instanceRequest.getPlanId();

        // Check the availability of used services and managers
        checkAutowiring();

        // Check pre conditions
        checkCatalog(true, false);
        checkEmptyStorage();

        // Register new service broker and check catalog afterwards
        String brokerId = registerBroker(0, serviceBrokerCreate);
        ResponseEntity<CatalogResponse> catalogResponse = checkCatalog(false, false);

        // Fill values of singleton of ServiceBrokerMockClient for later use
        sbMock.setupMock(catalogResponse.getBody().getServices(), ServiceBrokerMockClient.SERVICE_BROKER_ORG, ServiceBrokerMockClient.SERVICE_BROKER_SPACE);

        // Provision original instance and add a binding
        String instanceId = provisionInstance(0,
                instanceRequest,
                ServiceBrokerMockClient.TEST_INSTANCE_1,
                false,
                null);
        String binding1Id = bind(0, bindingRequest, instanceId, ServiceBrokerMockClient.TEST_BINDING_1);

        // Try to unregister broker -> should be blocked
        unregisterBroker(1, brokerId, true);

        // Share original instance and check definition in catalog
        shareInstance(instanceId);
        checkCatalog(false, true);

        // Create a shared instance
        String sharedInstanceId = provisionInstance(1,
                sharedInstanceRequest,
                ServiceBrokerMockClient.TEST_INSTANCE_1_SHARED,
                true,
                instanceId);

        // Create bindings on original and shared instance
        String sharedBinding1Id = bind(1, bindingRequest, sharedInstanceId, ServiceBrokerMockClient.TEST_BINDING_OF_SHARED_1);
        String binding2Id = bind(2, bindingRequest, instanceId, ServiceBrokerMockClient.TEST_BINDING_2);

        // Try to delete instances with active bindings -> should be blocked
        deleteInstance(2, instanceId, serviceDefinitionId, planId, true);
        deleteInstance(2, sharedInstanceId, sharedInstanceRequest.getServiceDefinitionId(), sharedInstanceRequest.getPlanId(), true);

        // Try to unshare instances that have more than one reference -> should be blocked
        unshareInstance(instanceId, false);
        unshareInstance(sharedInstanceId, false);

        // Delete all bindings of original instance
        unbind(3, instanceId, binding1Id, serviceDefinitionId, planId);
        unbind(2, instanceId, binding2Id, serviceDefinitionId, planId);

        // Delete original instance and check catalog
        deleteInstance(2, instanceId, serviceDefinitionId, planId, false);
        checkCatalog(false, true);

        // Try to unregister broker after original instance was unprovisioned -> should be blocked
        unregisterBroker(1, brokerId, true);

        // Create new binding on shared instance with original instance being deleted
        String sharedBinding2Id = bind(1, bindingRequest, sharedInstanceId, ServiceBrokerMockClient.TEST_BINDING_OF_SHARED_2);

        // Unshare instance
        unshareInstance(sharedInstanceId, true);
        checkCatalog(false, false);

        // Delete bindings of shared instance
        unbind(2, sharedInstanceId, sharedBinding1Id, serviceDefinitionId, planId);
        unbind(1, sharedInstanceId, sharedBinding2Id, serviceDefinitionId, planId);

        // Unprovision shared instance
        deleteInstance(1, sharedInstanceId, serviceDefinitionId, planId, false);

        // Unregister service broker
        unregisterBroker(1, brokerId, false);

        // Check post conditions
        checkCatalog(true, false);
        checkEmptyStorage();

    }

    public void checkAutowiring() {
        // Check for correctly autowired managers and services
        assertNotNull("Service broker manager was not autowired correctly.", sbManager);
        assertNotNull("Shared instance manager was not autowired correctly.", sharedInstancesManager);
        assertNotNull("Registry instance manager was not autowired correctly.", instanceManager);
        assertNotNull("Service definition cache manager was not autowired correctly.", definitionCacheManager);
        assertNotNull("Cryptor service was not autowired correctly.", cryptor);
        assertNotNull("Registry binding manager was not autowired correctly.", bindingManager);
    }

    public void checkEmptyStorage() {
        assertTrue("There are registry service instances, but should not.", instanceManager.count() == 0);
        assertTrue("There are service brokers, but should not.", sbManager.count() == 0);
        assertTrue("There are service definitions cached, but should not.", definitionCacheManager.getUnmodifiableDefinitions().isEmpty());
        assertTrue("There are registry bindings, but should not.", bindingManager.count() == 0);
        assertTrue("There are shared service instances, but should not.", sharedInstancesManager.getSharedServiceInstances().isEmpty());
        assertTrue("There are shared contexts, but should not.", sharedContextManager.count() == 0);
    }

    public ResponseEntity<CatalogResponse> checkCatalog(boolean hasToBeEmpty, boolean sharedDefinitionExists) {
        // Check for empty catalog and service definitions
        ResponseEntity<CatalogResponse> catalogResponse = TestRequestService.getCatalog();
        assertFalse("Catalog request did not return successfully.",
                catalogResponse == null
                        || catalogResponse.getStatusCode() != HttpStatus.OK
                        || catalogResponse.getBody() == null
                        || catalogResponse.getBody().getServices() == null);
        List<ServiceDefinition> definitions = definitionCacheManager.getUnmodifiableDefinitions();
        List<ServiceDefinition> catalogDefinitions = catalogResponse.getBody().getServices();
        if (hasToBeEmpty) {
            assertTrue("Service Registry already has service definitions but should be empty.", definitions.isEmpty());
            assertTrue("Service Registry returned service definitions but should not.", catalogDefinitions.isEmpty());
        } else {
            // Check catalog after a broker was registered ( a broker has to hold atleast one service definition!)
            assertFalse("Service Registry should have service definitions by now.", definitions.isEmpty());
            assertFalse("Catalog should not be empty after registering a service broker.", catalogDefinitions.isEmpty());

            if (sharedDefinitionExists) {
                // Check catalog for a shared service definition and its values
                ServiceDefinition sharedDefinition = getSharedDefinition(catalogResponse.getBody());
                assertTrue("No service definition for shared instances was found after sharing an instance.",
                        sharedDefinition != null);
                assertTrue("The shared definitons lacks information to ensure proper usage.",
                        sharedDefinitionHasAllValues(sharedDefinition));

                for (Plan plan : getSharedDefinition(catalogResponse.getBody()).getPlans()) {
                    assertTrue("No instance was found with for a shared plan for the corresponding id",
                            instanceManager.get(plan.getId()).isPresent());
                    assertTrue("A shared plan does not hold all values or has incorrect ones.",
                            sharedDefinitionPlanHasCorrectValues(plan, instanceManager.get(plan.getId()).get()));
                }
            }
        }

        return catalogResponse;
    }

    public String registerBroker(int alreadyActiveBrokers, ServiceBrokerCreate serviceBrokerCreate) throws ResourceNotFoundException {
        // Register new broker and check pre and post conditions
        if (alreadyActiveBrokers < 0)
            throw new IllegalArgumentException("alreadyActiveBrokers must be zero or bigger.");
        assertTrue("There are more or less active service brokers than the expected " + alreadyActiveBrokers + ".",
                countExistingObjects(alreadyActiveBrokers, sbManager.getAll().iterator()));

        assertFalse("There are already active service brokers.", sbManager.getAll().iterator().hasNext());
        ResponseEntity<String> sbResponse = TestRequestService.registerTestServiceBroker(serviceBrokerCreate);
        assertSame("Response code is not 200 when registering the test service broker",
                sbResponse.getStatusCode(), HttpStatus.OK);

        assertTrue("There are more or less active service brokers than the expected " + (alreadyActiveBrokers + 1) + ".",
                countExistingObjects(alreadyActiveBrokers + 1, sbManager.getAll().iterator()));

        ServiceBroker sb = sbManager.searchForServiceBrokerWithServiceDefinitionId(sbMock.getDefinition().getId());
        assertTrue("Created service broker does not match the intended one.", brokerEqualsCreate(sb, serviceBrokerCreate));
        return sb.getId();
    }

    public String provisionInstance(int alreadyActiveInstances, ServiceInstanceRequest instanceRequest, String serviceInstanceId, boolean shouldBeShared, String originalInstanceId) throws ResourceNotFoundException {
        if (alreadyActiveInstances < 0)
            throw new IllegalArgumentException("alreadyActiveInstances must be zero or bigger.");
        if (shouldBeShared && (originalInstanceId == null || originalInstanceId.isEmpty()))
            throw new IllegalArgumentException("If the instance should be shared, originalInstanceId can not be null or empty.");

        assertTrue("There are more or less active registry service instances than the expected " + alreadyActiveInstances + ".",
                countExistingObjects(alreadyActiveInstances, instanceManager.getAll().iterator()));

        ResponseEntity<ServiceInstanceResponse> provisionInstanceResponse = TestRequestService.provisionInstance(serviceInstanceId, instanceRequest);
        assertTrue("Service instance was not provisioned correctly.",
                provisionInstanceResponse.getStatusCode() == HttpStatus.CREATED && provisionInstanceResponse.getBody() != null);

        assertTrue("There should be " + (alreadyActiveInstances + 1) + " registry service instances by now.",
                countExistingObjects(alreadyActiveInstances + 1, instanceManager.getAll().iterator()));
        RegistryServiceInstance serviceInstance = instanceManager.searchServiceInstance(serviceInstanceId);
        assertTrue("Created service instance does not match the intended one.", instanceEqualsRequest(serviceInstance, instanceRequest));
        assertTrue("Service instance already has bindings after creation.", serviceInstance.getBindings().isEmpty());

        if (shouldBeShared) {
            assertTrue("Provisioned shared instance is not specified as shared, but should be.", serviceInstance.isShared());
            assertFalse("Provisioned shared instance is specified as originial, but should not be.", serviceInstance.isOriginalInstance());
            RegistryServiceInstance originalInstance = instanceManager.searchServiceInstance(originalInstanceId);
            assertTrue("Original instance is not flagged as shared, but was used to share.", originalInstance.isShared());
            assertNotNull("Referenced original service instance does not exist.", originalInstance);
            assertEquals("SharedContext of the shared instance and the original instance are not the same.",
                    serviceInstance.getSharedContext(), originalInstance.getSharedContext());
        }
        return serviceInstance.getId();
    }

    public String bind(int alreadyActiveBindings, ServiceInstanceBindingRequest bindingRequest, String serviceInstanceId, String serviceBindingId) throws ResourceNotFoundException {
        if (alreadyActiveBindings < 0)
            throw new IllegalArgumentException("alreadyActiveBindings must be zero or bigger.");

        assertTrue("There are more or less active registry service bindings than the expected " + alreadyActiveBindings + ".",
                countExistingObjects(alreadyActiveBindings, bindingManager.getAll().iterator()));
        assertTrue("Referenced service instance does not exist.", instanceManager.get(serviceInstanceId).isPresent());

        ResponseEntity<ServiceInstanceBindingResponse> response = TestRequestService.bind(serviceInstanceId, serviceBindingId, bindingRequest);
        assertTrue("Service Binding was not created properly.", response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null);

        assertTrue("There should be " + (alreadyActiveBindings + 1) + " registry service bindings by now.",
                countExistingObjects(alreadyActiveBindings + 1, bindingManager.getAll().iterator()));
        RegistryBinding binding = bindingManager.searchRegistryBinding(serviceBindingId);
        assertTrue("Created binding does not match the intended one.", bindingEqualsRequest(binding, bindingRequest));
        assertEquals("The id of the service instance of the created registry binding does not match the provided one.", serviceInstanceId, binding.getServiceInstance().getId());

        return binding.getId();
    }

    public void shareInstance(String instanceId) {
        // Share previously created instance and check pre and post conditions
        assertFalse("Previously created service instance is already shared, but should not be.",
                instanceManager.get(instanceId).get().isShared());
        ResponseEntity<?> sharingResponse = TestRequestService.setShared(true, instanceId);
        assertSame("Unsharing an instance did not function properly.", sharingResponse.getStatusCode(), HttpStatus.OK);
        assertTrue("Previously shared service instance is not registered as shared.",
                instanceManager.get(instanceId).get().isShared());
        assertTrue("Previously shared service instance is missing essential values for being shared.",
                hasAllSharedValuesForOriginal(instanceManager.get(instanceId).get()));
    }

    public void deleteInstance(int alreadyActiveInstances, String instanceId, String definitionId, String planId, boolean hasActiveBindings) {
        if (alreadyActiveInstances < 1)
            throw new IllegalArgumentException("alreadyActiveInstances must be one or bigger.");

        assertTrue("There are more or less active registry service instances than the expected " + alreadyActiveInstances + ".",
                countExistingObjects(alreadyActiveInstances, instanceManager.getAll().iterator()));
        ResponseEntity<JobProgress> response = null;
        try {
            response = TestRequestService.unprovisionInstance(instanceId, definitionId, planId);
        } catch (HttpClientErrorException ex) {
            response = new ResponseEntity<>(ex.getStatusCode());
        }
        if (hasActiveBindings) {
            assertFalse("Service instance is supposed to have active bindings but none were found.",
                    instanceManager.get(instanceId).get().getBindings().isEmpty());
            assertEquals("Service instance deletion was not blocked, although it had active bindings.",
                    response.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
            alreadyActiveInstances++;
        } else {
            assertEquals("Service instance was not deleted properly.", response.getStatusCode(), HttpStatus.OK);
        }
        assertTrue("There are more or less active registry service instances than the expected " + alreadyActiveInstances + ".",
                countExistingObjects(alreadyActiveInstances - 1, instanceManager.getAll().iterator()));
    }

    public void unshareInstance(String instanceId, boolean isTheOnlyInstance) {
        // Unshare previously created instance and check pre and post conditions
        assertTrue("Previously created service instance is not shared, but should be.",
                instanceManager.get(instanceId).get().isShared());
        ResponseEntity<?> unsharingResponse = null;
        try {
            unsharingResponse = TestRequestService.setShared(false, instanceId);
        } catch (HttpClientErrorException ex) {
            unsharingResponse = new ResponseEntity<String>(ex.getResponseBodyAsString(), ex.getStatusCode());
        }
        if (!isTheOnlyInstance) {
            assertTrue("The service instance is not the only instance referencing the service instance, but trying to unshare it was not blocked.",
                    unsharingResponse.getStatusCode() == HttpStatus.BAD_REQUEST && unsharingResponse.getBody() != null);
            assertTrue("The shared service instance is not registered as shared anymore.",
                    instanceManager.get(instanceId).get().isShared());
        } else {
            assertSame("Unsharing an instance did not function properly.", unsharingResponse.getStatusCode(), HttpStatus.OK);
            assertFalse("Previously shared service instance is still registered as shared.",
                    instanceManager.get(instanceId).get().isShared());
        }
    }

    public void unbind(int alreadyActiveBindings, String serviceInstanceId, String serviceBindingId, String definitionId, String planId) {
        if (alreadyActiveBindings < 1)
            throw new IllegalArgumentException("alreadyActiveBindings must be one or bigger.");

        assertTrue("There are more or less active registry service bindings than the expected " + alreadyActiveBindings + ".",
                countExistingObjects(alreadyActiveBindings, bindingManager.getAll().iterator()));
        assertTrue("Referenced service instance does not exist.", instanceManager.get(serviceInstanceId).isPresent());

        ResponseEntity<JobProgress> response = TestRequestService.unbind(serviceInstanceId, serviceBindingId, definitionId, planId);
        assertTrue("Service Binding was not deleted properly.", response.getStatusCode() == HttpStatus.OK && response.getBody() != null);

        assertTrue("There should be " + (alreadyActiveBindings - 1) + " registry service bindings by now.",
                countExistingObjects(alreadyActiveBindings - 1, bindingManager.getAll().iterator()));

        try {
            RegistryBinding binding = bindingManager.searchRegistryBinding(serviceBindingId);
            fail("Deleted binding still exists in storage.");
        } catch (ResourceNotFoundException ex) {}

        assertTrue("Referenced service instance does not exist anymore after binding delete.", instanceManager.get(serviceInstanceId).isPresent());
    }

    public void unregisterBroker(int alreadyActiveBrokers, String brokerId, boolean hasActiveInstances) {
        // Unregister an existing broker and check pre and post conditions
        if (alreadyActiveBrokers < 1)
            throw new IllegalArgumentException("alreadyActiveBrokers must be one or bigger.");
        assertTrue("There are more or less active service brokers than the expected " + alreadyActiveBrokers + ".",
                countExistingObjects(alreadyActiveBrokers, sbManager.getAll().iterator()));

        ResponseEntity<?> sbResponse = null;
        try {
            sbResponse = TestRequestService.unregisterTestServiceBroker(brokerId);
        } catch (HttpClientErrorException ex) {
            sbResponse = new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getStatusCode());
            assertSame("Response code is not 412 when unregistering a service broker, which has active instances",
                    sbResponse.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
            return;
        }
        assertSame("Response code is not 204 when unregistering a service broker",
                sbResponse.getStatusCode(), HttpStatus.NO_CONTENT);
        assertTrue("There are more or less active service brokers than the expected " + (alreadyActiveBrokers - 1) + ".",
                countExistingObjects(alreadyActiveBrokers - 1, sbManager.getAll().iterator()));
    }

    // #### | Helper methods | ####
    //      V                V

    private boolean brokerEqualsCreate(ServiceBroker broker, ServiceBrokerCreate create) {
        boolean equal = broker.getHost().equals(create.getHost())
                && broker.getPort() == create.getPort()
                && broker.getApiVersion().equals(create.getApiVersion())
                && broker.getApiVersion().equals(create.getApiVersion())
                && broker.getDescription().equals(create.getDescription())
                && broker.isCloudFoundryAllowed() == create.isCloudFoundryAllowed()
                && broker.isKubernetesAllowed() == create.isKubernetesAllowed();

        String decrypted = cryptor.decrypt(broker.getSalt(), broker.getEncryptedBasicAuthToken());
        String encoded = cryptor.getBasicAuthEncoded(create.getUsername(), create.getPassword());
        return equal && decrypted.equals(encoded);
    }

    private boolean countExistingObjects(int activeObjects, Iterator iterator) {
        try {
            for (int i = activeObjects; i > 0; i--) {
                iterator.next();
            }
        } catch (NoSuchElementException ex) {
            return false;
        }
        return !iterator.hasNext();
    }

    private boolean instanceEqualsRequest(RegistryServiceInstance serviceInstance, ServiceInstanceRequest instanceRequest) {
        String orgId = instanceRequest.getOrganizationGuid() == null ? instanceRequest.getContext().get("organization_guid") : instanceRequest.getOrganizationGuid();
        String spaceId = instanceRequest.getSpaceGuid() == null ? instanceRequest.getContext().get("space_guid") : instanceRequest.getSpaceGuid();

        return serviceInstance.getServiceDefinitionId().equals(instanceRequest.getServiceDefinitionId())
                && serviceInstance.getPlanId().equals(instanceRequest.getPlanId())
                && serviceInstance.getOrganizationGuid().equals(orgId)
                && serviceInstance.getSpaceGuid().equals(spaceId);
    }

    private boolean bindingEqualsRequest(RegistryBinding binding, ServiceInstanceBindingRequest bindingRequest) {
        String appId = bindingRequest.getAppGuid() == null ? bindingRequest.getBindResource().getAppGuid() : bindingRequest.getAppGuid();
        return appId.equals(binding.getAppId())
                && (bindingRequest.getBindResource().getRoute() == null ? binding.getRoute() == null : bindingRequest.getBindResource().getRoute().equals(binding.getRoute()))
                && bindingRequest.getServiceDefinitionId().equals(binding.getServiceInstance().getServiceDefinitionIdForServiceBroker())
                && bindingRequest.getPlanId().equals(binding.getServiceInstance().getPlanIdForServiceBroker());

    }

    private boolean hasAllSharedValuesForOriginal(RegistryServiceInstance serviceInstance) {
        if (!serviceInstance.isShared() || !serviceInstance.isOriginalInstance()) return false;

        SharedContext context = serviceInstance.getSharedContext();
        try {
            context.validate();
        } catch (SharedContextInvalidException e) {
            return false;
        }
        return context.getServiceInstanceId().equals(serviceInstance.getId())
                && context.getServiceDefinitionId().equals(serviceInstance.getServiceDefinitionId())
                && context.getPlanId().equals(serviceInstance.getPlanId())
                && !context.getDescription().isEmpty()
                && context.getOrganization().equals(serviceInstance.getOrganizationGuid())
                && context.getSpace().equals(serviceInstance.getSpaceGuid())
                && context.getNameSpace().equals(serviceInstance.getNameSpace());
    }

    private ServiceDefinition getSharedDefinition(CatalogResponse catalogResponse) {
        for (ServiceDefinition definition : catalogResponse.getServices()) {
            if (definition.getId().equals(SharedInstancesManager.SHARED_DEFINITIONS_ID))
                return definition;
        }
        return null;
    }

    private boolean sharedDefinitionHasAllValues(ServiceDefinition definition) {
        return definition.getId().equals(SharedInstancesManager.SHARED_DEFINITIONS_ID)
                && definition.getName().equals(SharedInstancesManager.SHARED_DEFINITIONS_NAME)
                && definition.getDescription().equals(SharedInstancesManager.SHARED_DEFINITIONS_DESCRIPTION)
                && definition.isBindable()
                && !definition.isUpdateable();

    }

    private boolean sharedDefinitionPlanHasCorrectValues(Plan plan, RegistryServiceInstance instance) {
        return plan.getId().equals(instance.getId())
                && plan.getName().equals("si-" + instance.getSharedContext().getDisplayNameOrDefaultName())
                && plan.getDescription().contains("Definition:")
                && plan.getDescription().contains("Org:")
                && plan.getDescription().contains("Space:")
                && plan.getDescription().contains("Namespace:");
    }

}

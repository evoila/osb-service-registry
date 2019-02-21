package de.evoila.osb.service.registry.complex;

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
import de.evoila.osb.service.registry.util.ServiceBrokerMockClient;
import de.evoila.osb.service.registry.util.TestRequestService;
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
public class SharingTest {


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
    private Cryptor cryptor;

    @BeforeClass
    public static void startMockServer() throws IOException {
        TestRequestService.initHeaders();
        mockServer = new MockServer(MockServer.SHARE, 8081);
        mockServer.startServer();
        mockServer.prepareEndpoints();
        ServiceBrokerMockClient.mockServiceBroker();
        sbMock = ServiceBrokerMockClient.getServiceBrokerMock();
    }

    @AfterClass
    public static void stopServer() {
        mockServer.stopServer();
    }

    /**
     * This test fulfills a end-to-end test for sharing instances.
     * In it following steps are done and checked:
     * <ul>
     * <li>Used managers and services autowiring</li>
     * <li>Empty catalog and cache at the start</li>
     * <li>Service Broker registration</li>
     * <li>Catalog after registration</li>
     * <li>Service instance provisioning</li>
     * <li>Sharing a service instance</li>
     * <li>Catalog after sharing a service instance</li>
     * <li>Shared service instance provisioning</li>
     * <li>Catalog after shared service instance provisioning</li>
     * <li>Original service instance deletion</li>
     * <li>Catalog after first deletion</li>
     *
     * </ul>
     */
    @Test
    public void runTest() throws ResourceNotFoundException {
        ServiceInstanceRequest instanceRequest = ServiceBrokerMockClient.getServiceInstanceRequest(sbMock);
        ServiceInstanceRequest sharedInstanceRequest = ServiceBrokerMockClient.getSharedServiceInstanceRequest(sbMock, ServiceBrokerMockClient.TEST_INSTANCE_1);

        checkAutowiring();
        checkCatalog(true, false);
        String brokerId = registerBroker();
        ResponseEntity<CatalogResponse> catalogResponse = checkCatalog(false, false);

        // Fill values of singleton of ServiceBrokerMockClient for later use
        sbMock.setupMock(catalogResponse.getBody().getServices(), ServiceBrokerMockClient.SERVICE_BROKER_ORG, ServiceBrokerMockClient.SERVICE_BROKER_SPACE);

        String instanceId = provisionInstance(0,
                instanceRequest,
                ServiceBrokerMockClient.TEST_INSTANCE_1,
                false,
                null);
        String binding1Id = bind(0,ServiceBrokerMockClient.getBindingRequest(sbMock), instanceId, ServiceBrokerMockClient.TEST_BINDING_1);
        shareInstance(instanceId);
        checkCatalog(false, true);
        String sharedInstanceId = provisionInstance(1,
                sharedInstanceRequest,
                ServiceBrokerMockClient.TEST_INSTANCE_1_SHARED,
                true,
                instanceId);
        String shareBinding1Id = bind(1, ServiceBrokerMockClient.getBindingRequest(sbMock), sharedInstanceId, ServiceBrokerMockClient.TEST_BINDING_OF_SHARED_1);
        String binding2Id = bind(2, ServiceBrokerMockClient.getBindingRequest(sbMock), instanceId, ServiceBrokerMockClient.TEST_BINDING_2);

        // Try to delete instances with active bindings -> should be blocked
        deleteInstance(2, instanceId, instanceRequest.getServiceDefinitionId(), instanceRequest.getPlanId(), true);
        deleteInstance(2, sharedInstanceId, sharedInstanceRequest.getServiceDefinitionId(), sharedInstanceRequest.getPlanId(), true);

        // Try to unshare an instance that has more than one reference
        unshareInstance(instanceId, false);
        unshareInstance(sharedInstanceId, false);



        // TODO:
        // - deletion of original instance
        // - deletion of shared instance
        // - post test check
        // - bindings at some point / several points
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

    public String registerBroker() {
        // Register new broker and check pre and post conditions
        assertFalse("There are already active service brokers.", sbManager.getAll().iterator().hasNext());
        ResponseEntity<String> sbResponse = TestRequestService.registerTestServiceBroker();
        assertSame("Response code is not 200 when registering the test service broker",
                sbResponse.getStatusCode(), HttpStatus.OK);
        assertTrue("No service broker was registered.", sbManager.getAll().iterator().hasNext());
        String sbId = sbManager.getAll().iterator().next().getId();
        ServiceBrokerCreate sbCreate = ServiceBrokerMockClient.getServiceBrokerCreate();
        assertTrue("Created service broker does not match the intended one.", brokerEqualsCreate(sbManager.get(sbId).get(), sbCreate));
        return sbId;
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
        ResponseEntity<?> sharingResponse = TestRequestService.setShared(true, ServiceBrokerMockClient.TEST_INSTANCE_1);
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
                countExistingObjects(alreadyActiveInstances-1, instanceManager.getAll().iterator()));
    }

    public void unshareInstance(String instanceId, boolean isTheOnlyInstance) {
        // Unshare previously created instance and check pre and post conditions
        assertTrue("Previously created service instance is not shared, but should be.",
                instanceManager.get(instanceId).get().isShared());
        ResponseEntity<?> unsharingResponse = null;
        try {
            unsharingResponse = TestRequestService.setShared(false, ServiceBrokerMockClient.TEST_INSTANCE_1);
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

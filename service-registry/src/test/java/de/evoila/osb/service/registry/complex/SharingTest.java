package de.evoila.osb.service.registry.complex;

import de.evoila.cf.broker.model.ServiceInstanceRequest;
import de.evoila.cf.broker.model.ServiceInstanceResponse;
import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.cf.broker.model.catalog.plan.Plan;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.exceptions.SharedContextInvalidException;
import de.evoila.osb.service.registry.manager.RegistryServiceInstanceManager;
import de.evoila.osb.service.registry.manager.ServiceBrokerManager;
import de.evoila.osb.service.registry.manager.ServiceDefinitionCacheManager;
import de.evoila.osb.service.registry.manager.SharedInstancesManager;
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

import java.io.IOException;

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

        checkAutowiring();
        checkCatalogPreTest();
        String brokerId = registerBroker();
        ResponseEntity<CatalogResponse> catalogResponse = checkCatalogPostRegister();

        // Fill values of singleton of ServiceBrokerMockClient for later use
        sbMock.setServiceDefinitions(catalogResponse.getBody().getServices());
        sbMock.setDefinition(sbMock.getServiceDefinitions().get(0));
        sbMock.setPlan(sbMock.getDefinition().getPlans().get(0));

        String instanceId = provisionInstance();
        shareInstance(instanceId);
        checkCatalogPostSharing();
        String sharedInstanceId = provisionSharedInstance(instanceId);

        // TODO:
        // - deletion of original instance
        // - deletion of shared instance
        // - post check
        // - bindings at some point / several points
    }

    public void checkAutowiring() {
        // Check for correctly autowired managers and services
        assertNotNull("Service broker manager was not autowired correctly.", sbManager);
        assertNotNull("Shared instance manager was not autowired correctly.", sharedInstancesManager);
        assertNotNull("Registry instance manager was not autowired correctly.", instanceManager);
        assertNotNull("Service definition cache manager was not autowired correctly.", definitionCacheManager);
        assertNotNull("Cryptor service was not autowired correctly.", cryptor);
    }

    public ResponseEntity<CatalogResponse> checkCatalogPreTest() {
        // Check for empty catalog and service definitions
        ResponseEntity<CatalogResponse> catalogResponse = TestRequestService.getCatalog();
        assertTrue("Service Registry already has service definitions but should be empty.", definitionCacheManager.getUnmodifiableDefinitions().isEmpty());
        assertTrue("Service Registry returned service definitions but should not.",
                catalogResponse.getStatusCode() == HttpStatus.OK && catalogResponse.getBody().getServices().isEmpty());
        return catalogResponse;
    }

    public String registerBroker(){
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

    public ResponseEntity<CatalogResponse> checkCatalogPostRegister() {
        // Check catalog again after broker was registered
        ResponseEntity<CatalogResponse> catalogResponse = TestRequestService.getCatalog();
        assertTrue("Service Registry should have service definitions by now.", !definitionCacheManager.getUnmodifiableDefinitions().isEmpty());
        assertTrue("Catalog should not be empty after registering a service broker.",
                catalogResponse.getStatusCode() == HttpStatus.OK && !catalogResponse.getBody().getServices().isEmpty());
        return catalogResponse;
    }

    public String provisionInstance() {
        // Provision new instance and check pre and post conditions
        assertFalse("There are already active registry service instances.", instanceManager.getAll().iterator().hasNext());
        ServiceInstanceRequest instanceRequest = ServiceBrokerMockClient.getServiceInstanceRequest(sbMock);
        ResponseEntity<ServiceInstanceResponse> provisionInstanceResponse = TestRequestService.provisionInstance(ServiceBrokerMockClient.TEST_INSTANCE_1, instanceRequest);
        assertTrue("Service instance was not provisioned correctly.",
                provisionInstanceResponse.getStatusCode() == HttpStatus.CREATED && provisionInstanceResponse.getBody() != null);
        assertTrue("There should be registry service instances by now.", instanceManager.getAll().iterator().hasNext());
        String instanceId = instanceManager.getAll().iterator().next().getId();
        assertTrue("Created service instance does not match the intended one.", instanceEqualsRequest(instanceManager.get(instanceId).get(), instanceRequest));
        return instanceId;
    }

    public void shareInstance(String instanceId) {
        // Share previously created instance and check pre and post conditions
        assertFalse("Previously created service instance is already shared, but should not be.",
                instanceManager.get(instanceId).get().isShared());
        ResponseEntity<Void> sharingResponse = TestRequestService.setShared(true, ServiceBrokerMockClient.TEST_INSTANCE_1);
        assertSame(sharingResponse.getStatusCode(), HttpStatus.OK);
        assertTrue("Previously shared service instance is not registered as shared.",
                instanceManager.get(instanceId).get().isShared());
        assertTrue("Previously shared service instance is missing essential values for being shared.",
                hasAllSharedValuesForOriginial(instanceManager.get(instanceId).get()));
    }

    public ResponseEntity<CatalogResponse> checkCatalogPostSharing(){
        // Check catalog for a shared service definition and its values
        ResponseEntity<CatalogResponse> catalogResponse = TestRequestService.getCatalog();
        assertTrue("No service definition for shared instances was found after sharing an instance.",
                catalogResponse.getStatusCode() == HttpStatus.OK && getSharedDefinition(catalogResponse.getBody()) != null);
        assertTrue("", sharedDefinitionHasAllValues(getSharedDefinition(catalogResponse.getBody())));
        for (Plan plan : getSharedDefinition(catalogResponse.getBody()).getPlans()) {
            assertTrue("No instance was found with for a shared plan for the corresponding id",
                    instanceManager.get(plan.getId()).isPresent());
            assertTrue("A shared plan does not hold all values or has incorrect ones.",
                    sharedDefinitionPlanHasCorrectValues(plan, instanceManager.get(plan.getId()).get()));
        }
        return catalogResponse;
    }

    public String provisionSharedInstance(String instanceId) throws ResourceNotFoundException {
        // Provision a shared instance and check pre and post conditions
        ServiceInstanceRequest sharedInstanceRequest = ServiceBrokerMockClient.getSharedServiceInstanceRequest(sbMock);
        ResponseEntity<ServiceInstanceResponse> provisionSharedInstanceResponse = TestRequestService.provisionInstance(ServiceBrokerMockClient.TEST_INSTANCE_1_SHARED, sharedInstanceRequest);
        assertTrue("Shared service instance was not provisioned correctly.",
                provisionSharedInstanceResponse.getStatusCode() == HttpStatus.CREATED && provisionSharedInstanceResponse.getBody() != null);
        assertTrue("There should be exactly two instances at this moment, not " + instanceManager.count() + ".",
                instanceManager.count() == 2);
        String sharedInstanceId = instanceManager.searchServiceInstance(ServiceBrokerMockClient.TEST_INSTANCE_1_SHARED).getId();
        RegistryServiceInstance sharedInstance = instanceManager.searchServiceInstance(ServiceBrokerMockClient.TEST_INSTANCE_1_SHARED);
        assertTrue("Created shared service instance does not match the intended one.",
                instanceEqualsRequest(sharedInstance, sharedInstanceRequest));
        assertTrue("Provisioned shared instance is not specified as shared, but should be.", sharedInstance.isShared());
        assertFalse("Provisioned shared instance is specified as originial, but should not be.", sharedInstance.isOriginalInstance());
        assertEquals("SharedContext of the shared instance and the original instance are not the same.",
                sharedInstance.getSharedContext(), instanceManager.get(instanceId).get().getSharedContext());
        return sharedInstanceId;
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

    private boolean instanceEqualsRequest(RegistryServiceInstance serviceInstance, ServiceInstanceRequest instanceRequest) {
        String orgId = instanceRequest.getOrganizationGuid() == null ? instanceRequest.getContext().get("organization_guid") : instanceRequest.getOrganizationGuid();
        String spaceId = instanceRequest.getSpaceGuid() == null ? instanceRequest.getContext().get("space_guid") : instanceRequest.getSpaceGuid();

        return serviceInstance.getServiceDefinitionId().equals(instanceRequest.getServiceDefinitionId())
                && serviceInstance.getPlanId().equals(instanceRequest.getPlanId())
                && serviceInstance.getOrganizationGuid().equals(orgId)
                && serviceInstance.getSpaceGuid().equals(spaceId);
    }

    private boolean hasAllSharedValuesForOriginial(RegistryServiceInstance serviceInstance) {
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

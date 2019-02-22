package de.evoila.osb.service.registry.util.servicebroker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.cf.broker.model.catalog.plan.Plan;
import de.evoila.osb.service.registry.manager.SharedInstancesManager;
import de.evoila.osb.service.registry.web.bodies.CatalogResponse;
import de.evoila.osb.service.registry.web.bodies.ServiceBrokerCreate;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServiceBrokerMockClient {

    public static final String SERVICE_BROKER_HOST = "127.0.0.1";
    public static final int SERVICE_BROKER_PORT = 8081;
    public static final String SERVICE_BROKER_USER = "test";
    public static final String SERVICE_BROKER_PASSWORD = "test";
    public static final String SERVICE_BROKER_API_VERSION = "2.14";
    public static final String SERVICE_BROKER_ORIGINATING_IDENTITY = "cloudfoundry faketoken";
    public static final String SERVICE_BROKER_ORG = "test-org";
    public static final String SERVICE_BROKER_SPACE = "test-space";

    public static final String TEST_INSTANCE_1 = "test-instance-1";
    public static final String TEST_INSTANCE_2 = "test-instance-2";
    public static final String TEST_INSTANCE_1_SHARED = "test-instance-1-shared";
    public static final String TEST_INSTANCE_2_SHARED = "test-instance-2-shared";
    public static final String TEST_BINDING_1 = "test-binding-1";
    public static final String TEST_BINDING_2 = "test-binding-2";
    public static final String TEST_BINDING_3 = "test-binding-3";
    public static final String TEST_BINDING_4 = "test-binding-4";
    public static final String TEST_BINDING_OF_SHARED_1 = "test-binding-of-shared-1";
    public static final String TEST_BINDING_OF_SHARED_2 = "test-binding-of-shared-2";
    public static final String TEST_BINDING_OF_SHARED_3 = "test-binding-of-shared-3";
    public static final String TEST_BINDING_OF_SHARED_4 = "test-binding-of-shared-4";

    public static final String CATALOG_FILE_PATH = "./src/test/resources/example-catalog.json";

    private static final int REQUEST_DELAY = 200;

    private static ServiceBrokerMockClient serviceBrokerMock;

    private static ObjectMapper mapper = new ObjectMapper();

    public static void mockServiceBroker(String catalogFilePath) throws IOException {
        CatalogResponse catalogResponse = mapper.readValue(new File(catalogFilePath), CatalogResponse.class);
        ServiceBrokerMockClient sbMock = getServiceBrokerMock();
        sbMock.setupMock(catalogResponse.getServices(), SERVICE_BROKER_ORG, SERVICE_BROKER_SPACE);

        ServiceInstanceRequest instanceRequest = getServiceInstanceRequest(sbMock);
        ServiceInstanceBindingRequest binding1Request = getBindingRequest(sbMock);

        mockCatalogRequest(catalogResponse);
        mockProvisionInstance(TEST_INSTANCE_1, instanceRequest);
        mockProvisionInstance(TEST_INSTANCE_2, instanceRequest);

        mockBinding(TEST_INSTANCE_1, TEST_BINDING_1, binding1Request);
        mockBinding(TEST_INSTANCE_1, TEST_BINDING_2, binding1Request);
        mockBinding(TEST_INSTANCE_2, TEST_BINDING_3, binding1Request);
        mockBinding(TEST_INSTANCE_2, TEST_BINDING_4, binding1Request);
        mockBinding(TEST_INSTANCE_1, TEST_BINDING_OF_SHARED_1, binding1Request);
        mockBinding(TEST_INSTANCE_1, TEST_BINDING_OF_SHARED_2, binding1Request);
        mockBinding(TEST_INSTANCE_2, TEST_BINDING_OF_SHARED_3, binding1Request);
        mockBinding(TEST_INSTANCE_2, TEST_BINDING_OF_SHARED_4, binding1Request);
    }

    public static void mockCatalogRequest(CatalogResponse catalogResponse) throws JsonProcessingException {
        new MockServerClient(SERVICE_BROKER_HOST, SERVICE_BROKER_PORT)
                .when(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/v2/catalog")
                                .withHeader("Authorization", getServiceBrokerBasicAuthToken()),
                        Times.exactly(1)
                )
                .respond(
                        HttpResponse.response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
                                .withBody(mapper.writeValueAsString(catalogResponse))
                                .withDelay(TimeUnit.MILLISECONDS, REQUEST_DELAY)

                );
    }

    public static void mockProvisionInstance(String instanceId, ServiceInstanceRequest instanceRequest) throws JsonProcessingException {
        ServiceInstanceResponse response = new ServiceInstanceResponse("https://osb-example.cf/custom/v2/authentication/" + instanceId);
        new MockServerClient(SERVICE_BROKER_HOST, SERVICE_BROKER_PORT)
                .when(
                        HttpRequest.request()
                                .withMethod("PUT")
                                .withPath("/v2/service_instances/" + instanceId)
                                .withHeaders(new Header("Authorization", getServiceBrokerBasicAuthToken()),
                                        new Header("X-Broker-API-Version", SERVICE_BROKER_API_VERSION),
                                        new Header("X-Broker-API-Originating-Identity", SERVICE_BROKER_ORIGINATING_IDENTITY)
                                )
                                .withBody(mapper.writeValueAsString(instanceRequest)),
                        Times.exactly(1)
                )
                .respond(
                        HttpResponse.response()
                                .withStatusCode(201)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
                                .withBody(mapper.writeValueAsString(response))
                                .withDelay(TimeUnit.MILLISECONDS, REQUEST_DELAY)
                );

        mockUnprovisioning(instanceId);
    }

    public static void mockBinding(String instanceId, String bindingId, ServiceInstanceBindingRequest bindingRequest) throws JsonProcessingException {
        ServiceInstanceBindingResponse response = new ServiceInstanceBindingResponse();
        response.setCredentials(new HashMap<>());
        response.getCredentials().put("binding", bindingId);
        new MockServerClient(SERVICE_BROKER_HOST, SERVICE_BROKER_PORT)
                .when(
                        HttpRequest.request()
                                .withMethod("PUT")
                                .withPath("/v2/service_instances/" + instanceId + "/service_bindings/" + bindingId)
                                .withHeaders(new Header("Authorization", getServiceBrokerBasicAuthToken()),
                                        new Header("X-Broker-API-Version", SERVICE_BROKER_API_VERSION),
                                        new Header("X-Broker-API-Originating-Identity", SERVICE_BROKER_ORIGINATING_IDENTITY)
                                )
                                .withBody(mapper.writeValueAsString(bindingRequest)),

                        Times.exactly(1)
                )
                .respond(
                        HttpResponse.response()
                                .withStatusCode(201)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
                                .withBody(mapper.writeValueAsString(response))
                                .withDelay(TimeUnit.MILLISECONDS, REQUEST_DELAY)
                );
        mockUnbinding(instanceId, bindingId);
    }

    public static void mockUnprovisioning(String instanceId) {
        String definitionId = serviceBrokerMock.getDefinition().getId();
        String planId = serviceBrokerMock.getPlan().getId();
        new MockServerClient(SERVICE_BROKER_HOST, SERVICE_BROKER_PORT)
                .when(
                        HttpRequest.request()
                                .withMethod("DELETE")
                                .withPath("/v2/service_instances/" + instanceId)
                                .withQueryStringParameter("service_id", definitionId)
                                .withQueryStringParameter("plan_id", planId)
                                .withHeaders(new Header("Authorization", getServiceBrokerBasicAuthToken()),
                                        new Header("X-Broker-API-Version", SERVICE_BROKER_API_VERSION),
                                        new Header("X-Broker-API-Originating-Identity", SERVICE_BROKER_ORIGINATING_IDENTITY)
                                ),
                        Times.exactly(1)
                )
                .respond(
                        HttpResponse.response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
                                .withBody("{}")
                                .withDelay(TimeUnit.MILLISECONDS, REQUEST_DELAY)
                );
    }

    public static void mockUnbinding(String instanceId, String bindingId) throws JsonProcessingException {
        String definitionId = serviceBrokerMock.getDefinition().getId();
        String planId = serviceBrokerMock.getPlan().getId();
        new MockServerClient(SERVICE_BROKER_HOST, SERVICE_BROKER_PORT)
                .when(
                        HttpRequest.request()
                                .withMethod("DELETE")
                                .withPath("/v2/service_instances/" + instanceId + "/service_bindings/" + bindingId)
                                .withQueryStringParameter("service_id", definitionId)
                                .withQueryStringParameter("plan_id", planId)
                                .withHeaders(new Header("Authorization", getServiceBrokerBasicAuthToken()),
                                        new Header("X-Broker-API-Version", SERVICE_BROKER_API_VERSION),
                                        new Header("X-Broker-API-Originating-Identity", SERVICE_BROKER_ORIGINATING_IDENTITY)
                                ),
                        Times.exactly(1)
                )
                .respond(
                        HttpResponse.response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
                                .withBody("{}")
                                .withDelay(TimeUnit.MILLISECONDS, REQUEST_DELAY)
                );
    }

    public static String getServiceBrokerBasicAuthToken() {
        return "Basic " + Base64.getEncoder().encodeToString((SERVICE_BROKER_USER + ":" + SERVICE_BROKER_PASSWORD).getBytes());
    }

    public static ServiceBrokerCreate getServiceBrokerCreate() {
        return new ServiceBrokerCreate(
                "http://" + SERVICE_BROKER_HOST,
                SERVICE_BROKER_PORT,
                SERVICE_BROKER_USER,
                SERVICE_BROKER_PASSWORD,
                SERVICE_BROKER_API_VERSION,
                "Test SB Descritpion",
                true,
                false);
    }

    public static ServiceInstanceRequest getServiceInstanceRequest(ServiceBrokerMockClient mock) {
        return new ServiceInstanceRequest(mock.getDefinition().getId(), mock.getPlan().getId(), mock.getOrgId(), mock.getSpaceId(), new HashMap<>());
    }

    public static ServiceInstanceRequest getSharedServiceInstanceRequest(ServiceBrokerMockClient mock, String originalInstanceId) {
        return new ServiceInstanceRequest(SharedInstancesManager.SHARED_DEFINITIONS_ID, originalInstanceId, mock.getOrgId(), mock.getSpaceId(), new HashMap<>());
    }

    public static ServiceInstanceBindingRequest getBindingRequest(ServiceBrokerMockClient mock) {
        return new ServiceInstanceBindingRequest(mock.getDefinition().getId(), mock.getPlan().getId(), "test-app", new BindResource());
    }

    public static ServiceBrokerMockClient getServiceBrokerMock() {
        if (serviceBrokerMock == null) serviceBrokerMock = new ServiceBrokerMockClient();
        return serviceBrokerMock;
    }


    // ######## Non-static content ########
    // Used for getting service definitions and its content like plan ids
    // -> Handled via singleton pattern -> getServiceBrokerMock()
    // -> Values have to be set via setters and are NOT automatically received

    private List<ServiceDefinition> serviceDefinitions;
    private ServiceDefinition definition;
    private Plan plan;
    private String orgId;
    private String spaceId;

    private ServiceBrokerMockClient() {
    }

    public boolean setupMock(List<ServiceDefinition> serviceDefinitions, String orgId, String spaceId) {
        this.orgId = orgId;
        this.spaceId = spaceId;

        if (serviceDefinitions == null) return false;
        this.serviceDefinitions = serviceDefinitions;

        if (this.serviceDefinitions.isEmpty()) return false;
        definition = this.serviceDefinitions.get(0);

        if (definition.getPlans() == null || definition.getPlans().isEmpty()) return false;
        plan = definition.getPlans().get(0);

        return !(StringUtils.isEmpty(orgId) || StringUtils.isEmpty(spaceId));
    }

    public List<ServiceDefinition> getServiceDefinitions() {
        return serviceDefinitions;
    }

    public void setServiceDefinitions(List<ServiceDefinition> serviceDefinitions) {
        this.serviceDefinitions = serviceDefinitions;
    }

    public ServiceDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(ServiceDefinition definition) {
        this.definition = definition;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }
}

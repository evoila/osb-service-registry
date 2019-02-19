package de.evoila.osb.service.registry.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.evoila.cf.broker.model.ServiceInstanceRequest;
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

    public static final String TEST_INSTANCE_1 = "test-instance";
    public static final String TEST_INSTANCE_1_SHARED = "test-instance-shared";

    public static final String CATALOG_FILE_PATH = "./src/test/resources/example-catalog.json";

    private static ServiceBrokerMockClient serviceBrokerMock;

    private static ObjectMapper mapper = new ObjectMapper();

    public static void mockServiceBroker() throws IOException {
        CatalogResponse catalogResponse = mapper.readValue(new File(CATALOG_FILE_PATH), CatalogResponse.class);
        ServiceBrokerMockClient sbMock = getServiceBrokerMock();
        sbMock.setServiceDefinitions(catalogResponse.getServices());
        sbMock.setDefinition(sbMock.getServiceDefinitions().get(0));
        sbMock.setPlan(sbMock.getDefinition().getPlans().get(0));

        ServiceInstanceRequest instanceRequest = getServiceInstanceRequest(sbMock);
        ServiceInstanceRequest sharedInstanceRequest = getSharedServiceInstanceRequest(sbMock);

        mockCatalogRequest(catalogResponse);
        mockProvisionInstance(TEST_INSTANCE_1, instanceRequest);
        mockProvisionInstance(TEST_INSTANCE_1_SHARED, sharedInstanceRequest);
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
                                .withDelay(TimeUnit.SECONDS, 1)

                );
    }

    public static void mockProvisionInstance(String instanceId, ServiceInstanceRequest instanceRequest) throws JsonProcessingException {
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
                                .withBody("{\n" +
                                        "    \"dashboard_url\": \"https://osb-example.cf/custom/v2/authentication/" + instanceId + "\"\n" +
                                        "}")
                                .withDelay(TimeUnit.SECONDS, 1)
                );
    }

    public static String getServiceBrokerBasicAuthToken() {
        return "Basic " + Base64.getEncoder().encodeToString((SERVICE_BROKER_USER + ":" + SERVICE_BROKER_PASSWORD).getBytes());
    }

    public static ServiceBrokerCreate getServiceBrokerCreate() {
        return new ServiceBrokerCreate(
                "http://"+SERVICE_BROKER_HOST,
                SERVICE_BROKER_PORT,
                SERVICE_BROKER_USER,
                SERVICE_BROKER_PASSWORD,
                SERVICE_BROKER_API_VERSION,
                "Test SB Descritpion",
                true,
                false);
    }

    public static ServiceInstanceRequest getServiceInstanceRequest(ServiceBrokerMockClient mock) {
        return new ServiceInstanceRequest(mock.getDefinition().getId(), mock.getPlan().getId(), SERVICE_BROKER_ORG, SERVICE_BROKER_SPACE, new HashMap<>());
    }

    public static ServiceInstanceRequest getSharedServiceInstanceRequest(ServiceBrokerMockClient mock) {
        return new ServiceInstanceRequest(SharedInstancesManager.SHARED_DEFINITIONS_ID, TEST_INSTANCE_1, SERVICE_BROKER_ORG, SERVICE_BROKER_SPACE, new HashMap<>());
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

    private ServiceBrokerMockClient() {
    }

    public List<ServiceDefinition> getServiceDefinitions() { return serviceDefinitions; }

    public void setServiceDefinitions(List<ServiceDefinition> serviceDefinitions) { this.serviceDefinitions = serviceDefinitions; }

    public ServiceDefinition getDefinition() { return definition; }

    public void setDefinition(ServiceDefinition definition) { this.definition = definition; }

    public Plan getPlan() { return plan; }

    public void setPlan(Plan plan) { this.plan = plan; }

}

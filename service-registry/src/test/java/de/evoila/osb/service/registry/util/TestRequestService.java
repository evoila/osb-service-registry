package de.evoila.osb.service.registry.util;

import de.evoila.cf.broker.model.*;
import de.evoila.osb.service.registry.web.bodies.CatalogResponse;
import de.evoila.osb.service.registry.web.bodies.ServiceBrokerCreate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class TestRequestService {

    public static RestTemplate restTemplate = new RestTemplate();
    public static HttpHeaders headers = new HttpHeaders();
    @LocalServerPort
    public static final int PORT = 8080;
    public static final String URL = "http://127.0.0.1:" + PORT;

    public static void initHeaders() {
        headers = new HttpHeaders();
        headers.add("Content-type", "application/json");
        headers.add("Authorization", ServiceBrokerMockClient.getServiceBrokerBasicAuthToken());
        headers.add("X-Broker-API-Version", ServiceBrokerMockClient.SERVICE_BROKER_API_VERSION);
        headers.add("X-Broker-API-Originating-Identity", ServiceBrokerMockClient.SERVICE_BROKER_ORIGINATING_IDENTITY);
    }

    public static ResponseEntity<CatalogResponse> getCatalog() {
        return restTemplate.exchange(URL + "/v2/catalog",
                HttpMethod.GET,
                new HttpEntity<String>(null, headers),
                CatalogResponse.class);
    }

    public static ResponseEntity<String> registerTestServiceBroker(ServiceBrokerCreate create) {
        return restTemplate.exchange(URL + "/brokers",
                HttpMethod.POST,
                new HttpEntity<>(create, headers),
                String.class);
    }

    public static ResponseEntity<ServiceInstanceResponse> provisionInstance(String instanceId, ServiceInstanceRequest instanceRequest) {
        return restTemplate.exchange(URL + "/v2/service_instances/" + instanceId + "?accepts_incomplete=false",
                HttpMethod.PUT,
                new HttpEntity<ServiceInstanceRequest>(instanceRequest, headers),
                ServiceInstanceResponse.class
        );
    }

    public static ResponseEntity<ServiceInstanceBindingResponse> bind(String instanceId, String bindingId, ServiceInstanceBindingRequest bindingRequest) {
        return restTemplate.exchange(URL + "/v2/service_instances/" + instanceId + "/service_bindings/" + bindingId + "?accepts_incomplete=false",
                HttpMethod.PUT,
                new HttpEntity<ServiceInstanceBindingRequest>(bindingRequest, headers),
                ServiceInstanceBindingResponse.class);
    }

    public static ResponseEntity<?> setShared(boolean share, String instanceId) {
        HttpEntity entity = new HttpEntity<>(null, headers);
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate.exchange(URL + "/service_instance/" + instanceId + "/shareable?sharing=" + share,
                HttpMethod.PATCH,
                new HttpEntity<>(null, headers),
                Void.class);
    }

    public static ResponseEntity<JobProgress> unprovisionInstance(String instanceId, String definitionId, String planId){
        return restTemplate.exchange(
                URL + "/v2/service_instances/" + instanceId + "?accepts_incomplete=false&service_id="+definitionId+"&plan_id="+planId,
                HttpMethod.DELETE,
                new HttpEntity<>(null, headers),
                JobProgress.class);
    }

    public static ResponseEntity<JobProgress> unbind(String instanceId, String bindingId, String definitionId, String planId) {
        return restTemplate.exchange(
                URL+"/v2/service_instances/"+instanceId+"/service_bindings/"+bindingId+"?accepts_incomplete=false&service_id="+definitionId+"&plan_id="+planId,
                HttpMethod.DELETE,
                new HttpEntity<>(null, headers),
                JobProgress.class);
    }

    public static ResponseEntity<?> unregisterTestServiceBroker(String brokerId) {
        return restTemplate.exchange(
                URL + "/brokers/" + brokerId,
                HttpMethod.DELETE,
                new HttpEntity<>(null, headers),
                String.class
        );
    }
}

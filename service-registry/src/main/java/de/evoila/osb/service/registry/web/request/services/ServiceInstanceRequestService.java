package de.evoila.osb.service.registry.web.request.services;

import de.evoila.cf.broker.model.*;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.model.ResponseWithHttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class ServiceInstanceRequestService extends BaseRequestService {

    private static Logger log = LoggerFactory.getLogger(ServiceInstanceRequestService.class);

    public static ResponseWithHttpStatus<ServiceInstance> fetchServiceInstance(ServiceBroker serviceBroker, String serviceInstanceId, String apiHeader) throws HttpClientErrorException {

        String url = serviceBroker.getHostWithPort() + "/v2/service_instances/" + serviceInstanceId;
        HttpEntity entity = new HttpEntity(createBasicHeaders(serviceBroker.getBasicAuthToken(), apiHeader));
        log.info("Sending fetch service request to " + url);
        return makeRequest(url, HttpMethod.GET, entity, ServiceInstance.class);
    }

    public static ResponseWithHttpStatus<ServiceInstanceResponse> createServiceInstance(
            ServiceBroker serviceBroker, String serviceInstanceId,
            String apiHeader, String originatingIdentity, boolean accepts_incomplete, ServiceInstanceRequest request)
            throws HttpClientErrorException {

        String url = serviceBroker.getHostWithPort() + "/v2/service_instances/" + serviceInstanceId;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("accepts_incomplete", accepts_incomplete);
        HttpHeaders headers = addOriginatingIdentityHeader(originatingIdentity, createBasicHeaders(serviceBroker.getBasicAuthToken(), apiHeader));
        HttpEntity<ServiceInstanceRequest> entity = new HttpEntity<>(request, headers);
        log.info("Sending create service request to " + url);
        return makeRequest(builder.build().toUriString(), HttpMethod.PUT, entity, ServiceInstanceResponse.class);
    }

    public static ResponseWithHttpStatus<ServiceInstanceUpdateResponse> updateServiceInstance(
            ServiceBroker serviceBroker, String serviceInstanceId, String apiHeader, String originatingIdentity,
            boolean accepts_incomplete, ServiceInstanceUpdateRequest request) {

        String url = serviceBroker.getHostWithPort() + "/v2/service_instance/" + serviceInstanceId;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("accepts_incomplete", accepts_incomplete);
        HttpHeaders headers = addOriginatingIdentityHeader(originatingIdentity, createBasicHeaders(serviceBroker.getBasicAuthToken(), apiHeader));
        HttpEntity<ServiceInstanceUpdateRequest> entity = new HttpEntity<>(request, headers);
        return makeRequest(builder.build().toUriString(), HttpMethod.PATCH, entity, ServiceInstanceUpdateResponse.class);
    }

    public static ResponseWithHttpStatus<String> deleteServiceInstance(
            ServiceBroker serviceBroker, String serviceInstanceId, String serviceId,
            String planId, String apiHeader, String originatingIdentity, boolean accepts_incomplete) throws ResourceNotFoundException {

        String url = serviceBroker.getHostWithPort() + "/v2/service_instances/" + serviceInstanceId;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("service_id", serviceId)
                .queryParam("plan_id", planId)
                .queryParam("accepts_incomplete", accepts_incomplete);
        HttpHeaders headers = addOriginatingIdentityHeader(originatingIdentity, createBasicHeaders(serviceBroker.getBasicAuthToken(), apiHeader));
        HttpEntity entity = new HttpEntity(headers);
        log.info("Sending delete service request to " + url);
        return makeRequest(builder.build().toUriString(), HttpMethod.DELETE, entity, String.class);
    }

    public static ResponseWithHttpStatus<JobProgressResponse> pollServiceInstance(
            ServiceBroker serviceBroker, String serviceInstanceId,
            String apiHeader, Map<String, String> optionalQueryParams) throws ResourceNotFoundException {

        String url = serviceBroker.getHostWithPort() + "/v2/service_instances/" + serviceInstanceId + "/last_operation";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (optionalQueryParams != null) {
            for (Map.Entry<String, String> entry : optionalQueryParams.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }
        HttpEntity entity = new HttpEntity(createBasicHeaders(serviceBroker.getBasicAuthToken(), apiHeader));
        log.info("Sending service instance last operation request to " + url);
        return makeRequest(builder.build().toUriString(), HttpMethod.GET, entity, JobProgressResponse.class);
    }
}

package de.evoila.osb.service.registry.web.request.services;

import de.evoila.cf.broker.model.JobProgressResponse;
import de.evoila.cf.broker.model.ServiceInstanceBinding;
import de.evoila.cf.broker.model.ServiceInstanceBindingRequest;
import de.evoila.cf.broker.model.ServiceInstanceBindingResponse;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.model.ResponseWithHttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

public class ShadowServiceBindingRequestService extends BaseRequestService {

    private static Logger log = LoggerFactory.getLogger(ShadowServiceBindingRequestService.class);

    public static ResponseWithHttpStatus<ServiceInstanceBindingResponse> createServiceBinding(
            ServiceBroker serviceBroker, String serviceInstanceId, String serviceBindingId,
            String apiHeader, String originatingIdentity, boolean accepts_incomplete, ServiceInstanceBindingRequest request)
            throws HttpClientErrorException {

        log.info("Building create binding request for " + serviceBroker.getLoggingNameString());
        String url = serviceBroker.getHostWithPort() + "/v2/service_instances/" + serviceInstanceId + "/service_bindings/" + serviceBindingId;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("accepts_incomplete", accepts_incomplete);
        HttpHeaders headers = addOriginatingIdentityHeader(originatingIdentity, createBasicHeaders(serviceBroker.getBasicAuthToken(), apiHeader));
        HttpEntity<ServiceInstanceBindingRequest> entity = new HttpEntity<>(request, headers);
        log.info("Sending create binding request to " + url);
        return makeRequest(builder.build().toUriString(), HttpMethod.PUT, entity, ServiceInstanceBindingResponse.class);
    }

    public static ResponseWithHttpStatus<String> deleteBinding(
            ServiceBroker serviceBroker, String serviceInstanceId, String serviceDefinitionId, String bindingId,
            String planId, String apiHeader, String originatingIdentity, boolean accepts_incomplete) throws ResourceNotFoundException {

        log.info("Building delete binding request for " + serviceBroker.getLoggingNameString());
        String url = serviceBroker.getHostWithPort() + "/v2/service_instances/" + serviceInstanceId + "/service_bindings/" + bindingId;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("service_id", serviceDefinitionId)
                .queryParam("plan_id", planId)
                .queryParam("accepts_incomplete", accepts_incomplete);
        HttpHeaders headers = addOriginatingIdentityHeader(originatingIdentity, createBasicHeaders(serviceBroker.getBasicAuthToken(), apiHeader));
        HttpEntity entity = new HttpEntity(headers);
        log.info("Sending delete binding request to " + url);
        return makeRequest(builder.build().toUriString(), HttpMethod.DELETE, entity, String.class);
    }

    public static ResponseWithHttpStatus<JobProgressResponse> pollServiceBinding(ServiceBroker serviceBroker, String serviceInstanceId, String serviceBindingId,
                                                                                 String apiHeader, Map<String, String> optionalQueryParams) throws ResourceNotFoundException {

        String url = serviceBroker.getHostWithPort() + "/v2/service_instances/" + serviceInstanceId + "/service_bindings/" + serviceBindingId + "/last_operation";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (optionalQueryParams != null) {
            for (Map.Entry<String, String> entry : optionalQueryParams.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }
        HttpEntity entity = new HttpEntity(createBasicHeaders(serviceBroker.getBasicAuthToken(), apiHeader));
        log.info("Sending service binding last operation request to " + url);
        return makeRequest(builder.build().toUriString(), HttpMethod.GET, entity, JobProgressResponse.class);
    }

    public static ResponseWithHttpStatus<ServiceInstanceBinding> fetchServiceBinding(ServiceBroker serviceBroker, String serviceInstanceId, String serviceBinding,
                                                                                     String apiHeader) {
        String url = serviceBroker.getHostWithPort() + "/v2/service_instances/" + serviceInstanceId + "/service_bindings/" + serviceBinding;
        HttpEntity entity = new HttpEntity(createBasicHeaders(serviceBroker.getBasicAuthToken(), apiHeader));
        log.info("Sending service binding fetch request to " + url);
        return makeRequest(url, HttpMethod.GET, entity, ServiceInstanceBinding.class);
    }
}

package de.evoila.osb.service.registry.web.request.services;

import de.evoila.osb.service.registry.model.ResponseWithHttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class BaseRequestService {

    public static final int TIMEOUT_CONNECTION = 10000;
    public static final int TIMEOUT_READ = 30000;

    private static Logger log = LoggerFactory.getLogger(BaseRequestService.class);

    private static RestTemplate restTemplate;

    public static RestTemplate createRestTemplateWithTimeouts(int connectionTimeout, int readTimeout) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        return new RestTemplate(factory);
    }

    public static RestTemplate getRestTemplate() {
        if (restTemplate == null)
            restTemplate = createRestTemplateWithTimeouts(TIMEOUT_CONNECTION, TIMEOUT_READ);
        return restTemplate;
    }

    public static HttpHeaders createBasicHeaders(String basicAuthToken, String apiVersion) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + basicAuthToken);
        headers.add("X-Broker-API-Version", apiVersion);
        headers.add("Content-type", "application/json");
        return headers;
    }

    public static HttpHeaders addOriginatingIdentityHeader(String originatingIdentity, HttpHeaders headers) {
        if (originatingIdentity != null && headers != null)
            headers.add("X-Broker-API-Originating-Identity", originatingIdentity);
        return headers;
    }

    public static <T> ResponseWithHttpStatus<T> makeRequest(RestTemplate restTemplate, String url, HttpMethod method, HttpEntity<?> entity, Class<T> responseClass) throws HttpClientErrorException {
        ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseClass);
        return new ResponseWithHttpStatus<T>(response.getBody(), response.getStatusCode());
    }

    public static <T> ResponseWithHttpStatus<T> makeRequest(String url, HttpMethod method, HttpEntity<?> entity, Class<T> responseClass) throws HttpClientErrorException {
        return makeRequest(getRestTemplate(), url, method, entity, responseClass);
    }
}

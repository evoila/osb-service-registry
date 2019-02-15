package de.evoila.osb.service.registry.web.request.services;

import de.evoila.osb.service.registry.model.ResponseWithHttpStatus;
import de.evoila.osb.service.registry.util.Cryptor;
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

    private static Logger log = LoggerFactory.getLogger(BaseRequestService.class);

    private static int connectionTimeout;
    private static int readTimeout;

    private static RestTemplate restTemplate;
    private static Cryptor cryptor;

    public static int getConnectionTimeout() { return connectionTimeout; }

    public static void setConnectionTimeout(int connectionTimeout) { BaseRequestService.connectionTimeout = connectionTimeout; }

    public static int getReadTimeout() { return readTimeout; }

    public static void setReadTimeout(int readTimeout) { BaseRequestService.readTimeout = readTimeout; }

    public static RestTemplate createRestTemplateWithTimeouts(int connectionTimeout, int readTimeout) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        return new RestTemplate(factory);
    }

    public static RestTemplate getRestTemplate() {
        if (restTemplate == null)
            restTemplate = createRestTemplateWithTimeouts(connectionTimeout, readTimeout);
        return restTemplate;
    }

    public static HttpHeaders createBasicHeaders(String encryptedBasicAuthToken, String salt, String apiVersion) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Broker-API-Version", apiVersion);
        headers.add("Content-type", "application/json");

        // Needs an initialized cryptor object. This is done in the Cryptor constructor.
        if (cryptor == null || !cryptor.isInitialized()) {
            log.error("Cryptor object in the BaseRequestService is NOT initialized, therefore no decryption of the basic auth token can be done. -> Will not add a authorization header.");
        } else {
            String basicAuthToken = cryptor.decrypt(salt, encryptedBasicAuthToken);
            headers.add("Authorization", "Basic " + basicAuthToken);
        }

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

    public static Cryptor getCryptor() {
        return cryptor;
    }

    public static void setCryptor(Cryptor cryptor) {
        BaseRequestService.cryptor = cryptor;
    }
}

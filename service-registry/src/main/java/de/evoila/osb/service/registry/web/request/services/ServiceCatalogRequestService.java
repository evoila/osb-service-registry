package de.evoila.osb.service.registry.web.request.services;

import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.model.ResponseWithHttpStatus;
import de.evoila.osb.service.registry.web.bodies.CatalogResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
public class ServiceCatalogRequestService extends BaseRequestService {

    private static Logger log = LoggerFactory.getLogger(ServiceCatalogRequestService.class);

    public static ResponseWithHttpStatus<CatalogResponse> getCatalog(ServiceBroker serviceBroker) {
        return getCatalog(serviceBroker, serviceBroker.getApiVersion());
    }

    public static ResponseWithHttpStatus<CatalogResponse> getCatalog(ServiceBroker serviceBroker, String apiVersion) {

        String url = serviceBroker.getHostWithPort() + "/v2/catalog";
        HttpEntity entity = new HttpEntity(createBasicHeaders(serviceBroker.getEncryptedBasicAuthToken(), serviceBroker.getSalt(), apiVersion));

        log.info("Sending catalog request to " + url);
        return makeRequest(url, HttpMethod.GET, entity, CatalogResponse.class);
    }
}

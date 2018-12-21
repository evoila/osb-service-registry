package de.evoila.osb.service.registry.web.controller.service_broker;


import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.util.IdService;
import de.evoila.osb.service.registry.exceptions.InvalidFieldException;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.manager.ServiceBrokerManager;
import de.evoila.osb.service.registry.manager.ServiceDefinitionCacheManager;
import de.evoila.osb.service.registry.web.bodies.CatalogResponse;
import de.evoila.osb.service.registry.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class CatalogController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(CatalogController.class);

    private ServiceBrokerManager sbManager;
    private ServiceDefinitionCacheManager cacheManager;

    public CatalogController(ServiceBrokerManager sbManager, ServiceDefinitionCacheManager cacheManager) {
        this.sbManager = sbManager;
        this.cacheManager = cacheManager;
    }

    @GetMapping(value = "/brokers/{brokerId}/v2/catalog")
    public ResponseEntity<?> getCatalog(@PathVariable String brokerId) throws InvalidFieldException, ResourceNotFoundException {
        log.info("Catalog request received for: " + brokerId);
        ServiceBroker serviceBroker = getServiceBrokerWithExistenceCheck(brokerId);

        sbManager.updateServiceBrokerCatalog(serviceBroker, false);

        List<ServiceDefinition> services = cacheManager.getUnmodifiableDefinitions(brokerId);
        if (services == null) {
            log.error("Could not get service definitions for service broker '" + brokerId + "'.");
            throw new ResourceNotFoundException("service definitions");
        }

        log.info("Catalog prepared for: " + brokerId);
        return new ResponseEntity<CatalogResponse>(new CatalogResponse(services), HttpStatus.OK);
    }

    private ServiceBroker getServiceBrokerWithExistenceCheck(String serviceBrokerId) throws InvalidFieldException, ResourceNotFoundException {
        if (!IdService.verifyId(serviceBrokerId))
            throw new InvalidFieldException("service-broker");

        Optional<ServiceBroker> serviceBroker = sbManager.get(serviceBrokerId);
        if (!serviceBroker.isPresent())
            throw new ResourceNotFoundException("service broker");

        return serviceBroker.get();
    }
}

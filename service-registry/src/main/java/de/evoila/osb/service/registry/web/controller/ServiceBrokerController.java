package de.evoila.osb.service.registry.web.controller;

import de.evoila.osb.service.registry.data.repositories.ServiceBrokerRepository;
import de.evoila.osb.service.registry.exceptions.EncryptionException;
import de.evoila.osb.service.registry.exceptions.InUseException;
import de.evoila.osb.service.registry.exceptions.InvalidFieldException;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.manager.ServiceBrokerManager;
import de.evoila.osb.service.registry.manager.ServiceDefinitionCacheManager;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.util.Cryptor;
import de.evoila.osb.service.registry.web.bodies.ServiceBrokerCreate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RepositoryRestController
public class ServiceBrokerController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(ServiceBrokerController.class);

    private ServiceBrokerManager serviceBrokerManager;
    private ServiceBrokerRepository serviceBrokerRepository;
    private ServiceDefinitionCacheManager cacheManager;
    private Cryptor cryptor;

    public ServiceBrokerController(ServiceBrokerManager serviceBrokerManager, ServiceBrokerRepository serviceBrokerRepository, ServiceDefinitionCacheManager cacheManager, Cryptor cryptor) {
        this.serviceBrokerManager = serviceBrokerManager;
        this.serviceBrokerRepository = serviceBrokerRepository;
        this.cacheManager = cacheManager;
        this.cryptor = cryptor;
    }

    @PostMapping(value = "/brokers")
    public ResponseEntity<?> createServiceBroker(@RequestBody @Valid ServiceBrokerCreate serviceBrokerCreate) throws EncryptionException, InvalidFieldException {
        log.info("Received service broker creation request.");

        if (!serviceBrokerCreate.getHost().startsWith("http")) throw new InvalidFieldException("host");
        ServiceBroker serviceBroker = new ServiceBroker(serviceBrokerCreate);
        String salt = cryptor.getNewSalt();
        serviceBroker.setSalt(salt);

        try {
            String encrypted = cryptor.encrypt(salt, cryptor.getBasicAuthEncoded(serviceBrokerCreate.getUsername(),serviceBrokerCreate.getPassword()));
            serviceBroker.setEncryptedBasicAuthToken(encrypted);
            serviceBroker = serviceBrokerManager.add(serviceBroker).get();
        } catch(Exception ex) {
            log.error("Encrypting a basic auth token from a service broker resulted in an unexpected result.",ex);
            throw new EncryptionException("Could not successfully fulfill the security measures for service brokers, therefore aborting creation.");
        }

        return new ResponseEntity<Resource<ServiceBroker>>(buildLink(serviceBroker), HttpStatus.OK);
    }

    @PutMapping(value = "/brokers/{brokerId}")
    public ResponseEntity<?> updateServiceBroker(@RequestBody  @Valid ServiceBrokerCreate serviceBrokerCreate,
                                                 @PathVariable String brokerId) throws ResourceNotFoundException, InvalidFieldException {
        log.info("Received service broker update request");
        if (!serviceBrokerCreate.getHost().startsWith("http")) throw new InvalidFieldException("host");

        ServiceBroker serviceBroker = serviceBrokerManager.getServiceBrokerWithExistenceCheck(brokerId);
        boolean updateSb = !serviceBrokerCreate.getHost().equals(serviceBroker.getHost());
        serviceBroker.updateBasicFields(serviceBrokerCreate);

        String encrypted = cryptor.encrypt(serviceBroker.getSalt(), cryptor.getBasicAuthEncoded(serviceBrokerCreate.getUsername(), serviceBrokerCreate.getPassword()));
        if (!encrypted.equals(serviceBroker.getEncryptedBasicAuthToken())) serviceBroker.setEncryptedBasicAuthToken(encrypted);

        serviceBrokerManager.update(serviceBroker);
        serviceBrokerManager.updateServiceBrokerCatalog(serviceBroker, true);

        return new ResponseEntity<Resource<ServiceBroker>>(buildLink(serviceBroker), HttpStatus.OK);
    }

    @DeleteMapping (value = "/brokers/{brokerId}")
    public ResponseEntity<?> deleteServiceBroker(@PathVariable String brokerId) throws ResourceNotFoundException, InvalidFieldException {
        log.info("Received service broker deletion request.");

        ServiceBroker serviceBroker = serviceBrokerManager.getServiceBrokerWithExistenceCheck(brokerId);
        if (serviceBroker.getServiceInstances() != null && serviceBroker.getServiceInstances().size() > 0 )
            throw new InUseException("There are still active service instances in existence. Deprovision them before deleting the service broker.");

        serviceBrokerManager.remove(serviceBroker);
        cacheManager.remove(serviceBroker.getId());
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    private Resource<ServiceBroker> buildLink(ServiceBroker serviceBroker) {
        Resource<ServiceBroker> resource = new Resource<>(serviceBroker);
        resource.add(ControllerLinkBuilder.linkTo(ServiceBrokerController.class)
                .slash("brokers")
                .withSelfRel());
        resource.add(ControllerLinkBuilder.linkTo(ServiceBrokerController.class)
                .slash("brokers")
                .slash(serviceBroker.getId())
                .withRel("service-broker"));
        resource.add(ControllerLinkBuilder.linkTo(ServiceBrokerController.class)
                .slash("brokers")
                .slash(serviceBroker.getId())
                .slash("serviceInstances")
                .withRel("serviceInstances"));
        resource.add(ControllerLinkBuilder.linkTo(ServiceBrokerController.class)
                .slash("brokers")
                .slash(serviceBroker.getId())
                .slash("sites")
                .withRel("sites"));
        return resource;
    }
}

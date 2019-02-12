package de.evoila.osb.service.registry.web.controller;

import de.evoila.osb.service.registry.data.repositories.ServiceBrokerRepository;
import de.evoila.osb.service.registry.exceptions.InUseException;
import de.evoila.osb.service.registry.exceptions.InvalidFieldException;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.manager.ServiceBrokerManager;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RepositoryRestController
public class ServiceBrokerController extends BaseController {

    private Logger log = LoggerFactory.getLogger(ServiceBrokerController.class);

    private ServiceBrokerManager serviceBrokerManager;
    private ServiceBrokerRepository serviceBrokerRepository;

    public ServiceBrokerController(ServiceBrokerManager serviceBrokerManager, ServiceBrokerRepository serviceBrokerRepository) {
        this.serviceBrokerManager = serviceBrokerManager;
        this.serviceBrokerRepository = serviceBrokerRepository;
    }

    @DeleteMapping (value = "/brokers/{brokerId}")
    public ResponseEntity<?> deleteServiceBroker(@PathVariable String brokerId) throws ResourceNotFoundException, InvalidFieldException {
        log.info("Received service broker deletion request.");

        ServiceBroker serviceBroker = serviceBrokerManager.getServiceBrokerWithExistenceCheck(brokerId);
        if (serviceBroker.getServiceInstances() != null && serviceBroker.getServiceInstances().size() > 0 )
            throw new InUseException("There are still active service instances in existence. Deprovision them before deleting the service broker.");

        serviceBrokerManager.remove(serviceBroker);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }
}

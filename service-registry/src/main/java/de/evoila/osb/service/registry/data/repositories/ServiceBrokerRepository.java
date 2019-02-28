package de.evoila.osb.service.registry.data.repositories;

import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path="brokers", collectionResourceRel = "brokers" , itemResourceRel = "broker")
public interface ServiceBrokerRepository extends CrudRepository<ServiceBroker, String> {

}

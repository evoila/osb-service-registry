package de.evoila.osb.service.registry.data.repositories;

import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import org.springframework.data.repository.CrudRepository;

public interface RegistryServiceInstanceRepository extends CrudRepository<RegistryServiceInstance, String> {

}

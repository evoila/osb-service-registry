package de.evoila.osb.service.registry.data.repositories;

import de.evoila.osb.service.registry.model.service.broker.RegistryBinding;
import org.springframework.data.repository.CrudRepository;

public interface RegistryBindingRepository extends CrudRepository<RegistryBinding, String> {

}

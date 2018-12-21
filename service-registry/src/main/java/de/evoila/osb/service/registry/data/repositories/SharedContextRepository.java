package de.evoila.osb.service.registry.data.repositories;

import de.evoila.osb.service.registry.model.service.broker.SharedContext;
import org.springframework.data.repository.CrudRepository;

public interface SharedContextRepository extends CrudRepository<SharedContext, String> {

}

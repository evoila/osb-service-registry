package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.data.repositories.SharedContextRepository;
import de.evoila.osb.service.registry.model.service.broker.SharedContext;
import org.springframework.stereotype.Service;

@Service
public class SharedContextManager extends BasicManager<SharedContext> {

    private SharedContextRepository sharedContextRepository;

    public SharedContextManager(SharedContextRepository repository) {
        super(repository);
        sharedContextRepository = repository;
    }

    public SharedContext findByServiceInstanceId(String serviceInstanceId) {
        return sharedContextRepository.findByServiceInstanceId(serviceInstanceId);
    }
}

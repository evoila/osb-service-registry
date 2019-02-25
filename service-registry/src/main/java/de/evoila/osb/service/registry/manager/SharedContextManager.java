package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.data.repositories.SharedContextRepository;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.model.service.broker.SharedContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SharedContextManager extends BasicManager<SharedContext> {

    private SharedContextRepository sharedContextRepository;

    public SharedContextManager(SharedContextRepository repository) {
        super(repository);
        sharedContextRepository = repository;
    }

    public SharedContext findByServiceInstanceId(String serviceInstanceId) throws ResourceNotFoundException {
        List<SharedContext> sharedContexts = sharedContextRepository.findByServiceInstanceId(serviceInstanceId);
        if (sharedContexts.size() == 0) throw new ResourceNotFoundException("shared context");
        return sharedContexts.get(0);
    }
}

package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.data.repositories.RegistryServiceInstanceRepository;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RegistryServiceInstanceManager extends BasicManager<RegistryServiceInstance> {

    private RegistryServiceInstanceManager(RegistryServiceInstanceRepository registryServiceInstanceRepository) {
        super(registryServiceInstanceRepository);
    }

    /**
     * Searches for an instance with the given serviceInstanceId while using the default http error code for the {@linkplain ResourceNotFoundException}
     *
     * @param serviceInstanceId id to identify the instance with
     * @return an instance matching the given id, guaranteed to not be null
     * @throws ResourceNotFoundException if no matching instance was found
     */
    public RegistryServiceInstance searchServiceInstance(String serviceInstanceId) throws ResourceNotFoundException {
        return searchServiceInstance(serviceInstanceId, HttpStatus.GONE);
    }

    /**
     * Searches for an instance with the given serviceInstanceId while using the default http error code for the {@linkplain ResourceNotFoundException}
     *
     * @param serviceInstanceId                         id to identify the instance with
     * @param resourceNotFoundExceptionCustomStatusCode custom {@linkplain HttpStatus} for the potential thrown {@linkplain ResourceNotFoundException}
     * @return an instance matching the given id, guaranteed to not be null
     * @throws ResourceNotFoundException if no matching instance was found
     */
    public RegistryServiceInstance searchServiceInstance(String serviceInstanceId, HttpStatus resourceNotFoundExceptionCustomStatusCode) throws ResourceNotFoundException {
        Optional<RegistryServiceInstance> serviceInstance = get(serviceInstanceId);
        if (!serviceInstance.isPresent())
            throw new ResourceNotFoundException("service instance", resourceNotFoundExceptionCustomStatusCode);
        return serviceInstance.get();
    }
}

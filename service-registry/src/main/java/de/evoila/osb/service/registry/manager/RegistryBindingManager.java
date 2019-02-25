package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.data.repositories.RegistryBindingRepository;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.model.service.broker.RegistryBinding;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RegistryBindingManager extends BasicManager<RegistryBinding> {

    private RegistryServiceInstanceManager instanceManager;

    private RegistryBindingManager(RegistryBindingRepository registryBindingRepository, RegistryServiceInstanceManager instanceManager) {
        super(registryBindingRepository);
        this.instanceManager = instanceManager;
    }

    /**
     * Searches for a binding with the given serviceBindingId while using the default http error code for the {@linkplain ResourceNotFoundException}
     *
     * @param serviceBindingId id to identify the binding with
     * @return a binding matching the given id, guaranteed to not be null
     * @throws ResourceNotFoundException if no matching binding was found
     */
    public RegistryBinding searchRegistryBinding(String serviceBindingId) throws ResourceNotFoundException {
        return searchRegistryBinding(serviceBindingId, HttpStatus.GONE);
    }

    /**
     * Searches for a binding with the given serviceBindingId while using the given custom http error code for the {@linkplain ResourceNotFoundException}
     *
     * @param serviceBindingId                          id to identify the binding with
     * @param resourceNotFoundExceptionCustomStatusCode custom {@linkplain HttpStatus} for the potential thrown {@linkplain ResourceNotFoundException}
     * @return a binding matching the given id, guaranteed to not be null
     * @throws ResourceNotFoundException if no matching binding was found
     */
    public RegistryBinding searchRegistryBinding(String serviceBindingId, HttpStatus resourceNotFoundExceptionCustomStatusCode) throws ResourceNotFoundException {
        Optional<RegistryBinding> binding = get(serviceBindingId);
        if (!binding.isPresent())
            throw new ResourceNotFoundException("service binding", resourceNotFoundExceptionCustomStatusCode);
        return binding.get();
    }

    @Override
    public void remove(RegistryBinding binding) {
        if (binding == null) return;
        RegistryServiceInstance instance = binding.getServiceInstance();
        if (instance != null && instance.getBindings() != null) {
            instance.getBindings().remove(binding);
            instanceManager.update(instance);
        }
        super.remove(binding.getId());
    }

    @Override
    public void remove(String id) {
        Optional<RegistryBinding> binding = get(id);
        if (binding.isPresent())
            remove(binding.get());
    }
}

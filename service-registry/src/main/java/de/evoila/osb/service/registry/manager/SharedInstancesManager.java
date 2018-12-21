package de.evoila.osb.service.registry.manager;

import de.evoila.cf.broker.model.Platform;
import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.cf.broker.model.catalog.plan.Plan;
import de.evoila.osb.service.registry.exceptions.NotSharedException;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class SharedInstancesManager {

    public static final String SHARED_DEFINITIONS_ID = "shared-instances-id";

    private RegistryServiceInstanceManager instanceManager;
    private ServiceDefinitionCacheManager cacheManager;

    public SharedInstancesManager(RegistryServiceInstanceManager instanceManager, ServiceDefinitionCacheManager cacheManager) {
        this.instanceManager = instanceManager;
        this.cacheManager = cacheManager;
    }

    /**
     * Gathers all instances that are shared in a list.
     *
     * @return a list of all shared instances.
     */
    public List<RegistryServiceInstance> getSharedServiceInstances() {
        List<RegistryServiceInstance> sharedList = new LinkedList<>();
        for (RegistryServiceInstance instance : instanceManager.getAll()) {
            if (instance.isShared())
                sharedList.add(instance);
        }
        return sharedList;
    }

    /**
     * Gathers all instances that target the same real service instance identified by the given shareId.
     *
     * @param shareId id to identify the real service instance
     * @return a list with all shared instances that target the given shareId
     */
    public List<RegistryServiceInstance> getSharedServiceInstances(String shareId) {
        List<RegistryServiceInstance> sharedList = new LinkedList<>();
        for (RegistryServiceInstance sharedInstance : getSharedServiceInstances()) {
            if (sharedInstance.getSharedContext().getServiceInstanceId().equals(shareId))
                sharedList.add(sharedInstance);
        }
        return sharedList;
    }

    /**
     * Return the first of the list of other instances that target the same shared instance. This used to get information about the original target instance.
     * This method either returns a non empty list or throws an exception.
     *
     * @param serviceInstance instance to get values for searching from
     * @return the first of the lost of other instances that target the same shared instance.
     * @throws NotSharedException        if the given serviceInstance is not shared
     * @throws ResourceNotFoundException if the list of instances is empty
     */
    public RegistryServiceInstance getAnyOtherSharedServiceInstance(RegistryServiceInstance serviceInstance) throws NotSharedException, ResourceNotFoundException {
        if (!serviceInstance.isShared())
            throw new NotSharedException();
        return getAnyOtherSharedServiceInstance(serviceInstance.getSharedContext().getServiceInstanceId());
    }

    public RegistryServiceInstance getAnyOtherSharedServiceInstance(String shareId) throws ResourceNotFoundException {
        List<RegistryServiceInstance> sharedInstances = getSharedServiceInstances(shareId);
        if (sharedInstances.isEmpty())
            throw new ResourceNotFoundException("shared service instance");
        return sharedInstances.get(0);
    }

    public boolean isTheOnlySharedInstance(RegistryServiceInstance instance) {
        return isTheOnlySharedInstance(instance.getSharedContext().getServiceInstanceId());
    }

    public boolean isTheOnlySharedInstance(String idAtServiceBroker) {
        return getSharedServiceInstances(idAtServiceBroker).size() < 2;
    }

    public ServiceDefinition getSharedServiceDefinition() {
        ServiceDefinition sharedDefinition = new ServiceDefinition();
        sharedDefinition.setId(SHARED_DEFINITIONS_ID);
        sharedDefinition.setName("shared-instances");
        sharedDefinition.setDescription("This service definition represents the shared service instances.");
        sharedDefinition.setBindable(true);
        sharedDefinition.setInstancesRetrievable(false);
        sharedDefinition.setBindingsRetrievable(false);
        sharedDefinition.setPlans(new LinkedList<>());
        addSharedInstancesAsPlan(sharedDefinition);
        return sharedDefinition;
    }

    private void addSharedInstancesAsPlan(ServiceDefinition sharedDefinition) {
        List<RegistryServiceInstance> sharedInstances = getSharedServiceInstances();
        List<Plan> plans = sharedDefinition.getPlans();
        for (RegistryServiceInstance instance : sharedInstances) {
            Plan plan = new Plan();
            ServiceDefinition definition = cacheManager.getDefinition(instance.getBroker().getId()
                    , instance.isOriginalInstance() ? instance.getServiceDefinitionId() : instance.getSharedContext().getServiceDefinitionId());
            plan.setId(instance.getId());
            plan.setName(definition == null ? "si-of-an-unknown-service" : "si-of-" + definition.getName());
            plan.setDescription("Org: " + instance.getOrganizationGuid() + ", Space: " + instance.getSpaceGuid());
            plan.setFree(false);
            plan.setPlatform(Platform.EXISTING_SERVICE);

            plans.add(plan);
        }
    }
}

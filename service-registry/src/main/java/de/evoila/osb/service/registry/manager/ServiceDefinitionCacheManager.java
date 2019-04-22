package de.evoila.osb.service.registry.manager;

import de.evoila.cf.broker.model.Platform;
import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.cf.broker.model.catalog.plan.Plan;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ServiceDefinitionCacheManager {

    private static Logger log = LoggerFactory.getLogger(ServiceDefinitionCacheManager.class);

    private Map<String, List<ServiceDefinition>> cache;

    public ServiceDefinitionCacheManager() {
        cache = new HashMap<>();
    }

    public synchronized boolean exists(String brokerId) {
        return cache.containsKey(brokerId);
    }

    public synchronized boolean remove(String brokerId) {
        return cache.remove(brokerId) != null;
    }

    public synchronized void clear() {
        log.info("Clearing service definition cache.");
        cache.clear();
    }

    public synchronized List<ServiceDefinition> getUnmodifiableDefinitions() {
        List<ServiceDefinition> definitions = new LinkedList<>();
        for (Map.Entry<String, List<ServiceDefinition>> entry : cache.entrySet()) {
            if (entry.getValue() != null)
                definitions.addAll(entry.getValue());
        }
        return Collections.unmodifiableList(definitions);
    }

    public synchronized List<ServiceDefinition> getUnmodifiableDefinitions(String brokerId) {
        if (exists(brokerId))
            return Collections.unmodifiableList(cache.get(brokerId));
        return Collections.unmodifiableList(new LinkedList<>());
    }

    public synchronized void put(String brokerId, List<ServiceDefinition> definitions) {
        cache.put(brokerId, definitions);
    }

    public synchronized ServiceDefinition getDefinition(String serviceBrokerId, String definitionId) {
        for (ServiceDefinition definition : getUnmodifiableDefinitions(serviceBrokerId)) {
            if (definition.getId().equals(definitionId))
                return definition;
        }
        return null;
    }

    public synchronized ServiceDefinition getDefinition(String definitionId) {
        for (String serviceBrokerId : cache.keySet()) {
            ServiceDefinition definition = getDefinition(serviceBrokerId, definitionId);
            if (definition != null) return definition;
        }
        return null;
    }

    public synchronized Plan getPlan(String definitionId, String planId) throws ResourceNotFoundException {
        ServiceDefinition definition = getDefinition(definitionId);
        for (Plan plan : definition.getPlans()) {
            if (plan.getId().equals(planId)) return plan;
        }
        throw new ResourceNotFoundException("plan");
    }

    public ServiceDefinition getDummyServiceDefinition() {
        ServiceDefinition sharedDefinition = new ServiceDefinition();
        sharedDefinition.setId("empty-service-definition");
        sharedDefinition.setName("empty-service-definition");
        sharedDefinition.setDescription("This definition is a dummy definition for when the service registry does not return any visible service definitions for the user and does not hold any other value. Creating an instance will not work!");
        sharedDefinition.setBindable(false);
        sharedDefinition.setInstancesRetrievable(false);
        sharedDefinition.setBindingsRetrievable(false);
        sharedDefinition.setPlans(new LinkedList<>());

        Plan plan = new Plan();
        plan.setId("dummy-plan");
        plan.setName("dummy-plan");
        plan.setDescription("This service plan is a dummy plan for the dummy service definition. Do NOT try to provision an instance with this plan.");
        plan.setFree(true);
        plan.setPlatform(Platform.EXISTING_SERVICE);

        sharedDefinition.getPlans().add(plan);
        return sharedDefinition;
    }
}

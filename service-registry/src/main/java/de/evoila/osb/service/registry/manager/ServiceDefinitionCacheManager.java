package de.evoila.osb.service.registry.manager;

import de.evoila.cf.broker.model.catalog.ServiceDefinition;
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
}

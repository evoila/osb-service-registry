package de.evoila.osb.service.registry.manager;

import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.osb.service.registry.config.BasicAuthSecurityConfig;
import de.evoila.osb.service.registry.exceptions.NotAuthorizedException;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.model.CloudContext;
import de.evoila.osb.service.registry.model.CloudSite;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Service
public class VisibilityManager {

    private static Logger log = LoggerFactory.getLogger(VisibilityManager.class);

    private CloudContextManager contextManager;
    private ServiceDefinitionCacheManager cacheManager;
    private ServiceBrokerManager brokerManager;
    private SharedInstancesManager sharedInstancesManager;

    public VisibilityManager(CloudContextManager contextManager, ServiceDefinitionCacheManager cacheManager, ServiceBrokerManager brokerManager, SharedInstancesManager sharedInstancesManager) {
        this.contextManager = contextManager;
        this.cacheManager = cacheManager;
        this.brokerManager = brokerManager;
        this.sharedInstancesManager = sharedInstancesManager;
    }

    private List<ServiceBroker> getVisibleServiceBrokers(CloudContext context) {
        CloudSite site = context.getSite();
        if (site == null || site.getBrokers() == null || site.getBrokers().isEmpty()) return new LinkedList<>();
        return site.getBrokers();
    }

    public List<RegistryServiceInstance> getVisibleSharedRegistryServiceInstances(Authentication authentication) throws NotAuthorizedException {
        String username = authentication.getName();

        LinkedList<RegistryServiceInstance> sharedInstances = new LinkedList<>();
        List<ServiceBroker> brokers = getVisibleServiceBrokersForUser(authentication);
        for (ServiceBroker broker : brokers) {
            sharedInstances.addAll(broker.getSharedServiceInstances());
        }
        return sharedInstances;
    }

    public List<ServiceDefinition> getVisibleServiceDefinitionsForUser(Authentication authentication) throws NotAuthorizedException {
        String username = authentication.getName();

        List<ServiceDefinition> definitions = new LinkedList<>();
        List<ServiceBroker> brokers = getVisibleServiceBrokersForUser(authentication);
        for (ServiceBroker broker : brokers) {
            definitions.addAll(cacheManager.getUnmodifiableDefinitions(broker.getId()));
        }

        return definitions;
    }

    public List<ServiceBroker> getVisibleServiceBrokersForUser(Authentication authentication) throws NotAuthorizedException {
        if (authentication == null || !authentication.isAuthenticated()) return new LinkedList<>();
        String username = authentication.getName();

        if (isAdminUser(authentication)) {
            LinkedList<ServiceBroker> brokers = new LinkedList<>();
            for (ServiceBroker broker : brokerManager.getAll()) {
                brokers.add(broker);
            }
            return brokers;
        }
        if (!canAccessServices(authentication)) throw new NotAuthorizedException(username, BasicAuthSecurityConfig.SERVICES_AUTHORITY);
        try {
            CloudContext context = contextManager.getContextFromUsername(username);
            return getVisibleServiceBrokers(context);
        } catch (ResourceNotFoundException ex) {
            return new LinkedList<ServiceBroker>();
        }
    }

    private boolean isAdminUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getAuthorities() == null) return false;
        return canAccessServices(authentication) && canManage(authentication);
    }

    private boolean canAccessServices(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getAuthorities() == null) return false;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(BasicAuthSecurityConfig.SERVICES_AUTHORITY))
                return true;
        }
        return false;
    }

    private boolean canManage(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getAuthorities() == null) return false;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
        if (authority.getAuthority().equals(BasicAuthSecurityConfig.MANAGING_AUTHORITY))
            return true;
        }
        return false;
    }
}

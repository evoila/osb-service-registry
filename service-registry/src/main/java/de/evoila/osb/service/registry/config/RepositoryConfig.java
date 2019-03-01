package de.evoila.osb.service.registry.config;

import de.evoila.osb.service.registry.model.CloudContext;
import de.evoila.osb.service.registry.model.CloudSite;
import de.evoila.osb.service.registry.model.Company;
import de.evoila.osb.service.registry.model.service.broker.RegistryBinding;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.model.service.broker.SharedContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

@Configuration
public class RepositoryConfig extends RepositoryRestConfigurerAdapter {
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(CloudContext.class);
        config.exposeIdsFor(CloudSite.class);
        config.exposeIdsFor(Company.class);
        config.exposeIdsFor(RegistryBinding.class);
        config.exposeIdsFor(RegistryServiceInstance.class);
        config.exposeIdsFor(ServiceBroker.class);
        config.exposeIdsFor(SharedContext.class);
    }
}

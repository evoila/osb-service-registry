package de.evoila.osb.service.registry.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties(prefix = "registry")
public class ServiceRegistryBean {

    public ServiceRegistryBean() {
    }
}

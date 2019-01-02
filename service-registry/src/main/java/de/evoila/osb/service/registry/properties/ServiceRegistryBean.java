package de.evoila.osb.service.registry.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.Min;

@Configuration
@ConfigurationProperties(prefix = "registry")
public class ServiceRegistryBean {

    @Min(1)
    private int updateThreadNumber;

    public ServiceRegistryBean() {
    }

    public ServiceRegistryBean(int updateThreadNumber) {
        this.updateThreadNumber = updateThreadNumber;
    }

    public int getUpdateThreadNumber() {
        return updateThreadNumber;
    }

    public void setUpdateThreadNumber(int updateThreadNumber) {
        this.updateThreadNumber = updateThreadNumber;
    }
}

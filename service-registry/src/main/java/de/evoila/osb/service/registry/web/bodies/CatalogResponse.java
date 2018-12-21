package de.evoila.osb.service.registry.web.bodies;


import de.evoila.cf.broker.model.catalog.ServiceDefinition;

import java.util.LinkedList;
import java.util.List;

public class CatalogResponse {

    private List<ServiceDefinition> services;

    public CatalogResponse() {
        services = new LinkedList<>();
    }

    public CatalogResponse(List<ServiceDefinition> services) {
        this.services = services;
    }

    public List<ServiceDefinition> getServices() {
        return services;
    }

    public void setServices(List<ServiceDefinition> services) {
        this.services = services == null ? new LinkedList<>() : services;
    }

    @Override
    public String toString() {
        return "CatalogResponse{" +
                "services=" + services +
                '}';
    }
}

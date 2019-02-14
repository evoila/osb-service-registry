package de.evoila.osb.service.registry.data.repositories;

import de.evoila.osb.service.registry.model.CloudSite;
import org.springframework.data.repository.CrudRepository;

public interface CloudSiteRepository extends CrudRepository<CloudSite, String> {

    public CloudSite findByHost(String host);
}

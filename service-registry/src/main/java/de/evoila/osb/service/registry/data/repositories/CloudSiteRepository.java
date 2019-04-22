package de.evoila.osb.service.registry.data.repositories;

import de.evoila.osb.service.registry.model.CloudSite;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path="sites", collectionResourceRel = "sites" , itemResourceRel = "site")
public interface CloudSiteRepository extends CrudRepository<CloudSite, String> {

    @RestResource(exported = false)
    public CloudSite findByHost(String host);
}

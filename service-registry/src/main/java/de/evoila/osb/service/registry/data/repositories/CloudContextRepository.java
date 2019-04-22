package de.evoila.osb.service.registry.data.repositories;

import de.evoila.osb.service.registry.model.CloudContext;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path="cloudContexts", collectionResourceRel = "cloudContexts" , itemResourceRel = "cloudContext")
public interface CloudContextRepository extends CrudRepository<CloudContext, String> {

    @RestResource(exported = false)
    public CloudContext findByUsername(String username);
}

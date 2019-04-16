package de.evoila.osb.service.registry.web.controller.managing;

import de.evoila.osb.service.registry.exceptions.ParseLinkException;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.manager.CloudContextManager;
import de.evoila.osb.service.registry.manager.CloudSiteManager;
import de.evoila.osb.service.registry.manager.CompanyManager;
import de.evoila.osb.service.registry.model.CloudContext;
import de.evoila.osb.service.registry.model.CloudSite;
import de.evoila.osb.service.registry.model.Company;
import de.evoila.osb.service.registry.util.Cryptor;
import de.evoila.osb.service.registry.util.HateoasBuilder;
import de.evoila.osb.service.registry.web.bodies.CloudContextCreate;
import de.evoila.osb.service.registry.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@RepositoryRestController
public class CloudContextController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(CloudContextController.class);

    private CloudContextManager contextManager;
    private CompanyManager companyManager;
    private CloudSiteManager siteManager;
    private Cryptor cryptor;
    private HateoasBuilder hateoasBuilder;
    private UserDetailsManager userDetailsManager;
    private PasswordEncoder passwordEncoder;

    public CloudContextController(CloudContextManager contextManager, CompanyManager companyManager, CloudSiteManager siteManager, Cryptor cryptor, HateoasBuilder hateoasBuilder, UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.contextManager = contextManager;
        this.companyManager = companyManager;
        this.siteManager = siteManager;
        this.cryptor = cryptor;
        this.hateoasBuilder = hateoasBuilder;
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(path = "cloudContexts")
    public ResponseEntity<?> getCloudContexts() {
        log.info("Received cloudContext list request");
        List<Resource<CloudContext>> cloudContextResources = new LinkedList<>();
        for (CloudContext cloudContext : contextManager.getAll()) {
            Resource <CloudContext> resource = hateoasBuilder.buildResource(cloudContext);
            resource.getContent().setPassword("<redacted>");
            cloudContextResources.add(resource);
        }
        Resources<Resource<CloudContext>> resources = new Resources<Resource<CloudContext>>(cloudContextResources);
        resources.add(hateoasBuilder.getLinksForCollection(CloudContextController.class, "cloudContexts"));
        return new ResponseEntity<Resources<Resource<CloudContext>>>(resources, HttpStatus.OK);
    }

    @GetMapping(path = "cloudContexts/{contextId}")
    public ResponseEntity<?> getCloudContext(@PathVariable String contextId) throws ResourceNotFoundException {
        log.info("Received cloud context get request.");

        Optional<CloudContext> context = contextManager.get(contextId);
        if (!context.isPresent()) throw new ResourceNotFoundException("cloud context");
        Resource<CloudContext> resource = hateoasBuilder.buildResource(context.get());
        resource.getContent().setPassword("<redacted");
        return new ResponseEntity<Resource<CloudContext>>(resource, HttpStatus.OK);
    }

    @PostMapping (path = "cloudContexts")
    public ResponseEntity<?> createContext(@RequestBody @Valid CloudContextCreate create) {
        log.info("Received cloud context creation request.");

        CloudContext cloudContext = new CloudContext(create);
        String password = contextManager.addNewCredentials(cloudContext);

        Optional<Company> company = Optional.empty();
        Optional<CloudSite> site = Optional.empty();
        try {
            String companyId = hateoasBuilder.getIdAfterObjectInLink("companies", create.getCompany());
            company = companyManager.get(companyId);
        } catch (ParseLinkException ex) { log.debug("Failed to parse company link.", ex); }
        try {
            String siteId = hateoasBuilder.getIdAfterObjectInLink("sites", create.getSite());
            site = siteManager.get(siteId);
        } catch (ParseLinkException ex) { log.debug("Failed to parse site link.", ex); }

        cloudContext = contextManager.add(cloudContext).get();

        if (company.isPresent()) {
            log.debug("Add new cloud context to " + company.get().getId());
            cloudContext.setCompany(company.get());
            company.get().getContexts().add(cloudContext);
            companyManager.update(company.get());
        }
        if (site.isPresent()) {
            log.debug("Add new cloud context to " + site.get().getId());
            cloudContext.setSite(site.get());
            site.get().getContexts().add(cloudContext);
            siteManager.update(site.get());
        }
        if (company.isPresent() || site.isPresent()) cloudContext = contextManager.update(cloudContext).get();


        contextManager.registerUser(cloudContext, password);
        CloudContext responseContext = new CloudContext(cloudContext);
        responseContext.setPassword(password);
        return new ResponseEntity<Resource<CloudContext>>(hateoasBuilder.buildResource(responseContext), HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/cloudContexts/{contextId}")
    public ResponseEntity<?> deleteContext(@PathVariable String contextId) throws ResourceNotFoundException {
        log.info("Received cloud context deletion request.");

        Optional<CloudContext> context = contextManager.get(contextId);
        if (!context.isPresent()) throw new ResourceNotFoundException("cloud context");
        contextManager.remove(context.get());

        contextManager.unregisterUser(context.get().getUsername());
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }
}

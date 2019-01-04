package de.evoila.osb.service.registry.manager;

import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.osb.service.registry.data.repositories.ServiceBrokerRepository;
import de.evoila.osb.service.registry.exceptions.InvalidFieldException;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.model.CloudSite;
import de.evoila.osb.service.registry.model.ResponseWithHttpStatus;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.properties.ServiceRegistryBean;
import de.evoila.osb.service.registry.util.IdService;
import de.evoila.osb.service.registry.web.AsyncCatalogUpdateTask;
import de.evoila.osb.service.registry.web.bodies.CatalogResponse;
import de.evoila.osb.service.registry.web.request.services.ShadowServiceCatalogRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


@Service
public class ServiceBrokerManager extends BasicManager<ServiceBroker> {

    private static Logger log = LoggerFactory.getLogger(ServiceBrokerManager.class);

    private CloudSiteManager siteManager;
    private ServiceDefinitionCacheManager cacheManager;
    private ServiceRegistryBean serviceRegistryBean;
    private final int catalogUpdateMaxThreadCount;

    public ServiceBrokerManager(ServiceBrokerRepository repository, @Lazy CloudSiteManager siteManager, ServiceDefinitionCacheManager cacheManager, ServiceRegistryBean serviceRegistryBean) {
        super(repository);
        this.siteManager = siteManager;
        this.cacheManager = cacheManager;
        catalogUpdateMaxThreadCount = serviceRegistryBean.getUpdateThreadNumber();
    }

    /**
     * Adds a cloud site to a service broker's {@linkplain ServiceBroker#sites} list (if it is not already present) and updates the object in the storage.
     *
     * @param serviceBroker service broker object to add the cloud site
     * @param cloudSite     cloud site object to add
     * @return {@linkplain Optional} with the updated service broker, with the unchanged service broker or an empty one
     */
    public Optional<ServiceBroker> addCloudSite(ServiceBroker serviceBroker, CloudSite cloudSite) {
        if (serviceBroker.getSites().contains(cloudSite))
            return Optional.<ServiceBroker>of(serviceBroker);

        serviceBroker.getSites().add(cloudSite);
        return update(serviceBroker);
    }

    /**
     * Sets {@linkplain ServiceBroker#sites} to null, before calling {@linkplain BasicManager#clear()}.
     */
    @Override
    public void clear() {
        log.debug("Clearing all service brokers.");
        Iterable<ServiceBroker> iterable = getAll();
        for (ServiceBroker serviceBroker : iterable)
            serviceBroker.setSites(null);
        super.clear();
    }

    /**
     * Forces an service definition cache update for every service broker in the storage.
     * Clears the cache before starting to update.
     * This method is scheduled to be run every 15 minutes.
     */
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void scheduledServiceBrokerCatalogUpdate() {
        log.info("Updating catalogs on schedule...");
        try {
            updateAllServiceBrokerCatalogs(true);
            log.info("Finished updating catalogs on schedule.");
        } catch (Exception ex) {
            log.error("Updating catalogs on schedule failed.", ex);
        }
    }

    /**
     * Triggers a catalog update for every service broker in the storage.
     * This will be done via {@linkplain FutureTask} in parallel!
     *
     * @param forceUpdate flag to indicate a forced catalog update
     */
    public void updateAllServiceBrokerCatalogs(boolean forceUpdate) {
        if (forceUpdate) cacheManager.clear();
        Iterable<ServiceBroker> brokers = getAll();
        int brokersCount = 0;
        for (ServiceBroker serviceBroker : brokers)
            brokersCount++;

        if (brokersCount == 0) return;

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(brokersCount, catalogUpdateMaxThreadCount));
        List<FutureTask<String>> tasks = new LinkedList<FutureTask<String>>();

        for (ServiceBroker serviceBroker : brokers) {
            FutureTask<String> task = new FutureTask<String>(new AsyncCatalogUpdateTask(serviceBroker, this, forceUpdate));
            tasks.add(task);
            log.info("Starting new async " + (forceUpdate ? "forced" : "soft") + " catalog update handler for: " + serviceBroker.getLoggingNameString());
            executor.execute(task);
        }

        for (FutureTask<String> task : tasks) {
            try {
                String logName = task.get();
                log.info("Finished async catalog update for " + logName);
            } catch (InterruptedException | ExecutionException ex) {
                log.error("Executing an async catalog update failed.", ex);
            }
        }
    }

    /**
     * Updates the service definition cache for a service broker. This involves a request to the service brokers catalog endpoint.
     * If the forceUpdate flag is not set, only a service broker that has no entry in the cache is updated.
     *
     * @param broker      service broker to update the cache for
     * @param forceUpdate flag to indicate a forced update
     */
    public void updateServiceBrokerCatalog(ServiceBroker broker, boolean forceUpdate) {
        if (broker == null || (!forceUpdate && cacheManager.exists(broker.getId()))) return;

        log.info((forceUpdate ? "Force" : "Soft" )+ " updating service catalog for " + broker.getLoggingNameString());
        try {
            ResponseWithHttpStatus<CatalogResponse> response = ShadowServiceCatalogRequestService.getCatalog(broker);
            if (response != null && response.getBody() != null && response.getBody().getServices() != null) {
                log.debug("Caching received catalog for " + broker.getLoggingNameString());
                cacheManager.put(broker.getId(), response.getBody().getServices());
            } else {
                if (response != null)
                    throw new HttpClientErrorException(response.getStatus());
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
            }
        } catch (HttpClientErrorException ex) {
            log.error("Updating service catalog failed with " + ex.getStatusCode() + " for " + broker.getId(), ex);
        } catch (ResourceAccessException ex) {
            log.error("Updating service catalog failed for " + broker.getId(), ex);
        }
    }

    /**
     * Calls {@linkplain ServiceBrokerManager#searchForServiceBrokerWithServiceDefinitionId(String, HttpStatus)}
     * with the default {@linkplain HttpStatus#BAD_REQUEST} code.
     *
     * @param serviceDefinitionId id to identify the definition with
     * @return the service broker with the matching definition, guaranteed to not be null
     * @throws ResourceNotFoundException if no matching definition was found
     */
    public ServiceBroker searchForServiceBrokerWithServiceDefinitionId(String serviceDefinitionId) throws ResourceNotFoundException {
        return searchForServiceBrokerWithServiceDefinitionId(serviceDefinitionId, HttpStatus.BAD_REQUEST);
    }

    /**
     * Searches for a service broker by one of its service definitions.
     *
     * @param serviceDefinitionId                       id to identify the definition with
     * @param resourceNotFoundExceptionCustomStatusCode custom {@linkplain HttpStatus} for the potential thrown {@linkplain ResourceNotFoundException}
     * @return the service broker with the matching definition, guaranteed to not be null
     * @throws ResourceNotFoundException if no matching definition was found
     */
    public ServiceBroker searchForServiceBrokerWithServiceDefinitionId(String serviceDefinitionId, HttpStatus resourceNotFoundExceptionCustomStatusCode) throws ResourceNotFoundException {
        log.debug("Searching for service broker via service definition id.");
        Iterator<ServiceBroker> iterator = getAll().iterator();
        ServiceBroker sb = null;
        while (iterator.hasNext()) {
            sb = iterator.next();
            if (!cacheManager.exists(sb.getId())) {
                log.debug("Invoke soft update on service definitions for " + sb.getLoggingNameString() + " due to no cached definitions yet.");
                updateServiceBrokerCatalog(sb, false);
            }

            List<ServiceDefinition> definitions = cacheManager.getUnmodifiableDefinitions(sb.getId());
            log.debug("Found following definitions for " + sb.getLoggingNameString() + " : " + definitions);
            for (int i = 0; definitions != null && i < definitions.size(); i++) {
                if (definitions.get(i).getId().equals(serviceDefinitionId)) {
                    log.debug("Found following matching service broker for " + serviceDefinitionId + " -> " + sb.getLoggingNameString());
                    return sb;
                }
            }
        }
        log.debug("No matching service broker found for " + serviceDefinitionId);
        throw new ResourceNotFoundException("service definition", resourceNotFoundExceptionCustomStatusCode);
    }

    /**
     * Calls {@linkplain ServiceBrokerManager#searchForServiceBrokerWithServiceInstanceId(String, HttpStatus)}
     * with the default {@linkplain HttpStatus#BAD_REQUEST} code.
     *
     * @param serviceInstanceId id to identify the instance with
     * @return the service broker with the matching instance, guaranteed to not be null
     * @throws ResourceNotFoundException if no matching instance was found
     */
    public ServiceBroker searchForServiceBrokerWithServiceInstanceId(String serviceInstanceId) throws ResourceNotFoundException {
        return searchForServiceBrokerWithServiceInstanceId(serviceInstanceId, HttpStatus.BAD_REQUEST);
    }

    /**
     * Searches for a service broker by one of its service instances.
     *
     * @param serviceInstanceId                         id to identify the instance with
     * @param resourceNotFoundExceptionCustomStatusCode custom {@linkplain HttpStatus} for the potential thrown {@linkplain ResourceNotFoundException}
     * @return the service broker with the matching instance, guaranteed to not be null
     * @throws ResourceNotFoundException if no matching instance was found
     */
    public ServiceBroker searchForServiceBrokerWithServiceInstanceId(String serviceInstanceId, HttpStatus resourceNotFoundExceptionCustomStatusCode) throws ResourceNotFoundException {
        log.debug("Searching for service broker via service instance id.");
        Iterator<ServiceBroker> iterator = getAll().iterator();
        ServiceBroker serviceBroker = null;
        while (iterator.hasNext()) {
            serviceBroker = iterator.next();
            if (serviceBroker.getServiceInstances() != null) {
                if (serviceBroker.getServiceInstance(serviceInstanceId) != null) {
                    log.debug("Found following matching service broker for " + serviceInstanceId + " -> " + serviceBroker.getLoggingNameString());
                    return serviceBroker;
                }
            }
        }
        log.debug("No matching service broker found for " + serviceInstanceId);
        throw new ResourceNotFoundException("service instance", resourceNotFoundExceptionCustomStatusCode);
    }


    public ServiceBroker getServiceBrokerWithExistenceCheck(String serviceBrokerId) throws InvalidFieldException, ResourceNotFoundException {
        if (!IdService.verifyId(serviceBrokerId))
            throw new InvalidFieldException("service-broker");

        Optional<ServiceBroker> serviceBroker = get(serviceBrokerId);
        if (!serviceBroker.isPresent())
            throw new ResourceNotFoundException("service broker");

        return serviceBroker.get();
    }
}

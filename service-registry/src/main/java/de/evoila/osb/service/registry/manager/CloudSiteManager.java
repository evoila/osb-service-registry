package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.data.repositories.CloudSiteRepository;
import de.evoila.osb.service.registry.model.CloudContext;
import de.evoila.osb.service.registry.model.CloudSite;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
public class CloudSiteManager extends BasicManager<CloudSite> {

    private CloudContextManager contextManager;
    private ServiceBrokerManager sbManager;

    public CloudSiteManager(CloudSiteRepository repository, @Lazy CloudContextManager contextManager, @Lazy ServiceBrokerManager sbManager) {
        super(repository);
        this.contextManager = contextManager;
        this.sbManager = sbManager;
    }

    /**
     * Clears all cloud sites from the storage.
     * Additionally removes entries from all {@linkplain ServiceBroker#sites} and the reference from {@linkplain CloudContext#site} first, before calling {@linkplain BasicManager#clear()}.
     */
    @Override
    public void clear() {
        Iterator<CloudSite> iterator = getAll().iterator();
        while (iterator.hasNext()) {
            CloudSite site = iterator.next();

            List<ServiceBroker> serviceBrokers = site.getBrokers();
            for (ServiceBroker serviceBroker : serviceBrokers) {
                serviceBroker.getSites().remove(site);
                sbManager.update(serviceBroker);
            }

            List<CloudContext> cloudContexts = site.getContexts();
            for (CloudContext cloudContext : cloudContexts) {
                cloudContext.setSite(null);
                contextManager.update(cloudContext);
            }

            site.setBrokers(null);
            site.setContexts(null);
        }
        super.clear();
    }
}

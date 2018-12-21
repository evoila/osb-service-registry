package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.data.repositories.CloudContextRepository;
import de.evoila.osb.service.registry.model.CloudContext;
import de.evoila.osb.service.registry.model.CloudSite;
import org.springframework.stereotype.Service;

import java.util.Iterator;

@Service
public class CloudContextManager extends BasicManager<CloudContext> {

    private CloudSiteManager siteManager;

    public CloudContextManager(CloudContextRepository repository, CloudSiteManager siteManager) {
        super(repository);
        this.siteManager = siteManager;
    }

    /**
     * Clears all cloud contexts from the storage.
     * Additionally removes entries from all {@linkplain CloudSite#contexts} first, before calling {@linkplain BasicManager#clear()}.
     */
    public void clear() {
        Iterator<CloudContext> iterator = getAll().iterator();
        while (iterator.hasNext()) {
            CloudContext context = iterator.next();
            if (context.getSite() != null) {
                context.getSite().getContexts().remove(context);
                siteManager.update(context.getSite());
            }
            context.setSite(null);
        }
        super.clear();
    }

}

package de.evoila.osb.service.registry.web;

import de.evoila.osb.service.registry.manager.ServiceBrokerManager;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;

import java.util.concurrent.Callable;

public class AsyncCatalogUpdateTask implements Callable<String> {

    private ServiceBroker broker;
    private ServiceBrokerManager manager;

    private boolean forceUpdate;

    public AsyncCatalogUpdateTask(ServiceBroker broker, ServiceBrokerManager manager, boolean forceUpdate) {
        this.broker = broker;
        this.manager = manager;
        this.forceUpdate = forceUpdate;
    }

    @Override
    public String call() throws Exception {
        manager.updateServiceBrokerCatalog(broker, forceUpdate);
        return broker.getLoggingNameString();
    }

    public ServiceBroker getBroker() {
        return broker;
    }

    public void setBroker(ServiceBroker broker) {
        this.broker = broker;
    }

    public ServiceBrokerManager getManager() {
        return manager;
    }

    public void setManager(ServiceBrokerManager manager) {
        this.manager = manager;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
}

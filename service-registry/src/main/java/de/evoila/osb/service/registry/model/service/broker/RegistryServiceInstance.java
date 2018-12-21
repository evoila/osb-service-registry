package de.evoila.osb.service.registry.model.service.broker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.evoila.osb.service.registry.manager.Identifiable;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "service_instance")
public class RegistryServiceInstance implements Identifiable {

    @Id
    @JsonProperty("service_instance_id")
    private String id;

    @JsonProperty("service_id")
    private String serviceDefinitionId;
    private String planId;
    private String organizationGuid;
    private String spaceGuid;
    private String nameSpace;

    private boolean creationInProgress;
    private boolean deletionInProgress;
    private boolean originalInstance;

    private String dashboardUrl;

    @ManyToOne
    @JoinColumn(name = "broker", nullable = true)
    private ServiceBroker broker;

    @OneToMany(mappedBy = "serviceInstance", cascade = CascadeType.REMOVE)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<RegistryBinding> bindings;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "shared_context_id")
    private SharedContext sharedContext;


    public RegistryServiceInstance() {
        this("", "", "", "", "", "", false, false, false, "", null, new LinkedList<>(), null);
    }

    public RegistryServiceInstance(boolean originalInstance) {
        this();
        this.originalInstance = originalInstance;
    }

    public RegistryServiceInstance(String id, String serviceDefinitionId, String planId, String organizationGuid, String spaceGuid, String nameSpace, boolean creationInProgress, boolean deletionInProgress, boolean originalInstance, String dashboardUrl, ServiceBroker broker, List<RegistryBinding> bindings, SharedContext sharedContext) {
        this.id = id;
        this.serviceDefinitionId = serviceDefinitionId;
        this.planId = planId;
        this.organizationGuid = organizationGuid;
        this.spaceGuid = spaceGuid;
        this.nameSpace = nameSpace;
        this.creationInProgress = creationInProgress;
        this.deletionInProgress = deletionInProgress;
        this.originalInstance = originalInstance;
        this.dashboardUrl = dashboardUrl;
        this.broker = broker;
        this.bindings = bindings;
        this.sharedContext = sharedContext;

        if (sharedContext != null)
            sharedContext.setServiceInstance(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceDefinitionId() { return serviceDefinitionId; }

    public void setServiceDefinitionId(String serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; }

    public String getPlanId() { return planId; }

    public void setPlanId(String planId) { this.planId = planId; }

    public String getOrganizationGuid() { return organizationGuid; }

    public void setOrganizationGuid(String organizationGuid) { this.organizationGuid = organizationGuid; }

    public String getSpaceGuid() { return spaceGuid; }

    public void setSpaceGuid(String spaceGuid) { this.spaceGuid = spaceGuid; }

    public String getNameSpace() { return nameSpace; }

    public void setNameSpace(String nameSpace) { this.nameSpace = nameSpace; }

    public boolean isCreationInProgress() { return creationInProgress; }

    public void setCreationInProgress(boolean creationInProgress) {
        this.creationInProgress = creationInProgress;
        if (creationInProgress) deletionInProgress = false;
    }

    public boolean isDeletionInProgress() { return deletionInProgress; }

    public void setDeletionInProgress(boolean deletionInProgress) { this.deletionInProgress = deletionInProgress; }

    public boolean isOriginalInstance() { return originalInstance; }

    public void setOriginalInstance(boolean originalInstance) { this.originalInstance = originalInstance; }

    /**
     * Checks for field {@linkplain SharedContext#serviceInstanceId} to be not null and not empty.
     *
     * @return true if {@linkplain SharedContext#serviceInstanceId} is neither null nor empty
     */
    @JsonIgnore
    private boolean hasSharedServiceInstanceId() {
        return sharedContext != null && sharedContext.getServiceInstanceId() != null && !sharedContext.getServiceInstanceId().isEmpty();
    }

    /**
     * Checks whether the service instance is the original instance or if it is a shared one
     * and returns the {@linkplain SharedContext#serviceInstanceId} of the {@linkplain RegistryServiceInstance#sharedContext}  for shared instances
     * or the {@linkplain RegistryServiceInstance#id} for original instances.
     * Used for getting the correct id for requests to the service broker.
     *
     * @return {@linkplain SharedContext#serviceInstanceId} or {@linkplain RegistryServiceInstance#id}
     */
    @JsonIgnore
    public String getIdForServiceBroker() {
        if (!originalInstance && hasSharedServiceInstanceId())
            return sharedContext.getServiceInstanceId();
        return id;
    }

    @JsonIgnore
    public boolean isShared() {
        return sharedContext != null && sharedContext.isShared();
    }

    public String getDashboardUrl() { return dashboardUrl; }

    public void setDashboardUrl(String dashboardUrl) { this.dashboardUrl = dashboardUrl; }

    public ServiceBroker getBroker() { return broker; }

    public void setBroker(ServiceBroker broker) { this.broker = broker;}

    public List<RegistryBinding> getBindings() { return bindings; }

    public void setBindings(List<RegistryBinding> bindings) { this.bindings = bindings; }

    public SharedContext getSharedContext() { return sharedContext; }

    public void setSharedContext(SharedContext sharedContext) { this.sharedContext = sharedContext; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegistryServiceInstance that = (RegistryServiceInstance) o;

        if (creationInProgress != that.creationInProgress) return false;
        if (deletionInProgress != that.deletionInProgress) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (serviceDefinitionId != null ? !serviceDefinitionId.equals(that.serviceDefinitionId) : that.serviceDefinitionId != null)
            return false;
        if (planId != null ? !planId.equals(that.planId) : that.planId != null) return false;
        if (organizationGuid != null ? !organizationGuid.equals(that.organizationGuid) : that.organizationGuid != null)
            return false;
        if (spaceGuid != null ? !spaceGuid.equals(that.spaceGuid) : that.spaceGuid != null) return false;
        if (nameSpace != null ? !nameSpace.equals(that.nameSpace) : that.nameSpace != null) return false;
        if (dashboardUrl != null ? !dashboardUrl.equals(that.dashboardUrl) : that.dashboardUrl != null) return false;
        if (bindings != null ? bindings.size() != that.bindings.size() : that.bindings != null) return false;
        return sharedContext != null ? sharedContext.equals(that.sharedContext) : that.sharedContext == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (serviceDefinitionId != null ? serviceDefinitionId.hashCode() : 0);
        result = 31 * result + (planId != null ? planId.hashCode() : 0);
        result = 31 * result + (organizationGuid != null ? organizationGuid.hashCode() : 0);
        result = 31 * result + (spaceGuid != null ? spaceGuid.hashCode() : 0);
        result = 31 * result + (nameSpace != null ? nameSpace.hashCode() : 0);
        result = 31 * result + (creationInProgress ? 1 : 0);
        result = 31 * result + (deletionInProgress ? 1 : 0);
        result = 31 * result + (dashboardUrl != null ? dashboardUrl.hashCode() : 0);
        result = 31 * result + (bindings != null ? bindings.hashCode() : 0);
        result = 31 * result + (sharedContext != null ? sharedContext.hashCode() : 0);
        return result;
    }
}

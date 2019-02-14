package de.evoila.osb.service.registry.model.service.broker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.osb.service.registry.exceptions.SharedContextInvalidException;
import de.evoila.osb.service.registry.manager.Identifiable;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "shared_context")
public class SharedContext implements Identifiable {

    @Id @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    private String serviceInstanceId;
    private String serviceDefinitionId;
    private String planId;
    private String organization;
    private String space;
    private String nameSpace;
    private String displayName;
    private String description;

    private boolean shared;

    @OneToOne
    @JoinColumn(name = "instance_id")
    private RegistryServiceInstance serviceInstance;

    public SharedContext() {
        this("", "", "", "", "", "", "", "", "", false);
    }

    public SharedContext(SharedContext other) {
        this("", other.getServiceInstanceId()
                , other.getServiceDefinitionId(), other.getPlanId(), other.getOrganization(), other.getSpace()
                , other.getNameSpace(), other.getDisplayName(), other.getDescription(), other.isShared(), null);
    }

    public SharedContext(String id, String serviceInstanceId, String serviceDefinitionId, String planId, String organization, String space, String nameSpace, String displayName, String description, boolean shared) {
        this(id, serviceInstanceId, serviceDefinitionId, planId, organization, space, nameSpace, displayName, description, shared, null);
    }

    public SharedContext(String id, String serviceInstanceId, String serviceDefinitionId, String planId, String organization, String space, String nameSpace, String displayName, String description, boolean shared, RegistryServiceInstance serviceInstance) {
        this.id = id;
        this.serviceInstanceId = serviceInstanceId;
        this.serviceDefinitionId = serviceDefinitionId;
        this.planId = planId;
        this.organization = organization;
        this.space = space;
        this.nameSpace = nameSpace;
        this.displayName = displayName;
        this.description = description;
        this.shared = shared;
        this.serviceInstance = serviceInstance;
    }

    /**
     * Calls {@linkplain #initShared(RegistryServiceInstance, ServiceDefinition)} with its {@linkplain #serviceInstance}
     */
    public void initShared(ServiceDefinition definition) {
        if (serviceInstance != null)
            initShared(serviceInstance, definition);
    }

    /**
     * Sets shared to true and initializes the SharedContext object with values from the service instance.
     * This should not be called upon an uninitialized SharedContext object bound to a non original service instance,
     * because this would set up the SharedContext in way, which does not refer to an actual existing service instance.
     * Following fields are set if they are empty or null.
     * <ul>
     *     <li>serviceInstanceId</li>
     *     <li>serviceDefinitionId</li>
     *     <li>planId</li>
     *     <li>organization</li>
     *     <li>space</li>
     *     <li>nameSpace</li>
     *     <li>description</li>
     * </ul>
     * @param serviceInstance
     * @param definition
     */
    public void initShared(RegistryServiceInstance serviceInstance, ServiceDefinition definition) {
        shared = true;
        if (serviceInstanceId == null || serviceInstanceId.isEmpty()) serviceInstanceId = serviceInstance.getId();
        if (serviceDefinitionId == null || serviceDefinitionId.isEmpty()) serviceDefinitionId = serviceInstance.getServiceDefinitionId();
        if (planId == null || planId.isEmpty()) planId = serviceInstance.getPlanId();
        if (organization == null || organization.isEmpty()) organization = serviceInstance.getOrganizationGuid();
        if (space == null || space.isEmpty()) space = serviceInstance.getSpaceGuid();
        if (nameSpace == null || nameSpace.isEmpty()) nameSpace = serviceInstance.getNameSpace();
        if (description == null || description.isEmpty())
            description = "Definition: " + (definition == null ? "unknown-service" : definition.getName())
                    + ", Org: " + organization
                    + ", Space: " + space
                    + ", Namespace: " + nameSpace;
    }

    /**
     * Validates the fields of the SharedContext for proper usage.
     * Checks for null pointers and empty fields, if shared is true.
     * @return true if SharedContext is valid in its current state and will not return false!
     * @throws SharedContextInvalidException if this object is invalid in its current state
     */
    public boolean validate() throws SharedContextInvalidException {
        if (shared) {
            if (serviceInstanceId == null) throw new SharedContextInvalidException("serviceInstanceId", SharedContextInvalidException.FIELD_IS_NULL);
            if (serviceDefinitionId == null) throw new SharedContextInvalidException("serviceDefinitionId", SharedContextInvalidException.FIELD_IS_NULL);
            if (planId == null) throw new SharedContextInvalidException("planId", SharedContextInvalidException.FIELD_IS_NULL);
            if (serviceInstance == null) throw new SharedContextInvalidException("serviceInstance", SharedContextInvalidException.FIELD_IS_NULL);

            if (serviceInstanceId.isEmpty()) throw new SharedContextInvalidException("serviceInstanceId", SharedContextInvalidException.FIELD_IS_EMPTY);
            if (serviceDefinitionId.isEmpty()) throw new SharedContextInvalidException("serviceDefinitionId", SharedContextInvalidException.FIELD_IS_EMPTY);
            if (planId.isEmpty()) throw new SharedContextInvalidException("planId", SharedContextInvalidException.FIELD_IS_EMPTY);
        }
        return true;
    }

    @Override
    public String getId() { return id; }

    @Override
    public void setId(String id) { this.id = id; }

    public boolean isShared() { return shared; }

    public void setShared(boolean shared) { this.shared = shared; }

    public String getServiceInstanceId() { return serviceInstanceId; }

    public void setServiceInstanceId(String serviceInstanceId) { this.serviceInstanceId = serviceInstanceId; }

    public String getServiceDefinitionId() { return serviceDefinitionId; }

    public void setServiceDefinitionId(String serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; }

    public String getPlanId() { return planId; }

    public void setPlanId(String planId) { this.planId = planId; }

    public String getOrganization() { return organization; }

    public void setOrganization(String organization) { this.organization = organization; }

    public String getSpace() { return space; }

    public void setSpace(String space) { this.space = space; }

    public String getNameSpace() { return nameSpace; }

    public void setNameSpace(String nameSpace) { this.nameSpace = nameSpace; }

    public String getDisplayName() { return displayName; }

    public boolean hasEmptyDisplayName() { return displayName == null || displayName.isEmpty(); }

    @JsonIgnore
    public String getDisplayNameOrDefaultName() {
        return hasEmptyDisplayName() ? serviceInstanceId.substring(0, Math.min(serviceInstanceId.length(), 13)) : displayName;
    }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public RegistryServiceInstance getServiceInstance() { return serviceInstance; }

    public void setServiceInstance(RegistryServiceInstance serviceInstance) { this.serviceInstance = serviceInstance; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SharedContext that = (SharedContext) o;

        if (shared != that.shared) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (serviceInstanceId != null ? !serviceInstanceId.equals(that.serviceInstanceId) : that.serviceInstanceId != null)
            return false;
        if (serviceDefinitionId != null ? !serviceDefinitionId.equals(that.serviceDefinitionId) : that.serviceDefinitionId != null)
            return false;
        if (planId != null ? !planId.equals(that.planId) : that.planId != null) return false;
        if (organization != null ? !organization.equals(that.organization) : that.organization != null) return false;
        if (space != null ? !space.equals(that.space) : that.space != null) return false;
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        return nameSpace != null ? nameSpace.equals(that.nameSpace) : that.nameSpace == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (shared ? 1 : 0);
        result = 31 * result + (serviceInstanceId != null ? serviceInstanceId.hashCode() : 0);
        result = 31 * result + (serviceDefinitionId != null ? serviceDefinitionId.hashCode() : 0);
        result = 31 * result + (planId != null ? planId.hashCode() : 0);
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        result = 31 * result + (space != null ? space.hashCode() : 0);
        result = 31 * result + (nameSpace != null ? nameSpace.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}

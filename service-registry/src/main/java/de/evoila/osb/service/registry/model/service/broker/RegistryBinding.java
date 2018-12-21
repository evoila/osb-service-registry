package de.evoila.osb.service.registry.model.service.broker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.evoila.osb.service.registry.manager.Identifiable;

import javax.persistence.*;

@Entity
@Table(name = "service_binding")
public class RegistryBinding implements Identifiable {

    @Id
    @JsonProperty("binding_id")
    private String id;

    private boolean creationInProgress;
    private boolean deleteInProgress;

    @ManyToOne
    @JoinColumn(name = "service_instance")
    private RegistryServiceInstance serviceInstance;

    public RegistryBinding() {
        this("", false, false, null);
    }

    public RegistryBinding(String id, boolean creationInProgress, boolean deleteInProgress, RegistryServiceInstance serviceInstance) {
        this.id = id;
        this.creationInProgress = creationInProgress;
        this.deleteInProgress = deleteInProgress;
        this.serviceInstance = serviceInstance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isCreationInProgress() {
        return creationInProgress;
    }

    public void setCreationInProgress(boolean creationInProgress) {
        this.creationInProgress = creationInProgress;
    }

    public boolean isDeleteInProgress() {
        return deleteInProgress;
    }

    public void setDeleteInProgress(boolean deleteInProgress) {
        this.deleteInProgress = deleteInProgress;
    }

    public RegistryServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(RegistryServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    @JsonIgnore
    public boolean isBindingOf(RegistryServiceInstance serviceInstance) {
        return serviceInstance != null && serviceInstance.getBindings() != null && serviceInstance.getBindings().contains(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegistryBinding binding = (RegistryBinding) o;

        if (creationInProgress != binding.creationInProgress) return false;
        if (deleteInProgress != binding.deleteInProgress) return false;
        return id != null ? id.equals(binding.id) : binding.id == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (creationInProgress ? 1 : 0);
        result = 31 * result + (deleteInProgress ? 1 : 0);
        return result;
    }
}

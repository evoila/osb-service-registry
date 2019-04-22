package de.evoila.osb.service.registry.model.service.broker;


import com.fasterxml.jackson.annotation.JsonIgnore;
import de.evoila.osb.service.registry.manager.Identifiable;
import de.evoila.osb.service.registry.model.CloudSite;
import de.evoila.osb.service.registry.web.bodies.ServiceBrokerCreate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "service_broker")
public class ServiceBroker implements Identifiable {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;

    private String host;
    private int port;

    private String salt;
    private String encryptedBasicAuthToken;
    private String apiVersion;
    private String description;

    private boolean cloudFoundryAllowed;
    private boolean kubernetesAllowed;

    @ManyToMany(mappedBy = "brokers")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<CloudSite> sites;

    @OneToMany(mappedBy = "broker", cascade = CascadeType.REMOVE)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<RegistryServiceInstance> serviceInstances;

    public ServiceBroker() {
        this("", "", 0, "", "", "", "", false, false);
    }

    public ServiceBroker(ServiceBrokerCreate create) {
        this();
        updateBasicFields(create);
    }

    public ServiceBroker(String id, String host, int port, String salt, String encryptedBasicAuthToken, String apiVersion, String description, boolean cfAllowed, boolean k8Allowed) {
        this(id, host, port, salt, encryptedBasicAuthToken, apiVersion, description, cfAllowed, k8Allowed, new LinkedList<CloudSite>(), new LinkedList<RegistryServiceInstance>());
    }

    public ServiceBroker(String id, String host, int port, String salt, String encryptedBasicAuthToken, String apiVersion,
                         String description, boolean cloudFoundryAllowed, boolean kubernetesAllowed,
                         List<CloudSite> sites, List<RegistryServiceInstance> serviceInstances) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.salt = salt;
        this.encryptedBasicAuthToken = encryptedBasicAuthToken;
        this.apiVersion = apiVersion;
        this.description = description;
        this.cloudFoundryAllowed = cloudFoundryAllowed;
        this.kubernetesAllowed = kubernetesAllowed;
        this.sites = sites == null ? new LinkedList<>() : sites;
        this.serviceInstances = serviceInstances == null ? new LinkedList<>() : serviceInstances;
    }

    public void updateBasicFields(ServiceBrokerCreate create) {
        if (create.getHost() != null && !create.getHost().isEmpty()) setHost(create.getHost());
        if (create.getPort() > 0) setPort(create.getPort());
        if (create.getApiVersion() != null && !create.getApiVersion().isEmpty()) setApiVersion(create.getApiVersion());
        if (create.getDescription() != null) setDescription(create.getDescription());
        setKubernetesAllowed(create.isKubernetesAllowed());
        setCloudFoundryAllowed(create.isCloudFoundryAllowed());
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @JsonIgnore
    public String getHostWithPort() {
        return host + ":" + port;
    }

    @JsonIgnore
    public String getSalt() { return salt; }

    public void setSalt(String salt) { this.salt = salt; }

    @JsonIgnore
    public String getEncryptedBasicAuthToken() { return encryptedBasicAuthToken; }

    public void setEncryptedBasicAuthToken(String encryptedBasicAuthToken) {
        this.encryptedBasicAuthToken = encryptedBasicAuthToken;
    }

    public String getId() { return id; }

    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public String getLoggingNameString() { return getHost()+" / Id: "+getId();}

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getApiVersion() { return apiVersion; }

    public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }

    public boolean isCloudFoundryAllowed() {
        return cloudFoundryAllowed;
    }

    public void setCloudFoundryAllowed(boolean cloudFoundryAllowed) {
        this.cloudFoundryAllowed = cloudFoundryAllowed;
    }

    public boolean isKubernetesAllowed() {
        return kubernetesAllowed;
    }

    public void setKubernetesAllowed(boolean kubernetesAllowed) {
        this.kubernetesAllowed = kubernetesAllowed;
    }

    @JsonIgnore
    public List<CloudSite> getSites() {
        return sites;
    }

    public synchronized void setSites(List<CloudSite> sites) { this.sites = sites == null ? new LinkedList<>() : sites; }

    @JsonIgnore
    public synchronized List<RegistryServiceInstance> getServiceInstances() { return serviceInstances; }

    public synchronized void setServiceInstances(List<RegistryServiceInstance> serviceInstances) {
        this.serviceInstances = serviceInstances == null ? new LinkedList<>() : serviceInstances;
    }

    @JsonIgnore
    public synchronized Optional<RegistryServiceInstance> getServiceInstance(String serviceInstanceId) {
        for (RegistryServiceInstance instance : serviceInstances) {
            if (serviceInstanceId.equals(instance.getId()))
                return Optional.of(instance);
        }
        return Optional.empty();
    }

    @JsonIgnore
    public synchronized List<RegistryServiceInstance> getSharedServiceInstances() {
        List<RegistryServiceInstance> sharedInstances = new LinkedList<>();
        if (serviceInstances == null) return sharedInstances;

        for (RegistryServiceInstance serviceInstance: serviceInstances) {
            if (serviceInstance.isShared())
                sharedInstances.add(serviceInstance);
        }
        return sharedInstances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceBroker that = (ServiceBroker) o;

        if (port != that.port) return false;
        if (cloudFoundryAllowed != that.cloudFoundryAllowed) return false;
        if (kubernetesAllowed != that.kubernetesAllowed) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (salt != null ? !salt.equals(that.salt) : that.salt != null) return false;
        if (encryptedBasicAuthToken != null ? !encryptedBasicAuthToken.equals(that.encryptedBasicAuthToken) : that.encryptedBasicAuthToken != null)
            return false;
        if (apiVersion != null ? !apiVersion.equals(that.apiVersion) : that.apiVersion != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (salt != null ? salt.hashCode() : 0);
        result = 31 * result + (encryptedBasicAuthToken != null ? encryptedBasicAuthToken.hashCode() : 0);
        result = 31 * result + (apiVersion != null ? apiVersion.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (cloudFoundryAllowed ? 1 : 0);
        result = 31 * result + (kubernetesAllowed ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ServiceBroker{" +
                "id='" + id + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", salt='" + salt + '\'' +
                ", encryptedBasicAuthToken='" + encryptedBasicAuthToken + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", description='" + description + '\'' +
                ", cloudFoundryAllowed=" + cloudFoundryAllowed +
                ", kubernetesAllowed=" + kubernetesAllowed +
                '}';
    }
}

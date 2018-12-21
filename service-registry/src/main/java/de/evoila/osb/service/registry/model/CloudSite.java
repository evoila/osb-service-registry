package de.evoila.osb.service.registry.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.manager.Identifiable;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "cloud_site")
public class CloudSite implements Identifiable {

    @Id @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    private Platform platform;
    private String host;

    @OneToMany(mappedBy = "site", cascade = CascadeType.REMOVE)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    private List<CloudContext> contexts;

    @ManyToMany
    @JoinTable(
            name = "broker_site",
            joinColumns = { @JoinColumn(name = "broker_id") },
            inverseJoinColumns = { @JoinColumn(name = "site_id") }
    )
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    private List<ServiceBroker> brokers;

    public CloudSite() {
        this("", Platform.unknown, "");
    }

    public CloudSite(String id, Platform platform, String host) {
        this(id, platform, host, new LinkedList<>(), new LinkedList<>());
    }

    public CloudSite(String id, Platform platform, String host, List<CloudContext> contexts, List<ServiceBroker> brokers) {
        this.id = id;
        this.platform = platform;
        this.host = host;
        this.contexts = contexts;
        this.brokers = brokers;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<CloudContext> getContexts() {
        return contexts;
    }

    @JsonGetter("contexts")
    public List<String> getContextsIds() {
        List<String> output = new LinkedList<>();
        for (CloudContext c: contexts)
            output.add(c.getId());
        return output;
    }

    public Optional<CloudContext> getContext(String id) {
        for (CloudContext c: contexts) {
            if (id.equals(c.getId()))
                return Optional.of(c);
        }
        return Optional.<CloudContext>empty();
    }

    public void setContexts(List<CloudContext> contexts) { this.contexts = contexts == null ? new LinkedList<>() : contexts; }

    public List<ServiceBroker> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<ServiceBroker> brokers) { this.brokers = brokers == null ? new LinkedList<>() : brokers; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CloudSite site = (CloudSite) o;

        if (id != null ? !id.equals(site.id) : site.id != null) return false;
        if (platform != site.platform) return false;
        if (host != null ? !host.equals(site.host) : site.host != null) return false;
        if (contexts != null ? contexts.size() != site.contexts.size() : site.contexts != null) return false;
        return brokers != null ? brokers.size() == site.brokers.size() : site.brokers == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (platform != null ? platform.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CloudSite{" +
                "id='" + id + '\'' +
                ", platform=" + platform +
                ", host='" + host + '\'' +
                ", contexts[" + contexts.size() + "]" +
                ", brokers[" + brokers.size() + "]" +
                '}';
    }
}

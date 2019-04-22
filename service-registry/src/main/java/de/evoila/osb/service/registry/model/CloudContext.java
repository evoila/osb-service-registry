package de.evoila.osb.service.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.evoila.osb.service.registry.manager.Identifiable;
import de.evoila.osb.service.registry.web.bodies.CloudContextCreate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "cloud_context")
public class CloudContext implements Identifiable {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;

    private String organization;
    private String space;
    private String namespace;
    private String username;
    private String password;
    private String salt;

    @ManyToOne
    @JoinColumn(name = "cloud_site")
    private CloudSite site;

    @ManyToOne
    @JoinColumn(name = "company")
    private Company company;

    public CloudContext() {
        this("", "", "", "", "", "", "");
    }

    public CloudContext(CloudContextCreate create) {
        this("", create.getOrganization(), create.getSpace(), create.getNamespace(), "", "", "");
    }

    public CloudContext(CloudContext other) {
        this(   other.getId(), other.getOrganization(), other.getSpace(),
                other.getNamespace(), other.getUsername(), other.getPassword(),
                other.getSalt(), other.getSite(), other.getCompany());
    }

    public CloudContext(String id, String organization, String space, String namespace, String username, String password, String salt) {
        this(id, organization, space, namespace, username, password, salt, null, null);
    }

    public CloudContext(String id, String organization, String space, String namespace, String username, String password, String salt, CloudSite site, Company company) {
        this.id = id;
        this.organization = organization;
        this.space = space;
        this.namespace = namespace;
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.site = site;
        this.company = company;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public CloudSite getSite() {
        return site;
    }

    public void setSite(CloudSite site) {
        this.site = site;
    }

    @JsonIgnore
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    @JsonIgnore
    public String getSalt() { return salt; }

    public void setSalt(String salt) { this.salt = salt; }

    @Override
    public String toString() {
        return "CloudContext{" +
                "id='" + id + '\'' +
                ", organization='" + organization + '\'' +
                ", space='" + space + '\'' +
                ", namespace='" + namespace + '\'' +
                ", username='" + username + '\'' +
                ", password='[redacted]'" +
                ", salt='" + salt + '\'' +
                ", site=" + (site == null ? "null" : site.getId()) +
                ", company=" + (company == null ? "null" : company.getId()) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CloudContext that = (CloudContext) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (organization != null ? !organization.equals(that.organization) : that.organization != null) return false;
        if (space != null ? !space.equals(that.space) : that.space != null) return false;
        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null)
            return false;
        if (password != null ? !password.equals(that.password) : that.password != null)
            return false;
        if (salt != null ? !salt.equals(that.salt) : that.salt != null)
            return false;
        if (site != null ? !site.getId().equals(that.site.getId()) : that.site != null) return false;
        return company != null ? company.getId().equals(that.company.getId()) : that.company == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        result = 31 * result + (space != null ? space.hashCode() : 0);
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (salt != null ? salt.hashCode() : 0);
        result = 31 * result + (site != null ? site.hashCode() : 0);
        result = 31 * result + (company != null ? company.hashCode() : 0);
        return result;
    }
}

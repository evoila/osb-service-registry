package de.evoila.osb.service.registry.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import de.evoila.osb.service.registry.manager.Identifiable;
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
    private String authToken;

    @ManyToOne
    @JoinColumn(name = "cloud_site")
    private CloudSite site;

    @ManyToOne
    @JoinColumn(name = "company")
    private Company company;

    public CloudContext() {
        this("", "", "", "", "");
    }

    public CloudContext(String id, String organization, String space, String namespace, String authToken) {
        this(id, organization, space, namespace, authToken, null, null);
    }

    public CloudContext(String id, String organization, String space, String namespace, String authToken, CloudSite site, Company company) {
        this.id = id;
        this.organization = organization;
        this.space = space;
        this.namespace = namespace;
        this.authToken = authToken;
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

    public CloudSite getSite() {
        return site;
    }

    @JsonGetter("site")
    public String getSiteId() {
        return site.getId();
    }

    public void setSite(CloudSite site) {
        this.site = site;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @JsonGetter("company")
    public String getCompanyId() {
        return company.getId();
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public String toString() {
        return "CloudContext{" +
                "id='" + id + '\'' +
                ", organization='" + organization + '\'' +
                ", space='" + space + '\'' +
                ", namespace='" + namespace + '\'' +
                ", authToken='" + authToken + '\'' +
                ", site=" + site.getId() +
                ", company=" + company.getId() +
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
        if (authToken != null ? !authToken.equals(that.authToken) : that.authToken != null) return false;
        if (site != null ? !site.getId().equals(that.site.getId()) : that.site != null) return false;
        return company != null ? company.getId().equals(that.company.getId()) : that.company == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        result = 31 * result + (space != null ? space.hashCode() : 0);
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (authToken != null ? authToken.hashCode() : 0);
        return result;
    }
}

package de.evoila.osb.service.registry.web.bodies;

import org.springframework.hateoas.Link;

public class CloudContextCreate {

    private String organization;
    private String space;
    private String namespace;
    private Link company;
    private Link site;

    public CloudContextCreate() {
        this("", "", "");
    }

    public CloudContextCreate(String organization, String space, String namespace) {
        this(organization, space, namespace, null, null);
    }

    public CloudContextCreate(String organization, String space, String namespace, Link company, Link site) {
        this.organization = organization;
        this.space = space;
        this.namespace = namespace;
        this.company = company;
        this.site = site;
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

    public Link getCompany() {
        return company;
    }

    public void setCompany(Link company) {
        this.company = company;
    }

    public Link getSite() {
        return site;
    }

    public void setSite(Link site) {
        this.site = site;
    }

    @Override
    public String toString() {
        return "CloudContextCreate{" +
                "organization='" + organization + '\'' +
                ", space='" + space + '\'' +
                ", namespace='" + namespace + '\'' +
                ", company=" + company +
                ", site=" + site +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CloudContextCreate that = (CloudContextCreate) o;

        if (organization != null ? !organization.equals(that.organization) : that.organization != null) return false;
        if (space != null ? !space.equals(that.space) : that.space != null) return false;
        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) return false;
        if (company != null ? !company.equals(that.company) : that.company != null) return false;
        return site != null ? site.equals(that.site) : that.site == null;
    }

    @Override
    public int hashCode() {
        int result = organization != null ? organization.hashCode() : 0;
        result = 31 * result + (space != null ? space.hashCode() : 0);
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (company != null ? company.hashCode() : 0);
        result = 31 * result + (site != null ? site.hashCode() : 0);
        return result;
    }
}

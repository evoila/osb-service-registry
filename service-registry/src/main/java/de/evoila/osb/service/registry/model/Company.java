package de.evoila.osb.service.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.evoila.osb.service.registry.manager.Identifiable;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "companies")
public class Company implements Identifiable {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;

    private String name;
    private String basicAuthToken;

    @OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    private List<CloudContext> contexts;

    public Company() {
        this("", "", "");
    }

    public Company(String id, String name, String basicAuthToken) {
        this(id, name, basicAuthToken, new LinkedList<>());
    }

    public Company(String id, String name, String basicAuthToken, List<CloudContext> contexts) {
        this.id = id;
        this.name = name;
        this.basicAuthToken = basicAuthToken;
        this.contexts = contexts;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBasicAuthToken() {
        return basicAuthToken;
    }

    public void setBasicAuthToken(String basicAuthToken) {
        this.basicAuthToken = basicAuthToken;
    }

    public List<CloudContext> getContexts() {
        return contexts;
    }

    public void setContexts(List<CloudContext> contexts) { this.contexts = contexts == null ? new LinkedList<>() : contexts; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Company company = (Company) o;

        if (id != null ? !id.equals(company.id) : company.id != null) return false;
        if (name != null ? !name.equals(company.name) : company.name != null) return false;
        if (basicAuthToken != null ? !basicAuthToken.equals(company.basicAuthToken) : company.basicAuthToken != null)
            return false;
        return contexts != null ? contexts.size() == company.contexts.size() : company.contexts == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (basicAuthToken != null ? basicAuthToken.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", basicAuthToken='" + basicAuthToken + '\'' +
                ", contexts=[" + contexts.size() + "]" +
                '}';
    }
}

package de.evoila.osb.service.registry.web.bodies;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ServiceBrokerCreate {

    @NotEmpty
    @NotNull
    private String host;
    @NotEmpty
    private int port;

    @NotNull
    @NotEmpty
    private String username;
    @NotNull
    @NotEmpty
    private String password;

    @NotNull
    @NotEmpty
    private String apiVersion;
    @NotNull
    private String description;

    @NotEmpty
    private boolean cloudFoundryAllowed;
    @NotEmpty
    private boolean kubernetesAllowed;

    public ServiceBrokerCreate() {
    }

    public ServiceBrokerCreate(@NotEmpty @NotNull String host, @NotEmpty int port, @NotNull @NotEmpty String username, @NotNull @NotEmpty String password, @NotNull @NotEmpty String apiVersion, @NotNull String description, @NotEmpty boolean cloudFoundryAllowed, @NotEmpty boolean kubernetesAllowed) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.apiVersion = apiVersion;
        this.description = description;
        this.cloudFoundryAllowed = cloudFoundryAllowed;
        this.kubernetesAllowed = kubernetesAllowed;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceBrokerCreate that = (ServiceBrokerCreate) o;

        if (port != that.port) return false;
        if (cloudFoundryAllowed != that.cloudFoundryAllowed) return false;
        if (kubernetesAllowed != that.kubernetesAllowed) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (apiVersion != null ? !apiVersion.equals(that.apiVersion) : that.apiVersion != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (apiVersion != null ? apiVersion.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (cloudFoundryAllowed ? 1 : 0);
        result = 31 * result + (kubernetesAllowed ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ServiceBrokerCreate{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", description='" + description + '\'' +
                ", cloudFoundryAllowed=" + cloudFoundryAllowed +
                ", kubernetesAllowed=" + kubernetesAllowed +
                '}';
    }
}

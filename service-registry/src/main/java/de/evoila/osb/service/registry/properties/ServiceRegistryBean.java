package de.evoila.osb.service.registry.properties;

import de.evoila.osb.service.registry.web.request.services.BaseRequestService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;

@Configuration
@ConfigurationProperties(prefix = "registry")
public class ServiceRegistryBean {

    @Min(1)
    private int updateThreadNumber;

    @Min(100)
    private int timeoutConnection;
    @Min(100)
    private int timeoutRead;

    @Min(16)
    private char[] encryptionKey;

    public ServiceRegistryBean() {
    }

    public ServiceRegistryBean(@Min(1) int updateThreadNumber, @Min(100) int timeoutConnection, @Min(100) int timeoutRead, @Min(16) char[] encryptionKey) {
        this.updateThreadNumber = updateThreadNumber;
        this.timeoutConnection = timeoutConnection;
        this.timeoutRead = timeoutRead;
        this.encryptionKey = encryptionKey;
    }

    /**
     * Gets called after spring creates this bean and sets defaults, if the values are to small.
     * Also sets the {@linkplain BaseRequestService#connectionTimeout} and {@linkplain BaseRequestService#readTimeout} fields with the given values.
     */
    @PostConstruct
    public void initFieldsWithDefaults() {
        if (updateThreadNumber < 1) updateThreadNumber = 1;
        BaseRequestService.setConnectionTimeout(timeoutConnection >= 100 ? timeoutConnection : 10000);
        BaseRequestService.setReadTimeout(timeoutRead >= 100 ? timeoutRead : 20000);
    }

    public int getUpdateThreadNumber() { return updateThreadNumber; }

    public void setUpdateThreadNumber(int updateThreadNumber) { this.updateThreadNumber = updateThreadNumber; }

    public int getTimeoutConnection() { return timeoutConnection; }

    public void setTimeoutConnection(int timeoutConnection) { this.timeoutConnection = timeoutConnection; }

    public int getTimeoutRead() { return timeoutRead; }

    public void setTimeoutRead(int timeoutRead) { this.timeoutRead = timeoutRead; }

    public char[] getEncryptionKey() { return encryptionKey; }

    public void setEncryptionKey(char[] encryptionKey) { this.encryptionKey = encryptionKey; }
}

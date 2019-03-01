package de.evoila.osb.service.registry.properties;

import de.evoila.osb.service.registry.util.CredentialsGenerator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@ConfigurationProperties(prefix = "login")
public class BaseAuthenticationBean {

    private String adminUsername;
    private String adminPassword;
    private int passwordLength;
    private int usernameLength;

    @PostConstruct
    public void setUp(){
        CredentialsGenerator.usernameLength = Math.max(usernameLength, CredentialsGenerator.DEFAULT_CREDENTIALS_LENGTH);
        CredentialsGenerator.passwordLength = Math.max(passwordLength, CredentialsGenerator.DEFAULT_CREDENTIALS_LENGTH);
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public int getPasswordLength() { return passwordLength; }

    public void setPasswordLength(int passwordLength) { this.passwordLength = passwordLength; }

    public int getUsernameLength() { return usernameLength; }

    public void setUsernameLength(int usernameLength) { this.usernameLength = usernameLength; }
}

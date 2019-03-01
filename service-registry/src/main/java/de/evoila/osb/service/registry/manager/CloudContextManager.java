package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.config.BasicAuthSecurityConfig;
import de.evoila.osb.service.registry.model.CloudContext;
import de.evoila.osb.service.registry.model.CloudSite;
import de.evoila.osb.service.registry.model.Company;
import de.evoila.osb.service.registry.properties.BaseAuthenticationBean;
import de.evoila.osb.service.registry.util.CredentialsGenerator;
import de.evoila.osb.service.registry.util.Cryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Service
public class CloudContextManager extends BasicManager<CloudContext> {

    private static Logger log = LoggerFactory.getLogger(CloudContextManager.class);

    private BaseAuthenticationBean baseAuthenticationBean;
    private CloudSiteManager siteManager;
    private CompanyManager companyManager;
    private Cryptor cryptor;
    private UserDetailsManager userDetailsManager;
    private PasswordEncoder passwordEncoder;

    public CloudContextManager(CrudRepository<CloudContext, String> repository, BaseAuthenticationBean baseAuthenticationBean, CloudSiteManager siteManager, CompanyManager companyManager, Cryptor cryptor, UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        super(repository);
        this.baseAuthenticationBean = baseAuthenticationBean;
        this.siteManager = siteManager;
        this.companyManager = companyManager;
        this.cryptor = cryptor;
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void registerAllUsersFromContexts() {
        for (CloudContext cloudContext : getAll()) {
            String password = getUnencryptedPassword(cloudContext);
            if (password != null) registerUser(cloudContext, password);
        }
    }

    public UserDetails registerUser(CloudContext cloudContext, String plaintextPassword) {
        if (cloudContext == null || StringUtils.isEmpty(cloudContext.getUsername()) || StringUtils.isEmpty(plaintextPassword)) return null;
        UserDetails userDetails = User.withUsername(cloudContext.getUsername())
                .password(passwordEncoder.encode(plaintextPassword))
                .authorities(BasicAuthSecurityConfig.SERVICES_AUTHORITY)
                .build();
        userDetailsManager.createUser(userDetails);
        log.info("Registered new user: " + userDetails.getUsername() + " - authorities=" + userDetails.getAuthorities());
        return userDetails;
    }

    public void unregisterUser(String username) {
        if (username.equals(baseAuthenticationBean.getAdminUsername())) {
            log.warn("Deletion of admin user was blocked! This should not be triggered in the first place.");
            return;
        }
        userDetailsManager.deleteUser(username);
        log.info("Deleted user "+username);
    }

    /**
     * Clears all cloud contexts from the storage.
     * Additionally removes entries from all {@linkplain CloudSite#getContexts()} first, before calling {@linkplain BasicManager#clear()}.
     */
    public void clear() {
        Iterator<CloudContext> iterator = getAll().iterator();
        while (iterator.hasNext()) {
            CloudContext context = iterator.next();
            if (context.getSite() != null) {
                context.getSite().getContexts().remove(context);
                siteManager.update(context.getSite());
            }
            context.setSite(null);
        }
        super.clear();
    }

    @Override
    public void removeReferencesFromRelatedObjects(CloudContext cloudContext) {
        if (cloudContext == null) return;
        Company company = cloudContext.getCompany();
        if (company != null && company.getContexts() != null) {
            company.getContexts().remove(cloudContext);
            companyManager.update(company);
        }
        CloudSite site = cloudContext.getSite();
        if (site != null&& site.getContexts() != null) {
            site.getContexts().remove(cloudContext);
            siteManager.update(site);
        }
    }

    public Set<String> getTakenUsernames() {
        Set<String> takenUsernames = new HashSet<>();
        takenUsernames.add(baseAuthenticationBean.getAdminUsername());
        for (CloudContext cloudContext : getAll()) {
            takenUsernames.add(cloudContext.getUsername());
        }
        return takenUsernames;
    }

    public String addNewCredentials(CloudContext context) {
        if (context == null) return null;
        context.setUsername(CredentialsGenerator.randomUsername(getTakenUsernames()));
        String password = CredentialsGenerator.randomPassword();
        context.setSalt(cryptor.getNewSalt());
        context.setPassword(cryptor.encrypt(context.getSalt(), password));
        return password;
    }

    public String getUnencryptedPassword(CloudContext context) {
        if (context == null || StringUtils.isEmpty(context.getPassword()) || StringUtils.isEmpty(context.getSalt()))
            return null;
        return cryptor.decrypt(context.getSalt(), context.getPassword());
    }
}

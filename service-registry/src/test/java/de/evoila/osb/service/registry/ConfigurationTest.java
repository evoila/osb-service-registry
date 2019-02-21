package de.evoila.osb.service.registry;

import de.evoila.osb.service.registry.properties.BaseAuthenticationBean;
import de.evoila.osb.service.registry.properties.ServiceRegistryBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import static org.junit.Assert.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration
public class ConfigurationTest {

    public static String[] otherFields = new String[]{
            "spring.jpa.hibernate.ddl-auto",
            "spring.jpa.properties.hibernate.dialect",

            "spring.datasource.url",
            "spring.datasource.username",
            "spring.datasource.password",
    };

    @Autowired
    ServiceRegistryBean serviceRegistryBean;
    @Autowired
    BaseAuthenticationBean baseAuthenticationBean;

    @Autowired
    private Environment env;

    @Test
    public void testServiceRegistryBean() {
        assertTrue("Encryption key is null or empty", serviceRegistryBean.getEncryptionKey() != null && serviceRegistryBean.getEncryptionKey().length > 0);
        assertTrue("Update thread number is smaller than 1.", serviceRegistryBean.getUpdateThreadNumber() > 0);
        assertTrue("Connection timeout is smaller than 1.", serviceRegistryBean.getTimeoutConnection() > 0);
        assertTrue("Read timeout is smaller than 1.", serviceRegistryBean.getTimeoutRead() > 0);
    }

    @Test
    public void testBaseAuthenticationBean() {
        assertFalse("Basic auth username is null or empty.", StringUtils.isEmpty(baseAuthenticationBean.getUsername()));
        assertFalse("Basic auth password is null or empty.", StringUtils.isEmpty(baseAuthenticationBean.getPassword()));
        assertFalse("Basic auth role is null or empty.", StringUtils.isEmpty(baseAuthenticationBean.getRole()));
    }

    @Test
    public void testOtherFields() {
        // Be aware that hibernate or spring.datasource fields might be empty (!= null) to enable h2 usage
        for (int i = 0 ; i < otherFields.length; i++) {
            assertTrue("Configuration field " + otherFields[i] + " is null.", env.getProperty(otherFields[i]) != null);
            System.out.println("Field '" + otherFields[i] + "' = '" + env.getProperty(otherFields[i]) + "'");
        }
    }
}

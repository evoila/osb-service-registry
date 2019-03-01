package de.evoila.osb.service.registry;

import de.evoila.osb.service.registry.data.repositories.*;
import de.evoila.osb.service.registry.manager.*;
import de.evoila.osb.service.registry.properties.BaseAuthenticationBean;
import de.evoila.osb.service.registry.properties.ServiceRegistryBean;
import de.evoila.osb.service.registry.util.Cryptor;
import de.evoila.osb.service.registry.web.controller.RegistryController;
import de.evoila.osb.service.registry.web.controller.managing.ServiceBrokerController;
import de.evoila.osb.service.registry.web.controller.managing.SharingInstanceController;
import de.evoila.osb.service.registry.web.controller.servicebroker.CatalogController;
import de.evoila.osb.service.registry.web.controller.shadowbroker.ShadowCatalogController;
import de.evoila.osb.service.registry.web.controller.shadowbroker.ShadowServiceBindingController;
import de.evoila.osb.service.registry.web.controller.shadowbroker.ShadowServiceInstanceController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration
public class ApplicationContextTest {

    @Autowired
    public ServiceBrokerManager sbManager;
    @Autowired
    public SharedInstancesManager sharedInstancesManager;
    @Autowired
    public RegistryServiceInstanceManager instanceManager;
    @Autowired
    public RegistryBindingManager bindingManager;
    @Autowired
    public ServiceDefinitionCacheManager definitionCacheManager;
    @Autowired
    public CloudContextManager cloudContextManager;
    @Autowired
    public CloudSiteManager siteManager;
    @Autowired
    public CompanyManager companyManager;
    @Autowired
    public SharedContextManager sharedContextManager;

    @Autowired
    public Cryptor cryptor;

    @Autowired
    public CatalogController catalogController;
    @Autowired
    public ShadowCatalogController shadowCatalogController;
    @Autowired
    public ShadowServiceBindingController shadowServiceBindingController;
    @Autowired
    public ShadowServiceInstanceController shadowServiceInstanceController;
    @Autowired
    public RegistryController registryController;
    @Autowired
    public ServiceBrokerController serviceBrokerController;
    @Autowired
    public SharingInstanceController sharingInstanceController;

    @Autowired
    public CloudContextRepository cloudContextRepository;
    @Autowired
    public CloudSiteRepository cloudSiteRepository;
    @Autowired
    public CompanyRepository companyRepository;
    @Autowired
    public RegistryBindingRepository registryBindingRepository;
    @Autowired
    public RegistryServiceInstanceRepository registryServiceInstanceRepository;
    @Autowired
    public ServiceBrokerRepository serviceBrokerRepository;
    @Autowired
    public SharedContextRepository sharedContextRepository;

    @Autowired
    public BaseAuthenticationBean baseAuthenticationBean;
    @Autowired
    public ServiceRegistryBean serviceRegistryBean;

    @Test
    public void testServiceAvailability() throws IllegalAccessException {
        Class<ApplicationContextTest> cl = ApplicationContextTest.class;
        Field[] fields = cl.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Object fieldValue = field.get(this);
            assertNotNull("Service, manager or controller " + field.getType() + " " +field.getName() + " is null but should be present.", fieldValue);
        }
    }
}

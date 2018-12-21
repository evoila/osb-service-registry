package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.util.TestUtils;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration
public class RegistryServiceInstanceManagerTest {

    @Autowired
    private RegistryBindingManager bindingManager;

    @Autowired
    private RegistryServiceInstanceManager manager;

    @Autowired
    private ServiceDefinitionCacheManager cacheManager;

    @After
    public void dropAll() {
        ManagerTestService.dropAll(manager);
    }

    @Test
    public void getAll() {
        ManagerTestService.<RegistryServiceInstance>getAll(manager, TestUtils.getRandomRegistryServiceInstance(), TestUtils.getRandomRegistryServiceInstance());
    }

    @Test
    public void add() {
        ManagerTestService.<RegistryServiceInstance>add(manager, TestUtils.getRandomRegistryServiceInstance());
    }

    @Test
    public void update() {
        RegistryServiceInstance instance = TestUtils.getRandomRegistryServiceInstance();
        RegistryServiceInstance alteredInstance = TestUtils.getRandomRegistryServiceInstance();
        alteredInstance.setCreationInProgress(true);
        ManagerTestService.<RegistryServiceInstance>update(manager, instance, alteredInstance);
    }

    @Test
    public void remove() {
        ManagerTestService.remove(manager, TestUtils.getRandomRegistryServiceInstance());
    }
}

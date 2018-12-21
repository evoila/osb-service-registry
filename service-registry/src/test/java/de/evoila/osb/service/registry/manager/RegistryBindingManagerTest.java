package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.util.TestUtils;
import de.evoila.osb.service.registry.model.service.broker.RegistryBinding;
import org.junit.After;
import org.junit.Before;
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
public class RegistryBindingManagerTest {

    @Autowired
    private RegistryBindingManager manager;

    @Autowired
    private RegistryServiceInstanceManager instanceManager;

    @Autowired
    private ServiceDefinitionCacheManager cacheManager;

    @Before
    @After
    public void dropAll() {
        ManagerTestService.dropAll(manager);
    }

    @Test
    public void get() {
        RegistryBinding binding = TestUtils.getRandomRegistryBinding();
        ManagerTestService.<RegistryBinding>get(manager, binding, false);
    }

    @Test
    public void getAll() {
        ManagerTestService.<RegistryBinding>getAll(manager, TestUtils.getRandomRegistryBinding(), TestUtils.getRandomRegistryBinding());
    }

    @Test
    public void add() {
        ManagerTestService.<RegistryBinding>add(manager, TestUtils.getRandomRegistryBinding());
    }

    @Test
    public void update() {
        RegistryBinding binding = TestUtils.getRandomRegistryBinding();
        RegistryBinding alteredBinding = TestUtils.getRandomRegistryBinding();
        alteredBinding.setCreationInProgress(true);
        ManagerTestService.<RegistryBinding>update(manager, binding, alteredBinding);
    }

    @Test
    public void remove() {
        ManagerTestService.remove(manager, TestUtils.getRandomRegistryBinding());
    }
}
package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.model.CloudContext;
import de.evoila.osb.service.registry.util.TestUtils;
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
public class CloudContextManagerTest {

    @Autowired
    private CloudContextManager manager;

    @Before
    @After
    public void dropAll() {
        ManagerTestService.dropAll(manager);
    }

    @Test
    public void getCloudContext() {
        CloudContext randomContext = TestUtils.getRandomCloudContext(null, null);
        ManagerTestService.<CloudContext>get(manager, randomContext, false);
    }

    @Test
    public void getCloudContexts() {
        ManagerTestService.<CloudContext>getAll(manager, TestUtils.getRandomCloudContext(null, null), TestUtils.getRandomCloudContext(null, null));
    }

    @Test
    public void addCloudContext() {
        ManagerTestService.add(manager, TestUtils.getRandomCloudContext(null, null));
    }

    @Test
    public void updateServiceBroker() {
        CloudContext cloudContext = TestUtils.getRandomCloudContext(null, null);
        CloudContext alteredContext = TestUtils.getRandomCloudContext(null, null);
        alteredContext.setId(cloudContext.getId());
        alteredContext.setOrganization("this-is-the-altered-organization");
        ManagerTestService.update(manager, cloudContext, alteredContext);
    }

    @Test
    public void removeServiceBroker() {
        ManagerTestService.remove(manager, TestUtils.getRandomCloudContext(null, null));
    }
}

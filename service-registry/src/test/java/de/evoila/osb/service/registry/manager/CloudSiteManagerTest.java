package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.model.CloudContext;
import de.evoila.osb.service.registry.model.CloudSite;
import de.evoila.osb.service.registry.model.Platform;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
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

import java.util.LinkedList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration
public class CloudSiteManagerTest {

    @Autowired
    private CloudSiteManager manager;

    @Autowired
    private CloudContextManager contextManager;

    @Autowired
    private ServiceBrokerManager sbManager;

    @Before
    @After
    public void dropAllCloudSites() {
        ManagerTestService.dropAll(manager);
        ManagerTestService.dropAll(contextManager);
        ManagerTestService.dropAll(sbManager);
    }

    @Test
    public void getCloudSite() {
        CloudSite randomSite = TestUtils.getRandomCloudSite();

        ServiceBroker serviceBroker = TestUtils.getRandomServiceBroker();
        serviceBroker = sbManager.add(serviceBroker).get();
        randomSite.getBrokers().add(serviceBroker);

        CloudContext cloudContext = TestUtils.getRandomCloudContext();
        cloudContext = contextManager.add(cloudContext).get();
        randomSite.getContexts().add(cloudContext);

        randomSite = manager.add(randomSite).get();
        cloudContext.setSite(randomSite);
        contextManager.update(cloudContext);
        serviceBroker.getSites().add(randomSite);
        sbManager.update(serviceBroker);

        ManagerTestService.<CloudSite>get(manager, randomSite, true);
    }

    @Test
    public void getCloudSites() {
        List<CloudSite> sites = new LinkedList<>();
        sites.add(TestUtils.getRandomCloudSite());
        sites.add(TestUtils.getRandomCloudSite());
        sites.add(TestUtils.getRandomCloudSite());
        ManagerTestService.<CloudSite>getAll(manager, sites, false);
    }

    @Test
    public void addCloudSite() {
        ManagerTestService.add(manager, TestUtils.getRandomCloudSite());
    }

    @Test
    public void updateCloudSite() {
        CloudSite cloudSite = TestUtils.getRandomCloudSite();
        CloudSite alteredSite = TestUtils.getRandomCloudSite();
        alteredSite.setId(cloudSite.getId());
        if (cloudSite.getPlatform() == Platform.cloudfoundry)
            alteredSite.setPlatform(Platform.kubernetes);
        else
            alteredSite.setPlatform(Platform.cloudfoundry);
        ManagerTestService.update(manager, cloudSite, alteredSite);
    }

    @Test
    public void removeCloudSite() {
        ManagerTestService.remove(manager, TestUtils.getRandomCloudSite());
    }
}

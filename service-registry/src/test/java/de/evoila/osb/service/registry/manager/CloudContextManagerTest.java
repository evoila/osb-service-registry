package de.evoila.osb.service.registry.manager;

import de.evoila.osb.service.registry.model.CloudContext;
import de.evoila.osb.service.registry.model.CloudSite;
import de.evoila.osb.service.registry.model.Company;
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
public class CloudContextManagerTest {

    @Autowired
    private CloudContextManager manager;

    @Autowired
    private CloudSiteManager siteManager;

    @Autowired
    private CompanyManager companyManager;

    @Before
    @After
    public void dropAll() {
        ManagerTestService.dropAll(manager);
        ManagerTestService.dropAll(siteManager);
        ManagerTestService.dropAll(companyManager);
    }

    @Test
    public void getCloudContext() {
        CloudContext randomContext = TestUtils.getRandomCloudContext(null, null);
        ManagerTestService.<CloudContext>get(manager, randomContext, false);

        CloudSite site = TestUtils.getRandomCloudSite();
        site = siteManager.add(site).get();
        CloudContext randomContextWithSite = TestUtils.getRandomCloudContext(site, null);
        randomContextWithSite = manager.add(randomContextWithSite).get();
        site.getContexts().add(randomContextWithSite);
        site = siteManager.update(site).get();
        ManagerTestService.<CloudContext>get(manager, randomContextWithSite, true);

        Company company = TestUtils.getRandomCompany();
        company = companyManager.add(company).get();
        CloudContext randomContextWithCompany = TestUtils.getRandomCloudContext(null, company);
        randomContextWithCompany = manager.add(randomContextWithCompany).get();
        company.getContexts().add(randomContextWithCompany);
        company = companyManager.update(company).get();
        ManagerTestService.<CloudContext>get(manager, randomContextWithCompany, true);

        CloudContext randomContextWithSiteAndCompany = TestUtils.getRandomCloudContext(site, company);
        randomContextWithSiteAndCompany = manager.add(randomContextWithSiteAndCompany).get();
        site.getContexts().add(randomContextWithSiteAndCompany);
        site = siteManager.update(site).get();
        company.getContexts().add(randomContextWithSiteAndCompany);
        company = companyManager.update(company).get();
        ManagerTestService.<CloudContext>get(manager, randomContextWithSiteAndCompany, true);
    }

    @Test
    public void getCloudContexts() {
        List<CloudContext> contexts = new LinkedList<>();
        contexts.add(TestUtils.getRandomCloudContext(null, null));
        contexts.add(TestUtils.getRandomCloudContext(null, null));
        contexts.add(TestUtils.getRandomCloudContext(null, null));
        ManagerTestService.<CloudContext>getAll(manager, contexts, false);
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

package de.evoila.osb.service.registry.manager;

import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
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

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration
public class ServiceBrokerManagerTest {

    @Autowired
    private ServiceBrokerManager manager;

    @Autowired
    private RegistryServiceInstanceManager instanceManager;

    @Autowired
    private ServiceDefinitionCacheManager cacheManager;

    @Before
    @After
    public void dropAllServiceBrokers() {
        ManagerTestService.dropAll(manager);
    }

    @Test
    public void getServiceBroker() {
        ServiceBroker randomSB = TestUtils.getRandomServiceBroker();
        ManagerTestService.<ServiceBroker>get(manager, randomSB, false);
    }

    @Test
    public void getServiceBrokers() {
        ManagerTestService.<ServiceBroker>getAll(manager, TestUtils.getRandomServiceBroker(), TestUtils.getRandomServiceBroker());
    }

    @Test
    public void addServiceBroker() {
        ManagerTestService.add(manager, TestUtils.getRandomServiceBroker());
    }

    @Test
    public void updateServiceBroker() {
        ServiceBroker serviceBroker = TestUtils.getRandomServiceBroker();
        ServiceBroker alteredSB = TestUtils.getRandomServiceBroker();
        alteredSB.setId(serviceBroker.getId());
        alteredSB.setCloudFoundryAllowed(!serviceBroker.isCloudFoundryAllowed());
        ManagerTestService.update(manager, serviceBroker, alteredSB);
    }

    @Test
    public void removeServiceBroker() {
        ManagerTestService.remove(manager, TestUtils.getRandomServiceBroker());
    }

    @Test
    public void searchServiceBrokerByServiceInstance() throws ResourceNotFoundException {
        ServiceBroker rndSb = TestUtils.getRandomServiceBroker();
        rndSb = manager.add(rndSb).get();
        RegistryServiceInstance serviceInstance = TestUtils.getRandomRegistryServiceInstance();
        serviceInstance.setBroker(rndSb);
        instanceManager.add(serviceInstance);
        rndSb.getServiceInstances().add(serviceInstance);
        manager.update(rndSb);

        ServiceBroker foundServiceBroker = manager.searchForServiceBrokerWithServiceInstanceId(serviceInstance.getId());
        assertTrue("Initial and found service broker are not the same.", rndSb.equals(foundServiceBroker));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void failServiceBrokerByServiceInstanceSearch() throws ResourceNotFoundException {
        ServiceBroker serviceBroker = manager.add(TestUtils.getRandomServiceBroker()).get();
        RegistryServiceInstance serviceInstance = TestUtils.getRandomRegistryServiceInstance();
        serviceInstance.setBroker(serviceBroker);
        instanceManager.add(serviceInstance);
        serviceBroker.getServiceInstances().add(serviceInstance);
        manager.update(serviceBroker);

        ServiceBroker emptyServiceBroker = manager.add(TestUtils.getRandomServiceBroker()).get();

        ServiceBroker foundServiceBroker = manager.searchForServiceBrokerWithServiceInstanceId("nonExistingId");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void failServiceBrokerByServiceDefinitionSearch() throws ResourceNotFoundException {
        ServiceBroker serviceBroker = manager.add(TestUtils.getRandomServiceBroker()).get();

        ServiceDefinition definiton1 = TestUtils.getRandomServiceDefinition();
        ServiceDefinition definiton2 = TestUtils.getRandomServiceDefinition();

        List<ServiceDefinition> definitions = new LinkedList<>();
        definitions.add(definiton1);
        definitions.add(definiton2);

        cacheManager.put(serviceBroker.getId(), definitions);
        manager.searchForServiceBrokerWithServiceDefinitionId("nonExistingDefinitionId");
    }

    @Test
    public void searchServiceBrokerByServiceDefinition() throws ResourceNotFoundException {
        ServiceBroker serviceBroker = manager.add(TestUtils.getRandomServiceBroker()).get();

        ServiceDefinition definiton1 = TestUtils.getRandomServiceDefinition();
        ServiceDefinition definiton2 = TestUtils.getRandomServiceDefinition();

        List<ServiceDefinition> definitions = new LinkedList<>();
        definitions.add(definiton1);
        definitions.add(definiton2);

        cacheManager.put(serviceBroker.getId(), definitions);

        ServiceBroker foundServiceBroker = manager.searchForServiceBrokerWithServiceDefinitionId(definiton1.getId());
        assertTrue("Initial and found service broker are not the same.", serviceBroker.equals(foundServiceBroker));
        foundServiceBroker = manager.searchForServiceBrokerWithServiceDefinitionId(definiton2.getId());
        assertTrue("Initial and found service broker are not the same.", serviceBroker.equals(foundServiceBroker));
    }
}
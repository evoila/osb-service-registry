package de.evoila.osb.service.registry.manager;

import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.osb.service.registry.exceptions.InvalidFieldException;
import de.evoila.osb.service.registry.exceptions.ResourceNotFoundException;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        Iterable<ServiceBroker> serviceBrokers = manager.getAll();
        for (ServiceBroker serviceBroker: serviceBrokers) {
            List<RegistryServiceInstance> instances = serviceBroker.getServiceInstances();
            List<RegistryServiceInstance> toDelete = new LinkedList<>();
            for (RegistryServiceInstance instance : instances) {
                instance.setBroker(null);
                toDelete.add(instance);
            }
            instances.removeAll(toDelete);
            instanceManager.removeMultiple(toDelete);
        }
        ManagerTestService.dropAll(manager);
    }

    @Test
    public void getServiceBroker() {
        ServiceBroker randomSB = TestUtils.getRandomServiceBroker();
        ManagerTestService.<ServiceBroker>get(manager, randomSB, false);
    }

    @Test
    public void getServiceBrokers() {
        List<ServiceBroker> serviceBrokers = new LinkedList<>();
        serviceBrokers.add(TestUtils.getRandomServiceBroker());
        serviceBrokers.add(TestUtils.getRandomServiceBroker());
        serviceBrokers.add(TestUtils.getRandomServiceBroker());
        ManagerTestService.<ServiceBroker>getAll(manager, serviceBrokers, false);
    }

    @Test
    public void getServiceBrokerWithExistenceCheck() throws ResourceNotFoundException, InvalidFieldException {
        ServiceBroker serviceBroker = manager.add(TestUtils.getRandomServiceBroker()).get();

        ServiceBroker foundServiceBroker = manager.getServiceBrokerWithExistenceCheck(serviceBroker.getId());
        assertTrue("Initial and found service broker are not the same.", serviceBroker.equals(foundServiceBroker));

        try {
            manager.getServiceBrokerWithExistenceCheck("###_invalid_id_###");
            fail("Expected an InvalidFieldException when searching with an ID with forbidden special characters");
        } catch (InvalidFieldException ex) {
        }

        try {
            manager.getServiceBrokerWithExistenceCheck("nonExistingId");
            fail("Expected an ResourceNotFoundException when searching with an ID that does not belong to any existing service broker.");
        } catch (ResourceNotFoundException ex) {
        }
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

        // Supposed throw a ResourceNotFoundException
        manager.searchForServiceBrokerWithServiceInstanceId("nonExistingId");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void failServiceBrokerByServiceDefinitionSearch() throws ResourceNotFoundException {
        ServiceBroker serviceBroker = manager.add(TestUtils.getRandomServiceBroker()).get();

        ServiceDefinition definition1 = TestUtils.getRandomServiceDefinition(1);
        ServiceDefinition definition2 = TestUtils.getRandomServiceDefinition(1);

        List<ServiceDefinition> definitions = new LinkedList<>();
        definitions.add(definition1);
        definitions.add(definition2);

        cacheManager.put(serviceBroker.getId(), definitions);

        // Expected to throw a ResourceNotFoundException
        manager.searchForServiceBrokerWithServiceDefinitionId("nonExistingDefinitionId");
    }

    @Test
    public void searchServiceBrokerByServiceDefinition() throws ResourceNotFoundException {
        ServiceBroker serviceBroker = manager.add(TestUtils.getRandomServiceBroker()).get();

        ServiceDefinition definition1 = TestUtils.getRandomServiceDefinition(1);
        ServiceDefinition definition2 = TestUtils.getRandomServiceDefinition(1);

        List<ServiceDefinition> definitions = new LinkedList<>();
        definitions.add(definition1);
        definitions.add(definition2);

        cacheManager.put(serviceBroker.getId(), definitions);

        ServiceBroker foundServiceBroker = manager.searchForServiceBrokerWithServiceDefinitionId(definition1.getId());
        assertTrue("Initial and found service broker are not the same.", serviceBroker.equals(foundServiceBroker));
        foundServiceBroker = manager.searchForServiceBrokerWithServiceDefinitionId(definition2.getId());
        assertTrue("Initial and found service broker are not the same.", serviceBroker.equals(foundServiceBroker));
    }
}
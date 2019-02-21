package de.evoila.osb.service.registry;

import de.evoila.osb.service.registry.manager.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CloudContextManagerTest.class,
        CloudSiteManagerTest.class,
        ServiceBrokerManagerTest.class,
        RegistryBindingManagerTest.class,
        RegistryServiceInstanceManagerTest.class,
        ApplicationContextTest.class,
        ConfigurationTest.class,
        EndToEndTest.class
})
public class AllTests {

}

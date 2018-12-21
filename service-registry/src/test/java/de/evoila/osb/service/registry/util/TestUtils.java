package de.evoila.osb.service.registry.util;

import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.broker.model.catalog.ServiceDefinition;
import de.evoila.cf.broker.model.catalog.plan.Plan;
import de.evoila.osb.service.registry.model.CloudContext;
import de.evoila.osb.service.registry.model.CloudSite;
import de.evoila.osb.service.registry.model.Company;
import de.evoila.osb.service.registry.model.service.broker.ServiceBroker;
import de.evoila.osb.service.registry.model.Platform;
import de.evoila.osb.service.registry.model.service.broker.RegistryBinding;
import de.evoila.osb.service.registry.model.service.broker.RegistryServiceInstance;

import java.util.*;

public class TestUtils {

    private static Random random = new Random();

    public static CloudContext getRandomCloudContext() {
        return new CloudContext(getRandomUUID(), getRandomUUID(), getRandomUUID(), getRandomUUID(), getRandomBasicAuthToken());
    }

    public static CloudContext getRandomCloudContext(CloudSite site, Company company) {
        return new CloudContext(getRandomUUID(), getRandomUUID(), getRandomUUID(), getRandomUUID(), getRandomBasicAuthToken(), site, company);
    }

    public static CloudSite getRandomCloudSite() {
        return new CloudSite(getRandomUUID(), Platform.cloudfoundry, "127.0.0.1");
    }

    public static CloudSite getRandomCloudSite(List<CloudContext> contexts, List<ServiceBroker> serviceBrokers) {
        return new CloudSite(getRandomUUID(), Platform.cloudfoundry, "127.0.0.1", contexts, serviceBrokers);
    }

    public static ServiceBroker getRandomServiceBroker() {
        return new ServiceBroker(getRandomUUID(), "127.0.0.1", 8080, getRandomBasicAuthToken(), "2.14", "random Description #" + random.nextInt(), random.nextBoolean(), random.nextBoolean());
    }

    public static ServiceInstance getRandomServiceInstance() {
        return new ServiceInstance("test-id", "test-service-definition", "test-plan-id", "test-org-od", "test-space-id", new HashMap<String, Object>(), "http://dashboard.url");
    }

    public static ServiceDefinition getRandomServiceDefinition() {
        List<Plan> plans = new LinkedList<>();
        plans.add(getRandomPlan());
        return new ServiceDefinition("test-definition-id", "test-definition-name", "test definition description", true, plans, true);
    }

    public static Plan getRandomPlan() {
        return new Plan("test-plan-id", "test-plan-name", "test plan description", de.evoila.cf.broker.model.Platform.EXISTING_SERVICE, false);
    }

    public static RegistryServiceInstance getRandomRegistryServiceInstance() {
        return new RegistryServiceInstance(getRandomUUID(), getRandomUUID(), getRandomUUID(), "test-org-id", "test-space-id", "test-name-space", false, false, true, "", null, new LinkedList<>(), null);
    }

    public static RegistryBinding getRandomRegistryBinding() {
        return new RegistryBinding(getRandomUUID(), false, false, null);
    }

    public static String getRandomBasicAuthToken() {
        return "Basic " + getRandomLowercaseString(24);
    }

    public static String getRandomLowercaseString(int length) {
        char[] arr = new char[length];
        for (int i = 0; i < arr.length; i++)
            arr[i] = (char) ((int) (Math.random() * 26 + 97));
        return String.valueOf(arr);
    }

    public static String getRandomUUID() {
        return UUID.randomUUID().toString();
    }
}

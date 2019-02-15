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
    private static int planSerialNumber = 0;
    private static int definitionSerialNumber = 0;
    private static int organizationSerialNumber = 0;
    private static int spaceSerialNumber = 0;
    private static int companySerialNumber = 0;
    private static int siteSerialNumber = 0;

    public static CloudContext getRandomCloudContext() {
        return new CloudContext(getRandomUUID(), getRandomUUID(), getRandomUUID(), getRandomUUID(), getRandomBasicAuthToken());
    }

    public static CloudContext getRandomCloudContext(CloudSite site, Company company) {
        return new CloudContext(getRandomUUID(), getRandomUUID(), getRandomUUID(), getRandomUUID(), getRandomBasicAuthToken(), site, company);
    }

    public static CloudSite getRandomCloudSite() {
        return new CloudSite(getRandomUUID(), Platform.cloudfoundry, "127.0.0.1", "test-cloud-site-"+getNextSiteSerialNumber());
    }

    public static CloudSite getRandomCloudSite(List<CloudContext> contexts, List<ServiceBroker> serviceBrokers) {
        return new CloudSite(getRandomUUID(), Platform.cloudfoundry, "127.0.0.1", "test-cloud-site-"+getNextSiteSerialNumber(), contexts, serviceBrokers);
    }

    public static ServiceBroker getRandomServiceBroker() {
        return new ServiceBroker(getRandomUUID(), "127.0.0.1", 8080, "test-salt", getRandomBasicAuthToken(), "2.14", "random Description #" + random.nextInt(), random.nextBoolean(), random.nextBoolean());
    }

    public static Company getRandomCompany() {
        return new Company(getRandomUUID(), "test-company-"+getNextCompanySerialNumber(), getRandomBasicAuthToken());
    }

    public static ServiceInstance getRandomServiceInstance() {
        ServiceDefinition definition = getRandomServiceDefinition(1);
        Plan plan = definition.getPlans().get(0);
        return new ServiceInstance(getRandomUUID(), "test-service-definition-"+getNextDefinitionSerialNumber(), plan.getName(),
                "test-org-id-"+getNextOrganizationSerialNumber(),
                "test-space-id-"+getNextSpaceSerialNumber(),
                new HashMap<String, Object>(), "http://dashboard.url");
    }

    public static ServiceDefinition getRandomServiceDefinition(int numberOfPlans) {
        List<Plan> plans = new LinkedList<>();
        for (int i = 0; i < numberOfPlans; i++)
            plans.add(getRandomPlan());
        return new ServiceDefinition(getRandomUUID(), "test-definition-name-"+getNextDefinitionSerialNumber(), "test definition description", true, plans, true);
    }

    public static Plan getRandomPlan() {
        return new Plan(getRandomUUID(), "test-plan-name-"+getNextPlanSerialNumber(), "test plan description", de.evoila.cf.broker.model.Platform.EXISTING_SERVICE, false);
    }

    public static RegistryServiceInstance getRandomRegistryServiceInstance() {
        return new RegistryServiceInstance(getRandomUUID(), getRandomUUID(), getRandomUUID(), "test-org-id", "test-space-id", "test-name-space", false, false, true, "", null, new LinkedList<>(), null);
    }

    public static RegistryBinding getRandomRegistryBinding() {
        return new RegistryBinding(getRandomUUID(), false, false, null);
    }

    public static String getRandomBasicAuthToken() {
        return getRandomLowercaseString(24);
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

    public static int getNextPlanSerialNumber() { return ++planSerialNumber; }
    public static int getNextDefinitionSerialNumber() { return ++definitionSerialNumber; }
    public static int getNextOrganizationSerialNumber() { return ++organizationSerialNumber; }
    public static int getNextSpaceSerialNumber() { return ++spaceSerialNumber; }
    public static int getNextCompanySerialNumber() { return ++companySerialNumber; }
    public static int getNextSiteSerialNumber() { return ++siteSerialNumber; }

}

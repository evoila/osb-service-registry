package de.evoila.osb.service.registry.model.service.broker.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;

public class ServiceInstanceUpdateRequest {

    @JsonSerialize
    @JsonProperty("context")
    private Map<String, String> context = new HashMap<>();

    @NotEmpty
    @JsonSerialize
    @JsonProperty("service_id")
    private String serviceDefinitionId;

    @JsonSerialize
    @JsonProperty("plan_id")
    private String planId;

    @JsonSerialize
    @JsonProperty("parameters")
    private Map<String, Object> parameters = new HashMap<>();

    @JsonSerialize
    @JsonProperty("previous_values")
    private ServiceInstanceUpdatePreviousValues previousValues;

    public ServiceInstanceUpdateRequest() {
        this(new HashMap<>(), "","", new HashMap<>(), new ServiceInstanceUpdatePreviousValues());
    }

    public ServiceInstanceUpdateRequest(Map<String, String> context, @NotEmpty String serviceDefinitionId, @NotEmpty String planId, Map<String, Object> parameters, ServiceInstanceUpdatePreviousValues previousValues) {
        this.context = context;
        this.serviceDefinitionId = serviceDefinitionId;
        this.planId = planId;
        this.parameters = parameters;
        this.previousValues = previousValues;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public String getServiceDefinitionId() {
        return serviceDefinitionId;
    }

    public void setServiceDefinitionId(String serviceDefinitionId) {
        this.serviceDefinitionId = serviceDefinitionId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public ServiceInstanceUpdatePreviousValues getPreviousValues() {
        return previousValues;
    }

    public void setPreviousValues(ServiceInstanceUpdatePreviousValues previousValues) {
        this.previousValues = previousValues;
    }
}

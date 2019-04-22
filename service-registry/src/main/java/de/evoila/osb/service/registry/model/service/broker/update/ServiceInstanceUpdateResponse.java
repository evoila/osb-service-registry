package de.evoila.osb.service.registry.model.service.broker.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class ServiceInstanceUpdateResponse {

    @JsonSerialize
    @JsonProperty("dashboard_url")
    private String dashboardUrl;

    @JsonSerialize
    @JsonProperty("operation")
    private String operation;

    public ServiceInstanceUpdateResponse() {
        this("","");
    }

    public ServiceInstanceUpdateResponse(String dashboardUrl, String operation) {
        this.dashboardUrl = dashboardUrl;
        this.operation = operation;
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}

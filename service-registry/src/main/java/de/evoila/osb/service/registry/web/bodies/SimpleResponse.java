package de.evoila.osb.service.registry.web.bodies;

public class SimpleResponse {

    private String message;

    public SimpleResponse() {
        message = "";
    }

    public SimpleResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? "" : message;
    }
}

package de.evoila.osb.service.registry.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends Exception {

    private HttpStatus status;

    public ResourceNotFoundException(String resourceType) {
        this(resourceType, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceType, HttpStatus customStatus) {
        super("Can not find a matching " + resourceType + ".");
        status = customStatus;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}

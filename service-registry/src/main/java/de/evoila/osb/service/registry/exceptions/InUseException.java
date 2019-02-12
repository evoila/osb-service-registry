package de.evoila.osb.service.registry.exceptions;

public class InUseException extends RuntimeException {

    public InUseException(String message) {
        super(message);
    }
}

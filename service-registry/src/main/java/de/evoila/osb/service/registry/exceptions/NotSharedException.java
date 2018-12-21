package de.evoila.osb.service.registry.exceptions;

public class NotSharedException extends Exception {

    public NotSharedException() {
        super("This service instance is not shared!");
    }

    public NotSharedException(String message) {
        super(message);
    }
}

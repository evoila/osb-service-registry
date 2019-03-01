package de.evoila.osb.service.registry.exceptions;

public class ParseLinkException extends Exception {

    public ParseLinkException(String message) {
        super(message);
    }

    public ParseLinkException(String message, IndexOutOfBoundsException cause) {
        super(message, cause);
    }
}

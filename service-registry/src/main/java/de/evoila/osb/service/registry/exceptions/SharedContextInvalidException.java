package de.evoila.osb.service.registry.exceptions;

public class SharedContextInvalidException extends Exception {

    public static final String FIELD_IS_NULL = "Instance is shared but an essential field is null:";
    public static final String FIELD_IS_EMPTY = "Instance is shared but an essential field is empty:";

    public SharedContextInvalidException() {
        super("SharedContext is null.");
    }

    public SharedContextInvalidException(String field, String errorCase) {
        super(errorCase + " " + field);
    }
}

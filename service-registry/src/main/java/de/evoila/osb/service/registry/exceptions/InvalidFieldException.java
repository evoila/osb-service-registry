package de.evoila.osb.service.registry.exceptions;

public class InvalidFieldException extends Exception {

    private String fieldName;

    public InvalidFieldException(String fieldName) {
        super(fieldName + " is not existing, empty or malformed.");
        this.fieldName = fieldName;
    }

    public String getFieldName() { return fieldName; }

    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
}

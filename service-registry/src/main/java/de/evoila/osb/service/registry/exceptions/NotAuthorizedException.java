package de.evoila.osb.service.registry.exceptions;

public class NotAuthorizedException extends Exception {

    public NotAuthorizedException(String username, String authority) {
        super("User '"+username+"' does not hold the '"+authority+"' authority.");
    }

    public NotAuthorizedException(String username) {
        super("User '"+username+"' is not authorized to access the requested object.");
    }
}

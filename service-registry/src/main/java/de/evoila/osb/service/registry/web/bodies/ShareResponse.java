package de.evoila.osb.service.registry.web.bodies;

public class ShareResponse {

    private boolean shared;
    private String displayName;

    public ShareResponse() {
        this(false, "");
    }

    public ShareResponse(boolean shared, String displayName) {
        this.shared = shared;
        this.displayName = displayName;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

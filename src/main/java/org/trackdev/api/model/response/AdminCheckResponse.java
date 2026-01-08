package org.trackdev.api.model.response;

/**
 * DTO for admin check response
 */
public class AdminCheckResponse {
    private boolean isAdmin;

    public AdminCheckResponse() {}

    public AdminCheckResponse(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}

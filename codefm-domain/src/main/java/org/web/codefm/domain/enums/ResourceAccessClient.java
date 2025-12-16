package org.web.codefm.domain.enums;

public enum ResourceAccessClient {
    CODEFM("codefm"),
    TEACHER_NOTEBOOK("teacher_notebook");

    private final String clientId;

    ResourceAccessClient(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}

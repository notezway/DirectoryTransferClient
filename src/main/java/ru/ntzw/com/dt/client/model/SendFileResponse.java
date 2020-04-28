package ru.ntzw.com.dt.client.model;

public class SendFileResponse {

    private boolean allow;

    public SendFileResponse(boolean allow) {
        this.allow = allow;
    }

    public boolean isAllow() {
        return allow;
    }
}

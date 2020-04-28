package ru.ntzw.com.dt.client.model;

public class SendFileResult {

    private boolean success;
    private String message;

    public SendFileResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "SendFileResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}

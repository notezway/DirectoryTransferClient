package ru.ntzw.com.dt.client.model;

import java.util.List;

public class SendFileData {

    private String fullname;
    private List<String> emails;
    private String directoryPath;

    public SendFileData(String fullname, List<String> emails, String directoryPath) {
        this.fullname = fullname;
        this.emails = emails;
        this.directoryPath = directoryPath;
    }

    public String getFullname() {
        return fullname;
    }

    public List<String> getEmails() {
        return emails;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }
}

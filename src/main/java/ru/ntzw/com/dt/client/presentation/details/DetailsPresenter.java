package ru.ntzw.com.dt.client.presentation.details;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class DetailsPresenter {

    public Label fullnameLabel;
    public TextArea emailsTextArea;
    public Label statusLabel;
    public TextArea detailsTextArea;

    public void setFullname(String fullname) {
        fullnameLabel.setText(fullname);
    }

    public void setEmails(String emails) {
        emailsTextArea.setText(emails);
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void setDetails(String details) {
        detailsTextArea.setText(details);
    }
}

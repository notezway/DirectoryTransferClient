package ru.ntzw.com.dt.client.presentation.input;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import ru.ntzw.com.dt.client.model.SendFileData;
import ru.ntzw.com.dt.client.presentation.UserInterface;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class InputPresenter {

    @Inject
    private UserInterface userInterface;

    public TextField fullnameTextField;
    public TextArea emailsTextArea;

    private String directoryPath;
    private Consumer<SendFileData> sendFileDataConsumer;

    private Pattern fullnamePattern = Pattern.compile("[a-zA-Zа-яА-ЯёЁ ]+");
    private Pattern emailPattern = Pattern.compile("^[a-zA-Zа-яА-ЯёЁ0-9_-]+(?:\\.[a-zA-Zа-яА-ЯёЁ0-9_-]+)*@[a-zA-Zа-яА-ЯёЁ0-9-]+(?:\\.[a-zA-Zа-яА-ЯёЁ0-9-]+)*$");

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public void setSendFileDataConsumer(Consumer<SendFileData> sendFileDataConsumer) {
        this.sendFileDataConsumer = sendFileDataConsumer;
    }

    public void onSendButtonAction(ActionEvent event) {
        String fullname = fullnameTextField.getText();
        ArrayList<String> emails = new ArrayList<>(Arrays.asList(emailsTextArea.getText().split("\\s+")));
        ArrayList<String> filteredEmails = new ArrayList<>(emails.size());
        if(fullname.isEmpty()) {
            warning("Введите ФИО в соответствующее поле.");
            return;
        }
        if(!fullnamePattern.matcher(fullname).matches()) {
            warning("Некорректное содержимое поля \"ФИО\".\nРазрешены только латинские и кириллические буквы.");
            return;
        }
        int emailCount = 0;
        for(String email : emails) {
            String trimmedEmail = email.trim();
            if(!trimmedEmail.isEmpty()) {
                emailCount++;
                if (!emailPattern.matcher(trimmedEmail).matches()) {
                    warning("Некорректный адрес: " + trimmedEmail);
                    return;
                } else {
                    filteredEmails.add(trimmedEmail);
                }
            }
        }
        if(emailCount == 0) {
            warning("Введите хотя бы один электронный адрес.");
            return;
        }
        Platform.runLater(() -> sendFileDataConsumer.accept(new SendFileData(fullname, filteredEmails, directoryPath)));
        userInterface.getInputStage().close();
    }

    private void warning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(userInterface.getInputStage());
        alert.setTitle("Ошибка");
        alert.showAndWait();
    }
}

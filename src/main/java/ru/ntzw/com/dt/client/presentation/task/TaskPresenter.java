package ru.ntzw.com.dt.client.presentation.task;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import ru.ntzw.com.dt.client.model.SendTask;
import ru.ntzw.com.dt.client.model.ServiceProvider;
import ru.ntzw.com.dt.client.model.TaskState;
import ru.ntzw.com.dt.client.presentation.UserInterface;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

public class TaskPresenter {

    @Inject
    private UserInterface userInterface;
    @Inject
    private ServiceProvider serviceProvider;

    private Runnable cancelCallback;
    private final ObjectProperty<TaskState> sendState = new SimpleObjectProperty<>(this, "sendState", TaskState.WAITING);
    private final ObjectProperty<Throwable> exception = new SimpleObjectProperty<>(this, "exception");
    private final StringProperty path = new SimpleStringProperty(this, "path", "");

    private static final HashMap<Pair<TaskState, Boolean>, String> glyphMap = new HashMap<>();
    static {
        glyphMap.put(new Pair<>(TaskState.WAITING,  false), "CLOCK_ALT");
        glyphMap.put(new Pair<>(TaskState.WAITING,  true ), "CLOCK_ALT");
        glyphMap.put(new Pair<>(TaskState.PACKING,  false), "FILE_ARCHIVE_ALT");
        glyphMap.put(new Pair<>(TaskState.PACKING,  true ), "REMOVE");
        glyphMap.put(new Pair<>(TaskState.SENDING,  false), "UPLOAD");
        glyphMap.put(new Pair<>(TaskState.SENDING,  true ), "REMOVE");
        glyphMap.put(new Pair<>(TaskState.COMPLETE, false), "CHECK_CIRCLE_ALT");
        glyphMap.put(new Pair<>(TaskState.COMPLETE, true ), "INFO_CIRCLE");
        glyphMap.put(new Pair<>(TaskState.FAILED,   false), "EXCLAMATION_TRIANGLE");
        glyphMap.put(new Pair<>(TaskState.FAILED,   true ), "INFO_CIRCLE");
        glyphMap.put(new Pair<>(TaskState.CANCELED, false), "USER_TIMES");
        glyphMap.put(new Pair<>(TaskState.CANCELED, true ), "INFO_CIRCLE");
    }

    public FontAwesomeIconView iconView;
    public HBox iconHitBox;
    public Label fullnameLabel;
    public Label emailsLabel;
    public Label statusLabel;
    public ProgressBar progressBar;

    @FXML
    public void initialize() {
        sendState.addListener((observable, oldValue, newValue) -> updateGlyph(newValue));
        exception.addListener((observable, oldValue, newValue) -> {
            if(sendState.get() == TaskState.FAILED)
                openDetails(newValue);
        });
    }

    public void onIconClicked(MouseEvent event) {
        if(event.getButton()== MouseButton.PRIMARY && event.getClickCount() == 1) {
            TaskState state = sendState.get();
            if(state == TaskState.COMPLETE || state == TaskState.FAILED || state == TaskState.CANCELED) {
                openDetails(exception.get());
            } else {
                cancelCallback.run();
            }
        }
    }

    public void onIconEntered(MouseEvent event) {
        updateGlyph(sendState.get());
    }

    public void onIconExited(MouseEvent event) {
        updateGlyph(sendState.get());
    }

    public void setTask(SendTask task) {
        fullnameLabel.textProperty().bind(task.titleProperty());
        emailsLabel.textProperty().bind(task.messageProperty());
        statusLabel.textProperty().bind(task.statusProperty());
        progressBar.progressProperty().bind(task.progressProperty());
        sendState.bind(task.sendStateProperty());
        exception.bind(task.exceptionProperty());
        path.bind(task.pathProperty());
        cancelCallback = task::cancel;
    }

    private void updateGlyph(TaskState state) {
        iconView.setGlyphName(glyphMap.get(new Pair<>(state, iconHitBox.isHover())));
    }

    private void openDetails(Throwable e) {
        String details = "Отправка успешно завершена.";
        if(e != null) {
            details = getExceptionDetails(e);

            String msg = String.format("Failed to complete task:\nFullName: %s\nEmails: %s\nFilePath: %s",
                    fullnameLabel.getText(),
                    emailsLabel.getText().replaceAll("\\s+", "; "),
                    path.get()
            );
            serviceProvider.getLoggingService().error(msg + "\n" + details, null);
        }

        String finalDetails = details;
        Platform.runLater(() -> userInterface.newDetailsStage(
                fullnameLabel.getText(),
                emailsLabel.getText(),
                statusLabel.getText(),
                finalDetails
        ));
    }

    private String getExceptionDetails(Throwable throwable) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        throwable.printStackTrace(printStream);
        return byteArrayOutputStream.toString();
    }
}

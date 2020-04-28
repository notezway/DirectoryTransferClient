package ru.ntzw.com.dt.client.presentation;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.ntzw.com.dt.client.model.SendFileData;
import ru.ntzw.com.dt.client.model.SendTask;
import ru.ntzw.com.dt.client.presentation.details.DetailsPresenter;
import ru.ntzw.com.dt.client.presentation.details.DetailsView;
import ru.ntzw.com.dt.client.presentation.input.InputPresenter;
import ru.ntzw.com.dt.client.presentation.input.InputView;
import ru.ntzw.com.dt.client.presentation.task.TaskPresenter;
import ru.ntzw.com.dt.client.presentation.task.TaskView;
import ru.ntzw.com.dt.client.presentation.tasklist.TaskListPresenter;
import ru.ntzw.com.dt.client.presentation.tasklist.TaskListView;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UserInterface {

    private URL logo16url = getClass().getResource("/picasso_logo_base_16.png");
    private URL logo32url = getClass().getResource("/picasso_logo_base_32.png");
    private URL logo64url = getClass().getResource("/picasso_logo_base_64.png");

    private Stage tasksStage;
    private Parent tasksView;
    private TaskListPresenter taskListPresenter;

    private Stage inputStage;
    private Parent inputView;
    private InputPresenter inputPresenter;

    private Stage detailsStage;
    private Parent detailsView;
    private DetailsPresenter detailsPresenter;

    private Map<SendTask, TaskView> taskViewMap;

    public void init(Stage primaryStage) {
        tasksStage = primaryStage;

        {
            TaskListView container = new TaskListView();
            tasksView = container.getView();
            taskListPresenter = (TaskListPresenter) container.getPresenter();

            Scene scene = new Scene(tasksView);

            tasksStage.setScene(scene);
            tasksStage.setResizable(false);
            tasksStage.initStyle(StageStyle.UNDECORATED);
            tasksStage.setAlwaysOnTop(true);
            tasksStage.show();
        }

        taskViewMap = new HashMap<>();
    }

    public Parent newTaskView(SendTask task) {
        TaskView container = new TaskView();
        ((TaskPresenter)container.getPresenter()).setTask(task);
        taskViewMap.put(task, container);
        return container.getView();
    }

    public Parent getTaskView(SendTask task) {
        return taskViewMap.get(task).getView();
    }

    public void newInputStage(String directoryPath, Consumer<SendFileData> sendFileDataConsumer) {
        inputStage = new Stage();
        InputView container = new InputView();
        inputView = container.getView();
        inputPresenter = (InputPresenter) container.getPresenter();
        inputPresenter.setDirectoryPath(directoryPath);
        inputPresenter.setSendFileDataConsumer(sendFileDataConsumer);
        Scene scene = new Scene(inputView);
        inputStage.setScene(scene);
        inputStage.setResizable(false);
        showAsModal(inputStage, "Отправить " + directoryPath);
    }

    public void newDetailsStage(String fullname, String emails, String status, String details) {
        if(detailsStage != null)
            detailsStage.close();
        detailsStage = new Stage();
        DetailsView container = new DetailsView();
        detailsView = container.getView();
        detailsPresenter = (DetailsPresenter) container.getPresenter();
        detailsPresenter.setFullname(fullname);
        detailsPresenter.setEmails(emails);
        detailsPresenter.setStatus(status);
        detailsPresenter.setDetails(details);
        Scene scene = new Scene(detailsView);
        detailsStage.setScene(scene);
        detailsStage.setResizable(false);
        detailsStage.setOnHiding(event -> detailsStage = null);
        showAsModal(detailsStage, "Детали задачи");
    }

    private void showAsModal(Stage stage, String title) {
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(tasksStage);
        stage.setTitle(title);
        try {
            stage.getIcons().addAll(
                    new Image(getLogo16url().openStream()),
                    new Image(getLogo32url().openStream()),
                    new Image(getLogo64url().openStream())
            );
        } catch (IOException e) {
            System.err.println("Failed to add icons");
            e.printStackTrace();
        }
        stage.show();
        stage.requestFocus();
    }

    public URL getLogo16url() {
        return logo16url;
    }

    public URL getLogo32url() {
        return logo32url;
    }

    public URL getLogo64url() {
        return logo64url;
    }

    public Stage getTasksStage() {
        return tasksStage;
    }

    public Parent getTasksView() {
        return tasksView;
    }

    public TaskListPresenter getTaskListPresenter() {
        return taskListPresenter;
    }

    public Stage getInputStage() {
        return inputStage;
    }

    public Parent getInputView() {
        return inputView;
    }

    public InputPresenter getInputPresenter() {
        return inputPresenter;
    }

    public Stage getDetailsStage() {
        return detailsStage;
    }

    public Parent getDetailsView() {
        return detailsView;
    }

    public DetailsPresenter getDetailsPresenter() {
        return detailsPresenter;
    }
}

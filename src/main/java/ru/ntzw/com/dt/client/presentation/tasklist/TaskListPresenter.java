package ru.ntzw.com.dt.client.presentation.tasklist;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ru.ntzw.com.dt.client.model.SendTask;
import ru.ntzw.com.dt.client.model.ServiceProvider;
import ru.ntzw.com.dt.client.model.properties.PropertiesService;
import ru.ntzw.com.dt.client.presentation.UserInterface;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class TaskListPresenter {

    @Inject
    private UserInterface userInterface;
    @Inject
    private ServiceProvider serviceProvider;

    private Runnable clearTasksCallback;

    public VBox vBox;
    public VBox taskListVBox;

    @FXML
    private void initialize() {
        setInitialPosition();
        createTrayIcon(userInterface.getTasksStage());
    }

    public void minimize(ActionEvent actionEvent) {
        userInterface.getTasksStage().hide();
    }

    public void setTaskList(ObservableList<SendTask> taskList) {
        taskList.addListener((ListChangeListener<SendTask>) c -> {
            while(c.next()) {
                for (SendTask task : c.getRemoved()) {
                    taskListVBox.getChildren().remove(userInterface.getTaskView(task));
                }
                for (SendTask task : c.getAddedSubList()) {
                    taskListVBox.getChildren().add(0, userInterface.newTaskView(task));
                }
                taskListVBox.autosize();
            }
        });
    }

    public void setClearTasksCallback(Runnable clearTasksCallback) {
        this.clearTasksCallback = clearTasksCallback;
    }

    private void setInitialPosition() {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        Stage stage = userInterface.getTasksStage();
        PropertiesService propertiesService = serviceProvider.getPropertiesService();
        stage.setX(primaryScreenBounds.getMaxX() - vBox.getPrefWidth() - propertiesService.getInteger("taskListXOffset", 0));
        stage.setY(primaryScreenBounds.getMaxY() - propertiesService.getInteger("taskListYOffset", 600));
    }

    private void createTrayIcon(Stage stage) {
        if(SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            try {
                userInterface.getTasksStage().getIcons().addAll(
                        new Image(userInterface.getLogo16url().openStream()),
                        new Image(userInterface.getLogo32url().openStream()),
                        new Image(userInterface.getLogo64url().openStream())
                );
            } catch(Exception e) {
                e.printStackTrace();
            }

            stage.setOnCloseRequest(t -> Platform.runLater(stage::hide));
            ActionListener showListener = e -> Platform.runLater(stage::show);
            ActionListener hideListener = e -> Platform.runLater(stage::hide);
            ActionListener clearListener = e -> Platform.runLater(clearTasksCallback);
            ActionListener closeListener = e -> Platform.exit();

            PopupMenu popup = new PopupMenu();

            MenuItem showItem = new MenuItem("Показать окно");
            showItem.addActionListener(showListener);
            popup.add(showItem);

            MenuItem hideItem = new MenuItem("Скрыть окно");
            hideItem.addActionListener(hideListener);
            popup.add(hideItem);

            /*MenuItem clearItem = new MenuItem("Очистить список");
            clearItem.addActionListener(clearListener);
            popup.add(clearItem);*/

            MenuItem closeItem = new MenuItem("Завершить работу");
            closeItem.addActionListener(closeListener);
            popup.add(closeItem);

            java.awt.Image icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            try {
                icon = ImageIO.read(userInterface.getLogo16url());
            } catch(IOException e) {
                e.printStackTrace();
            }
            TrayIcon trayIcon = new TrayIcon(icon, "Отправка файлов", popup);
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1)
                        showListener.actionPerformed(null);
                }
            });
            try {
                tray.add(trayIcon);
            } catch(AWTException e) {
                e.printStackTrace();
            }
        }
    }
}

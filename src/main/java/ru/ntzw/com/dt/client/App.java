package ru.ntzw.com.dt.client;

import com.airhacks.afterburner.injection.Injector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ru.ntzw.com.dt.client.model.SendFileData;
import ru.ntzw.com.dt.client.model.SendTask;
import ru.ntzw.com.dt.client.model.ServiceProvider;
import ru.ntzw.com.dt.client.model.logging.DailyRotatingLoggingService;
import ru.ntzw.com.dt.client.model.properties.SimplePropertiesService;
import ru.ntzw.com.dt.client.model.singleinstance.SingleInstanceService;
import ru.ntzw.com.dt.client.model.singleinstance.SocketSingleInstanceService;
import ru.ntzw.com.dt.client.model.task.SimpleTaskService;
import ru.ntzw.com.dt.client.presentation.UserInterface;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends Application {

    private List<Initializable> initializables = new ArrayList<>();
    private List<Disposable> disposables = new ArrayList<>();
    private ServiceProvider serviceProvider;
    private UserInterface userInterface;

    @Override
    public void start(Stage primaryStage) {
        try {
            createServices();
            initServices();
            if(!doSingleInstanceCheck()) {
                return;
            }
            Platform.setImplicitExit(false);
            createUI();
            initInjections();
            initUI(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createServices() {
        serviceProvider = new ServiceProvider();

        SimplePropertiesService propertiesService = new SimplePropertiesService(Paths.get("properties.cfg"));
        serviceProvider.setPropertiesService(propertiesService);
        initializables.add(propertiesService);
        disposables.add(propertiesService);

        SocketSingleInstanceService singleInstanceService = new SocketSingleInstanceService();
        serviceProvider.setSingleInstanceService(singleInstanceService);
        initializables.add(singleInstanceService);
        disposables.add(singleInstanceService);

        DailyRotatingLoggingService loggingService = new DailyRotatingLoggingService();
        serviceProvider.setLoggingService(loggingService);
        initializables.add(loggingService);
        disposables.add(loggingService);

        SimpleTaskService<SendTask> taskService = new SimpleTaskService<>();
        serviceProvider.setTaskService(taskService);
        initializables.add(taskService);
        disposables.add(taskService);
    }

    private void initServices() {
        for(Initializable initializable : initializables) {
            try {
                initializable.init(serviceProvider);
            } catch(Exception e) {
                e.printStackTrace();
                try {
                    serviceProvider.getLoggingService().error("Error occurred while service initialization", e);
                } catch (Exception e1) {
                    //ignored
                }
            }
        }
        serviceProvider.getLoggingService().info("Services initialized without errors");
    }

    private boolean doSingleInstanceCheck() {
        SingleInstanceService service = serviceProvider.getSingleInstanceService();
        List<String> unnamedArgs = getParameters().getUnnamed();
        String arg = unnamedArgs.isEmpty() ? null : unnamedArgs.get(0);
        if(!pathIsValid(arg)) return false;
        if(service.isSingle()) {
            onNewPath(arg);
            service.setStringFromOthersCallback(this::onNewPath);
        } else {
            try {
                service.sendStringToSingle(arg);
            } catch (Exception e) {
                e.printStackTrace();
                serviceProvider.getLoggingService().error("Error occurred while sending string to single instance", e);
            }
            System.exit(0);
        }
        return true;
    }

    private boolean pathIsValid(String path) {
        try {
            if (!Files.isDirectory(Paths.get(path, "Data")))
                throw new RuntimeException();
            return true;
        } catch (Exception e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING, "В указанной директории отсутствует папка \"Data\"");
                alert.setTitle("Неверный путь");
                alert.showAndWait();
                System.exit(0);
            });
            return false;
        }
    }

    private void onNewPath(String path) {
        try {
            if (Files.isDirectory(Paths.get(path))) {
                Platform.runLater(() -> userInterface.newInputStage(path, this::onNewTask));
            }
        } catch (InvalidPathException e) {
            serviceProvider.getLoggingService().error("Invalid path to directory: " + path, e);
        } catch (NullPointerException e) {
            //
        }
    }

    private void onNewTask(SendFileData fileData) {
        SendTask sendTask = new SendTask(
                fileData.getDirectoryPath(),
                serviceProvider.getPropertiesService().getString("serverHostName", "localhost"),
                serviceProvider.getPropertiesService().getInteger("serverPort", 37512),
                fileData.getFullname(),
                fileData.getEmails(),
                serviceProvider
        );
        serviceProvider.getTaskService().submit(sendTask);
    }

    private void clearTasks() {
        serviceProvider.getTaskService().getTaskList().removeIf(task -> !task.isRunning());
    }

    private void createUI() {
        userInterface = new UserInterface();
    }

    private void initInjections() {
        Map<Object, Object> injectSource = new HashMap<>();
        injectSource.put("serviceProvider", serviceProvider);
        injectSource.put("userInterface", userInterface);
        Injector.setConfigurationSource(injectSource::get);
    }

    private void initUI(Stage primaryStage) {
        userInterface.init(primaryStage);
        userInterface.getTaskListPresenter().setTaskList(serviceProvider.getTaskService().getTaskList());
        userInterface.getTaskListPresenter().setClearTasksCallback(this::clearTasks);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        for(Disposable disposable : disposables) {
            disposable.dispose();
        }
        System.exit(0);
    }
}

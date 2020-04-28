package ru.ntzw.com.dt.client.model;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SendTask extends Task<SendFileResult> {

    private final String path;
    private final InetSocketAddress address;
    private final String fullname;
    private final List<String> emails;
    private String emailsInOneString;
    private final ServiceProvider serviceProvider;
    private final StringProperty status = new SimpleStringProperty(this, "status", "");
    private final AtomicReference<String> statusUpdate = new AtomicReference<>();
    private final SimpleObjectProperty<TaskState> sendState = new SimpleObjectProperty<>(this, "sendState", TaskState.WAITING);
    private final AtomicReference<TaskState> sendStateUpdate = new AtomicReference<>();
    private final StringProperty pathProperty = new SimpleStringProperty(this, "pathProperty", "");
    private final AtomicReference<String> pathPropertyUpdate = new AtomicReference<>();

    private File archive;

    private static final String STATUS_PACKING = "Упаковка";
    private static final String STATUS_SENDING = "Отправка";
    private static final String STATUS_CANCELED = "Прервано";
    private static final String STATUS_FAILED = "Ошибка";
    private static final String STATUS_DONE = "Отправлено";

    public SendTask(String path, String host, int port, String fullname, List<String> emails, ServiceProvider serviceProvider) {
        this.path = path;
        this.address = new InetSocketAddress(host, port);
        this.fullname = fullname;
        this.emails = emails;
        this.emailsInOneString = "";
        for(String mail : emails) {
            this.emailsInOneString += mail + "\n";
        }
        this.emailsInOneString = this.emailsInOneString.substring(0, this.emailsInOneString.length() - 1);
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected SendFileResult call() throws Exception {
        updateTitle(fullname);
        updateMessage(emailsInOneString);
        updatePath(path);
        try {
            if (archive == null) {
                archive = compress();
            }
            SendFileResult result = send(archive);
            if (result.isSuccess()) {
                updateSendState(TaskState.COMPLETE);
                updateStatus(STATUS_DONE);
            }
            else {
                throw new RuntimeException(result.getMessage());
            }
            return result;
        } catch (RuntimeException e) {
            if(e.getCause() instanceof InterruptedException) {
                updateSendState(TaskState.CANCELED);
                updateStatus(STATUS_CANCELED);
            }
            else {
                updateSendState(TaskState.FAILED);
                updateStatus(STATUS_FAILED);
            }
            updateProgress(0, 0);
            throw e;
        }
    }

    private File compress() throws Exception {
        updateSendState(TaskState.PACKING);
        updateStatus(STATUS_PACKING);
        CompressTask compressTask = new CompressTask(path, serviceProvider.getPropertiesService());
        try {
            return compressTask.call();
        } catch (Exception e) {
            e.printStackTrace();
            if(e.getCause() instanceof InterruptedException)
                throw new RuntimeException("Упаковка прервана пользователем.", e.getCause());
            else
                throw new RuntimeException("Ошибка при упаковке архива.", e);
        }
    }

    private SendFileResult send(File archive) throws RuntimeException {
        updateSendState(TaskState.SENDING);
        updateStatus(STATUS_SENDING);
        Connection connection;
        try {
            connection = new Connection(address);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Не удалось подключиться к серверу.", e);
        }
        connection.setFileSendingProgressListener(pair -> updateProgress(pair.getKey(), pair.getValue()));
        try {
            connection.writeJson(new SendFileRequest(
                    archive.length(),
                    fullname,
                    emails
            ));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при отправке данных.", e);
        }
        SendFileResponse response;
        try {
            response = connection.readJson(SendFileResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при чтении ответа сервера.", e);
        }
        if (response.isAllow()) {
            try {
                connection.writeFile(archive);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Ошибка при отправке файла.", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("Отправка прервана пользователем.", e);
            }
        }
        SendFileResult result;
        try {
            result = connection.readJson(SendFileResult.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Не удалось получить подтверждение о получении файла.", e);
        }
        if (result.isSuccess()) {
            archive.delete();
        }
        try {
            connection.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Не удалось очистить буфер отправки.\nВероятно, передача не удалась.", e);
        }
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Не удалось закрыть соединение.\nОднако, отправка была завершена успешно.", e);
        }
        return new SendFileResult(true, "Отправка успешно завершена.");
    }

    private void updateStatus(String status) {
        if(Platform.isFxApplicationThread()) {
            this.status.set(status);
        } else {
            if(statusUpdate.getAndSet(status) == null) {
                Platform.runLater(() -> {
                    final String status1 = statusUpdate.getAndSet(null);
                    SendTask.this.status.set(status1);
                });
            }
        }
    }

    private void updateSendState(TaskState state) {
        if(Platform.isFxApplicationThread()) {
            this.sendState.set(state);
        } else {
            if(sendStateUpdate.getAndSet(state) == null) {
                Platform.runLater(() -> {
                    final TaskState state1 = sendStateUpdate.getAndSet(null);
                    SendTask.this.sendState.set(state1);
                });
            }
        }
    }

    private void updatePath(String path) {
        if(Platform.isFxApplicationThread()) {
            this.pathProperty.set(path);
        } else {
            if(pathPropertyUpdate.getAndSet(path) == null) {
                Platform.runLater(() -> {
                    final String path1 = pathPropertyUpdate.getAndSet(null);
                    SendTask.this.pathProperty.set(path1);
                });
            }
        }
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public TaskState getSendState() {
        return sendState.get();
    }

    public SimpleObjectProperty<TaskState> sendStateProperty() {
        return sendState;
    }

    public String getPath() {
        return pathProperty.get();
    }

    public StringProperty pathProperty() {
        return pathProperty;
    }
}

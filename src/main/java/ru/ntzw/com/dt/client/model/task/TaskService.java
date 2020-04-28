package ru.ntzw.com.dt.client.model.task;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public interface TaskService<T extends Task> {

    void submit(T task);

    ObservableList<T> getTaskList();
}

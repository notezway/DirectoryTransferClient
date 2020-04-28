package ru.ntzw.com.dt.client.model.task;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import ru.ntzw.com.dt.client.Disposable;
import ru.ntzw.com.dt.client.Initializable;
import ru.ntzw.com.dt.client.model.ServiceProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class SimpleTaskService<T extends Task> implements TaskService<T>, Initializable, Disposable {

    private ObservableList<T> taskList;
    private ExecutorService executorService;

    @Override
    public void submit(T task) {
        taskList.add(task);
    }

    @Override
    public ObservableList<T> getTaskList() {
        return taskList;
    }

    private void uncaughtException(Thread thread, Throwable e) {
        //TODO catch them
    }

    @Override
    public void init(ServiceProvider serviceProvider) {
        taskList = FXCollections.observableArrayList();
        taskList.addListener((ListChangeListener<T>) c -> {
            while (c.next()) {
                for (T t : c.getRemoved()) {
                    t.cancel();
                }
                for (T t : c.getAddedSubList()) {
                    executorService.submit(t);
                }
            }
        });

        ThreadFactory threadFactory = new ThreadFactory() {
            int n = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("Thread-Tasks-" + n++);
                thread.setUncaughtExceptionHandler(SimpleTaskService.this::uncaughtException);
                thread.setDaemon(true);
                return thread;
            }
        };
        executorService = Executors.newCachedThreadPool(threadFactory);
    }

    @Override
    public void dispose() {
        executorService.shutdownNow();
    }
}

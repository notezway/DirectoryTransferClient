package ru.ntzw.com.dt.client.model;

import ru.ntzw.com.dt.client.model.logging.LoggingService;
import ru.ntzw.com.dt.client.model.properties.PropertiesService;
import ru.ntzw.com.dt.client.model.singleinstance.SingleInstanceService;
import ru.ntzw.com.dt.client.model.task.TaskService;

public class ServiceProvider {

    private PropertiesService propertiesService;
    private SingleInstanceService singleInstanceService;
    private LoggingService loggingService;
    private TaskService<SendTask> taskService;

    public PropertiesService getPropertiesService() {
        return propertiesService;
    }

    public void setPropertiesService(PropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }

    public SingleInstanceService getSingleInstanceService() {
        return singleInstanceService;
    }

    public void setSingleInstanceService(SingleInstanceService singleInstanceService) {
        this.singleInstanceService = singleInstanceService;
    }

    public LoggingService getLoggingService() {
        return loggingService;
    }

    public void setLoggingService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public TaskService<SendTask> getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService<SendTask> taskService) {
        this.taskService = taskService;
    }
}

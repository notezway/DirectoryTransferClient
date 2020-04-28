package ru.ntzw.com.dt.client.model.logging;

public interface LoggingService {

    void error(String s, Throwable e);

    void info(String s);

    void debug(String s);
}

package ru.ntzw.com.dt.client.model.logging;

import ru.ntzw.com.dt.client.Disposable;
import ru.ntzw.com.dt.client.Initializable;
import ru.ntzw.com.dt.client.model.ServiceProvider;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Date;

public class DailyRotatingLoggingService implements LoggingService, Initializable, Disposable {

    private String path;
    private String fileNameBase;
    private String fileNameSuffix;
    private LogLevel logLevel;
    private int logsToKeep;

    private BufferedWriter writer;
    private LocalDate lastFileDate;

    public DailyRotatingLoggingService() {
        this.lastFileDate = LocalDate.now();
    }

    private File getLogFile(String uniquePart) throws IOException {
        Path p = Paths.get(path, fileNameBase + uniquePart + fileNameSuffix);
        Files.createDirectories(p.toAbsolutePath().getParent());
        return p.toFile();
    }

    private void newWriter(File logFile) throws IOException {
        if(writer != null) {
            writer.flush();
            writer.close();
        }
        writer = new BufferedWriter(new FileWriter(logFile, true));
    }

    private void doDateCheck() throws IOException {
        LocalDate now = LocalDate.now();
        if(!now.equals(lastFileDate)) {
            newWriter(getLogFile(now.toString()));
            getLogFile(now.minusDays(logsToKeep).toString()).delete();
            lastFileDate = now;
        }
    }

    private void write(String s, LogLevel currentLogLevel) {
        if(currentLogLevel.ordinal() <= logLevel.ordinal()) {
            try {
                doDateCheck();
                String toWrite = String.format("[%s] [%s] [%s]: %s", new Date().toString(), Thread.currentThread().getName(), currentLogLevel.toString(), s);
                writer.write(toWrite);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void init(ServiceProvider serviceProvider) throws Exception {
        this.path = serviceProvider.getPropertiesService().getString("logDirectory", "logs");
        this.fileNameBase = serviceProvider.getPropertiesService().getString("logBaseName", "log");
        this.fileNameSuffix = serviceProvider.getPropertiesService().getString("logNameSuffix", ".txt");
        this.logLevel = LogLevel.valueOf(serviceProvider.getPropertiesService().getString("logLevel", "INFO").toUpperCase());
        this.logsToKeep = serviceProvider.getPropertiesService().getInteger("logCountToKeep", 14);
        newWriter(getLogFile(LocalDate.now().toString()));
        writer.newLine();
        writer.flush();
    }

    @Override
    public void error(String s, Throwable e) {
        write(s, LogLevel.ERROR);
        if(e != null) {
            e.printStackTrace(new PrintWriter(writer));
            try {
                writer.newLine();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void info(String s) {
        write(s, LogLevel.INFO);
    }

    @Override
    public void debug(String s) {
        write(s, LogLevel.DEBUG);
    }

    @Override
    public void dispose() throws Exception {
        writer.flush();
        writer.close();
    }
}

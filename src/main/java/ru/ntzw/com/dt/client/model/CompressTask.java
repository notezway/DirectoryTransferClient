package ru.ntzw.com.dt.client.model;

import javafx.concurrent.Task;
import ru.ntzw.com.dt.client.model.properties.PropertiesService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompressTask extends Task<File> {

    private final String path;
    private final String directory;
    private final String command;
    private final String zipOutPath;
    private final String zipErrPath;

    public CompressTask(String path, PropertiesService propertiesService) {
        this.path = path;
        this.directory = propertiesService.getString("7zipPath", "7zip");
        this.command = propertiesService.getString("7zipCommand", "7za.exe a -tzip -mm=LZMA -mx=9 -mmt=1 -y");
        this.zipOutPath = propertiesService.getString("7zipOutPath", "logs/7zipout.txt");
        this.zipErrPath = propertiesService.getString("7zipErrPath", "logs/7ziperr.txt");
    }

    @Override
    protected File call() throws Exception {
        File archiveFile = File.createTempFile("archive", null);
        String archivePath = archiveFile.getAbsolutePath();
        archiveFile.delete();
        String toCompressPath = new File(path + File.separator + "data").getAbsolutePath();
        List<String> cmd = new ArrayList<>(Arrays.asList((directory + File.separator + command).split("\\s+")));
        cmd.add(archivePath);
        cmd.add(toCompressPath);
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectOutput(new File(zipOutPath));
        processBuilder.redirectError(new File(zipErrPath));
        //processBuilder.inheritIO();
        Process process = processBuilder.start();
        try {
            if (process.waitFor() != 0)
                throw new RuntimeException("7-zip exited with non zero code");
        } catch (InterruptedException e) {
            process.destroy();
            throw new RuntimeException("Process terminated", e);
        }
        return new File(archivePath);
    }
}

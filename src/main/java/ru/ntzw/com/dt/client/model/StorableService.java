package ru.ntzw.com.dt.client.model;

import ru.ntzw.com.dt.client.Disposable;
import ru.ntzw.com.dt.client.Initializable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class StorableService implements Initializable, Disposable {

    private Path storagePath;

    @Override
    public void init(ServiceProvider serviceProvider) throws Exception {
        this.storagePath = getStoragePath();
        ensureFileExists(storagePath);
        try(BufferedReader reader = Files.newBufferedReader(storagePath)) {
            load(reader);
        }
    }

    @Override
    public void dispose() throws Exception {
        ensureFileExists(storagePath);
        try(BufferedWriter writer = Files.newBufferedWriter(storagePath)) {
            store(writer);
        }
    }

    private void ensureFileExists(Path path) throws IOException {
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            Files.createFile(path);
        } catch(FileAlreadyExistsException e) {
            //ignored
        }
    }

    protected abstract Path getStoragePath();

    protected abstract void load(BufferedReader reader) throws IOException;

    protected abstract void store(BufferedWriter writer) throws IOException;
}

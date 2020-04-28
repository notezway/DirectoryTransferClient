package ru.ntzw.com.dt.client.model;

import com.google.gson.Gson;
import javafx.util.Pair;
import ru.ntzw.com.dt.client.GsonProvider;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

public class Connection implements AutoCloseable {

    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final Gson gson;
    private Consumer<Pair<Long, Long>> fileSendingProgressListener;

    public Connection(InetSocketAddress address) throws IOException {
        socket = new Socket();
        socket.connect(address, 10000);
        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        gson = GsonProvider.get();
    }

    public void writeJson(Object object) throws IOException {
        String s = gson.toJson(object);
        outputStream.writeUTF(s);
        outputStream.flush();
    }

    public <T> T readJson(Class<T> type) throws IOException {
        String s = inputStream.readUTF();
        return gson.fromJson(s, type);
    }

    public void writeFile(File file) throws IOException, InterruptedException {
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            int b;
            long written = 0;
            long length = file.length();
            long startTime = System.currentTimeMillis();
            while((b = bis.read()) != -1) {
                if(Thread.interrupted()) {
                    throw new InterruptedException();
                }
                outputStream.write(b);
                written++;
                if(fileSendingProgressListener != null && written % (1024*1024) == 0) {
                    fileSendingProgressListener.accept(new Pair<>(written, length));
                }
            }
            outputStream.flush();
            fileSendingProgressListener.accept(new Pair<>(length, length));
        }
    }

    public void setFileSendingProgressListener(Consumer<Pair<Long, Long>> fileSendingProgressListener) {
        this.fileSendingProgressListener = fileSendingProgressListener;
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws Exception {
        flush();
        outputStream.close();
        inputStream.close();
        socket.close();
    }
}

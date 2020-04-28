package ru.ntzw.com.dt.client.model.singleinstance;

import ru.ntzw.com.dt.client.Disposable;
import ru.ntzw.com.dt.client.Initializable;
import ru.ntzw.com.dt.client.model.ServiceProvider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketSingleInstanceService implements SingleInstanceService, Initializable, Disposable {

    private int port;
    private Consumer<String> stringFromOthersCallback;
    private boolean initialized = false;
    private boolean isSingle = true;
    private ServerSocket serverSocket;

    @Override
    public void init(ServiceProvider serviceProvider) throws Exception {
        this.port = serviceProvider.getPropertiesService().getInteger("singleInstancePort", 41803);
        serverSocket = new ServerSocket();
        try {
            serverSocket.bind(new InetSocketAddress("localhost", port));
            Thread listeningThread = new Thread(() -> {
                while(!serverSocket.isClosed() && serverSocket.isBound()) {
                    try(Socket socket = serverSocket.accept();
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {
                        if(stringFromOthersCallback != null) {
                            stringFromOthersCallback.accept(dataInputStream.readUTF());
                        }
                    } catch (IOException e) {
                        //ignored
                    }
                }
            });
            listeningThread.setName("Thread-SingleInstance");
            listeningThread.setDaemon(true);
            listeningThread.start();
        } catch (IOException e) {
            //socket probably is already occupied
            isSingle = false;
        }
        initialized = true;
    }

    private void ensureIsInitialized() {
        if(!initialized)
            throw new IllegalStateException("SocketSingleInstanceService has not been initialized");
    }

    @Override
    public boolean isSingle() {
        ensureIsInitialized();
        return isSingle;
    }

    @Override
    public void sendStringToSingle(String s) throws IOException {
        ensureIsInitialized();
        try(Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port));
            try(DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
                dataOutputStream.writeUTF(s);
                dataOutputStream.flush();
            }
        }
    }

    @Override
    public void setStringFromOthersCallback(Consumer<String> callback) {
        this.stringFromOthersCallback = callback;
    }

    @Override
    public void dispose() throws Exception {
        serverSocket.close();
    }
}

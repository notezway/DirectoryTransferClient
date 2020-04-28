package ru.ntzw.com.dt.client.model.singleinstance;

import java.io.IOException;
import java.util.function.Consumer;

public interface SingleInstanceService {

    boolean isSingle();

    void sendStringToSingle(String s) throws IOException;

    void setStringFromOthersCallback(Consumer<String> callback);
}

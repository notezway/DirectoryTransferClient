package ru.ntzw.com.dt.client;

import ru.ntzw.com.dt.client.model.ServiceProvider;

public interface Initializable {

    void init(ServiceProvider serviceProvider) throws Exception;
}

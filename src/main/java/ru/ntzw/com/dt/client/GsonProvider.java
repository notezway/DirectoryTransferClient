package ru.ntzw.com.dt.client;

import com.google.gson.Gson;

public class GsonProvider {

    private static Gson gson = new Gson();

    public static Gson get() {
        return gson;
    }
}

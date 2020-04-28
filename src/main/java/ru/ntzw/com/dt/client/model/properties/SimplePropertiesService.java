package ru.ntzw.com.dt.client.model.properties;

import ru.ntzw.com.dt.client.model.ServiceProvider;
import ru.ntzw.com.dt.client.model.StorableService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimplePropertiesService extends StorableService implements PropertiesService {

    private final Path storagePath;

    private LinkedHashMap<String, Object> properties;

    public SimplePropertiesService(Path storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public Object get(String name) {
        return get(name, null);
    }

    @Override
    public String getString(String name) {
        return getString(name, null);
    }

    @Override
    public Integer getInteger(String name) {
        return getInteger(name, null);
    }

    @Override
    public Double getDouble(String name) {
        return getDouble(name, null);
    }

    @Override
    public Boolean getBoolean(String name) {
        return getBoolean(name, null);
    }

    @Override
    public Object get(String name, Object defaultValue) {
        if(properties.containsKey(name))
            return properties.get(name);
        add(name, defaultValue);
        return defaultValue;
    }

    @Override
    public String getString(String name, String defaultValue) {
        return (String) get(name, defaultValue);
    }

    @Override
    public Integer getInteger(String name, Integer defaultValue) {
        return (Integer) get(name, defaultValue);
    }

    @Override
    public Double getDouble(String name, Double defaultValue) {
        return (Double) get(name, defaultValue);
    }

    @Override
    public Boolean getBoolean(String name, Boolean defaultValue) {
        return (Boolean) get(name, defaultValue);
    }

    @Override
    public Object set(String name, Object value) {
        return properties.put(name, value);
    }

    @Override
    public boolean add(String name, Object value) {
        if(properties.containsKey(name))
            return false;
        set(name, value);
        return true;
    }

    @Override
    public Object remove(String name) {
        return properties.remove(name);
    }

    @Override
    public void init(ServiceProvider serviceProvider) throws Exception {
        properties = new LinkedHashMap<>();
        super.init(serviceProvider);
    }

    @Override
    public void dispose() throws Exception {
        super.dispose();
    }

    @Override
    protected Path getStoragePath() {
        return storagePath;
    }

    @Override
    protected void load(BufferedReader reader) throws IOException {
        properties.clear();
        String line;
        while((line = reader.readLine()) != null) {
            String[] keyValuePair = line.split(KEY_SEPARATOR, 2);
            String[] typeValuePair = keyValuePair[1].split(TYPE_SEPARATOR, 2);
            properties.put(keyValuePair[0], stringToObject(typeValuePair[0], typeValuePair[1]));
        }
    }

    @Override
    protected void store(BufferedWriter writer) throws IOException {
        for(Map.Entry<String, Object> entry : properties.entrySet()) {
            writer.write(entry.getKey() + KEY_SEPARATOR + objectToString(entry.getValue()));
            writer.newLine();
        }
    }

    private Object stringToObject(String type, String value) {
        char prefix = type.charAt(0);
        switch(prefix) {
            case PREFIX_NULL:
                return null;
            case PREFIX_STRING:
                return value;
            case PREFIX_INTEGER:
                return Integer.parseInt(value);
            case PREFIX_DOUBLE:
                return Double.parseDouble(value);
            case PREFIX_BOOLEAN:
                return Boolean.parseBoolean(value);

            default:
                throw new UnsupportedOperationException();
        }
    }

    private String objectToString(Object object) {
        char prefix = getPrefix(object);
        switch(prefix) {
            case PREFIX_NULL:
                return PREFIX_NULL + TYPE_SEPARATOR;
            case PREFIX_STRING:
                return PREFIX_STRING + TYPE_SEPARATOR + object.toString();
            case PREFIX_INTEGER:
                return PREFIX_INTEGER + TYPE_SEPARATOR + object.toString();
            case PREFIX_DOUBLE:
                return PREFIX_DOUBLE + TYPE_SEPARATOR + object.toString();
            case PREFIX_BOOLEAN:
                return PREFIX_BOOLEAN + TYPE_SEPARATOR + object.toString();

            default:
                throw new UnsupportedOperationException();
        }
    }

    private char getPrefix(Object object) {
        if(object == null)
            return PREFIX_NULL;
        Class objClass = object.getClass();
        if(objClass == String.class)
            return PREFIX_STRING;
        if(objClass == Integer.class)
            return PREFIX_INTEGER;
        if(objClass == Double.class)
            return PREFIX_DOUBLE;
        if(objClass == Boolean.class)
            return PREFIX_BOOLEAN;
        return PREFIX_OBJECT;
    }

    private static final String KEY_SEPARATOR = "=";
    private static final String TYPE_SEPARATOR = ":";

    private static final char PREFIX_NULL = 'N';
    private static final char PREFIX_OBJECT = 'O';
    private static final char PREFIX_STRING = 'S';
    private static final char PREFIX_INTEGER = 'I';
    private static final char PREFIX_DOUBLE = 'D';
    private static final char PREFIX_BOOLEAN = 'B';
}

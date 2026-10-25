package com.ts.messenger.utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManager {
    public static Properties load(String file) {
        final Properties props = new Properties();
        try (InputStream stream = PropertiesManager.class.getResourceAsStream(file)) {
            if (stream == null)
                throw new IllegalStateException("Missing configuration file " + file);
            props.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read configuration file " + file, e);
        }
        return props;
    }
}

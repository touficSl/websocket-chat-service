package com.ts.messenger.utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.annotation.Configuration;

public class PropertiesManager {
    public static Properties load(String file) {
        final InputStream stream = Configuration.class.getResourceAsStream(file);
        final Properties props = new Properties();
        try {
            props.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {}
        }
        return props;
    }
}

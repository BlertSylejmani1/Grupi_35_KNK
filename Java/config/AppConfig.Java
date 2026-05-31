package com.smartinventory.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfig {
    private AppConfig() {
    }

    public static Properties load(String resourceName) {
        Properties properties = new Properties();
        Path projectResource = Path.of("src", "main", "resources", "com", "smartinventory", resourceName);
        if (Files.exists(projectResource)) {
            try (InputStream stream = Files.newInputStream(projectResource)) {
                properties.load(stream);
                return properties;
            } catch (IOException ignored) {
                // Classpath defaults are used when the editable project file cannot be read.
            }
        }
        Path localResource = Path.of(resourceName);
        if (Files.exists(localResource)) {
            try (InputStream stream = Files.newInputStream(localResource)) {
                properties.load(stream);
                return properties;
            } catch (IOException ignored) {
                // Classpath defaults are used when the local override cannot be read.
            }
        }
        try (InputStream stream = AppConfig.class.getResourceAsStream("/com/smartinventory/" + resourceName)) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException ignored) {
            // Defaults are used when a config file cannot be read.
        }
        return properties;
    }
}

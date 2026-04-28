package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApiConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = ApiConfig.class
                .getResourceAsStream("/config.properties")) {
            if (in != null) props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // PredictHQ
    public static String getToken() {
        return props.getProperty("predicthq.token");
    }

    public static String getUrl() {
        return props.getProperty("predicthq.url");
    }

    // Groq
    public static String getGroqKey() {
        return props.getProperty("groq.api.key");
    }

    public static String getGroqUrl() {
        return props.getProperty("groq.url");
    }

    // TTS
    public static String getTtsProvider() {
        return props.getProperty("tts.provider", "google");
    }

    public static String getTtsLanguage() {
        return props.getProperty("tts.language", "fr-FR");
    }
}

package org.example.bot;

import org.example.config.ApiConfig;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GroqAgent {

    private static final String SYSTEM_PROMPT =
            "You are a helpful assistant for an e-learning platform. " +
                    "You ONLY discuss topics related to online education, learning, courses, " +
                    "certifications, student resources, course management, and educational technology. " +
                    "If a user asks about something unrelated to e-learning, politely decline and " +
                    "redirect them back to e-learning topics. Be professional, helpful, and encouraging.";

    public String getResponse(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Please send me a message!";
        }
        try {
            return callGroqAPI(userMessage);
        } catch (Exception e) {
            return "❌ Error getting LLM response: " + e.getMessage();
        }
    }

    private String callGroqAPI(String userMessage) throws Exception {
        String apiKey = ApiConfig.getGroqKey();
        String apiUrl = ApiConfig.getGroqUrl();

        // Escape special characters
        String safeMessage = userMessage
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");

        String safeSystem = SYSTEM_PROMPT
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        String body = "{"
                + "\"model\":\"llama-3.3-70b-versatile\","
                + "\"max_tokens\":1024,"
                + "\"temperature\":0.7,"
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + safeSystem + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + safeMessage + "\"}"
                + "]}";

        HttpURLConnection conn =
                (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 400)
                ? conn.getErrorStream()
                : conn.getInputStream();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        if (status >= 400)
            throw new Exception("Groq API Error " + status + ": " + sb);

        return parseResponse(sb.toString());
    }

    private String parseResponse(String json) {
        int start = json.indexOf("\"content\":\"");
        if (start == -1) return "Unexpected response from AI.";
        start += 11;
        int end = json.indexOf("\"", start);
        if (end == -1) return "Parsing error.";
        return json.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\/", "/");
    }
}
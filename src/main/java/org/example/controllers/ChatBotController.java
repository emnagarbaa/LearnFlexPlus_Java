package org.example.controllers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Chatbot IA pour LearnFlex+
 * - Utilise l'API Groq (GRATUIT, ultra-rapide)
 * - Modele : llama-3.3-70b-versatile
 * - Restreint aux questions educatives liees a LearnFlex+
 */
public class ChatBotController {

    // =====================================================
    //   CONFIGURATION GROQ
    // =====================================================
    private static final String GROQ_API_KEY = loadApiKey();
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    // =====================================================
    //   CHARGEMENT CLE API DEPUIS config.properties
    // =====================================================
    private static String loadApiKey() {
        try (InputStream input = ChatBotController.class
                .getResourceAsStream("/config.properties")) {
            if (input == null) {
                System.err.println("[ChatBot] Fichier config.properties introuvable !");
                return "";
            }
            Properties props = new Properties();
            props.load(input);
            String key = props.getProperty("groq.api.key", "");
            System.out.println("[ChatBot] Cle Groq chargee : " + (key.isEmpty() ? "VIDE !" : key.substring(0, 8) + "..."));
            return key;
        } catch (IOException e) {
            System.err.println("[ChatBot] Erreur chargement config.properties : " + e.getMessage());
            return "";
        }
    }

    // =====================================================
    //   SYSTEM PROMPT
    // =====================================================
    private static final String SYSTEM_PROMPT =
            "Tu es un assistant educatif intelligent integre dans l'application LearnFlex+. " +
                    "Tu aides UNIQUEMENT les utilisateurs sur les sujets suivants :\n" +
                    "- Les cours et formations disponibles sur LearnFlex+\n" +
                    "- Les quiz et examens de la plateforme\n" +
                    "- Les publications et le forum de la communaute LearnFlex+\n" +
                    "- Les questions educatives generales (mathematiques, sciences, langues, informatique, etc.)\n" +
                    "- L'utilisation et les fonctionnalites de l'application LearnFlex+\n" +
                    "- Les conseils d'apprentissage et de revision\n\n" +
                    "Si l'utilisateur pose une question qui n'est PAS liee a l'education ou a LearnFlex+, " +
                    "reponds exactement ceci :\n" +
                    "Je suis desole, je suis limite aux questions educatives liees a LearnFlex+. " +
                    "Je ne peux pas vous aider sur ce sujet. " +
                    "N'hesitez pas a me poser une question sur vos cours, quiz ou le forum !\n\n" +
                    "Reponds toujours en francais. Sois concis, bienveillant et pedagogue.";

    // =====================================================
    //   OUVERTURE DE LA FENETRE CHATBOT
    // =====================================================
    public static void openChatBot() {
        Stage stage = new Stage();
        stage.setTitle("Assistant LearnFlex+");
        stage.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#f0f4f8;");

        // En-tete
        HBox header = new HBox(10);
        header.setStyle("-fx-background-color:#2c3e50; -fx-padding:15 20;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label botIcon = new Label("🤖");
        botIcon.setStyle("-fx-font-size:24px;");

        VBox headerText = new VBox(2);
        Label botName = new Label("Assistant LearnFlex+");
        botName.setStyle("-fx-text-fill:white; -fx-font-size:15px; -fx-font-weight:bold;");
        Label botStatus = new Label("● En ligne — Questions educatives uniquement");
        botStatus.setStyle("-fx-text-fill:#2ecc71; -fx-font-size:11px;");
        headerText.getChildren().addAll(botName, botStatus);
        header.getChildren().addAll(botIcon, headerText);

        // Zone des messages
        VBox messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(15));
        messagesBox.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(380);
        scrollPane.setStyle("-fx-background:transparent; -fx-background-color:transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        ajouterMessageBot(messagesBox, scrollPane,
                "Bonjour ! Je suis votre assistant LearnFlex+.\n\n" +
                        "Je peux vous aider sur :\n" +
                        "- Vos cours et formations\n" +
                        "- Les quiz et examens\n" +
                        "- Le forum et les publications\n" +
                        "- Toutes vos questions educatives\n\n" +
                        "Comment puis-je vous aider ?");

        // Indicateur typing
        Label typingIndicator = new Label("Assistant en train d'ecrire...");
        typingIndicator.setStyle("-fx-text-fill:#95a5a6; -fx-font-size:11px; -fx-padding:0 15 5 15;");
        typingIndicator.setVisible(false);
        typingIndicator.setManaged(false);

        // Zone de saisie
        HBox inputArea = new HBox(8);
        inputArea.setPadding(new Insets(12));
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setStyle("-fx-background-color:white; -fx-border-color:#e0e0e0; -fx-border-width:1 0 0 0;");

        TextArea inputField = new TextArea();
        inputField.setPromptText("Posez votre question educative...");
        inputField.setPrefHeight(50);
        inputField.setMaxHeight(80);
        inputField.setWrapText(true);
        inputField.setStyle("-fx-background-radius:20; -fx-border-color:#e0e0e0; " +
                "-fx-border-radius:20; -fx-padding:8 12; -fx-font-size:13px;");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button sendBtn = new Button(">");
        sendBtn.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; " +
                "-fx-background-radius:50; -fx-min-width:42; -fx-min-height:42; " +
                "-fx-font-size:16px; -fx-cursor:hand;");

        inputArea.getChildren().addAll(inputField, sendBtn);
        root.getChildren().addAll(header, scrollPane, typingIndicator, inputArea);

        // Action envoi
        Runnable sendMessage = () -> {
            String userText = inputField.getText().trim();
            if (userText.isEmpty()) return;

            if (GROQ_API_KEY == null || GROQ_API_KEY.isEmpty()) {
                ajouterMessageBot(messagesBox, scrollPane,
                        "Cle API manquante. Ajoutez groq.api.key dans config.properties.");
                return;
            }

            inputField.clear();
            inputField.setDisable(true);
            sendBtn.setDisable(true);
            sendBtn.setStyle("-fx-background-color:#95a5a6; -fx-text-fill:white; " +
                    "-fx-background-radius:50; -fx-min-width:42; -fx-min-height:42; " +
                    "-fx-font-size:16px; -fx-cursor:default;");

            ajouterMessageUser(messagesBox, scrollPane, userText);

            typingIndicator.setVisible(true);
            typingIndicator.setManaged(true);

            new Thread(() -> {
                String reponse = appellerGroq(userText);

                Platform.runLater(() -> {
                    typingIndicator.setVisible(false);
                    typingIndicator.setManaged(false);
                    ajouterMessageBot(messagesBox, scrollPane, reponse);

                    inputField.setDisable(false);
                    sendBtn.setDisable(false);
                    sendBtn.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; " +
                            "-fx-background-radius:50; -fx-min-width:42; -fx-min-height:42; " +
                            "-fx-font-size:16px; -fx-cursor:hand;");
                    inputField.requestFocus();
                });
            }).start();
        };

        sendBtn.setOnAction(e -> sendMessage.run());
        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                sendMessage.run();
            }
        });

        Scene scene = new Scene(root, 460, 560);
        stage.setScene(scene);
        stage.show();
    }

    // =====================================================
    //   APPEL API GROQ
    // =====================================================
    private static String appellerGroq(String userMessage) {
        try {
            String systemEscaped = jsonEscape(SYSTEM_PROMPT);
            String userEscaped   = jsonEscape(userMessage);

            String requestBody = "{"
                    + "\"model\":\"" + MODEL + "\","
                    + "\"messages\":["
                    + "{\"role\":\"system\",\"content\":" + systemEscaped + "},"
                    + "{\"role\":\"user\",\"content\":" + userEscaped + "}"
                    + "],"
                    + "\"max_tokens\":500,"
                    + "\"temperature\":0.7"
                    + "}";

            System.out.println("[ChatBot] Envoi requete a Groq avec modele : " + MODEL);

            URL url = new URL(GROQ_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            byte[] bodyBytes = requestBody.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyBytes);
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            System.out.println("[ChatBot] Groq response code: " + responseCode);

            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) response.append(line);
                    return extraireTexteGroq(response.toString());
                }
            } else {
                InputStream errStream = conn.getErrorStream();
                if (errStream != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(errStream, StandardCharsets.UTF_8))) {
                        StringBuilder err = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) err.append(line);
                        System.out.println("[ChatBot] Error body: " + err);
                        if (responseCode == 401) return "Cle API Groq invalide. Verifiez groq.api.key dans config.properties.";
                        if (responseCode == 429) return "Limite atteinte. Patientez quelques secondes et reessayez.";
                        return "Erreur serveur (code " + responseCode + "). Reessayez plus tard.";
                    }
                }
                return "Erreur serveur (code " + responseCode + "). Reessayez plus tard.";
            }

        } catch (java.net.SocketTimeoutException e) {
            return "La requete a pris trop de temps. Verifiez votre connexion internet.";
        } catch (Exception e) {
            System.out.println("[ChatBot] Exception: " + e.getMessage());
            e.printStackTrace();
            return "Erreur de connexion : " + e.getMessage();
        }
    }

    // =====================================================
    //   EXTRACTION DU TEXTE DEPUIS LA REPONSE GROQ
    //   Format : {"choices":[{"message":{"content":"..."}}]}
    // =====================================================
    private static String extraireTexteGroq(String json) {
        try {
            int idx = json.indexOf("\"content\":");
            if (idx == -1) {
                System.out.println("[ChatBot] 'content' not found in: " + json);
                return "Je n'ai pas pu generer de reponse.";
            }

            int start = json.indexOf("\"", idx + 10) + 1;
            int end   = json.indexOf("\"", start);

            StringBuilder result = new StringBuilder();
            while (end != -1 && json.charAt(end - 1) == '\\') {
                result.append(json, start, end - 1).append("\"");
                start = end + 1;
                end   = json.indexOf("\"", start);
            }
            result.append(json, start, end == -1 ? json.length() : end);

            return result.toString()
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\r", "")
                    .trim();
        } catch (Exception e) {
            System.out.println("[ChatBot] Extraction error: " + e.getMessage());
            return "Je n'ai pas pu generer de reponse. Reessayez.";
        }
    }

    // =====================================================
    //   AFFICHAGE DES MESSAGES
    // =====================================================
    private static void ajouterMessageUser(VBox container, ScrollPane scroll, String texte) {
        Label msg = new Label(texte);
        msg.setWrapText(true);
        msg.setMaxWidth(300);
        msg.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; " +
                "-fx-background-radius:18 18 4 18; -fx-padding:10 14; -fx-font-size:13px;");

        HBox row = new HBox(msg);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(2, 5, 2, 50));
        container.getChildren().add(row);
        scrollToBottom(scroll);
    }

    private static void ajouterMessageBot(VBox container, ScrollPane scroll, String texte) {
        Label icon = new Label("🤖");
        icon.setStyle("-fx-font-size:18px;");

        Label msg = new Label(texte);
        msg.setWrapText(true);
        msg.setMaxWidth(300);
        msg.setStyle("-fx-background-color:white; -fx-text-fill:#2c3e50; " +
                "-fx-background-radius:18 18 18 4; -fx-padding:10 14; -fx-font-size:13px; " +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        HBox row = new HBox(8, icon, msg);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 50, 2, 5));
        container.getChildren().add(row);
        scrollToBottom(scroll);
    }

    private static void scrollToBottom(ScrollPane scroll) {
        Platform.runLater(() -> scroll.setVvalue(1.0));
    }

    // =====================================================
    //   UTILITAIRE JSON
    // =====================================================
    private static String jsonEscape(String text) {
        if (text == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}

package org.example.controllers;

import org.example.Services.HerculesTcpClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HerculesChatController {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea messagesArea;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    private HerculesTcpClient client;

    @FXML
    public void initialize() {
        messagesArea.setEditable(false);
        connect("127.0.0.1", 8888);
    }

    private void connect(String host, int port) {
        statusLabel.setText("Connecting to " + host + ":" + port + " ...");
        client = new HerculesTcpClient(host, port, new HerculesTcpClient.Listener() {
            @Override
            public void onConnected() {
                Platform.runLater(() -> statusLabel.setText("Connected to " + host + ":" + port));
                append("Connected");
            }

            @Override
            public void onDisconnected() {
                Platform.runLater(() -> statusLabel.setText("Disconnected"));
                append("Disconnected");
            }

            @Override
            public void onMessage(String message) {
                append("RX: " + message);
            }

            @Override
            public void onError(String message, Throwable error) {
                append("Error: " + message + (error != null ? (" (" + error.getMessage() + ")") : ""));
                Platform.runLater(() -> statusLabel.setText("Error (see log)"));
            }
        });
        client.connectAsync(1500);
    }

    private void append(String line) {
        Platform.runLater(() -> {
            String ts = LocalTime.now().format(TIME);
            messagesArea.appendText("[" + ts + "] " + line + "\n");
        });
    }

    @FXML
    public void sendMessage() {
        String msg = messageField.getText();
        if (msg == null || msg.trim().isEmpty()) {
            return;
        }

        try {
            client.sendLine(msg.trim());
            append("TX: " + msg.trim());
            messageField.clear();
        } catch (IOException e) {
            append("Error sending: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (client != null) {
            client.close();
            client = null;
        }
    }
}

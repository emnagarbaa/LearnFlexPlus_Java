package org.example.utils;

import javafx.animation.TranslateTransition;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NotificationUtil {

    public static void showPopup(Stage stage, String message, boolean success) {
        Scene scene = stage.getScene();
        Pane root = (Pane) scene.getRoot(); // ✅ Works for AnchorPane or StackPane

        StackPane popup = new StackPane();
        popup.setStyle(
                "-fx-background-color: " + (success ? "#4CAF50" : "#F44336") + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15px;"
        );

        Text text = new Text(message);
        text.setFill(Color.WHITE);
        popup.getChildren().add(text);

        // Initial position above the window
        popup.setTranslateY(-80);
        popup.setOpacity(0);

        root.getChildren().add(popup);

        // ✅ Slide down animation
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(400), popup);
        slideDown.setFromY(-80);
        slideDown.setToY(20);
        slideDown.play();

        popup.setOpacity(1);

        // ✅ Auto remove after delay
        slideDown.setOnFinished(e -> {
            new Thread(() -> {
                try {
                    Thread.sleep(2500);
                    javafx.application.Platform.runLater(() -> {
                        TranslateTransition slideUp = new TranslateTransition(Duration.millis(400), popup);
                        slideUp.setFromY(20);
                        slideUp.setToY(-80);
                        slideUp.play();
                        slideUp.setOnFinished(ev -> root.getChildren().remove(popup));
                    });
                } catch (InterruptedException ignored) {}
            }).start();
        });
    }
}

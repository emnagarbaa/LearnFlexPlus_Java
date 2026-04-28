package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.bot.EvenementBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // ✅ Démarrer le bot Telegram dans un thread séparé
        new Thread(() -> {
            try {
                TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
                api.registerBot(new EvenementBot());
                System.out.println("✅ Bot Telegram démarré!");
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }).start();

        // Ton code existant intact
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/fxml/dashboard.fxml")
        );
        Scene scene = new Scene(loader.load(), 1200, 700);
        stage.setTitle("LearnFlex Admin");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
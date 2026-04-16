package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controllers.DashboardController;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/fxml/dashboard.fxml")
        );

        Scene scene = new Scene(loader.load(), 1200, 700);

        DashboardController controller = loader.getController();
        scene.setUserData(controller); // 🔥 IMPORTANT

        stage.setTitle("LearnFlex Admin");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
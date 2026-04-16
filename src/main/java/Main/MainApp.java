package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/front.fxml")
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
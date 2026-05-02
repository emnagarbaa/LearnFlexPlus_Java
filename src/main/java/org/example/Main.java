package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
<<<<<<< HEAD
                getClass().getResource("/org/example/fxml/front.fxml")
=======
                getClass().getResource("/org/example/fxml/dashboard.fxml")
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        );
        Scene scene = new Scene(loader.load(), 1200, 700);
        stage.setTitle("LearnFlex Admin");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
<<<<<<< HEAD
}
=======

}
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))

package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class FrontController {

    @FXML private ImageView logo;
    @FXML private ImageView heroImage;
    @FXML private ImageView serviceImage1;
    @FXML private ImageView serviceImage2;
    @FXML private ImageView serviceImage3;

    @FXML
    public void initialize() {
        loadImage(logo,          "/org/example/images/logo1.png");
        loadImage(heroImage,     "/org/example/images/hero1.png");
        loadImage(serviceImage1, "/org/example/images/service1.png");
        loadImage(serviceImage2, "/org/example/images/service2.png");
        loadImage(serviceImage3, "/org/example/images/service3.png");
    }

    private void loadImage(ImageView imageView, String path) {
        var stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.err.println("❌ Image introuvable : " + path);
            return;
        }
        imageView.setImage(new Image(stream));
    }

    @FXML private void goToCours()         { navigateTo("/org/example/fxml/cours.fxml", "Cours"); }
    @FXML private void explorerCours()     { navigateTo("/org/example/fxml/cours.fxml", "Cours"); }
    @FXML private void voirCours()         { navigateTo("/org/example/fxml/cours.fxml", "Cours"); }
    @FXML private void passerQuiz()        { navigateTo("/org/example/fxml/ExamenView.fxml", "Examens"); }
    @FXML private void accederForum()      { navigateTo("/org/example/fxml/forum.fxml", "Forum"); }
    @FXML private void goToConnexion()     { navigateTo("/org/example/fxml/login.fxml", "Connexion"); }
    @FXML private void goToAccueil()       {}
    @FXML
    private void goToEvaluation()          { navigateTo("/org/example/fxml/EvaluationFront.fxml", "Évaluation"); }
    @FXML private void goToQuestionnaire() { navigateTo("/org/example/fxml/questionnaire.fxml", "Questionnaire"); }
    @FXML private void goToOrientation()   { navigateTo("/org/example/fxml/orientation.fxml", "Orientation"); }
    @FXML private void goToForum()         { navigateTo("/org/example/fxml/forum.fxml", "Forum"); }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) logo.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
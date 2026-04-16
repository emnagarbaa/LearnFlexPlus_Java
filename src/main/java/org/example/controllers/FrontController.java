package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;

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
        System.out.println("✅ Image chargée : " + path);
    }

    // ── Navigation ────────────────────────────────────────────
    // Fichiers FXML disponibles :
    // front.fxml, EvaluationFront.fxml, ExamenFront.fxml, ExamenView.fxml,
    // quiz.fxml, quiz_front.fxml, reponse.fxml, dashboard.fxml, home.fxml,
    // AjouterChallenge.fxml, ChallengeUI.fxml, ChallengeView.fxml,
    // Commentaire.fxml, AjouterExamen.fxml, AjouterCommentaire.fxml

    @FXML private void goToAccueil()       { /* déjà sur l'accueil */ }
    @FXML private void goToCours()         { pageEnDeveloppement("Cours"); }           // cours.fxml n'existe pas
    @FXML private void explorerCours()     { pageEnDeveloppement("Cours"); }           // cours.fxml n'existe pas
    @FXML private void voirCours()         { pageEnDeveloppement("Cours"); }           // cours.fxml n'existe pas
    @FXML private void passerQuiz()        { navigateTo("/org/example/fxml/ExamenView.fxml",      "Examens"); }
    @FXML private void accederForum()      { pageEnDeveloppement("Forum"); }           // forum.fxml n'existe pas
    @FXML private void goToConnexion()     { navigateTo("/org/example/fxml/home.fxml",            "Connexion"); }
    @FXML private void goToEvaluation()    { navigateTo("/org/example/fxml/EvaluationFront.fxml", "Évaluation"); }
    @FXML private void goToQuestionnaire() { navigateTo("/org/example/fxml/quiz_front.fxml",      "Questionnaire"); } // ✅ quiz_front.fxml
    @FXML private void goToOrientation()   { pageEnDeveloppement("Orientation"); }     // orientation.fxml n'existe pas
    @FXML private void goToForum()         { pageEnDeveloppement("Forum"); }           // forum.fxml n'existe pas

    private void navigateTo(String fxmlPath, String title) {
        try {
            // ✅ FIX : vérification null avant FXMLLoader.load()
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + fxmlPath);
                new Alert(Alert.AlertType.ERROR,
                        "Page introuvable : " + fxmlPath, ButtonType.OK).showAndWait();
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = (Stage) logo.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation vers " + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void pageEnDeveloppement(String nomPage) {
        new Alert(Alert.AlertType.INFORMATION,
                "La page « " + nomPage + " » est en cours de développement.",
                ButtonType.OK).showAndWait();
    }
}
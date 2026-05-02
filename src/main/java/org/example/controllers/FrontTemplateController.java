package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class FrontTemplateController {

    @FXML private ImageView logo;
    @FXML private StackPane contentArea;
    @FXML private Label pageTitleLabel;

    @FXML
    public void initialize() {
        try {
            logo.setImage(new Image(getClass().getResourceAsStream("/org/example/images/logo1.png")));
        } catch (Exception e) {
            System.err.println("Logo non chargé");
        }
        // Charger la page d'accueil par défaut
        goToAccueil();
    }

    @FXML
    private void goToAccueil() {
        pageTitleLabel.setText("Accueil");
        loadPage("/org/example/fxml/HomePage.fxml");
    }

    @FXML
    private void goToPublications() {
        pageTitleLabel.setText("Publications");
        loadPage("/org/example/fxml/PublicationsFrontPage.fxml");
    }

    @FXML
    private void goToCommunications() {
        pageTitleLabel.setText("Communications");
        loadPage("/org/example/fxml/CommunicationsFrontPage.fxml");
    }

    @FXML
    private void goToCours() {
        pageTitleLabel.setText("Cours");
        loadPage("/org/example/fxml/cours.fxml");
    }

    @FXML
    private void goToQuiz() {
        pageTitleLabel.setText("Quiz");
        loadPage("/org/example/fxml/ExamenView.fxml");
    }

    @FXML
    private void goToConnexion() {
        loadPage("/org/example/fxml/login.fxml");
    }

    private void loadPage(String fxmlPath) {
        try {
            System.out.println("📂 Chargement : " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
        } catch (IOException e) {
            System.err.println("❌ Erreur de chargement: " + fxmlPath);
            e.printStackTrace();
        }
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))

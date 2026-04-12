package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML private StackPane mainContent;
    @FXML private VBox      evaluationSubmenu;
    @FXML private ImageView logoImage;

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/" + fxmlFile));
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
        } catch (IOException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }
    }

    @FXML public void showDashboard()     { mainContent.getChildren().clear(); }
    @FXML public void showUtilisateurs()  { loadPage("utilisateurs.fxml"); }
    @FXML public void showContenu()       { loadPage("contenu.fxml"); }
    @FXML public void showExamens()       { loadPage("ExamenView.fxml"); }
    @FXML public void showChallenges()    { loadPage("ChallengeView.fxml"); }
    @FXML public void showQuestionnaire() { loadPage("questionnaire.fxml"); }
    @FXML public void showOrientation()   { loadPage("orientation.fxml"); }
    @FXML public void showForum()         { loadPage("forum.fxml"); }

    @FXML
    public void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/front.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 900);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("LearnFlex+");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void toggleEvaluation() {
        boolean visible = evaluationSubmenu.isVisible();
        evaluationSubmenu.setVisible(!visible);
        evaluationSubmenu.setManaged(!visible);
    }

    @FXML
    public void initialize() {
        mainContent.getChildren().clear();
        try {
            logoImage.setImage(new Image(
                    getClass().getResourceAsStream("/org/example/images/logo1.png")));
        } catch (Exception ignored) {}
    }
    @FXML
    public void goToFront() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/front.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 900);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("LearnFlex+");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
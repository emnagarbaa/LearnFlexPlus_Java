package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML private StackPane mainContent;
    @FXML private VBox      evaluationSubmenu;
    @FXML private VBox      contenuSubmenu;
    @FXML private Button    btnContenu;
    @FXML private Button    btnEvaluation;
    @FXML private VBox      dashboardView;
    @FXML private ImageView logoImage;

    private void loadPage(String fxmlFile) {
        try {
            java.net.URL resource = getClass().getResource("/org/example/fxml/" + fxmlFile);
            if (resource == null) {
                System.err.println("❌ Fichier introuvable : " + fxmlFile);
                showComingSoon(fxmlFile);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlFile + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showComingSoon(String pageName) {
        dashboardView.setVisible(false);
        dashboardView.setManaged(false);
        mainContent.getChildren().clear();
        VBox placeholder = new VBox(20);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        Label label = new Label("🚧 Page '" + pageName + "' en cours de développement");
        label.setStyle("-fx-font-size: 18px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        placeholder.getChildren().add(label);
        mainContent.getChildren().add(placeholder);
    }

    @FXML public void showDashboard()     { 
        mainContent.getChildren().clear(); 
        dashboardView.setVisible(true);
        dashboardView.setManaged(true);
    }
    @FXML public void showUtilisateurs()  { loadPage("utilisateurs.fxml"); }
    @FXML public void showMatieres()      { loadPage("MatiereView.fxml"); }
    @FXML public void showCours()         { loadPage("CoursManage.fxml"); }
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
        boolean isVisible = !evaluationSubmenu.isVisible();
        evaluationSubmenu.setVisible(isVisible);
        evaluationSubmenu.setManaged(isVisible);
        btnEvaluation.setText(isVisible ? "  Evaluation  ▴" : "  Evaluation  ▾");
        if (isVisible) {
            contenuSubmenu.setVisible(false);
            contenuSubmenu.setManaged(false);
            btnContenu.setText("  Contenu pédagogique  ▾");
        }
    }

    @FXML
    public void toggleContenuSubmenu() {
        boolean isVisible = !contenuSubmenu.isVisible();
        contenuSubmenu.setVisible(isVisible);
        contenuSubmenu.setManaged(isVisible);
        btnContenu.setText(isVisible ? "  Contenu pédagogique  ▴" : "  Contenu pédagogique  ▾");
        if (isVisible) {
            evaluationSubmenu.setVisible(false);
            evaluationSubmenu.setManaged(false);
            btnEvaluation.setText("  Evaluation  ▾");
        }
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
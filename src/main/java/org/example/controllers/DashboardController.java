package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

    // =========================================================================
    //  INIT
    // =========================================================================

    @FXML
    public void initialize() {
        mainContent.getChildren().clear();
        try {
            logoImage.setImage(new Image(
                    getClass().getResource("/org/example/images/logo1.png").toExternalForm()
            ));
        } catch (Exception ignored) {}
    }

    // =========================================================================
    //  NAVIGATION INTERNE — charge un FXML dans le StackPane central
    // =========================================================================

    /**
     * Charge un FXML dans mainContent sans toucher à la sidebar.
     * Le FXML ciblé ne doit PAS avoir de sidebar propre.
     */
    private void loadPage(String fxmlFile) {
        try {
            var url = getClass().getResource("/org/example/fxml/" + fxmlFile);

            FXMLLoader loader = new FXMLLoader(url);

            Node view = loader.load();

            mainContent.getChildren().clear();
            mainContent.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    //  BOUTONS SIDEBAR
    // =========================================================================

    @FXML public void showDashboard()     { mainContent.getChildren().clear(); }
    @FXML public void showUtilisateurs()  { loadPage("utilisateurs.fxml"); }
    @FXML public void showContenu()       { loadPage("contenu.fxml"); }
    @FXML public void showExamens()       { loadPage("ExamenView.fxml"); }
    @FXML public void showChallenges()    { loadPage("ChallengeView.fxml"); }

    /** Questionnaire → charge quiz.fxml dans la zone centrale */
    @FXML
    public void showQuestionnaire() {
        loadPage("quiz.fxml");
    }
    @FXML public void showOrientation()   { loadPage("orientation.fxml"); }
    @FXML public void showForum()         { loadPage("forum.fxml"); }

    @FXML
    public void toggleEvaluation() {
        boolean visible = evaluationSubmenu.isVisible();
        evaluationSubmenu.setVisible(!visible);
        evaluationSubmenu.setManaged(!visible);
    }

    // =========================================================================
    //  DÉCONNEXION + LOGO → changement de scène vers front.fxml
    // =========================================================================

    @FXML
    public void logout() {
        naviguerVers("front.fxml", "LearnFlex+");
    }

    /**
     * Clic sur le logo → retour page d'accueil.
     * La méthode N'a PAS de paramètre MouseEvent car onMouseClicked dans
     * Dashboard.fxml est déclaré sans paramètre (#goToFront sur ImageView).
     * JavaFX accepte les deux formes — on garde la forme sans paramètre
     * pour rester cohérent avec le DashboardController existant.
     */
    @FXML
    public void goToFront() {
        naviguerVers("front.fxml", "LearnFlex+");
    }

    /** Change la scène entière (utilisé pour logout et logo). */
    private void naviguerVers(String fxmlFile, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/" + fxmlFile));
            Scene scene = new Scene(loader.load(), 1200, 900);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(titre);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void afficherQuiz() {
        loadPage("quiz.fxml");
    }
}
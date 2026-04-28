package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.scene.control.Label;

import java.io.IOException;

public class DashboardController {

    @FXML
    private StackPane mainContent;

    @FXML
    private VBox evaluationSubmenu;

    @FXML
    private ImageView logoImage;

    @FXML
    private Label lblUserName;

    @FXML
    private Label lblUserRole;

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/" + fxmlFile)
            );

            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());

        } catch (IOException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void showDashboard() {
        mainContent.getChildren().clear();
    }

    @FXML
    public void showUtilisateurs() {
        loadPage("usersView.fxml");
    }

    @FXML
    public void showContenu() {
        loadPage("contenu.fxml");
    }

    @FXML
    public void showExamens() {
        loadPage("ExamenView.fxml");
    }

    @FXML
    public void showChallenges() {
        loadPage("ChallengeView.fxml");
    }

    @FXML
    public void showQuestionnaire() {
        loadPage("questionnaire.fxml");
    }

    @FXML
    public void showOrientation() {
        loadPage("orientation.fxml");
    }

    @FXML
    public void showForum() {
        loadPage("forum.fxml");
    }

    @FXML
    public void logout() {
        try {
            // ── CLEAR SESSION ──────────────────────────
            Main.java.Utils.SessionManager.logout();
            // ── END CLEAR SESSION ──────────────────────

            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/login.fxml")
            );

            Stage stage = (Stage) mainContent.getScene().getWindow();

            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("LearnFlex+ Login");
            stage.centerOnScreen();
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
            logoImage.setImage(
                    new Image(getClass().getResourceAsStream("/images/logo1.png"))
            );
        } catch (Exception ignored) {}

        // ── REAL-TIME USER INFO ──────────────────────────────────
        Main.java.Models.User current = Main.java.Utils.SessionManager.getCurrentUser();
        if (current != null) {
            lblUserName.setText(current.getNom());

            // Map role to French display label
            String role = current.getRole();
            String roleDisplay;
            switch (role) {
                case "Admin", "ADMIN"     -> roleDisplay = "Administrateur";
                case "Enseignant"         -> roleDisplay = "Enseignant";
                case "Etudiant"           -> roleDisplay = "Étudiant";
                default                   -> roleDisplay = role;
            }
            lblUserRole.setText(roleDisplay);
        }
        // ── END USER INFO ────────────────────────────────────────
    }

    @FXML
    public void goToFront() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/front.fxml")
            );

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
    public void openWebsocketChat() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/hercules_chat.fxml")
            );

            Parent root = loader.load();
            Scene scene = new Scene(root, 520, 420);

            Stage stage = new Stage();
            stage.initOwner(mainContent.getScene().getWindow());
            stage.initModality(Modality.NONE);
            stage.setTitle("WebSocket Messages (TCP :8888)");
            stage.setScene(scene);
            stage.centerOnScreen();

            HerculesChatController controller = loader.getController();
            stage.setOnHidden(e -> controller.shutdown());

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openHerculesChat() {
        openWebsocketChat();
    }
}

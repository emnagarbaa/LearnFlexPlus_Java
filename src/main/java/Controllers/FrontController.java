package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class FrontController {

    @FXML
    private ImageView logo;

    @FXML
    private ImageView heroImage;

    @FXML
    private ImageView serviceImage1;

    @FXML
    private ImageView serviceImage2;

    @FXML
    private ImageView serviceImage3;

    @FXML
    public void initialize() {
        loadImage(logo, "/images/logo1.png");
        loadImage(heroImage, "/images/hero1.png");
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

    // ---------------- NAVIGATION ----------------

    @FXML
    private void goToCours() {
        navigateTo("/fxml/cours.fxml", "Cours");
    }

    @FXML
    private void explorerCours() {
        navigateTo("/fxml/cours.fxml", "Cours");
    }

    @FXML
    private void voirCours() {
        navigateTo("/fxml/cours.fxml", "Cours");
    }

    @FXML
    private void passerQuiz() {
        navigateTo("/fxml/ExamenView.fxml", "Examens");
    }

    @FXML
    private void accederForum() {
        navigateTo("/fxml/forum.fxml", "Forum");
    }

    @FXML
    private void goToConnexion() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/login.fxml")
            );

            Stage stage = (Stage) logo.getScene().getWindow();

            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);

            stage.setTitle("Connexion");
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAccueil() {
        // intentionally empty (home already loaded)
    }

    @FXML
    private void goToEvaluation() {
        navigateTo("/fxml/EvaluationFront.fxml", "Évaluation");
    }

    @FXML
    private void goToQuestionnaire() {
        navigateTo("/fxml/questionnaire.fxml", "Questionnaire");
    }

    @FXML
    private void goToOrientation() {
        navigateTo("/fxml/orientation.fxml", "Orientation");
    }

    @FXML
    private void goToForum() {
        navigateTo("/fxml/forum.fxml", "Forum");
    }

    // ---------------- CORE NAVIGATION METHOD ----------------

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) logo.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setWidth(1200);
            stage.setHeight(900);
            stage.centerOnScreen();
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- DASHBOARD ----------------

    @FXML
    private void goToDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/Dashboard.fxml")
            );

            Stage stage = (Stage) logo.getScene().getWindow();

            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);

            stage.setTitle("Dashboard");
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
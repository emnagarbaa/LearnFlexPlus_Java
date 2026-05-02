package org.example.controllers;

<<<<<<< HEAD
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.example.entities.Evenement;
import org.example.entities.Organisme;
import org.example.Models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
=======
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML private StackPane mainContent;
<<<<<<< HEAD
    @FXML private VBox evaluationSubmenu;
    @FXML private VBox orientationSubmenu;
    @FXML private VBox contenuSubmenu;
    @FXML private VBox forumSubmenu;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private ImageView logoImage;
    @FXML private HBox logoContainer;

    private void loadPage(String fxmlFile) {
        try {
            String path = "/org/example/fxml/" + fxmlFile;
            var url = getClass().getResource(path);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + path);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlFile + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML public void showDashboard() { mainContent.getChildren().clear(); }

    @FXML public void showUtilisateurs() { loadPage("usersView.fxml"); }

    @FXML public void showContenu() { loadPage("contenu.fxml"); }

    @FXML public void showExamens() { loadPage("ExamenView.fxml"); }

    @FXML public void showChallenges() { loadPage("ChallengeView.fxml"); }

    @FXML public void showQuestionnaire() { loadPage("quiz.fxml"); }

    // ── Présent dans LEUR version uniquement ──────────────────────────
    @FXML public void showOrientation() { loadPage("orientation.fxml"); }

    @FXML public void showForum() { loadPage("forum.fxml"); }
=======
    @FXML private VBox      evaluationSubmenu;
    @FXML private VBox      forumSubmenu;
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
    @FXML public void showPublication()   { loadPage("publication.fxml"); }
    @FXML public void showCommunication() { loadPage("communication.fxml"); }

    @FXML
    public void toggleEvaluation() {
        boolean visible = evaluationSubmenu.isVisible();
        evaluationSubmenu.setVisible(!visible);
        evaluationSubmenu.setManaged(!visible);
    }

    @FXML
    public void toggleForum() {
        boolean visible = forumSubmenu.isVisible();
        forumSubmenu.setVisible(!visible);
        forumSubmenu.setManaged(!visible);
    }
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))

    @FXML
    public void logout() {
        try {
<<<<<<< HEAD
            org.example.utils.SessionManager.logout();
            Parent root = FXMLLoader.load(
                    getClass().getResource("/org/example/fxml/login.fxml"));
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("LearnFlex+ Login");
            stage.centerOnScreen();
=======
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/front.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 900);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("LearnFlex+");
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

<<<<<<< HEAD
    // ── Sous-menu Contenu pédagogique ─────────────────────────────────
    @FXML
    public void toggleContenu() {
        if (contenuSubmenu != null) {
            boolean v = contenuSubmenu.isVisible();
            contenuSubmenu.setVisible(!v);
            contenuSubmenu.setManaged(!v);
        }
    }

    @FXML public void showMatiere() { loadPage("matiere.fxml"); }

    @FXML public void showCours() { loadPage("cours.fxml"); }

    // ── Sous-menu Évaluation ──────────────────────────────────────────
    @FXML
    public void toggleEvaluation() {
        if (evaluationSubmenu != null) {
            boolean v = evaluationSubmenu.isVisible();
            evaluationSubmenu.setVisible(!v);
            evaluationSubmenu.setManaged(!v);
        }
    }

    // ── Sous-menu Orientation ─────────────────────────────────────────
    @FXML
    public void toggleOrientation() {
        if (orientationSubmenu != null) {
            boolean v = orientationSubmenu.isVisible();
            orientationSubmenu.setVisible(!v);
            orientationSubmenu.setManaged(!v);
        }
    }

    // ── Sous-menu Forum ───────────────────────────────────────────────
    @FXML
    public void toggleForum() {
        if (forumSubmenu != null) {
            boolean v = forumSubmenu.isVisible();
            forumSubmenu.setVisible(!v);
            forumSubmenu.setManaged(!v);
        }
    }

    @FXML public void showPublication() { loadPage("publication.fxml"); }

    @FXML public void showCommunication() { loadPage("communication.fxml"); }

    // ── Organisme ─────────────────────────────────────────────────────
    @FXML
    public void showOrganisme() {
        try {
            String path = "/org/example/fxml/organisme.fxml";
            var url = getClass().getResource(path);
            if (url == null) { System.err.println("❌ FXML introuvable : " + path); return; }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
            OrganismeController ctrl = loader.getController();
            ctrl.setDashboardController(this);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void showAddOrganisme() {
        try {
            String path = "/org/example/fxml/addOrganisme.fxml";
            var url = getClass().getResource(path);
            if (url == null) { System.err.println("❌ FXML introuvable : " + path); return; }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
            AddOrganismeController ctrl = loader.getController();
            ctrl.setDashboardController(this);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void showEditOrganisme(Organisme organisme) {
        try {
            String path = "/org/example/fxml/editOrganisme.fxml";
            var url = getClass().getResource(path);
            if (url == null) { System.err.println("❌ FXML introuvable : " + path); return; }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
            EditOrganismeController ctrl = loader.getController();
            ctrl.setDashboardController(this);
            ctrl.setOrganisme(organisme);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Événement ─────────────────────────────────────────────────────
    @FXML
    public void showEvenement() {
        try {
            String path = "/org/example/fxml/evenement.fxml";
            var url = getClass().getResource(path);
            if (url == null) { System.err.println("❌ FXML introuvable : " + path); return; }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
            EvenementController ctrl = loader.getController();
            ctrl.setDashboardController(this);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void showAddEvenement() {
        try {
            String path = "/org/example/fxml/addEvenement.fxml";
            var url = getClass().getResource(path);
            if (url == null) { System.err.println("❌ FXML introuvable : " + path); return; }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
            AddEvenementController ctrl = loader.getController();
            ctrl.setDashboardController(this);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void showEditEvenement(Evenement evenement) {
        try {
            String path = "/org/example/fxml/EditEvenement.fxml";
            var url = getClass().getResource(path);
            if (url == null) { System.err.println("❌ FXML introuvable : " + path); return; }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
            EditEvenementController ctrl = loader.getController();
            ctrl.setDashboardController(this);
            ctrl.setEvenement(evenement);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Vue Publique (FRONT) ───────────────────────────────────────────
    @FXML
    public void goToFront() {
        try {
            User currentUser = org.example.utils.SessionManager.getCurrentUser();
            if (currentUser != null && isEnseignant(currentUser.getRole())) {
                String fxmlPath = "/org/example/fxml/front.fxml";
                var url = getClass().getResource(fxmlPath);
                if (url == null) { System.err.println("❌ FXML introuvable : " + fxmlPath); return; }
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();
                Stage stage = (Stage) mainContent.getScene().getWindow();
                stage.setScene(new Scene(root, 1200, 900));
                stage.setTitle("LearnFlex+ — Accueil");
                stage.centerOnScreen();
                stage.show();
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Accès refusé");
                alert.setHeaderText(null);
                alert.setContentText("Cette fonctionnalité est réservée aux enseignants.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isEnseignant(String role) {
        if (role == null) return false;
        return role.equalsIgnoreCase("Enseignant") || role.equalsIgnoreCase("Teacher");
    }

    @FXML public void switchToFront() { goToFront(); }

    // ── WebSocket Chat ─────────────────────────────────────────────────
    @FXML
    public void openWebsocketChat() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/hercules_chat.fxml")
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

=======
    @FXML
    public void goToFront() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/front.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 900);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("LearnFlex+");
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
<<<<<<< HEAD
    @FXML
    public void openWebsocketChatMouse(javafx.scene.input.MouseEvent event) {
        openWebsocketChat();
    }

    @FXML
    public void openHerculesChat() {
        openWebsocketChat();
    }
    // ── Initialisation ─────────────────────────────────────────────────
    @FXML
    public void initialize() {

        if (logoImage != null) {
            logoImage.setOnMouseClicked(event -> goToFront());
            logoImage.setStyle("-fx-cursor: hand;");
            try {
                var stream = getClass().getResourceAsStream("/org/example/images/logo1.png");
                if (stream != null) logoImage.setImage(new Image(stream));
            } catch (Exception e) {
                System.err.println("❌ Erreur chargement logo : " + e.getMessage());
            }
        }
        try {
            User currentUser = org.example.utils.SessionManager.getCurrentUser();
            if (currentUser != null) {
                if (lblUserName != null) lblUserName.setText(currentUser.getNom());
                if (lblUserRole != null) lblUserRole.setText(getRoleDisplay(currentUser.getRole()));
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement infos utilisateur: " + e.getMessage());
        }
    }

    private String getRoleDisplay(String role) {
        if (role == null) return "Utilisateur";
        switch (role) {
            case "Admin": case "ADMIN": return "Administrateur";
            case "Enseignant": case "ENSEIGNANT": return "Enseignant";
            case "Etudiant": case "ETUDIANT": return "Étudiant";
            default: return role;
        }
=======

    @FXML
    public void initialize() {
        mainContent.getChildren().clear();
        try {
            logoImage.setImage(new Image(
                    getClass().getResourceAsStream("/org/example/images/logo1.png")));

            // ===== AJOUTEZ CES 2 LIGNES =====
            logoImage.setOnMouseClicked(event -> goToFront());
            logoImage.setStyle("-fx-cursor: hand;");
            // ================================

        } catch (Exception ignored) {}
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
    }
}
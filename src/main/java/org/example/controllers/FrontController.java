package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FrontController {

    @FXML private ImageView logo;
    @FXML private ImageView heroImage;
    @FXML private ImageView serviceImage1;
    @FXML private ImageView serviceImage2;
    @FXML private ImageView serviceImage3;
    @FXML private VBox forumDropdown;
    @FXML private Button btnConnexion;
    @FXML private MenuButton userMenu;
    @FXML private HBox logoContainer;

    // Boutons de navigation
    @FXML private Button btnEvaluation;
    @FXML private Button btnGestionExamens;  // Bouton dans la section évaluation

    @FXML private MenuItem menuDashboard;
    @FXML private SeparatorMenuItem sepDashboard;

    @FXML
    public void initialize() {
        loadImage(logo,          "/org/example/images/logo1.png");
        loadImage(heroImage,     "/org/example/images/hero1.png");
        loadImage(serviceImage1, "/org/example/images/service1.png");
        loadImage(serviceImage2, "/org/example/images/service2.png");
        loadImage(serviceImage3, "/org/example/images/service3.png");

        // Initialiser l'affichage de la zone utilisateur
        refreshAuthArea();
    }

    private void refreshAuthArea() {
        boolean loggedIn = SessionManager.isLoggedIn();

        // Connexion button ↔ user menu
        if (btnConnexion != null) {
            btnConnexion.setVisible(!loggedIn);
            btnConnexion.setManaged(!loggedIn);
        }

        if (userMenu != null) {
            userMenu.setVisible(loggedIn);
            userMenu.setManaged(loggedIn);
        }

        if (loggedIn) {
            User user = SessionManager.getCurrentUser();
            if (user != null && userMenu != null) {
                userMenu.setText("👤  " + user.getNom());
            }

            if (user != null) {
                boolean canSeeDashboard = isDashboardRole(user.getRole());
                if (menuDashboard != null) {
                    menuDashboard.setVisible(canSeeDashboard);
                }
                if (sepDashboard != null) {
                    sepDashboard.setVisible(canSeeDashboard);
                }

                // ★★★★★ MODIFICATION : Afficher le bouton Gestion Examens seulement pour les enseignants ★★★★★
                boolean isEnseignant = isEnseignant(user.getRole());
                if (btnGestionExamens != null) {
                    btnGestionExamens.setVisible(isEnseignant);
                    btnGestionExamens.setManaged(isEnseignant);
                }

                // Optionnel: Changer le texte du bouton Évaluation pour les enseignants
                if (btnEvaluation != null && isEnseignant) {
                    btnEvaluation.setText("📝 Évaluation");
                }
            }
        } else {
            // Si non connecté, cacher le bouton
            if (btnGestionExamens != null) {
                btnGestionExamens.setVisible(false);
                btnGestionExamens.setManaged(false);
            }
        }
    }

    private void loadImage(ImageView imageView, String path) {
        if (imageView == null) return;
        var stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.err.println("❌ Image introuvable : " + path);
            return;
        }
        imageView.setImage(new Image(stream));
    }

    private boolean isDashboardRole(String role) {
        if (role == null) return false;
        return role.equalsIgnoreCase("Admin")
                || role.equalsIgnoreCase("Enseignant")
                || role.equalsIgnoreCase("ADMIN");
    }

    private boolean isEnseignant(String role) {
        if (role == null) return false;
        return role.equalsIgnoreCase("Enseignant")
                || role.equalsIgnoreCase("ENSEIGNANT")
                || role.equalsIgnoreCase("Teacher")
                || role.equalsIgnoreCase("TEACHER");
    }

    // ── LOGO CLICK → switch to back (dashboard) ─────────────────────
    @FXML
    private void switchToBack(MouseEvent event) {
        if (!SessionManager.isLoggedIn()) {
            navigateTo("/org/example/fxml/login.fxml", "Connexion", 1200, 800);
            return;
        }

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && isDashboardRole(currentUser.getRole())) {
            navigateTo("/org/example/fxml/dashboard.fxml", "Dashboard", 1280, 800);
        } else {
            System.out.println("❌ Accès refusé : vous n'avez pas les droits pour accéder au dashboard");
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING
            );
            alert.setTitle("Accès refusé");
            alert.setHeaderText(null);
            alert.setContentText("Vous devez être connecté en tant qu'Administrateur ou Enseignant pour accéder au dashboard.");
            alert.showAndWait();
        }
    }

    @FXML
    private void goToDashboard() {
        if (!SessionManager.isLoggedIn()) return;
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;
        if (!isDashboardRole(currentUser.getRole())) return;
        navigateTo("/org/example/fxml/dashboard.fxml", "Dashboard", 1280, 800);
    }

    @FXML
    private void goToDashboardMenu() {
        navigateTo("/org/example/fxml/dashboard.fxml", "Dashboard", 1280, 800);
    }

    @FXML
    private void goToProfile() {
        navigateTo("/org/example/fxml/profile.fxml", "Mon profil", 1200, 800);
    }

    @FXML
    private void logout() {
        SessionManager.logout();
        navigateTo("/org/example/fxml/front.fxml", "LearnFlex+", 1200, 900);
    }

    @FXML
    private void goToConnexion() {
        navigateTo("/org/example/fxml/login.fxml", "Connexion", 1200, 800);
    }

    // ★★★★★ MÉTHODE pour la gestion des examens (enseignant) ★★★★★
    @FXML
    private void goToGestionExamens() {
        navigateTo("/org/example/fxml/enseignant_examen.fxml", "Gestion des Examens", 1400, 800);
    }

    private void navigateTo(String fxml, String title, double w, double h) {
        try {
            if (logo == null && heroImage == null) {
                System.err.println("❌ Impossible de naviguer, aucun node de référence trouvé");
                return;
            }

            var url = getClass().getResource(fxml);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + fxml);
                return;
            }

            Parent root = FXMLLoader.load(url);
            Stage stage;

            if (logo != null && logo.getScene() != null) {
                stage = (Stage) logo.getScene().getWindow();
            } else if (heroImage != null && heroImage.getScene() != null) {
                stage = (Stage) heroImage.getScene().getWindow();
            } else {
                System.err.println("❌ Impossible d'obtenir le stage");
                return;
            }

            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation vers " + fxml + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Navigation principale ─────────────────────────────────────────
    @FXML
    private void goToCours() {
        navigateTo("/org/example/fxml/EtudiantCoursView.fxml", "Catalogue des Cours", 1200, 800);
    }

    @FXML
    private void explorerCours() {
        navigateTo("/org/example/fxml/EtudiantCoursView.fxml", "Catalogue des Cours", 1200, 800);
    }

    @FXML
    private void voirCours() {
        navigateTo("/org/example/fxml/EtudiantCoursView.fxml", "Catalogue des Cours", 1200, 800);
    }

    @FXML
    private void passerQuiz() {
        navigateTo("/org/example/fxml/ExamenView.fxml", "Examens", 1200, 800);
    }

    @FXML
    private void goToAccueil() {
        navigateTo("/org/example/fxml/front.fxml", "Accueil", 1200, 900);
    }

    @FXML
    private void goToEvaluation() {
        // Si c'est un enseignant, on peut afficher un message ou rediriger vers la gestion
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && isEnseignant(currentUser.getRole())) {
            // Pour l'enseignant, on va vers la gestion des examens
            goToGestionExamens();
        } else {
            // Pour l'étudiant, on va vers l'évaluation normale
            navigateTo("/org/example/fxml/EvaluationFront.fxml", "Évaluation", 1200, 800);
        }
    }

    @FXML
    private void goToQuestionnaire() {
        navigateTo("/org/example/fxml/quiz_front.fxml", "Questionnaire", 1200, 800);
    }

    @FXML
    private void goToOrientation() {
        navigateTo("/org/example/fxml/OrientationFront.fxml", "Orientation", 1200, 800);
    }

    // ── Sous-menu Forum ───────────────────────────────────────────────
    @FXML
    private void showForumDropdown() {
        if (forumDropdown != null) {
            forumDropdown.setVisible(true);
            forumDropdown.setManaged(true);
        }
    }

    @FXML
    private void hideForumDropdown() {
        if (forumDropdown != null) {
            forumDropdown.setVisible(false);
            forumDropdown.setManaged(false);
        }
    }

    @FXML
    private void goToPublication() {
        hideForumDropdown();
        navigateTo("/org/example/fxml/PublicationsFrontPage.fxml", "Publications", 1200, 800);
    }

    @FXML
    private void goToCommunication() {
        hideForumDropdown();
        navigateTo("/org/example/fxml/CommunicationsFrontPage.fxml", "Communication", 1200, 800);
    }

    @FXML
    private void accederForum() {
        navigateTo("/org/example/fxml/PublicationsFrontPage.fxml", "Publications", 1200, 800);
    }
}
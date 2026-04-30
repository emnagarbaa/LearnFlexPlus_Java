package Controllers;

import Main.java.Models.User;
import Main.java.Utils.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class FrontController {

    @FXML private ImageView  logo;
    @FXML private ImageView  heroImage;

    // Auth area
    @FXML private Button     btnConnexion;
    @FXML private MenuButton userMenu;
    @FXML private MenuItem   menuDashboard;
    @FXML private SeparatorMenuItem sepDashboard;

    // ── INIT ──────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        loadImage(logo,      "/images/logo1.png");
        loadImage(heroImage, "/images/hero1.png");
        refreshAuthArea();
    }

    /**
     * Shows either the Connexion button or the user dropdown
     * depending on whether a session exists.
     * Also controls Dashboard menu item visibility by role.
     */
    private void refreshAuthArea() {
        boolean loggedIn = SessionManager.isLoggedIn();

        // Connexion button ↔ user menu
        btnConnexion.setVisible(!loggedIn);
        btnConnexion.setManaged(!loggedIn);

        userMenu.setVisible(loggedIn);
        userMenu.setManaged(loggedIn);

        if (loggedIn) {
            User user = SessionManager.getCurrentUser();

            // Show user's name on the menu button
            userMenu.setText("👤  " + user.getNom());

            // Dashboard item: only Admin and Enseignant
            boolean canSeeDashboard = isDashboardRole(user.getRole());
            menuDashboard.setVisible(canSeeDashboard);
            sepDashboard.setVisible(canSeeDashboard);
        }
    }

    private boolean isDashboardRole(String role) {
        if (role == null) return false;
        return role.equalsIgnoreCase("Admin")
                || role.equalsIgnoreCase("Enseignant")
                || role.equalsIgnoreCase("ADMIN");
    }

    // ── LOGO CLICK → dashboard (blocked for students) ─────────────────────
    @FXML
    private void goToDashboard() {
        if (!SessionManager.isLoggedIn()) return;           // not logged in → do nothing
        if (!isDashboardRole(SessionManager.getCurrentUser().getRole())) return; // student → do nothing
        navigateTo("/fxml/dashboard.fxml", "Dashboard", 1200, 800);
    }

    // ── DASHBOARD MENU ITEM ────────────────────────────────────────────────
    @FXML
    private void goToDashboardMenu() {
        navigateTo("/fxml/dashboard.fxml", "Dashboard", 1200, 800);
    }

    // ── PROFILE ────────────────────────────────────────────────────────────
    @FXML
    private void goToProfile() {
        navigateTo("/fxml/profile.fxml", "Mon profil", 1200, 800);
    }

    // ── LOGOUT ─────────────────────────────────────────────────────────────
    @FXML
    private void logout() {
        SessionManager.logout();
        // Reload front page so navbar resets to "Connexion"
        navigateTo("/fxml/front.fxml", "LearnFlex+", 1200, 900);
    }

    // ── CONNEXION BUTTON ───────────────────────────────────────────────────
    @FXML
    private void goToConnexion() {
        navigateTo("/fxml/login.fxml", "Connexion", 1200, 800);
    }

    // ── NAV BUTTONS ────────────────────────────────────────────────────────
    @FXML private void goToAccueil()       { /* already home */ }
    @FXML private void goToCours()         { navigateTo("/fxml/cours.fxml",           "Cours",         1200, 900); }
    @FXML private void explorerCours()     { navigateTo("/fxml/cours.fxml",           "Cours",         1200, 900); }
    @FXML private void voirCours()         { navigateTo("/fxml/cours.fxml",           "Cours",         1200, 900); }
    @FXML private void passerQuiz()        { navigateTo("/fxml/ExamenView.fxml",      "Examens",       1200, 900); }
    @FXML private void accederForum()      { navigateTo("/fxml/forum.fxml",           "Forum",         1200, 900); }
    @FXML private void goToEvaluation()    { navigateTo("/fxml/EvaluationFront.fxml", "Évaluation",    1200, 900); }
    @FXML private void goToQuestionnaire() { navigateTo("/fxml/questionnaire.fxml",   "Questionnaire", 1200, 900); }
    @FXML private void goToOrientation()   { navigateTo("/fxml/orientation.fxml",     "Orientation",   1200, 900); }
    @FXML private void goToForum()         { navigateTo("/fxml/forum.fxml",           "Forum",         1200, 900); }

    // ── HELPERS ────────────────────────────────────────────────────────────
    private void loadImage(ImageView iv, String path) {
        var stream = getClass().getResourceAsStream(path);
        if (stream == null) { System.err.println("❌ Image introuvable : " + path); return; }
        iv.setImage(new Image(stream));
    }

    private void navigateTo(String fxml, String title, double w, double h) {
        try {
            Parent root  = FXMLLoader.load(getClass().getResource(fxml));
            Stage  stage = (Stage) logo.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package org.example.controllers;

<<<<<<< HEAD
import org.example.Models.User;
import org.example.utils.SessionManager;

=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
<<<<<<< HEAD
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
public class FrontController {

    @FXML private ImageView  logo;
    @FXML private ImageView  heroImage;
    @FXML private VBox forumDropdown;
    @FXML private Button btnConnexion;
    @FXML private MenuButton userMenu;
    @FXML private MenuItem menuDashboard;
    @FXML private SeparatorMenuItem sepDashboard;


    // ── INIT ──────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        loadImage(logo,      "org/example/images/logo1.png");
        loadImage(heroImage, "org/example/images/hero1.png");
        refreshAuthArea();
    }
    @FXML
    private void goToAccueil(ActionEvent event) {
        switchScene(event, "/org/example/fxml/front.fxml", "Accueil");
    }

    @FXML
    private void goToCours(ActionEvent event) {
        switchScene(event, "/org/example/fxml/EtudiantCoursView.fxml", "Cours");
    }

    @FXML
    private void goToEvaluation(ActionEvent event) {
        switchScene(event, "/org/example/fxml/EvaluationFront.fxml", "Évaluation");
    }

    @FXML
    private void goToQuestionnaire(ActionEvent event) {
        switchScene(event, "/org/example/fxml/OrientationFront.fxml", "Questionnaire");
    }

    @FXML
    private void goToOrientation(ActionEvent event) {
        switchScene(event, "/org/example/fxml/OrientationFront.fxml", "Orientation");
    }

    @FXML
    private void goToCommunication(ActionEvent event) {
        switchScene(event, "/org/example/fxml/evenement.fxml", "Communication");
    }

    @FXML
    private void goToConnexion(ActionEvent event) {
        switchScene(event, "/org/example/fxml/login.fxml", "Connexion");
    }

    @FXML
    void goToProfile(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/fxml/Profile.fxml"));
            Scene scene = new Scene(root);

            Stage stage = (Stage) ((MenuItem) event.getSource())
                    .getParentPopup()
                    .getOwnerWindow();

            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout(ActionEvent event) {
        SessionManager.logout();
        switchScene(event, "/org/example/fxml/login.fxml", "Connexion");
    }

    @FXML
    private void explorerCours(ActionEvent event) {
        switchScene(event, "/org/example/fxml/EtudiantCoursView.fxml", "Cours");
    }

    @FXML
    private void voirCours(ActionEvent event) {
        switchScene(event, "/org/example/fxml/EtudiantCoursView.fxml", "Cours");
    }

    @FXML
    private void passerQuiz(ActionEvent event) {
        switchScene(event, "/org/example/fxml/EvaluationFront.fxml", "Quiz");
    }

    @FXML
    private void accederForum(ActionEvent event) {
        switchScene(event, "/org/example/fxml/EvenementFront.fxml", "Forum");
    }

    @FXML
    private void showForumDropdown(MouseEvent event) {
        forumDropdown.setVisible(true);
        forumDropdown.setManaged(true);
    }

    @FXML
    private void hideForumDropdown(MouseEvent event) {
        forumDropdown.setVisible(false);
        forumDropdown.setManaged(false);
    }
    /**
     * Shows either the Connexion button or the user dropdown
     * depending on whether a session exists.
     * Also controls Dashboard menu item visibility by role.
     */
    private void refreshAuthArea() {
        boolean loggedIn = SessionManager.isLoggedIn();

        btnConnexion.setVisible(!loggedIn);
        btnConnexion.setManaged(!loggedIn);

        userMenu.setVisible(loggedIn);
        userMenu.setManaged(loggedIn);

        if (loggedIn) {
            User user = SessionManager.getCurrentUser();

            userMenu.setText("👤  " + user.getNom());

            boolean canSeeDashboard = isDashboardRole(user.getRole());
            menuDashboard.setVisible(canSeeDashboard);
            sepDashboard.setVisible(canSeeDashboard);

            // ✅ AJOUT ICI
            logo.setVisible(canSeeDashboard);
            logo.setManaged(canSeeDashboard);

        } else {
            // si pas connecté → cacher logo
            logo.setVisible(false);
            logo.setManaged(false);
        }
    }

    @FXML
    private void goToPublication(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/fxml/publication.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Publications");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void switchScene(javafx.event.Event event, String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));

            Object source = event.getSource();
            Stage stage;

            if (source instanceof MenuItem) {
                stage = (Stage) ((MenuItem) source)
                        .getParentPopup()
                        .getOwnerWindow();
            } else {
                stage = (Stage) ((Node) source)
                        .getScene()
                        .getWindow();
            }

            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean isDashboardRole(String role) {
        if (role == null) return false;
        return role.equalsIgnoreCase("Admin")
                || role.equalsIgnoreCase("Enseignant")
                || role.equalsIgnoreCase("ADMIN");
    }
    @FXML
    private void switchToBack(MouseEvent event) {
        // ta logique de navigation ici
        switchScene(event, "/org/example/fxml/dashboard.fxml", "Dashboard");
    }
    // ── LOGO CLICK → dashboard (blocked for students) ─────────────────────
    @FXML
    private void goToDashboard() {
        if (!SessionManager.isLoggedIn()) return;           // not logged in → do nothing
        if (!isDashboardRole(SessionManager.getCurrentUser().getRole())) return; // student → do nothing
        navigateTo("/fxml/dashboard.fxml", "Dashboard", 1200, 800);
    }
    @FXML
    private void goToDashboardMenu(ActionEvent event) {
        if (!SessionManager.isLoggedIn()) return;
        if (!isDashboardRole(SessionManager.getCurrentUser().getRole())) {
            // Optionnel : afficher un message
            System.out.println("Accès refusé : rôle insuffisant");
            return;
        }
        switchScene(event, "/org/example/fxml/dashboard.fxml", "Dashboard");
    }
    // ── PROFILE ────────────────────────────────────────────────────────────
    @FXML
    private void goToProfile() {
        navigateTo("/org/example/fxml/profile.fxml", "Mon profil", 1200, 800);
    }

    // ── LOGOUT ─────────────────────────────────────────────────────────────
    @FXML
    private void logout() {
        SessionManager.logout();
        // Reload front page so navbar resets to "Connexion"
        navigateTo("org/example/fxml/front.fxml", "LearnFlex+", 1200, 900);
    }

    // ── CONNEXION BUTTON ───────────────────────────────────────────────────
    @FXML
    private void goToConnexion() {
        navigateTo("/org/example/fxml/login.fxml", "Connexion", 1200, 800);
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
=======
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FrontController {

    @FXML private ImageView logo;
    @FXML private ImageView heroImage;
    @FXML private ImageView serviceImage1;
    @FXML private ImageView serviceImage2;
    @FXML private ImageView serviceImage3;
    @FXML private VBox forumMenuPane;
    @FXML private VBox forumDropdown;


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
    }

    // ===== DROPDOWN FORUM =====
    @FXML
    public void showForumMenu() {
        forumDropdown.setVisible(true);
        forumDropdown.setManaged(true);
        forumDropdown.toFront();
    }

    @FXML
    public void hideForumMenu() {
        forumDropdown.setVisible(false);
        forumDropdown.setManaged(false);
    }

    // ===== NAVIGATION =====
    @FXML private void goToCours()         { navigateTo("/org/example/fxml/cours.fxml", "Cours"); }
    @FXML private void explorerCours()     { navigateTo("/org/example/fxml/cours.fxml", "Cours"); }
    @FXML private void voirCours()         { navigateTo("/org/example/fxml/cours.fxml", "Cours"); }
    @FXML private void passerQuiz()        { navigateTo("/org/example/fxml/ExamenView.fxml", "Examens"); }
    @FXML private void accederForum()      { navigateTo("/org/example/fxml/forum.fxml", "Forum"); }
    @FXML private void goToConnexion()     { navigateTo("/org/example/fxml/login.fxml", "Connexion"); }
    @FXML private void goToAccueil()       { navigateTo("/org/example/fxml/front.fxml", "LearnFlex+"); }
    @FXML private void goToEvaluation()    { navigateTo("/org/example/fxml/EvaluationFront.fxml", "Évaluation"); }
    @FXML private void goToQuestionnaire() { navigateTo("/org/example/fxml/questionnaire.fxml", "Questionnaire"); }
    @FXML private void goToOrientation()   { navigateTo("/org/example/fxml/orientation.fxml", "Orientation"); }
    @FXML private void goToForum()         { navigateTo("/org/example/fxml/forum.fxml", "Forum"); }
    @FXML private void goToPublication()   { navigateTo("/org/example/fxml/PublicationsFrontPage.fxml", "Publications"); }
    @FXML private void goToCommunication() { navigateTo("/org/example/fxml/CommunicationsFrontPage.fxml", "Communications"); }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) logo.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))

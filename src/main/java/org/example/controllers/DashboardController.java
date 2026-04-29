package org.example.controllers;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;  // ← AJOUT IMPORT MANQUANT
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.example.entities.Evenement;
import org.example.entities.Organisme;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML private StackPane mainContent;
    @FXML private VBox evaluationSubmenu;
    @FXML private VBox orientationSubmenu;
    @FXML private VBox contenuSubmenu;
    @FXML private VBox forumSubmenu;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private ImageView logoImage;
    @FXML private HBox logoContainer;  // Maintenant HBox est importé

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
        loadPage("quiz.fxml");
    }

    @FXML
    public void showForum() {
        loadPage("forum.fxml");
    }

    @FXML
    public void logout() {
        try {
            // ── CLEAR SESSION ──────────────────────────
            org.example.utils.SessionManager.logout();
            // ── END CLEAR SESSION ──────────────────────

            Parent root = FXMLLoader.load(
                    getClass().getResource("/org/example/fxml/login.fxml"));

            Stage stage = (Stage) mainContent.getScene().getWindow();

            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("LearnFlex+ Login");
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Sous-menu Contenu pédagogique ─────────────────────
    @FXML
    public void toggleContenu() {
        if (contenuSubmenu != null) {
            boolean v = contenuSubmenu.isVisible();
            contenuSubmenu.setVisible(!v);
            contenuSubmenu.setManaged(!v);
        }
    }

    @FXML
    public void showMatiere() {
        loadPage("matiere.fxml");
    }

    @FXML
    public void showCours() {
        loadPage("cours.fxml");
    }

    // ── Sous-menu Évaluation ──────────────────────────────
    @FXML
    public void toggleEvaluation() {
        if (evaluationSubmenu != null) {
            boolean v = evaluationSubmenu.isVisible();
            evaluationSubmenu.setVisible(!v);
            evaluationSubmenu.setManaged(!v);
        }
    }

    // ── Sous-menu Orientation ─────────────────────────────
    @FXML
    public void toggleOrientation() {
        if (orientationSubmenu != null) {
            boolean v = orientationSubmenu.isVisible();
            orientationSubmenu.setVisible(!v);
            orientationSubmenu.setManaged(!v);
        }
    }

    // ── Sous-menu Forum ───────────────────────────────────
    @FXML
    public void toggleForum() {
        if (forumSubmenu != null) {
            boolean v = forumSubmenu.isVisible();
            forumSubmenu.setVisible(!v);
            forumSubmenu.setManaged(!v);
        }
    }

    @FXML
    public void showPublication() {
        loadPage("publication.fxml");
    }

    @FXML
    public void showCommunication() {
        loadPage("communication.fxml");
    }

    // ── Organisme ─────────────────────────────────────────
    @FXML
    public void showOrganisme() {
        try {
            String path = "/org/example/fxml/organisme.fxml";
            var url = getClass().getResource(path);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + path);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
            OrganismeController ctrl = loader.getController();
            ctrl.setDashboardController(this);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement organisme.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showAddOrganisme() {
        try {
            String path = "/org/example/fxml/addOrganisme.fxml";
            var url = getClass().getResource(path);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + path);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
            AddOrganismeController ctrl = loader.getController();
            ctrl.setDashboardController(this);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement addOrganisme.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showEditOrganisme(Organisme organisme) {
        try {
            String path = "/org/example/fxml/editOrganisme.fxml";
            var url = getClass().getResource(path);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + path);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
            EditOrganismeController ctrl = loader.getController();
            ctrl.setDashboardController(this);
            ctrl.setOrganisme(organisme);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement editOrganisme.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Événement ─────────────────────────────────────────
    @FXML
    public void showEvenement() {
        try {
            String path = "/org/example/fxml/evenement.fxml";
            var url = getClass().getResource(path);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + path);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
            EvenementController ctrl = loader.getController();
            ctrl.setDashboardController(this);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement evenement.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showAddEvenement() {
        try {
            String path = "/org/example/fxml/addEvenement.fxml";
            var url = getClass().getResource(path);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + path);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
            AddEvenementController ctrl = loader.getController();
            ctrl.setDashboardController(this);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement addEvenement.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showEditEvenement(Evenement evenement) {
        try {
            String path = "/org/example/fxml/EditEvenement.fxml";
            var url = getClass().getResource(path);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + path);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
            EditEvenementController ctrl = loader.getController();
            ctrl.setDashboardController(this);
            ctrl.setEvenement(evenement);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement EditEvenement.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Vue Publique (FRONT) ──────────────────────────────────────
    @FXML
    public void goToFront() {
        try {
            // Vérifier si l'utilisateur est connecté et a le rôle Enseignant
            org.example.entities.User currentUser = org.example.utils.SessionManager.getCurrentUser();

            if (currentUser != null && isEnseignant(currentUser.getRole())) {
                // Rediriger vers front.fxml
                String fxmlPath = "/org/example/fxml/front.fxml";
                var url = getClass().getResource(fxmlPath);

                if (url == null) {
                    System.err.println("❌ FXML introuvable : " + fxmlPath);
                    return;
                }

                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();
                Stage stage = (Stage) mainContent.getScene().getWindow();
                Scene scene = new Scene(root, 1200, 900);
                stage.setScene(scene);
                stage.setTitle("LearnFlex+ — Accueil");
                stage.centerOnScreen();
                stage.show();
            } else {
                System.out.println("❌ Accès refusé : Vous devez être connecté en tant qu'Enseignant");
                // Optionnel : afficher une alerte
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Accès refusé");
                alert.setHeaderText(null);
                alert.setContentText("Cette fonctionnalité est réservée aux enseignants.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation vers front.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour vérifier si l'utilisateur est un enseignant
    private boolean isEnseignant(String role) {
        if (role == null) return false;
        return role.equalsIgnoreCase("Enseignant")
                || role.equalsIgnoreCase("ENSEIGNANT")
                || role.equalsIgnoreCase("Teacher")
                || role.equalsIgnoreCase("TEACHER");
    }

    @FXML
    public void switchToFront() {
        goToFront();
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
            stage.setOnHidden(e -> {
                if (controller != null) controller.shutdown();
            });

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openHerculesChat() {
        openWebsocketChat();
    }

    // Méthode d'initialisation
    @FXML
    public void initialize() {
        // Configuration du logo cliquable
        if (logoImage != null) {
            logoImage.setOnMouseClicked(event -> goToFront());
            logoImage.setStyle("-fx-cursor: hand;");
        }

        // Charger l'image du logo
        try {
            var stream = getClass().getResourceAsStream("/org/example/images/logo1.png");
            if (stream != null && logoImage != null) {
                logoImage.setImage(new Image(stream));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement logo : " + e.getMessage());
        }

        // Afficher les infos utilisateur
        try {
            org.example.entities.User currentUser = org.example.utils.SessionManager.getCurrentUser();
            if (currentUser != null) {
                if (lblUserName != null) {
                    lblUserName.setText(currentUser.getNom());
                }
                if (lblUserRole != null) {
                    String role = currentUser.getRole();
                    String roleDisplay = getRoleDisplay(role);
                    lblUserRole.setText(roleDisplay);
                }
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
    }
}
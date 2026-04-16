package org.example.fxml;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class DashboardController {

    @FXML private StackPane mainContent;
    @FXML private VBox evaluationSubmenu;
    @FXML private VBox orientationSubmenu;

    private void loadPage(String fxmlFile) {
        try {
            String path = "/org/example/fxml/" + fxmlFile;
            var url = getClass().getResource(path);
            if (url == null) { System.err.println("❌ FXML introuvable : " + path); return; }
            FXMLLoader loader = new FXMLLoader(url);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(loader.load());
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlFile + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML public void showDashboard()     { mainContent.getChildren().clear(); }
    @FXML public void showUtilisateurs()  { loadPage("utilisateurs.fxml"); }
    @FXML public void showContenu()       { loadPage("contenu.fxml"); }
    @FXML public void showExamens()       { loadPage("ExamenView.fxml"); }
    @FXML public void showChallenges()    { loadPage("ChallengeView.fxml"); }
    @FXML public void showQuestionnaire() { loadPage("questionnaire.fxml"); }
    @FXML public void showForum()         { loadPage("forum.fxml"); }
    @FXML public void logout()            { System.exit(0); }

    @FXML
    public void toggleEvaluation() {
        boolean v = evaluationSubmenu.isVisible();
        evaluationSubmenu.setVisible(!v);
        evaluationSubmenu.setManaged(!v);
    }

    @FXML
    public void toggleOrientation() {
        boolean v = orientationSubmenu.isVisible();
        orientationSubmenu.setVisible(!v);
        orientationSubmenu.setManaged(!v);
    }

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
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement organisme.fxml : " + e.getMessage());
            e.printStackTrace();
        }
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
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement addOrganisme.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

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
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement evenement.fxml : " + e.getMessage());
            e.printStackTrace();
        }
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
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement addEvenement.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        mainContent.getChildren().clear();
        if (evaluationSubmenu  != null) { evaluationSubmenu.setVisible(false);  evaluationSubmenu.setManaged(false); }
        if (orientationSubmenu != null) { orientationSubmenu.setVisible(false); orientationSubmenu.setManaged(false); }
    }
}
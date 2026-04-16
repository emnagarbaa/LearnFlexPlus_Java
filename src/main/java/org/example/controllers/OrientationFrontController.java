package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Services.EvenementService;
import org.example.entities.Evenement;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrientationFrontController {

    @FXML private HBox cardsContainer;
    @FXML private VBox emptyBox;
    @FXML private ImageView logo;

    private final EvenementService service  = new EvenementService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        try {
            List<Evenement> list = service.findAll();
            if (list.isEmpty()) {
                emptyBox.setVisible(true);
                emptyBox.setManaged(true);
            } else {
                VBox wrapper = (VBox) cardsContainer.getParent();
                int baseIndex = wrapper.getChildren().indexOf(cardsContainer);
                HBox currentRow = cardsContainer;
                int count = 0;
                for (Evenement e : list) {
                    if (count > 0 && count % 3 == 0) {
                        currentRow = new HBox(24);
                        currentRow.setStyle("-fx-padding:0 0 24 0;");
                        wrapper.getChildren().add(baseIndex + (count / 3), currentRow);
                    }
                    currentRow.getChildren().add(buildCard(e));
                    count++;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ── Navigation Header ─────────────────────────────────────────────

    @FXML
    private void switchToBack() {
        navigateTo("/org/example/fxml/orientationBack.fxml", "Back — LearnFlex+");
    }

    @FXML
    private void goToAccueil() {
        navigateTo("/org/example/fxml/accueil.fxml", "Accueil — LearnFlex+");
    }

    @FXML
    private void goToCours() {
        navigateTo("/org/example/fxml/cours.fxml", "Cours — LearnFlex+");
    }

    @FXML
    private void goToEvaluation() {
        navigateTo("/org/example/fxml/evaluation.fxml", "Évaluation — LearnFlex+");
    }

    @FXML
    private void goToQuestionnaire() {
        navigateTo("/org/example/fxml/questionnaire.fxml", "Questionnaire — LearnFlex+");
    }

    @FXML
    private void goToOrientation() {
        navigateTo("/org/example/fxml/orientationFront.fxml", "Orientation — LearnFlex+");
    }

    @FXML
    private void goToForum() {
        navigateTo("/org/example/fxml/forum.fxml", "Forum — LearnFlex+");
    }

    @FXML
    private void goToConnexion() {
        navigateTo("/org/example/fxml/connexion.fxml", "Connexion — LearnFlex+");
    }

    // ── Utilitaire navigation ─────────────────────────────────────────

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1200, 850));
        } catch (Exception ex) {
            System.err.println("❌ Erreur navigation vers " + fxmlPath + " : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ── Cards ─────────────────────────────────────────────────────────

    private VBox buildCard(Evenement e) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color:white; -fx-background-radius:20;" +
                "-fx-border-color:rgba(151,195,162,0.3); -fx-border-radius:20; -fx-border-width:1;");
        HBox.setHgrow(card, Priority.ALWAYS);

        // Header
        VBox header = new VBox(12);
        header.setStyle("-fx-background-color:#f0f7f2; -fx-background-radius:20 20 0 0;" +
                "-fx-padding:24 22 20 22; -fx-border-color:rgba(151,195,162,0.2); -fx-border-width:0 0 1 0;");

        Label badge = new Label(e.getMode() != null ? e.getMode().toUpperCase() : "ÉVÉNEMENT");
        badge.setStyle("-fx-background-color:#1f4f65; -fx-text-fill:white;" +
                "-fx-background-radius:20; -fx-font-size:11px;" +
                "-fx-font-weight:bold; -fx-padding:5 12;");

        Label titre = new Label(e.getTitre());
        titre.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#1a1a2e; -fx-wrap-text:true;");
        titre.setWrapText(true);
        header.getChildren().addAll(badge, titre);

        // Body
        VBox body = new VBox(16);
        body.setStyle("-fx-padding:20 22;");
        VBox.setVgrow(body, Priority.ALWAYS);

        String desc = e.getDescription() != null ? e.getDescription() : "";
        if (desc.length() > 120) desc = desc.substring(0, 120) + "…";
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#555; -fx-wrap-text:true;");
        descLabel.setWrapText(true);

        HBox detailGrid = new HBox(10);
        VBox col1 = new VBox(10);
        VBox col2 = new VBox(10);
        HBox.setHgrow(col1, Priority.ALWAYS);
        HBox.setHgrow(col2, Priority.ALWAYS);

        col1.getChildren().add(detailBox("📅", e.getDateDebut() != null ? e.getDateDebut().format(DATE_FMT) : "-"));
        col1.getChildren().add(detailBox("📍", e.getLieu() != null ? e.getLieu() : "-"));
        col2.getChildren().add(detailBox("🕐", e.getDateDebut() != null ? e.getDateDebut().format(TIME_FMT) : "-"));
        col2.getChildren().add(detailBox("👥", e.getCapaciteMax() + " places"));
        detailGrid.getChildren().addAll(col1, col2);

        body.getChildren().addAll(descLabel, detailGrid);

        if (e.getOrganisme() != null && e.getOrganisme().getNom() != null) {
            HBox pill = new HBox(8);
            pill.setStyle("-fx-background-color:rgba(151,195,162,0.15); -fx-background-radius:10; -fx-padding:9 12;");
            pill.setAlignment(Pos.CENTER_LEFT);
            Label orgIcon = new Label("🏢");
            orgIcon.setStyle("-fx-font-size:13px;");
            Label orgName = new Label(e.getOrganisme().getNom());
            orgName.setStyle("-fx-font-size:13px; -fx-text-fill:#555;");
            pill.getChildren().addAll(orgIcon, orgName);
            body.getChildren().add(pill);
        }

        // Footer
        HBox footer = new HBox(12);
        footer.setStyle("-fx-padding:16 22; -fx-border-color:#f0f0f0; -fx-border-width:1 0 0 0;");
        footer.setAlignment(Pos.CENTER);

        Button btnDetails = new Button("👁  Voir détails");
        btnDetails.setStyle("-fx-background-color:white; -fx-text-fill:#1f4f65;" +
                "-fx-border-color:#97c3a2; -fx-border-width:1.5;" +
                "-fx-border-radius:10; -fx-background-radius:10;" +
                "-fx-font-size:13px; -fx-font-weight:bold; -fx-padding:11 18; -fx-cursor:hand;");
        HBox.setHgrow(btnDetails, Priority.ALWAYS);
        btnDetails.setMaxWidth(Double.MAX_VALUE);

        Button btnInscrire = new Button("✔  S'inscrire");
        btnInscrire.setStyle("-fx-background-color:#1f4f65; -fx-text-fill:white;" +
                "-fx-border-radius:10; -fx-background-radius:10;" +
                "-fx-font-size:13px; -fx-font-weight:bold; -fx-padding:11 18; -fx-cursor:hand;");
        HBox.setHgrow(btnInscrire, Priority.ALWAYS);
        btnInscrire.setMaxWidth(Double.MAX_VALUE);

        btnDetails.setOnAction(ev -> openDetail(e));
        btnInscrire.setOnAction(ev -> inscrire(e));

        footer.getChildren().addAll(btnDetails, btnInscrire);
        card.getChildren().addAll(header, body, footer);
        return card;
    }

    private HBox detailBox(String icon, String text) {
        HBox box = new HBox(8);
        box.setStyle("-fx-background-color:#f8faf9; -fx-background-radius:10;" +
                "-fx-padding:10 12; -fx-border-color:#97c3a2; -fx-border-width:0 0 0 3;");
        box.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size:13px;");
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:12px; -fx-text-fill:#444;");
        box.getChildren().addAll(ico, lbl);
        return box;
    }

    private void openDetail(Evenement e) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/evenementDetail.fxml"));
            Parent root = loader.load();

            EvenementDetailController ctrl = loader.getController();
            ctrl.setEvenement(e);

            Stage stage = new Stage();
            stage.setTitle(e.getTitre());
            stage.setScene(new Scene(root, 1200, 850));
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.show();

        } catch (Exception ex) {
            System.err.println("❌ Erreur ouverture détail : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void inscrire(Evenement e) {
        if (e.getLienInscription() != null && !e.getLienInscription().isBlank()) {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(e.getLienInscription()));
            } catch (Exception ex) {
                System.err.println("❌ Impossible d'ouvrir le lien : " + ex.getMessage());
            }
        }
    }

    @FXML
    private void goToOrganismes() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/organismesFront.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Organismes — LearnFlex+");
            stage.setScene(new Scene(root, 1200, 850));
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.show();
        } catch (Exception ex) {
            System.err.println("❌ Erreur ouverture organismes : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

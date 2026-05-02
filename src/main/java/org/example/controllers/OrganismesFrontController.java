package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Services.OrganismeService;
import org.example.entities.Organisme;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class OrganismesFrontController {

    @FXML private ImageView logo;
    @FXML private HBox firstRow;
    @FXML private VBox cardsWrapper;
    @FXML private VBox emptyBox;

    private final OrganismeService service = new OrganismeService();

    @FXML
    public void initialize() {
        var stream = getClass().getResourceAsStream("/org/example/images/logo1.png");
        if (stream != null) logo.setImage(new Image(stream));

        try {
            List<Organisme> list = service.findAll();
            if (list.isEmpty()) {
                emptyBox.setVisible(true);
                emptyBox.setManaged(true);
            } else {
                HBox currentRow = firstRow;
                int count = 0;
                for (Organisme o : list) {
                    if (count > 0 && count % 3 == 0) {
                        currentRow = new HBox(24);
                        currentRow.setStyle("-fx-padding:0 0 24 0;");
                        int emptyIdx = cardsWrapper.getChildren().indexOf(emptyBox);
                        cardsWrapper.getChildren().add(emptyIdx, currentRow);
                    }
                    currentRow.getChildren().add(buildCard(o));
                    count++;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML
    private void switchToBack(MouseEvent event) { navigateTo("/org/example/fxml/dashboard.fxml", "Administration"); }
    @FXML private void goToAccueil()       { navigateTo("/org/example/fxml/front.fxml",           "Accueil"); }
    @FXML private void goToCours()         { navigateTo("/org/example/fxml/cours.fxml",            "Cours"); }
    @FXML private void goToEvaluation()    { navigateTo("/org/example/fxml/EvaluationFront.fxml",  "Évaluation"); }
    @FXML private void goToQuestionnaire() { navigateTo("/org/example/fxml/questionnaire.fxml",    "Questionnaire"); }
    @FXML private void goToOrientation()   { navigateTo("/org/example/fxml/Orientationfront.fxml", "Orientation"); }
    @FXML private void goToForum()         { navigateTo("/org/example/fxml/forum.fxml",            "Forum"); }
    @FXML private void goToConnexion()     { navigateTo("/org/example/fxml/login.fxml",            "Connexion"); }
    @FXML private void goToEvenements()    { navigateTo("/org/example/fxml/front.fxml",            "Événements"); }

    private void navigateTo(String fxmlPath, String title) {
        try {
            var url = getClass().getResource(fxmlPath);
            if (url == null) { System.err.println("❌ FXML introuvable : " + fxmlPath); return; }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = (Stage) firstRow.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation : " + e.getMessage());
        }
    }

    // ── Construction d'une card ───────────────────────────────────────────────
    private VBox buildCard(Organisme o) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color:white; -fx-background-radius:20;" +
                "-fx-border-color:rgba(151,195,162,0.25); -fx-border-radius:20; -fx-border-width:1;");
        HBox.setHgrow(card, Priority.ALWAYS);

        // ── Photo banner ─────────────────────────────────────────────
        VBox photoBanner = buildPhotoBanner(o);
        card.getChildren().add(photoBanner);

        // ── Header (badge + nom) ──────────────────────────────────────
        VBox header = new VBox(10);
        header.setStyle("-fx-background-color:#f0f7f2;" +
                "-fx-padding:16 24 16 24; -fx-border-color:rgba(151,195,162,0.18);" +
                "-fx-border-width:0 0 1 0;");

        if (o.getType() != null && !o.getType().isBlank()) {
            Label badge = new Label(o.getType().toUpperCase());
            badge.setStyle("-fx-background-color:#1f4f65; -fx-text-fill:white;" +
                    "-fx-background-radius:20; -fx-font-size:11px;" +
                    "-fx-font-weight:bold; -fx-padding:5 12;");
            header.getChildren().add(badge);
        }

        Label nom = new Label(o.getNom());
        nom.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:#1a1a2e;");
        nom.setWrapText(true);
        header.getChildren().add(nom);

        // ── Body ──────────────────────────────────────────────────────
        VBox body = new VBox(16);
        body.setStyle("-fx-padding:20 24;");
        VBox.setVgrow(body, Priority.ALWAYS);

        if (o.getDescription() != null && !o.getDescription().isBlank()) {
            String desc = o.getDescription();
            if (desc.length() > 120) desc = desc.substring(0, 120) + "…";
            Label descLabel = new Label(desc);
            descLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#555; -fx-wrap-text:true;");
            descLabel.setWrapText(true);
            body.getChildren().add(descLabel);
        }

        HBox detailGrid = new HBox(10);
        VBox col1 = new VBox(10);
        VBox col2 = new VBox(10);
        HBox.setHgrow(col1, Priority.ALWAYS);
        HBox.setHgrow(col2, Priority.ALWAYS);

        if (o.getVille() != null && !o.getVille().isBlank())
            col1.getChildren().add(detailBox("📍", o.getVille()));
        if (o.getLangue() != null && !o.getLangue().isBlank())
            col2.getChildren().add(detailBox("🌐", o.getLangue()));
        if (o.getFraisMin() > 0)
            col1.getChildren().add(detailBox("💰", "Frais min. " + (int) o.getFraisMin() + " DT"));

        String statutTxt   = o.isActif() ? "✅ Actif" : "❌ Inactif";
        String statutColor = o.isActif() ? "-fx-text-fill:#2e7d32;" : "-fx-text-fill:#c62828;";
        HBox statutBox = new HBox(8);
        statutBox.setStyle("-fx-background-color:#f8faf9; -fx-background-radius:10;" +
                "-fx-padding:10 12; -fx-border-color:#97c3a2; -fx-border-width:0 0 0 3;");
        statutBox.setAlignment(Pos.CENTER_LEFT);
        Label statutLabel = new Label(statutTxt);
        statutLabel.setStyle("-fx-font-size:12px; -fx-font-weight:bold;" + statutColor);
        statutBox.getChildren().add(statutLabel);
        col2.getChildren().add(statutBox);

        detailGrid.getChildren().addAll(col1, col2);
        body.getChildren().add(detailGrid);

        boolean hasStage  = o.isOpportunitesStage();
        boolean hasEmploi = o.isOpportunitesEmploi();
        if (hasStage || hasEmploi) {
            HBox pill = new HBox(16);
            pill.setStyle("-fx-background-color:rgba(151,195,162,0.15); -fx-background-radius:10;" +
                    "-fx-padding:10 14;");
            pill.setAlignment(Pos.CENTER_LEFT);
            if (hasStage) {
                Label s = new Label("💼  Stage");
                s.setStyle("-fx-font-size:13px; -fx-text-fill:#555;");
                pill.getChildren().add(s);
            }
            if (hasEmploi) {
                Label e = new Label("👔  Emploi");
                e.setStyle("-fx-font-size:13px; -fx-text-fill:#555;");
                pill.getChildren().add(e);
            }
            body.getChildren().add(pill);
        }

        // ── Footer ────────────────────────────────────────────────────
        HBox footer = new HBox(12);
        footer.setStyle("-fx-padding:16 24; -fx-border-color:#f0f0f0; -fx-border-width:1 0 0 0;");
        footer.setAlignment(Pos.CENTER);

        if (o.getSiteWeb() != null && !o.getSiteWeb().isBlank()) {
            Button btnSite = new Button("🌐  Visiter le site");
            btnSite.setStyle("-fx-background-color:#1f4f65; -fx-text-fill:white;" +
                    "-fx-border-radius:10; -fx-background-radius:10;" +
                    "-fx-font-size:13px; -fx-font-weight:bold; -fx-padding:11 18; -fx-cursor:hand;");
            HBox.setHgrow(btnSite, Priority.ALWAYS);
            btnSite.setMaxWidth(Double.MAX_VALUE);
            btnSite.setOnAction(ev -> ouvrirSite(o.getSiteWeb()));
            footer.getChildren().add(btnSite);
        }

        card.getChildren().addAll(header, body, footer);
        return card;
    }

    // ── Construit le bandeau photo en haut de la card ─────────────────────────
    private VBox buildPhotoBanner(Organisme o) {
        VBox banner = new VBox();
        banner.setStyle("-fx-background-radius:20 20 0 0; -fx-min-height:160; -fx-pref-height:160;" +
                "-fx-background-color:#e8f4ed;");
        banner.setAlignment(Pos.CENTER);

        String photoPath = o.getPhoto();
        boolean photoLoaded = false;

        if (photoPath != null && !photoPath.isBlank()) {
            File f = new File(photoPath);
            if (f.exists()) {
                try {
                    Image img = new Image(f.toURI().toString(), 400, 160, false, true);
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(400);
                    iv.setFitHeight(160);
                    iv.setPreserveRatio(false);
                    // Clip arrondi en haut
                    iv.setStyle("-fx-background-radius:20 20 0 0;");
                    banner.getChildren().add(iv);
                    photoLoaded = true;
                } catch (Exception e) {
                    System.err.println("❌ Erreur chargement photo : " + e.getMessage());
                }
            }
        }

        // Fallback si pas de photo ou fichier introuvable
        if (!photoLoaded) {
            Label placeholder = new Label("🏢");
            placeholder.setStyle("-fx-font-size:48px;");
            Label nomInitiales = new Label(o.getNom() != null && !o.getNom().isBlank()
                    ? o.getNom().substring(0, Math.min(2, o.getNom().length())).toUpperCase()
                    : "?");
            nomInitiales.setStyle("-fx-font-size:13px; -fx-text-fill:#97c3a2; -fx-font-weight:bold;");
            banner.getChildren().addAll(placeholder, nomInitiales);
        }

        return banner;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
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

    private void ouvrirSite(String url) {
        try {
            String fullUrl = url.startsWith("http") ? url : "https://" + url;
            java.awt.Desktop.getDesktop().browse(new java.net.URI(fullUrl));
        } catch (Exception ex) {
            System.err.println("❌ Impossible d'ouvrir le site : " + ex.getMessage());
        }
    }
}

package org.example.controllers;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Services.ServiceChallenge;
import org.example.entities.Challenge;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ChallengeController implements Initializable {

    @FXML private TextField    searchField;
    @FXML private Label        lblFacileCount;
    @FXML private Label        lblMoyenCount;
    @FXML private Label        lblDifficileCount;
    @FXML private ToggleButton tabTous;
    @FXML private ToggleButton tabFacile;
    @FXML private ToggleButton tabMoyen;
    @FXML private ToggleButton tabDifficile;

    @FXML private TableView<Challenge>            challengeTable;
    @FXML private TableColumn<Challenge, Integer> colId;
    @FXML private TableColumn<Challenge, String>  colTitre;
    @FXML private TableColumn<Challenge, String>  colDescription;
    @FXML private TableColumn<Challenge, String>  colNiveau;
    @FXML private TableColumn<Challenge, String>  colEtat;
    @FXML private TableColumn<Challenge, Double>  colObjectif;
    @FXML private TableColumn<Challenge, Double>  colProgression;
    @FXML private TableColumn<Challenge, Void>    colActions;

    private ObservableList<Challenge> allChallenges = FXCollections.observableArrayList();
    private String currentNiveau = "tous";
    private final ServiceChallenge serviceChallenge = new ServiceChallenge();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadData();
        updateStats();
        setupActionsColumn();
    }

    private void loadData() {
        try {
            List<Challenge> list = serviceChallenge.recuperer();
            allChallenges.setAll(list);
            challengeTable.setItems(allChallenges);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les challenges : " + e.getMessage());
        }
    }

    private void updateStats() {
        lblFacileCount.setText(String.valueOf(
                allChallenges.stream()
                        .filter(c -> "facile".equalsIgnoreCase(c.getNiveaudifficulte()))
                        .count()));
        lblMoyenCount.setText(String.valueOf(
                allChallenges.stream()
                        .filter(c -> "moyen".equalsIgnoreCase(c.getNiveaudifficulte()))
                        .count()));
        lblDifficileCount.setText(String.valueOf(
                allChallenges.stream()
                        .filter(c -> "difficile".equalsIgnoreCase(c.getNiveaudifficulte()))
                        .count()));
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titrec"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionc"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveaudifficulte"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        colObjectif.setCellValueFactory(new PropertyValueFactory<>("objectifscore"));
        colProgression.setCellValueFactory(new PropertyValueFactory<>("progressionactuelle"));

        colEtat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item.toLowerCase()) {
                    case "actif"      -> setStyle("-fx-text-fill:#185FA5; -fx-font-weight:bold;");
                    case "termine"    -> setStyle("-fx-text-fill:#A32D2D; -fx-font-weight:bold;");
                    case "en attente" -> setStyle("-fx-text-fill:#854F0B; -fx-font-weight:bold;");
                    default           -> setStyle("");
                }
            }
        });

        colNiveau.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item.toLowerCase()) {
                    case "facile"    -> setStyle("-fx-text-fill:#3B6D11; -fx-font-weight:bold;");
                    case "moyen"     -> setStyle("-fx-text-fill:#854F0B; -fx-font-weight:bold;");
                    case "difficile" -> setStyle("-fx-text-fill:#A32D2D; -fx-font-weight:bold;");
                    default          -> setStyle("");
                }
            }
        });
    }

    // ── Colonne Actions avec bouton IA ────────────────────────
    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnModifier  = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final Button btnPdf       = new Button("📄 PDF");
            private final Button btnIA        = new Button("✨ IA");   // ← NOUVEAU
            private final HBox   box          = new HBox(6, btnModifier, btnSupprimer, btnPdf, btnIA);
            {
                btnModifier.setStyle(
                        "-fx-background-color:#007bff; -fx-text-fill:white; -fx-font-size:11px;" +
                                "-fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;");
                btnSupprimer.setStyle(
                        "-fx-background-color:#dc3545; -fx-text-fill:white; -fx-font-size:11px;" +
                                "-fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;");
                btnPdf.setStyle(
                        "-fx-background-color:#6f42c1; -fx-text-fill:white; -fx-font-size:11px;" +
                                "-fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;");
                btnIA.setStyle(
                        "-fx-background-color:#fd7e14; -fx-text-fill:white; -fx-font-size:11px;" +  // ← orange
                                "-fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;");
                box.setStyle("-fx-alignment:CENTER_LEFT;");
                btnModifier .setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                btnPdf      .setOnAction(e -> handleExportPdf(getTableView().getItems().get(getIndex())));
                btnIA       .setOnAction(e -> handleGenererQuestions(getTableView().getItems().get(getIndex()))); // ← NOUVEAU
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText().toLowerCase().trim();
        if (q.isEmpty()) { applyFilter(currentNiveau); return; }
        challengeTable.setItems(allChallenges.stream()
                .filter(c -> c.getTitrec().toLowerCase().contains(q)
                        || (c.getDescriptionc() != null
                        && c.getDescriptionc().toLowerCase().contains(q)))
                .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }

    @FXML private void filterTous()      { applyFilter("tous"); }
    @FXML private void filterFacile()    { applyFilter("facile"); }
    @FXML private void filterMoyen()     { applyFilter("moyen"); }
    @FXML private void filterDifficile() { applyFilter("difficile"); }

    private void applyFilter(String niveau) {
        currentNiveau = niveau;
        if ("tous".equals(niveau)) {
            challengeTable.setItems(allChallenges);
        } else {
            challengeTable.setItems(allChallenges.stream()
                    .filter(c -> niveau.equalsIgnoreCase(c.getNiveaudifficulte()))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList)));
        }
    }

    @FXML
    private void showAddChallenge() {
        ouvrirFormulaire(null, "Ajouter un Challenge");
    }

    private void handleEdit(Challenge c) {
        ouvrirFormulaire(c, "Modifier : " + c.getTitrec());
    }

    private void ouvrirFormulaire(Challenge challenge, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/AjouterChallenge.fxml"));
            Parent root = loader.load();
            AjouterChallengeController ctrl = loader.getController();
            if (challenge != null) ctrl.setChallenge(challenge);
            ctrl.setOnSuccessCallback(c -> { loadData(); updateStats(); });
            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    private void handleDelete(Challenge c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer « " + c.getTitrec() + " » ?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    serviceChallenge.supprimer(c);
                    loadData();
                    updateStats();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
                }
            }
        });
    }

    // ── Générer Questions IA ──────────────────────────────────  ← NOUVEAU
    private void handleGenererQuestions(Challenge c) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/GenererQuestions.fxml"));
            Parent root = loader.load();
            //Extrait l'instance du contrôleur associé au fichier FXML
            GenererQuestionsController ctrl = loader.getController();
            //Passe l'objet Challenge au contrôleur
            ctrl.setChallenge(c);
            //Instancie un objet Stage (conteneur de fenêtre JavaFX)
            Stage stage = new Stage();
            //Définition du titre
            stage.setTitle("✨ Générer des questions — " + c.getTitrec());
            //bloque toutes les autres fenêtres de l'application
            stage.initModality(Modality.APPLICATION_MODAL);
            //La scène contient toute l'interface utilisateur
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    // ── Export PDF ────────────────────────────────────────────
    private void handleExportPdf(Challenge c) {
        File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
        if (!downloadsDir.exists()) downloadsDir.mkdirs();
        if (!downloadsDir.exists()) downloadsDir = new File(System.getProperty("user.home"));

        String fileName = "Challenge_" + c.getId() + "_"
                + c.getTitrec().replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".pdf";
        File outputFile = new File(downloadsDir, fileName);

        DeviceRgb headerColor = new DeviceRgb(111, 66, 193);
        DeviceRgb labelBg     = new DeviceRgb(243, 240, 255);

        PdfWriter   writer   = null;
        PdfDocument pdfDoc   = null;
        Document    document = null;

        try {
            writer   = new PdfWriter(outputFile.getAbsolutePath());
            pdfDoc   = new PdfDocument(writer);
            document = new Document(pdfDoc);

            document.add(new Paragraph("FICHE CHALLENGE")
                    .setFontSize(24).setBold()
                    .setFontColor(headerColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4));

            document.add(new Paragraph("Plateforme d'Évaluation — Document généré automatiquement")
                    .setFontSize(10).setItalic()
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(16));

            document.add(new LineSeparator(new SolidLine(1.5f)).setMarginBottom(20));

            Table table = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                    .useAllAvailableWidth().setMarginBottom(24);

            addRow(table, "ID",                   String.valueOf(c.getId()),                 labelBg);
            addRow(table, "Titre",                nvl(c.getTitrec()),                        labelBg);
            addRow(table, "Description",          nvl(c.getDescriptionc()),                  labelBg);
            addRow(table, "Niveau de difficulté", nvl(c.getNiveaudifficulte()),               labelBg);
            addRow(table, "État",                 nvl(c.getEtat()),                          labelBg);
            addRow(table, "Objectif score",       String.valueOf(c.getObjectifscore()),       labelBg);
            addRow(table, "Progression actuelle", String.valueOf(c.getProgressionactuelle()), labelBg);

            document.add(table);

            document.add(new LineSeparator(new SolidLine(0.5f)).setMarginBottom(8));
            String now = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss"));
            document.add(new Paragraph("Généré le : " + now)
                    .setFontSize(9).setItalic()
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.RIGHT));

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur PDF",
                    "Impossible de générer le PDF : " + ex.getMessage());
            ex.printStackTrace();
            return;
        } finally {
            if (document != null) document.close();
        }

        showAlert(Alert.AlertType.INFORMATION, "PDF téléchargé",
                "Le fichier a été enregistré dans :\n" + outputFile.getAbsolutePath());
    }

    private void addRow(Table table, String label, String value, DeviceRgb labelBg) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setBold().setFontSize(11))
                .setBackgroundColor(labelBg)
                .setPaddingTop(7).setPaddingBottom(7)
                .setPaddingLeft(10).setPaddingRight(10));
        table.addCell(new Cell()
                .add(new Paragraph(value).setFontSize(11))
                .setPaddingTop(7).setPaddingBottom(7)
                .setPaddingLeft(10).setPaddingRight(10));
    }

    private String nvl(String s) {
        return (s != null && !s.isBlank()) ? s : "-";
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

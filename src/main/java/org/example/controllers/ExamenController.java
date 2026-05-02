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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Services.ServiceCommentaire;
import org.example.Services.ServiceExamen;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ExamenController implements Initializable {

    @FXML private VBox         evaluationSubmenu;
    @FXML private TextField    searchField;
    @FXML private Label        lblFacileCount;
    @FXML private Label        lblMoyenCount;
    @FXML private Label        lblDifficileCount;
    @FXML private ToggleButton tabTous;
    @FXML private ToggleButton tabFacile;
    @FXML private ToggleButton tabMoyen;
    @FXML private ToggleButton tabDifficile;

    @FXML private TableView<org.example.entities.Examen>            examenTable;
    @FXML private TableColumn<org.example.entities.Examen, Integer> colId;
    @FXML private TableColumn<org.example.entities.Examen, String>  colTitre;
    @FXML private TableColumn<org.example.entities.Examen, String>  colMatiere;
    @FXML private TableColumn<org.example.entities.Examen, String>  colNiveau;
    @FXML private TableColumn<org.example.entities.Examen, String>  colDateDebut;
    @FXML private TableColumn<org.example.entities.Examen, String>  colDateFin;
    @FXML private TableColumn<org.example.entities.Examen, Integer> colDuree;
    @FXML private TableColumn<org.example.entities.Examen, Integer> colNbQuestions;
    @FXML private TableColumn<org.example.entities.Examen, Double>  colScoreTotal;
    @FXML private TableColumn<org.example.entities.Examen, String>  colType;
    @FXML private TableColumn<org.example.entities.Examen, String>  colEtat;
    @FXML private TableColumn<org.example.entities.Examen, Void>    colActions;

    private ObservableList<org.example.entities.Examen> allExamens = FXCollections.observableArrayList();
    private String currentNiveau = "tous";

    // ✅ FIX : déclaration sans instanciation immédiate
    private ServiceExamen      serviceExamen;
    private ServiceCommentaire serviceCommentaire;
    private org.example.entities.Examen examenSelectionne = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // ✅ FIX : instanciation ici, après que JavaFX est prêt
        try {
            serviceExamen      = new ServiceExamen();
            serviceCommentaire = new ServiceCommentaire();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de connexion",
                    "Impossible de se connecter à la base de données : " + e.getMessage());
            return; // on arrête l'initialisation si la DB est inaccessible
        }

        setupColumns();
        loadData();
        updateStats();
        setupActionsColumn();
    }

    // ── Chargement ────────────────────────────────────────────
    private void loadData() {
        // ✅ FIX : guard null
        if (serviceExamen == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Service non initialisé !");
            return;
        }
        try {
            List<org.example.entities.Examen> list = serviceExamen.recuperer();
            allExamens.setAll(list);
            examenTable.setItems(allExamens);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les examens : " + e.getMessage());
        }
    }

    // ── Colonnes ──────────────────────────────────────────────
    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colMatiere.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveauexamen"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("datedebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("datefin"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colNbQuestions.setCellValueFactory(new PropertyValueFactory<>("nbquestion"));
        colScoreTotal.setCellValueFactory(new PropertyValueFactory<>("scoretotal"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeexamen"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));

        colNiveau.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
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

        colEtat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
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
    }

    // ── Colonne Actions ───────────────────────────────────────
    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {

            private final Button btnModifier    = new Button("Modifier");
            private final Button btnSupprimer   = new Button("Supprimer");
            private final Button btnCommentaire = new Button("Commentaires");
            private final Button btnPdf         = new Button("📄 PDF");
            private final HBox   box            = new HBox(6, btnModifier, btnSupprimer, btnCommentaire, btnPdf);

            {
                btnModifier.setStyle(
                        "-fx-background-color:#007bff; -fx-text-fill:white; -fx-font-size:11px;" +
                                "-fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;");
                btnSupprimer.setStyle(
                        "-fx-background-color:#dc3545; -fx-text-fill:white; -fx-font-size:11px;" +
                                "-fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;");
                btnCommentaire.setStyle(
                        "-fx-background-color:#28a745; -fx-text-fill:white; -fx-font-size:11px;" +
                                "-fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;");
                btnPdf.setStyle(
                        "-fx-background-color:#6f42c1; -fx-text-fill:white; -fx-font-size:11px;" +
                                "-fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;");
                box.setStyle("-fx-alignment:CENTER_LEFT;");

                btnModifier   .setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                btnSupprimer  .setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                btnCommentaire.setOnAction(e -> handleCommentaires(getTableView().getItems().get(getIndex())));
                btnPdf        .setOnAction(e -> handleExportPdf(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ── Stats ─────────────────────────────────────────────────
    private void updateStats() {
        lblFacileCount.setText(String.valueOf(
                allExamens.stream().filter(e -> "facile".equalsIgnoreCase(e.getNiveauexamen())).count()));
        lblMoyenCount.setText(String.valueOf(
                allExamens.stream().filter(e -> "moyen".equalsIgnoreCase(e.getNiveauexamen())).count()));
        lblDifficileCount.setText(String.valueOf(
                allExamens.stream().filter(e -> "difficile".equalsIgnoreCase(e.getNiveauexamen())).count()));
    }

    // ── Recherche ─────────────────────────────────────────────
    @FXML
    private void handleSearch() {
        String q = searchField.getText().toLowerCase().trim();
        if (q.isEmpty()) { applyFilter(currentNiveau); return; }
        examenTable.setItems(allExamens.stream()
                .filter(e -> e.getTitre().toLowerCase().contains(q)
                        || e.getMatiere().toLowerCase().contains(q))
                .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }

    // ── Filtres ───────────────────────────────────────────────
    @FXML private void filterTous()      { applyFilter("tous"); }
    @FXML private void filterFacile()    { applyFilter("facile"); }
    @FXML private void filterMoyen()     { applyFilter("moyen"); }
    @FXML private void filterDifficile() { applyFilter("difficile"); }

    private void applyFilter(String niveau) {
        currentNiveau = niveau;
        if ("tous".equals(niveau)) {
            examenTable.setItems(allExamens);
        } else {
            examenTable.setItems(allExamens.stream()
                    .filter(e -> niveau.equalsIgnoreCase(e.getNiveauexamen()))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList)));
        }
    }

    // ── Ajouter ───────────────────────────────────────────────
    @FXML
    private void showAddExamen() {
        ouvrirFormulaireExamen(null, "Ajouter un Examen");
    }

    private void handleEdit(org.example.entities.Examen e) {
        ouvrirFormulaireExamen(e, "Modifier : " + e.getTitre());
    }

    private void ouvrirFormulaireExamen(org.example.entities.Examen examen, String titre) {
        try {
            URL fxmlUrl = getClass().getResource("/org/example/fxml/AjouterExamen.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "AjouterExamen.fxml introuvable.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            AjouterExamenController ctrl = loader.getController();
            if (examen != null) ctrl.setExamen(examen);
            ctrl.setOnSuccessCallback(result -> { loadData(); updateStats(); });
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

    // ── Supprimer ─────────────────────────────────────────────
    private void handleDelete(org.example.entities.Examen e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer « " + e.getTitre() + " » ?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    serviceExamen.supprimer(e);
                    loadData();
                    updateStats();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
                }
            }
        });
    }

    // ── Commentaires ──────────────────────────────────────────
    private void handleCommentaires(org.example.entities.Examen e) {
        examenSelectionne = e;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/Commentaire.fxml"));
            Parent root = loader.load();
            CommentaireController ctrl = loader.getController();
            ctrl.setExamen(e);
            Stage stage = new Stage();
            stage.setTitle("Commentaires : " + e.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    // ── Export PDF ────────────────────────────────────────────
    private void handleExportPdf(org.example.entities.Examen e) {

        File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
        if (!downloadsDir.exists()) downloadsDir.mkdirs();
        if (!downloadsDir.exists()) downloadsDir = new File(System.getProperty("user.home"));

        String fileName = "Examen_" + e.getId() + "_"
                + e.getTitre().replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".pdf";
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

            document.add(new Paragraph("FICHE EXAMEN")
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

            addRow(table, "ID",               String.valueOf(e.getId()),        labelBg);
            addRow(table, "Titre",            nvl(e.getTitre()),                labelBg);
            addRow(table, "Matière",          nvl(e.getMatiere()),              labelBg);
            addRow(table, "Niveau",           nvl(e.getNiveauexamen()),         labelBg);
            addRow(table, "Type d'examen",    nvl(e.getTypeexamen()),           labelBg);
            addRow(table, "État",             nvl(e.getEtat()),                 labelBg);
            addRow(table, "Date de début",    String.valueOf(e.getDatedebut()), labelBg);
            addRow(table, "Date de fin",      String.valueOf(e.getDatefin()),   labelBg);
            addRow(table, "Durée (minutes)",  String.valueOf(e.getDuree()),     labelBg);
            addRow(table, "Nb questions",     String.valueOf(e.getNbquestion()),labelBg);
            addRow(table, "Score total",      String.valueOf(e.getScoretotal()),labelBg);

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

    // ── Helpers PDF ───────────────────────────────────────────
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

    // ── Alerte ───────────────────────────────────────────────
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // ── Sidebar ───────────────────────────────────────────────
    @FXML private void showDashboard()    { System.out.println("-> Dashboard"); }
    @FXML private void showUtilisateurs() { System.out.println("-> Utilisateurs"); }
    @FXML private void showContenu()      { System.out.println("-> Contenu"); }
    @FXML private void toggleEvaluation() {
        boolean v = evaluationSubmenu.isVisible();
        evaluationSubmenu.setVisible(!v);
        evaluationSubmenu.setManaged(!v);
    }
    @FXML private void showExamens()       {}
    @FXML private void showChallenges()    { System.out.println("-> Challenges"); }
    @FXML private void showQuestionnaire() { System.out.println("-> Questionnaire"); }
    @FXML private void showOrientation()   { System.out.println("-> Orientation"); }
    @FXML private void showForum()         { System.out.println("-> Forum"); }
    @FXML private void logout()            { System.out.println("-> Deconnexion"); }
}

package org.example.controllers;

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

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
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

    private final ServiceExamen      serviceExamen      = new ServiceExamen();
    private final ServiceCommentaire serviceCommentaire = new ServiceCommentaire();
    private org.example.entities.Examen examenSelectionne = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadData();
        updateStats();
        setupActionsColumn();
    }

    // ── Chargement depuis la base ─────────────────────────────
    private void loadData() {
        try {
            List<org.example.entities.Examen> list = serviceExamen.recuperer();
            allExamens.setAll(list);
            examenTable.setItems(allExamens);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les examens : " + e.getMessage());
        }
    }

    // ── Binding colonnes ──────────────────────────────────────
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
    }

    // ── Colonne Actions ───────────────────────────────────────
    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {

            private final Button btnModifier    = new Button("Modifier");
            private final Button btnSupprimer   = new Button("Supprimer");
            private final Button btnCommentaire = new Button("Commentaires");
            private final HBox   box            = new HBox(6, btnModifier, btnSupprimer, btnCommentaire);

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
                box.setStyle("-fx-alignment:CENTER_LEFT;");

                btnModifier   .setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                btnSupprimer  .setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                btnCommentaire.setOnAction(e -> handleCommentaires(getTableView().getItems().get(getIndex())));
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

    // ── Bouton "+ Ajouter un Examen" ──────────────────────────
    @FXML
    private void showAddExamen() {
        ouvrirFormulaireExamen(null, "Ajouter un Examen");
    }

    // ── Modifier examen ───────────────────────────────────────
    private void handleEdit(org.example.entities.Examen e) {
        ouvrirFormulaireExamen(e, "Modifier : " + e.getTitre());
    }

    // ── Formulaire examen (ajouter / modifier) ────────────────
    private void ouvrirFormulaireExamen(org.example.entities.Examen examen, String titre) {
        try {
            URL fxmlUrl = getClass().getResource("/org/example/fxml/AjouterExamen.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "AjouterExamen.fxml introuvable dans src/main/resources/org/example/fxml/");
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
            ex.printStackTrace();
        }
    }

    // ── Supprimer examen ──────────────────────────────────────
    private void handleDelete(org.example.entities.Examen e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer " + e.getTitre() + " ?",
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

    // ── Bouton Commentaires → ouvre interface commentaires ────
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
            ex.printStackTrace();
        }
    }

    // ── Utilitaire alerte ─────────────────────────────────────
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
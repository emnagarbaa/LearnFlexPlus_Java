package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.entities.Organisme;
import org.example.Services.OrganismeService;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OrganismeController {

    // ── Table ─────────────────────────────────────────────────────────
    @FXML private TableView<Organisme>           table;
    @FXML private TableColumn<Organisme,Integer> colId;
    @FXML private TableColumn<Organisme,String>  colNom;
    @FXML private TableColumn<Organisme,String>  colType;
    @FXML private TableColumn<Organisme,String>  colVille;
    @FXML private TableColumn<Organisme,String>  colEmail;
    @FXML private TableColumn<Organisme,String>  colTel;
    @FXML private TableColumn<Organisme,Double>  colFrais;
    @FXML private TableColumn<Organisme,String>  colLangue;
    @FXML private TableColumn<Organisme,String>  colActif;
    @FXML private TableColumn<Organisme,String>  colStage;
    @FXML private TableColumn<Organisme,String>  colEmploi;
    @FXML private TableColumn<Organisme,Void>    colActions;

    // ── Toolbar ───────────────────────────────────────────────────────
    @FXML private TextField searchField;

    // ── Edit Dialog ───────────────────────────────────────────────────
    @FXML private StackPane        dialogOverlay;
    @FXML private Label            dialogTitle;
    @FXML private TextField        fNom;
    @FXML private ComboBox<String> fType;
    @FXML private TextField        fVille;
    @FXML private TextField        fEmail;
    @FXML private TextField        fTelephone;
    @FXML private TextField        fSiteWeb;
    @FXML private TextField        fFraisMin;
    @FXML private ComboBox<String> fLangue;
    @FXML private TextArea         fDescription;
    @FXML private CheckBox         fActif;
    @FXML private CheckBox         fOpportunitesStage;
    @FXML private CheckBox         fOpportunitesEmploi;

    // ── State ─────────────────────────────────────────────────────────
    private final OrganismeService         service = new OrganismeService();
    private final ObservableList<Organisme> data   = FXCollections.observableArrayList();
    private Organisme editingOrganisme = null;

    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    // ══════════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ══════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
        setupComboBoxes();
        loadData();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colFrais.setCellValueFactory(new PropertyValueFactory<>("fraisMin"));
        colLangue.setCellValueFactory(new PropertyValueFactory<>("langue"));

        colActif.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().isActif() ? "✅" : "❌"));
        colStage.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().isOpportunitesStage() ? "✅" : "❌"));
        colEmploi.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().isOpportunitesEmploi() ? "✅" : "❌"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox   box       = new HBox(8, btnEdit, btnDelete);
            {
                btnEdit.setStyle(
                        "-fx-background-color:#f0ad4e; -fx-text-fill:white; -fx-cursor:hand;" +
                                "-fx-background-radius:4; -fx-padding:6 12; -fx-font-size:12px; -fx-font-weight:bold;");
                btnDelete.setStyle(
                        "-fx-background-color:#d9534f; -fx-text-fill:white;" +
                                "-fx-cursor:hand; -fx-background-radius:4; -fx-padding:6 12; -fx-font-size:12px; -fx-font-weight:bold;");

                btnEdit.setOnAction(e -> openEditDialog(
                        getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> confirmDelete(
                        getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.setItems(data);
    }

    private void setupComboBoxes() {
        fType.setItems(FXCollections.observableArrayList(
                "Université", "École", "Centre de formation",
                "Entreprise", "ONG", "Autre"));
        fLangue.setItems(FXCollections.observableArrayList(
                "Arabe", "Français", "Anglais", "Bilingue", "Multilingue"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD / SEARCH / SORT
    // ══════════════════════════════════════════════════════════════════
    private void loadData() {
        try {
            data.setAll(service.findAll());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur chargement", e.getMessage());
        }
    }

    @FXML
    private void onSearch() {
        String kw = searchField.getText().trim();
        try {
            data.setAll(kw.isEmpty() ? service.findAll() : service.search(kw));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur recherche", e.getMessage());
        }
    }

    @FXML private void sortAsc()  { sortBy(true); }
    @FXML private void sortDesc() { sortBy(false); }

    private void sortBy(boolean asc) {
        try {
            data.setAll(service.sortByFrais(asc));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur tri", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  ADD
    // ══════════════════════════════════════════════════════════════════
    @FXML
    private void openAddDialog() {
        if (dashboardController != null) {
            dashboardController.showAddOrganisme();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  EDIT
    // ══════════════════════════════════════════════════════════════════
    private void openEditDialog(Organisme o) {
        if (dashboardController != null) {
            dashboardController.showEditOrganisme(o);
        }
    }

    @FXML
    private void closeDialog() {
        dialogOverlay.setVisible(false);
        dialogOverlay.setManaged(false);
    }

    @FXML
    private void saveOrganisme() {
        if (!validateForm()) return;
        Organisme o = editingOrganisme != null ? editingOrganisme : new Organisme();
        fillModel(o);
        try {
            if (editingOrganisme == null) service.create(o);
            else                         service.update(o);
            closeDialog();
            loadData();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur sauvegarde", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════════════════════════
    private void confirmDelete(Organisme o) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmer la suppression");
        a.setHeaderText("Supprimer « " + o.getNom() + " » ?");
        a.setContentText("Cette action est irréversible.");
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    service.delete(o.getId());
                    loadData();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur suppression", e.getMessage());
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════
    //  ████  STATISTIQUES  ████
    // ══════════════════════════════════════════════════════════════════

    /**
     * Stats par Type — PieChart violet
     * Compte combien d'organismes pour chaque valeur du champ "type".
     */
    @FXML
    private void showStatsByType() {
        List<Organisme> all = getCurrentData();
        if (all.isEmpty()) { showAlert(Alert.AlertType.INFORMATION, "Stats par Type", "Aucune donnée disponible."); return; }

        // Compter par type
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Organisme o : all) {
            String key = o.getType() != null && !o.getType().isBlank() ? o.getType() : "Non défini";
            counts.merge(key, 1, Integer::sum);
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        counts.forEach((type, count) ->
                pieData.add(new PieChart.Data(type + " (" + count + ")", count)));

        openStatsWindow(
                "📊  Statistiques par Type",
                pieData,
                "#7c3aed",   // violet — même couleur que le bouton
                all.size()
        );
    }

    /**
     * Stats par Ville — PieChart vert foncé
     * Compte combien d'organismes par ville.
     */
    @FXML
    private void showStatsByVille() {
        List<Organisme> all = getCurrentData();
        if (all.isEmpty()) { showAlert(Alert.AlertType.INFORMATION, "Stats par Ville", "Aucune donnée disponible."); return; }

        Map<String, Integer> counts = new TreeMap<>();
        for (Organisme o : all) {
            String key = o.getVille() != null && !o.getVille().isBlank() ? o.getVille() : "Non défini";
            counts.merge(key, 1, Integer::sum);
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        counts.forEach((ville, count) ->
                pieData.add(new PieChart.Data(ville + " (" + count + ")", count)));

        openStatsWindow(
                "📊  Statistiques par Ville",
                pieData,
                "#0f6b5e",   // vert foncé — même couleur que le bouton
                all.size()
        );
    }

    /**
     * Stats Frais Min — PieChart marron/orange
     * Regroupe les organismes par tranche de frais :
     *   0–500 TND / 500–1000 / 1000–2000 / 2000–5000 / 5000+
     */
    @FXML
    private void showStatsByFrais() {
        List<Organisme> all = getCurrentData();
        if (all.isEmpty()) { showAlert(Alert.AlertType.INFORMATION, "Stats Frais Min", "Aucune donnée disponible."); return; }

        // Tranches de frais
        Map<String, Integer> tranches = new LinkedHashMap<>();
        tranches.put("0 – 500 TND",      0);
        tranches.put("500 – 1 000 TND",  0);
        tranches.put("1 000 – 2 000 TND",0);
        tranches.put("2 000 – 5 000 TND",0);
        tranches.put("5 000+ TND",        0);

        for (Organisme o : all) {
            double f = o.getFraisMin();
            if      (f < 500)   tranches.merge("0 – 500 TND",       1, Integer::sum);
            else if (f < 1000)  tranches.merge("500 – 1 000 TND",   1, Integer::sum);
            else if (f < 2000)  tranches.merge("1 000 – 2 000 TND", 1, Integer::sum);
            else if (f < 5000)  tranches.merge("2 000 – 5 000 TND", 1, Integer::sum);
            else                tranches.merge("5 000+ TND",          1, Integer::sum);
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        tranches.forEach((label, count) -> {
            if (count > 0)
                pieData.add(new PieChart.Data(label + " (" + count + ")", count));
        });

        openStatsWindow(
                "📈  Statistiques Frais Min",
                pieData,
                "#b45309",   // marron/orange — même couleur que le bouton
                all.size()
        );
    }

    // ══════════════════════════════════════════════════════════════════
    //  HELPER : ouvrir une fenêtre popup avec PieChart stylisé
    // ══════════════════════════════════════════════════════════════════
    private void openStatsWindow(String title,
                                 ObservableList<PieChart.Data> pieData,
                                 String accentColor,
                                 int total) {

        // ── PieChart ────────────────────────────────────────────────
        PieChart chart = new PieChart(pieData);
        chart.setTitle(title);
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setStartAngle(90);
        chart.setPrefSize(520, 400);

        // ── Label total ─────────────────────────────────────────────
        Label lblTotal = new Label("Total : " + total + " organisme" + (total > 1 ? "s" : ""));
        lblTotal.setStyle("-fx-font-size:13px; -fx-text-fill:#555; -fx-padding:0 0 8 0;");

        // ── Bouton fermer ────────────────────────────────────────────
        Button btnClose = new Button("Fermer");
        btnClose.setStyle(
                "-fx-background-color:" + accentColor + "; -fx-text-fill:white;" +
                        "-fx-font-weight:bold; -fx-background-radius:8;" +
                        "-fx-cursor:hand; -fx-padding:8 28; -fx-font-size:13px;");

        // ── Layout ───────────────────────────────────────────────────
        VBox root = new VBox(12, chart, lblTotal, btnClose);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setStyle(
                "-fx-background-color:#ffffff;" +
                        "-fx-padding:24;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),16,0,0,4);");
        root.setPrefWidth(580);

        // ── Stage popup ──────────────────────────────────────────────
        Stage popup = new Stage();
        popup.setTitle(title);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setResizable(false);

        btnClose.setOnAction(e -> popup.close());

        Scene scene = new Scene(root);
        popup.setScene(scene);
        popup.showAndWait();
    }

    // ══════════════════════════════════════════════════════════════════
    //  HELPER : récupère les données actuellement affichées dans la table
    //  (après recherche/filtre éventuel)
    // ══════════════════════════════════════════════════════════════════
    private List<Organisme> getCurrentData() {
        // On utilise data (ObservableList) qui reflète déjà le résultat
        // de la dernière recherche ou du tri.
        return data;
    }

    // ══════════════════════════════════════════════════════════════════
    //  FORM HELPERS
    // ══════════════════════════════════════════════════════════════════
    private void fillForm(Organisme o) {
        fNom.setText(o.getNom());
        fType.setValue(o.getType());
        fVille.setText(o.getVille());
        fEmail.setText(o.getEmail());
        fTelephone.setText(o.getTelephone());
        fSiteWeb.setText(o.getSiteWeb());
        fFraisMin.setText(String.valueOf(o.getFraisMin()));
        fLangue.setValue(o.getLangue());
        fDescription.setText(o.getDescription());
        fActif.setSelected(o.isActif());
        fOpportunitesStage.setSelected(o.isOpportunitesStage());
        fOpportunitesEmploi.setSelected(o.isOpportunitesEmploi());
    }

    private void fillModel(Organisme o) {
        o.setNom(fNom.getText().trim());
        o.setType(fType.getValue());
        o.setVille(fVille.getText().trim());
        o.setEmail(fEmail.getText().trim());
        o.setTelephone(fTelephone.getText().trim());
        o.setSiteWeb(fSiteWeb.getText().trim());
        try { o.setFraisMin(Double.parseDouble(fFraisMin.getText().trim())); }
        catch (NumberFormatException ex) { o.setFraisMin(0); }
        o.setLangue(fLangue.getValue());
        o.setDescription(fDescription.getText().trim());
        o.setActif(fActif.isSelected());
        o.setOpportunitesStage(fOpportunitesStage.isSelected());
        o.setOpportunitesEmploi(fOpportunitesEmploi.isSelected());
        o.setPhoto(null);
    }

    private boolean validateForm() {
        if (fNom.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nom est obligatoire.");
            return false;
        }
        if (fType.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un type.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
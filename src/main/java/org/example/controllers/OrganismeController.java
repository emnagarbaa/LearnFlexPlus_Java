package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.example.entities.Organisme;
import org.example.Services.OrganismeService;

import java.sql.SQLException;
import java.util.List;

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

    // ── Edit Dialog (kept for edit only) ──────────────────────────────
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

    // Dashboard reference for navigation
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
    //  ADD — navigate to dedicated page
    // ══════════════════════════════════════════════════════════════════
    @FXML
    private void openAddDialog() {
        if (dashboardController != null) {
            dashboardController.showAddOrganisme();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  EDIT DIALOG (inline, kept for editing existing records)
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

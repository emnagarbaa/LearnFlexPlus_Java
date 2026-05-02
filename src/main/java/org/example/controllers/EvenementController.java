package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.example.entities.Evenement;
import org.example.entities.Organisme;
import org.example.Services.EvenementService;
import org.example.Services.OrganismeService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class EvenementController {

    // ── Table ─────────────────────────────────────────────────────────
    @FXML private TableView<Evenement>              table;
    @FXML private TableColumn<Evenement,Integer>    colId;
    @FXML private TableColumn<Evenement,String>     colTitre;
    @FXML private TableColumn<Evenement,String>     colLieu;
    @FXML private TableColumn<Evenement,String>     colMode;
    @FXML private TableColumn<Evenement,String>     colCapacite;
    @FXML private TableColumn<Evenement,String>     colDateDebut;
    @FXML private TableColumn<Evenement,String>     colDateFin;
    @FXML private TableColumn<Evenement,String>     colPublicCible;
    @FXML private TableColumn<Evenement,String>     colOrganisme;
    @FXML private TableColumn<Evenement,String>     colEmail;
    @FXML private TableColumn<Evenement,String>     colTelephone;
    @FXML private TableColumn<Evenement,Void>       colActions;

    // ── Toolbar ───────────────────────────────────────────────────────
    @FXML private TextField searchField;

    // ── Edit Dialog ───────────────────────────────────────────────────
    @FXML private StackPane        dialogOverlay;
    @FXML private Label            dialogTitle;
    @FXML private TextField        fTitre;
    @FXML private TextField        fLieu;
    @FXML private ComboBox<String> fMode;
    @FXML private TextField        fCapaciteMax;
    @FXML private TextField        fDateDebut;
    @FXML private TextField        fDateFin;
    @FXML private TextField        fPublicCible;
    @FXML private ComboBox<String> fOrganisme;
    @FXML private TextField        fContactEmail;
    @FXML private TextField        fContactTelephone;
    @FXML private TextField        fLienInscription;
    @FXML private TextArea         fDescription;

    // ── State ─────────────────────────────────────────────────────────
    private final EvenementService service  = new EvenementService();
    private final OrganismeService          orgService = new OrganismeService();
    private final ObservableList<Evenement> data     = FXCollections.observableArrayList();
    private Evenement editingEvenement = null;
    private DashboardController dashboardController;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("mode"));
        colCapacite.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getCapaciteMax())));
        colDateDebut.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDateDebut() != null
                        ? c.getValue().getDateDebut().format(FMT) : "-"));
        colDateFin.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDateFin() != null
                        ? c.getValue().getDateFin().format(FMT) : "-"));
        colPublicCible.setCellValueFactory(new PropertyValueFactory<>("publicCible"));
        colOrganisme.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getOrganisme() != null
                        ? c.getValue().getOrganisme().getNom() : "-"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("contactTelephone"));

        // Actions
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
        fMode.setItems(FXCollections.observableArrayList(
                "Présentiel", "En ligne", "Hybride"));
        // Load organismes into combobox
        try {
            ObservableList<String> orgNames = FXCollections.observableArrayList();
            orgNames.add("-- Aucun --");
            orgService.findAll().forEach(o -> orgNames.add(o.getId() + " - " + o.getNom()));
            fOrganisme.setItems(orgNames);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            data.setAll(service.sortByCapacite(asc));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur tri", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  ADD — navigate to dedicated add page
    // ══════════════════════════════════════════════════════════════════
    @FXML
    private void openAddPage() {
        if (dashboardController != null) {
            dashboardController.showAddEvenement();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  EDIT DIALOG
    // ══════════════════════════════════════════════════════════════════
    private void openEditDialog(Evenement e) {
        if (dashboardController != null) {
            dashboardController.showEditEvenement(e);
        }
    }

    @FXML
    private void closeDialog() {
        dialogOverlay.setVisible(false);
        dialogOverlay.setManaged(false);
    }

    @FXML
    private void saveEvenement() {
        if (!validateForm()) return;
        Evenement e = editingEvenement != null ? editingEvenement : new Evenement();
        fillModel(e);
        try {
            if (editingEvenement == null) service.create(e);
            else                          service.update(e);
            closeDialog();
            loadData();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur sauvegarde", ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════════════════════════
    private void confirmDelete(Evenement e) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmer la suppression");
        a.setHeaderText("Supprimer « " + e.getTitre() + " » ?");
        a.setContentText("Cette action est irréversible.");
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    service.delete(e.getId());
                    loadData();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur suppression", ex.getMessage());
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════
    //  FORM HELPERS
    // ══════════════════════════════════════════════════════════════════
    private void fillForm(Evenement e) {
        fTitre.setText(e.getTitre());
        fLieu.setText(e.getLieu());
        fMode.setValue(e.getMode());
        fCapaciteMax.setText(String.valueOf(e.getCapaciteMax()));
        fDateDebut.setText(e.getDateDebut() != null ? e.getDateDebut().format(FMT) : "");
        fDateFin.setText(e.getDateFin()     != null ? e.getDateFin().format(FMT)   : "");
        fPublicCible.setText(e.getPublicCible());
        fContactEmail.setText(e.getContactEmail());
        fContactTelephone.setText(e.getContactTelephone());
        fLienInscription.setText(e.getLienInscription());
        fDescription.setText(e.getDescription());
        if (e.getOrganisme() != null) {
            fOrganisme.setValue(e.getOrganisme().getId() + " - " + e.getOrganisme().getNom());
        } else {
            fOrganisme.setValue("-- Aucun --");
        }
    }

    private void fillModel(Evenement e) {
        e.setTitre(fTitre.getText().trim());
        e.setLieu(fLieu.getText().trim());
        e.setMode(fMode.getValue());
        try { e.setCapaciteMax(Integer.parseInt(fCapaciteMax.getText().trim())); }
        catch (NumberFormatException ex) { e.setCapaciteMax(0); }
        try { e.setDateDebut(java.time.LocalDateTime.parse(fDateDebut.getText().trim(), FMT)); }
        catch (Exception ex) { e.setDateDebut(null); }
        try { e.setDateFin(java.time.LocalDateTime.parse(fDateFin.getText().trim(), FMT)); }
        catch (Exception ex) { e.setDateFin(null); }
        e.setPublicCible(fPublicCible.getText().trim());
        e.setContactEmail(fContactEmail.getText().trim());
        e.setContactTelephone(fContactTelephone.getText().trim());
        e.setLienInscription(fLienInscription.getText().trim());
        e.setDescription(fDescription.getText().trim());

        // Parse organisme from combobox "id - nom"
        String orgVal = fOrganisme.getValue();
        if (orgVal != null && !orgVal.startsWith("--")) {
            try {
                int orgId = Integer.parseInt(orgVal.split(" - ")[0].trim());
                Organisme org = new Organisme();
                org.setId(orgId);
                e.setOrganisme(org);
            } catch (Exception ex) { e.setOrganisme(null); }
        } else {
            e.setOrganisme(null);
        }
    }

    private boolean validateForm() {
        if (fTitre.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le titre est obligatoire.");
            return false;
        }
        if (fMode.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un mode.");
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

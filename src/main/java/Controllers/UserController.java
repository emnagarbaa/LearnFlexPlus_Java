package Controllers;

import entities.Users;
import entities.Ban;
import Services.ServiceUsers;
import Services.ServiceBan;
import Main.java.Utils.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.mindrot.jbcrypt.BCrypt;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class UserController {

    // ===== FORM FIELDS =====
    @FXML private TextField     txtNom;
    @FXML private TextField     txtPrenom;
    @FXML private TextField     txtEmail;
    @FXML private ComboBox<String> cmbRole;
    @FXML private TextField     txtPassword;
    @FXML private TextField     searchField;

    @FXML private Label errNom;
    @FXML private Label errPrenom;
    @FXML private Label errEmail;
    @FXML private Label errRole;
    @FXML private Label errPassword;

    // ===== USERS TABLE =====
    @FXML private TableView<Users>           usersTable;
    @FXML private TableColumn<Users, Integer> colId;
    @FXML private TableColumn<Users, String>  colNom;
    @FXML private TableColumn<Users, String>  colPrenom;
    @FXML private TableColumn<Users, String>  colEmail;
    @FXML private TableColumn<Users, String>  colRole;
    @FXML private TableColumn<Users, String>  colStatus;   // 🟢 Actif / 🔴 Banni

    // ===== STAT LABELS =====
    @FXML private Label lblTotalUsers;
    @FXML private Label lblAdmins;
    @FXML private Label lblStudents;
    @FXML private Label lblTeachers;
    @FXML private Label lblBanned;          // new banned counter

    // ===== BAN HISTORY PANEL =====
    @FXML private VBox    banHistoryPanel;
    @FXML private Label   lblBanHistoryTitle;
    @FXML private Label   lblBanHistorySub;
    @FXML private Button  btnUnban;

    @FXML private TableView<Ban>             banHistoryTable;
    @FXML private TableColumn<Ban, String>   colBanDate;
    @FXML private TableColumn<Ban, String>   colBanType;
    @FXML private TableColumn<Ban, String>   colBanReason;
    @FXML private TableColumn<Ban, String>   colBanExpiry;
    @FXML private TableColumn<Ban, String>   colBanActive;

    // ===== SERVICES =====
    private final ServiceUsers service    = new ServiceUsers();
    private final ServiceBan   serviceBan = new ServiceBan();

    // ===== STATE =====
    private FilteredList<Users> filteredData;
    private Set<Integer>        bannedIds;   // preloaded for O(1) status lookup

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────────────────────────────────────────────────
    // INITIALIZE
    // ─────────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {

        // ── Users table columns ──────────────────────────────────────────────
        colId.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getId()).asObject());
        colNom.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNom()));
        colPrenom.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPrenom()));
        colEmail.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEmail()));
        colRole.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getRole()));

        // ── Status column: green "Actif" or red "Banni" ──────────────────────
        colStatus.setCellValueFactory(d ->
                new SimpleStringProperty(
                        bannedIds != null && bannedIds.contains(d.getValue().getId())
                                ? "🔴 Banni"
                                : "🟢 Actif"
                ));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("🔴")) {
                        setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // ── Role dropdown ────────────────────────────────────────────────────
        cmbRole.setItems(FXCollections.observableArrayList(
                "Admin", "Etudiant", "Enseignant"
        ));

        // ── Row selection → fill form + load ban history ─────────────────────
        usersTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null) {
                        txtNom.setText(selected.getNom());
                        txtPrenom.setText(selected.getPrenom());
                        txtEmail.setText(selected.getEmail());
                        cmbRole.setValue(selected.getRole());
                        txtPassword.setText("");
                        loadBanHistory(selected);   // ← refresh history panel
                    }
                });

        // ── Ban action column ────────────────────────────────────────────────
        TableColumn<Users, Void> colBan = new TableColumn<>("Sanction");
        colBan.setPrefWidth(90);
        colBan.setCellFactory(col -> new TableCell<>() {
            private final Button btnBan = new Button("Ban");
            {
                btnBan.setStyle(
                        "-fx-background-color:#e74c3c;" +
                                "-fx-text-fill:white;" +
                                "-fx-font-size:11px;"
                );
                btnBan.setOnAction(e -> {
                    Users target = getTableView().getItems().get(getIndex());
                    openBanDialog(target);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnBan);
            }
        });
        usersTable.getColumns().add(colBan);

        // ── Ban history table columns ────────────────────────────────────────
        colBanDate.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getCreatedAt() != null
                                ? d.getValue().getCreatedAt().format(DT_FMT)
                                : "—"
                ));

        colBanType.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getBanType()));
        colBanType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "PERMANENT" ->
                            setStyle("-fx-text-fill:#c62828; -fx-font-weight:bold;");
                    case "TEMPORARY" ->
                            setStyle("-fx-text-fill:#e65100; -fx-font-weight:bold;");
                    case "WARNING"   ->
                            setStyle("-fx-text-fill:#f9a825; -fx-font-weight:bold;");
                    default          -> setStyle("");
                }
            }
        });

        colBanReason.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getReason()));

        colBanExpiry.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFormattedExpiry()));

        colBanActive.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().isActive() ? "Oui" : "Non"));
        colBanActive.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("Oui".equals(item)
                        ? "-fx-text-fill:#c62828; -fx-font-weight:bold;"
                        : "-fx-text-fill:#888;");
            }
        });

        setupLiveValidation();
        loadUsers();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOAD USERS
    // ─────────────────────────────────────────────────────────────────────────
    @FXML
    public void loadUsers() {
        try {
            // Load banned IDs first (single query) so status column is accurate
            bannedIds = serviceBan.getAllActiveBannedUserIds();

            filteredData = new FilteredList<>(
                    FXCollections.observableArrayList(service.recuperer()),
                    p -> true
            );
            SortedList<Users> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(usersTable.comparatorProperty());
            usersTable.setItems(sortedData);

            setupSearch();
            loadStats();

            // Refresh history panel if a user was already selected
            Users sel = usersTable.getSelectionModel().getSelectedItem();
            if (sel != null) loadBanHistory(sel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BAN HISTORY PANEL
    // ─────────────────────────────────────────────────────────────────────────

    /** Called every time a row is selected, or after a ban/unban action. */
    private void loadBanHistory(Users user) {
        try {
            List<Ban> history = serviceBan.getBanHistory(user.getId());
            banHistoryTable.setItems(FXCollections.observableArrayList(history));

            // Header title
            lblBanHistoryTitle.setText(
                    "Sanctions — " + user.getPrenom() + " " + user.getNom()
            );

            // Sub-label: summary counts
            long warnings = history.stream()
                    .filter(b -> "WARNING".equals(b.getBanType())).count();
            long bans     = history.stream()
                    .filter(b -> !"WARNING".equals(b.getBanType())).count();
            lblBanHistorySub.setText(
                    history.size() + " entrée(s)  ·  "
                            + warnings + " avertissement(s)  ·  "
                            + bans + " ban(s)"
            );

            // Show Unban button only if user has an active non-warning ban
            boolean hasBan = bannedIds != null && bannedIds.contains(user.getId());
            btnUnban.setVisible(hasBan);
            btnUnban.setManaged(hasBan);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Lifts all active bans for the selected user. */
    @FXML
    public void unbanUser() {
        Users selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Sélectionnez un utilisateur."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Lever le ban");
        confirm.setHeaderText("Lever le ban de "
                + selected.getPrenom() + " " + selected.getNom() + " ?");
        confirm.setContentText("Tous les bans actifs de cet utilisateur seront désactivés.");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            try {
                serviceBan.unbanUser(selected.getId());
                loadUsers();                     // refreshes bannedIds + status column
                loadBanHistory(selected);        // refreshes history panel
                showAlert("Ban levé pour "
                        + selected.getPrenom() + " " + selected.getNom() + ".");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur : " + e.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BAN DIALOG  (unchanged from original, kept here for completeness)
    // ─────────────────────────────────────────────────────────────────────────
    private void openBanDialog(Users target) {

        int warnings = 0;
        try { warnings = serviceBan.getWarningCount(target.getId()); }
        catch (Exception ignored) {}

        Label warnLabel = new Label("Avertissements : " + warnings + " / 3");
        warnLabel.setStyle("-fx-text-fill:#e67e22; -fx-font-weight:bold;");

        ToggleGroup group   = new ToggleGroup();
        RadioButton warnBtn = new RadioButton("Avertissement");
        RadioButton tempBtn = new RadioButton("Ban temporaire");
        RadioButton permBtn = new RadioButton("Ban permanent");
        warnBtn.setToggleGroup(group);
        tempBtn.setToggleGroup(group);
        permBtn.setToggleGroup(group);
        tempBtn.setSelected(true);

        Spinner<Integer> heuresSpinner = new Spinner<>(1, 8760, 24);
        heuresSpinner.setEditable(true);
        heuresSpinner.setPrefWidth(90);
        heuresSpinner.disableProperty().bind(tempBtn.selectedProperty().not());

        TextArea raisonField = new TextArea();
        raisonField.setPromptText("Raison (obligatoire)...");
        raisonField.setPrefRowCount(3);
        raisonField.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Action :"),                     0, 0);
        grid.add(new HBox(12, warnBtn, tempBtn, permBtn),   1, 0);
        grid.add(new Label("Durée (heures) :"),             0, 1);
        grid.add(heuresSpinner,                             1, 1);
        grid.add(new Label("Raison :"),                     0, 2);
        grid.add(raisonField,                               1, 2);
        grid.add(warnLabel,                                 1, 3);

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Gérer — " + target.getPrenom() + " " + target.getNom());
        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;

            String raison = raisonField.getText().trim();
            if (raison.isEmpty()) { showAlert("La raison est obligatoire."); return; }

            int adminId = SessionManager.getCurrentUser().getId();

            try {
                RadioButton sel = (RadioButton) group.getSelectedToggle();

                if (sel == warnBtn) {
                    int total = serviceBan.warnUser(target.getId(), raison, adminId);
                    String msg = "Avertissement enregistré. Total : " + total + "/3";
                    if (total >= 3) msg += "\nUtilisateur banni automatiquement pour 24h.";
                    showAlert(msg);

                } else if (sel == tempBtn) {
                    serviceBan.banUser(target.getId(), raison, adminId,
                            true, heuresSpinner.getValue());
                    showAlert("Ban temporaire appliqué.");

                } else {
                    serviceBan.banUser(target.getId(), raison, adminId, false, 0);
                    showAlert("Utilisateur banni définitivement.");
                }

                loadUsers();

                // Refresh history for this user immediately
                loadBanHistory(target);

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur : " + e.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────────────────
    @FXML
    public void addUser() {
        if (!validateForm()) { showAlert("Corrigez les erreurs avant de continuer."); return; }
        try {
            Users u = new Users();
            u.setNom(txtNom.getText());
            u.setPrenom(txtPrenom.getText());
            u.setEmail(txtEmail.getText());
            u.setRole(cmbRole.getValue());
            u.setPassword(BCrypt.hashpw(txtPassword.getText(), BCrypt.gensalt()));
            service.ajouter(u);
            loadUsers();
            clearFields();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void editUser() {
        try {
            Users selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showAlert("Sélectionnez un utilisateur."); return; }
            selected.setNom(txtNom.getText());
            selected.setPrenom(txtPrenom.getText());
            selected.setEmail(txtEmail.getText());
            selected.setRole(cmbRole.getValue());
            if (!txtPassword.getText().isBlank())
                selected.setPassword(BCrypt.hashpw(txtPassword.getText(), BCrypt.gensalt()));
            service.modifier(selected);
            loadUsers();
            clearFields();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void deleteUser() {
        try {
            Users selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showAlert("Sélectionnez un utilisateur."); return; }
            service.supprimer(selected);
            loadUsers();
            clearFields();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────────────────────────────────
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldValue, newValue) ->
                filteredData.setPredicate(user -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    String f = newValue.toLowerCase();
                    return user.getNom().toLowerCase().contains(f)
                            || user.getPrenom().toLowerCase().contains(f)
                            || user.getEmail().toLowerCase().contains(f)
                            || user.getRole().toLowerCase().contains(f);
                })
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATS
    // ─────────────────────────────────────────────────────────────────────────
    private void loadStats() {
        try {
            var users = service.recuperer();
            int total = users.size(), admins = 0, students = 0, teachers = 0;
            for (Users u : users) {
                switch (u.getRole()) {
                    case "Admin"      -> admins++;
                    case "Etudiant"   -> students++;
                    case "Enseignant" -> teachers++;
                }
            }
            lblTotalUsers.setText(String.valueOf(total));
            lblAdmins.setText(String.valueOf(admins));
            lblStudents.setText(String.valueOf(students));
            lblTeachers.setText(String.valueOf(teachers));
            lblBanned.setText(String.valueOf(
                    bannedIds != null ? bannedIds.size() : 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LIVE VALIDATION
    // ─────────────────────────────────────────────────────────────────────────
    private void setupLiveValidation() {
        txtNom.textProperty().addListener((o, a, b)      -> validateForm());
        txtPrenom.textProperty().addListener((o, a, b)   -> validateForm());
        txtEmail.textProperty().addListener((o, a, b)    -> validateForm());
        txtPassword.textProperty().addListener((o, a, b) -> validateForm());
        cmbRole.valueProperty().addListener((o, a, b)    -> validateForm());
    }

    private boolean validateForm() {
        boolean ok = true;
        errNom.setText(""); errPrenom.setText("");
        errEmail.setText(""); errRole.setText(""); errPassword.setText("");

        if (txtNom.getText() == null || !txtNom.getText().matches("^[a-zA-Z\\s]{2,}$")) {
            errNom.setText("Nom invalide"); ok = false;
        }
        if (txtPrenom.getText() == null || !txtPrenom.getText().matches("^[a-zA-Z\\s]{2,}$")) {
            errPrenom.setText("Prénom invalide"); ok = false;
        }
        if (txtEmail.getText() == null ||
                !txtEmail.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errEmail.setText("Email invalide"); ok = false;
        }
        if (cmbRole.getValue() == null) {
            errRole.setText("Rôle requis"); ok = false;
        }
        if (txtPassword.getText() == null || txtPassword.getText().length() < 8) {
            errPassword.setText("Min 8 caractères"); ok = false;
        }
        return ok;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private void clearFields() {
        txtNom.clear(); txtPrenom.clear();
        txtEmail.clear(); cmbRole.setValue(null); txtPassword.clear();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }
}

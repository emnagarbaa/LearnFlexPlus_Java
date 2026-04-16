package Controllers;

import entities.Users;
import Services.ServiceUsers;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.mindrot.jbcrypt.BCrypt;

public class UserController {

    // ===== FORM FIELDS =====
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbRole;
    @FXML private TextField txtPassword;

    @FXML private Label errNom;
    @FXML private Label errPrenom;
    @FXML private Label errEmail;
    @FXML private Label errRole;
    @FXML private Label errPassword;

    // ===== TABLE =====
    @FXML private TableView<Users> usersTable;
    @FXML private TableColumn<Users, Integer> colId;
    @FXML private TableColumn<Users, String> colNom;
    @FXML private TableColumn<Users, String> colPrenom;
    @FXML private TableColumn<Users, String> colEmail;
    @FXML private TableColumn<Users, String> colRole;

    private final ServiceUsers service = new ServiceUsers();

    // ==================================================
    // INIT
    // ==================================================
    @FXML
    public void initialize() {

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

        // ROLE DROPDOWN
        cmbRole.setItems(FXCollections.observableArrayList(
                "Admin",
                "Etudiant",
                "Enseignant"
        ));

        // SELECT ROW → FILL FORM
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                txtNom.setText(selected.getNom());
                txtPrenom.setText(selected.getPrenom());
                txtEmail.setText(selected.getEmail());
                cmbRole.setValue(selected.getRole());
                txtPassword.setText(""); // never show hashed password
            }
        });

        setupLiveValidation();
        loadUsers();
    }

    // ==================================================
    // LIVE VALIDATION
    // ==================================================
    private void setupLiveValidation() {

        txtNom.textProperty().addListener((o, a, b) -> validateForm());
        txtPrenom.textProperty().addListener((o, a, b) -> validateForm());
        txtEmail.textProperty().addListener((o, a, b) -> validateForm());
        txtPassword.textProperty().addListener((o, a, b) -> validateForm());
        cmbRole.valueProperty().addListener((o, a, b) -> validateForm());
    }

    // ==================================================
    // LOAD USERS
    // ==================================================
    @FXML
    public void loadUsers() {
        try {
            usersTable.setItems(
                    FXCollections.observableArrayList(service.recuperer())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================
    // ADD USER
    // ==================================================
    @FXML
    public void addUser() {

        if (!validateForm()) {
            showAlert("Fix errors first");
            return;
        }

        try {
            Users u = new Users();

            u.setNom(txtNom.getText());
            u.setPrenom(txtPrenom.getText());
            u.setEmail(txtEmail.getText());
            u.setRole(cmbRole.getValue());

            String hashed = BCrypt.hashpw(txtPassword.getText(), BCrypt.gensalt());
            u.setPassword(hashed);

            service.ajouter(u);

            loadUsers();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================
    // EDIT USER
    // ==================================================
    @FXML
    public void editUser() {
        try {
            Users selected = usersTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showAlert("Select user first");
                return;
            }

            selected.setNom(txtNom.getText());
            selected.setPrenom(txtPrenom.getText());
            selected.setEmail(txtEmail.getText());
            selected.setRole(cmbRole.getValue());

            // ONLY update password if filled
            if (!txtPassword.getText().isBlank()) {
                selected.setPassword(
                        BCrypt.hashpw(txtPassword.getText(), BCrypt.gensalt())
                );
            }

            service.modifier(selected);

            loadUsers();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================
    // DELETE USER
    // ==================================================
    @FXML
    public void deleteUser() {
        try {
            Users selected = usersTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showAlert("Select user");
                return;
            }

            service.supprimer(selected);

            loadUsers();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================
    // VALIDATION (SYMFONY STYLE)
    // ==================================================
    private boolean validateForm() {

        boolean ok = true;

        errNom.setText("");
        errPrenom.setText("");
        errEmail.setText("");
        errRole.setText("");
        errPassword.setText("");

        // NOM
        if (txtNom.getText() == null || !txtNom.getText().matches("^[a-zA-Z\\s]{2,}$")) {
            errNom.setText("Invalid name");
            ok = false;
        }

        // PRENOM
        if (txtPrenom.getText() == null || !txtPrenom.getText().matches("^[a-zA-Z\\s]{2,}$")) {
            errPrenom.setText("Invalid first name");
            ok = false;
        }

        // EMAIL
        if (txtEmail.getText() == null ||
                !txtEmail.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errEmail.setText("Invalid email");
            ok = false;
        }

        // ROLE
        if (cmbRole.getValue() == null) {
            errRole.setText("Role required");
            ok = false;
        }

        // PASSWORD
        if (txtPassword.getText() == null || txtPassword.getText().length() < 8) {
            errPassword.setText("Min 8 chars");
            ok = false;
        }

        return ok;
    }

    // ==================================================
    // CLEAR
    // ==================================================
    private void clearFields() {
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        cmbRole.setValue(null);
        txtPassword.clear();
    }

    // ==================================================
    // ALERT
    // ==================================================
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }
}
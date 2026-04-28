package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.sql.*;
import java.time.LocalDate;

public class CommunicationController {

    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField lienField;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private TextField dureeField;
    @FXML private ComboBox<String> etatCombo;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Publication> publicationCombo;
    @FXML private TableView<Communication> communicationTable;
    @FXML private TextField searchField;
    @FXML private Label statsLabel;

    private ObservableList<Communication> communicationList = FXCollections.observableArrayList();
    private ObservableList<Publication> publicationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll("live", "record");
        etatCombo.getItems().addAll("actif", "terminé", "annulé", "planifié");
        etatCombo.setValue("actif");
        heureField.setPromptText("HH:MM:SS");
        setupTableColumns();
        loadCommunications();
        loadPublicationsForCombo();
        updateStatistics();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/learnflexplus", "root", "");
    }

    private void setupTableColumns() {
        TableColumn<Communication, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        idCol.setPrefWidth(50);

        TableColumn<Communication, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
        typeCol.setPrefWidth(80);

        TableColumn<Communication, String> lienCol = new TableColumn<>("Lien");
        lienCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLien()));
        lienCol.setPrefWidth(200);

        TableColumn<Communication, String> dateCol = new TableColumn<>("Date & Heure");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDateHeure()));
        dateCol.setPrefWidth(160);

        TableColumn<Communication, Integer> dureeCol = new TableColumn<>("Durée (min)");
        dureeCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDuree()).asObject());
        dureeCol.setPrefWidth(80);

        TableColumn<Communication, String> etatCol = new TableColumn<>("État");
        etatCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEtat()));
        etatCol.setPrefWidth(100);

        TableColumn<Communication, String> publicationCol = new TableColumn<>("Publication liée");
        publicationCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPublicationTitre()));
        publicationCol.setPrefWidth(150);

        TableColumn<Communication, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(120);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color:#3498db;-fx-text-fill:white;-fx-cursor:hand;");
                editBtn.setOnAction(e -> editCommunication(getTableView().getItems().get(getIndex())));
                deleteBtn.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-cursor:hand;");
                deleteBtn.setOnAction(e -> deleteCommunication(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        communicationTable.getColumns().setAll(idCol, typeCol, lienCol, dateCol, dureeCol, etatCol, publicationCol, actionsCol);
        communicationTable.setItems(communicationList);
    }

    private void loadCommunications() {
        communicationList.clear();
        String sql = "SELECT c.*, p.titre as publication_titre FROM communication c LEFT JOIN publication p ON c.publication_id = p.id ORDER BY c.date_heure DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                communicationList.add(new Communication(
                        rs.getInt("id"), rs.getString("type"), rs.getString("lien"),
                        rs.getString("date_heure"), rs.getInt("duree"), rs.getString("etat"),
                        rs.getString("description_detaillee"), rs.getInt("publication_id"),
                        rs.getString("publication_titre")));
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les communications: " + e.getMessage());
        }
    }

    private void loadPublicationsForCombo() {
        publicationList.clear();
        publicationCombo.getItems().clear();
        String sql = "SELECT id, titre FROM publication ORDER BY date_creation DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Publication pub = new Publication(rs.getInt("id"), rs.getString("titre"));
                publicationList.add(pub);
                publicationCombo.getItems().add(pub);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des publications: " + e.getMessage());
        }
    }

    @FXML
    private void saveCommunication() {
        if (typeCombo.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un type !");
            typeCombo.requestFocus();
            return;
        }
        if (lienField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le lien est obligatoire !");
            lienField.requestFocus();
            return;
        }
        if (!lienField.getText().matches("^(http|https)://.*$")) {
            showAlert("Erreur", "Le lien doit commencer par http:// ou https:// !");
            lienField.requestFocus();
            return;
        }
        if (datePicker.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une date !");
            datePicker.requestFocus();
            return;
        }
        if (heureField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer l'heure !");
            heureField.requestFocus();
            return;
        }
        if (!heureField.getText().matches("^([0-1][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$")) {
            showAlert("Erreur", "Format d'heure invalide ! Utilisez HH:MM:SS");
            heureField.requestFocus();
            return;
        }
        if (dureeField.getText().trim().isEmpty()) {
            showAlert("Erreur", "La durée est obligatoire !");
            dureeField.requestFocus();
            return;
        }
        try {
            int duree = Integer.parseInt(dureeField.getText());
            if (duree <= 0 || duree > 480) {
                showAlert("Erreur", "La durée doit être entre 1 et 480 minutes !");
                dureeField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La durée doit être un nombre valide !");
            dureeField.requestFocus();
            return;
        }

        String sql = "INSERT INTO communication (type, lien, date_heure, duree, etat, description_detaillee, publication_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String dateTime = datePicker.getValue().toString() + " " + heureField.getText();
            pstmt.setString(1, typeCombo.getValue());
            pstmt.setString(2, lienField.getText().trim());
            pstmt.setString(3, dateTime);
            pstmt.setInt(4, Integer.parseInt(dureeField.getText()));
            pstmt.setString(5, etatCombo.getValue());
            pstmt.setString(6, descriptionArea.getText().trim().isEmpty() ? null : descriptionArea.getText().trim());
            if (publicationCombo.getValue() != null) {
                pstmt.setInt(7, publicationCombo.getValue().getId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            pstmt.executeUpdate();
            showAlert("Succès", "Communication ajoutée avec succès !");
            clearForm();
            loadCommunications();
            loadPublicationsForCombo();
            updateStatistics();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void editCommunication(Communication communication) {
        typeCombo.setValue(communication.getType());
        lienField.setText(communication.getLien());
        if (communication.getDateHeure() != null && communication.getDateHeure().length() >= 19) {
            datePicker.setValue(LocalDate.parse(communication.getDateHeure().substring(0, 10)));
            heureField.setText(communication.getDateHeure().substring(11, 19));
        }
        dureeField.setText(String.valueOf(communication.getDuree()));
        etatCombo.setValue(communication.getEtat());
        descriptionArea.setText(communication.getDescription());
        if (communication.getPublicationId() != 0) {
            publicationCombo.getItems().stream()
                    .filter(p -> p.getId() == communication.getPublicationId())
                    .findFirst()
                    .ifPresent(publicationCombo::setValue);
        }
        Button saveBtn = findSaveButton();
        if (saveBtn != null) {
            saveBtn.setText("Mettre à jour");
            saveBtn.setOnAction(e -> updateCommunication(communication.getId()));
        }
    }

    private void updateCommunication(int id) {
        String sql = "UPDATE communication SET type=?, lien=?, date_heure=?, duree=?, etat=?, description_detaillee=?, publication_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String dateTime = datePicker.getValue().toString() + " " + heureField.getText();
            pstmt.setString(1, typeCombo.getValue());
            pstmt.setString(2, lienField.getText().trim());
            pstmt.setString(3, dateTime);
            pstmt.setInt(4, Integer.parseInt(dureeField.getText()));
            pstmt.setString(5, etatCombo.getValue());
            pstmt.setString(6, descriptionArea.getText().trim().isEmpty() ? null : descriptionArea.getText().trim());
            if (publicationCombo.getValue() != null) {
                pstmt.setInt(7, publicationCombo.getValue().getId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            pstmt.setInt(8, id);
            pstmt.executeUpdate();
            showAlert("Succès", "Communication mise à jour !");
            clearForm();
            loadCommunications();
            loadPublicationsForCombo();
            updateStatistics();
            restoreSaveButton();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    private void deleteCommunication(Communication communication) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la communication");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette communication ?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            String sql = "DELETE FROM communication WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, communication.getId());
                pstmt.executeUpdate();
                showAlert("Succès", "Communication supprimée !");
                loadCommunications();
                updateStatistics();
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void searchCommunications() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadCommunications(); return; }
        communicationList.clear();
        String sql = "SELECT c.*, p.titre as publication_titre FROM communication c LEFT JOIN publication p ON c.publication_id = p.id WHERE c.type LIKE ? OR c.lien LIKE ? OR c.etat LIKE ? OR p.titre LIKE ? ORDER BY c.date_heure DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, pattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                communicationList.add(new Communication(
                        rs.getInt("id"), rs.getString("type"), rs.getString("lien"),
                        rs.getString("date_heure"), rs.getInt("duree"), rs.getString("etat"),
                        rs.getString("description_detaillee"), rs.getInt("publication_id"),
                        rs.getString("publication_titre")));
            }
            if (communicationList.isEmpty()) {
                showAlert("Information", "Aucune communication trouvée pour : " + keyword);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    @FXML private void filterByLive() {
        communicationList.clear();
        loadFilteredCommunications("SELECT c.*, p.titre as publication_titre FROM communication c LEFT JOIN publication p ON c.publication_id = p.id WHERE c.type='live' ORDER BY c.date_heure DESC");
    }

    @FXML private void filterByRecord() {
        communicationList.clear();
        loadFilteredCommunications("SELECT c.*, p.titre as publication_titre FROM communication c LEFT JOIN publication p ON c.publication_id = p.id WHERE c.type='record' ORDER BY c.date_heure DESC");
    }

    @FXML private void filterByActive() {
        communicationList.clear();
        loadFilteredCommunications("SELECT c.*, p.titre as publication_titre FROM communication c LEFT JOIN publication p ON c.publication_id = p.id WHERE c.etat='actif' ORDER BY c.date_heure DESC");
    }

    private void loadFilteredCommunications(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                communicationList.add(new Communication(
                        rs.getInt("id"), rs.getString("type"), rs.getString("lien"),
                        rs.getString("date_heure"), rs.getInt("duree"), rs.getString("etat"),
                        rs.getString("description_detaillee"), rs.getInt("publication_id"),
                        rs.getString("publication_titre")));
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du filtrage: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) as total FROM communication");
            rs1.next();
            int total = rs1.getInt("total");
            int liveCount = 0, recordCount = 0;
            ResultSet rs2 = stmt.executeQuery("SELECT type, COUNT(*) as count FROM communication GROUP BY type");
            while (rs2.next()) {
                if ("live".equals(rs2.getString("type"))) liveCount = rs2.getInt("count");
                else if ("record".equals(rs2.getString("type"))) recordCount = rs2.getInt("count");
            }
            statsLabel.setText(String.format("📊 Statistiques: %d communications | Live: %d | Record: %d", total, liveCount, recordCount));
        } catch (SQLException e) {
            statsLabel.setText("📊 Statistiques: erreur de chargement");
        }
    }

    @FXML private void clearForm() {
        typeCombo.setValue(null);
        lienField.clear();
        datePicker.setValue(null);
        heureField.clear();
        dureeField.clear();
        etatCombo.setValue("actif");
        descriptionArea.clear();
        publicationCombo.setValue(null);
    }

    private Button findSaveButton() {
        try { return (Button) lienField.getScene().lookup("#saveButton"); } catch (Exception e) { return null; }
    }

    private void restoreSaveButton() {
        Button saveBtn = findSaveButton();
        if (saveBtn != null) { saveBtn.setText("Enregistrer"); saveBtn.setOnAction(e -> saveCommunication()); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class Communication {
        private final int id, duree, publicationId;
        private final String type, lien, dateHeure, etat, description, publicationTitre;
        public Communication(int id, String type, String lien, String dateHeure, int duree, String etat, String description, int publicationId, String publicationTitre) {
            this.id = id; this.type = type; this.lien = lien; this.dateHeure = dateHeure;
            this.duree = duree; this.etat = etat; this.description = description;
            this.publicationId = publicationId; this.publicationTitre = publicationTitre != null ? publicationTitre : "Aucune";
        }
        public int getId() { return id; }
        public String getType() { return type; }
        public String getLien() { return lien; }
        public String getDateHeure() { return dateHeure; }
        public int getDuree() { return duree; }
        public String getEtat() { return etat; }
        public String getDescription() { return description; }
        public int getPublicationId() { return publicationId; }
        public String getPublicationTitre() { return publicationTitre; }
    }

    public static class Publication {
        private final int id;
        private final String titre;
        public Publication(int id, String titre) { this.id = id; this.titre = titre; }
        public int getId() { return id; }
        public String getTitre() { return titre; }
        @Override public String toString() { return titre; }
    }
}
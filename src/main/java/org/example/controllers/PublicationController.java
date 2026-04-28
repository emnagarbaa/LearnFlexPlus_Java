package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PublicationController {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchCriteriacombo;
    @FXML private TableView<Publication> publicationTable;
    @FXML private Label statsLabel;

    private ObservableList<Publication> publicationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        categorieCombo.getItems().addAll("Problème technique", "Demande d'information", "Suggestion", "Réclamation", "Autre");
        searchCriteriacombo.getItems().addAll("Titre", "Description", "Catégorie");
        searchCriteriacombo.setValue("Titre");
        setupTableColumns();
        loadPublications();
        updateStatistics();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/learnflexplus", "root", "");
    }

    private void setupTableColumns() {
        TableColumn<Publication, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        idCol.setPrefWidth(50);

        TableColumn<Publication, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitre()));
        titreCol.setPrefWidth(180);

        TableColumn<Publication, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        descriptionCol.setPrefWidth(250);

        TableColumn<Publication, String> dateCol = new TableColumn<>("Date création");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDateCreation()));
        dateCol.setPrefWidth(140);

        TableColumn<Publication, String> categorieCol = new TableColumn<>("Catégorie");
        categorieCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategorie()));
        categorieCol.setPrefWidth(120);

        TableColumn<Publication, Integer> vuesCol = new TableColumn<>("Vues");
        vuesCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNombreVues()).asObject());
        vuesCol.setPrefWidth(60);

        TableColumn<Publication, Integer> likesCol = new TableColumn<>("👍 Likes");
        likesCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNombreLikes()).asObject());
        likesCol.setPrefWidth(70);

        TableColumn<Publication, Integer> dislikesCol = new TableColumn<>("👎 Dislikes");
        dislikesCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNombreDislikes()).asObject());
        dislikesCol.setPrefWidth(80);

        TableColumn<Publication, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(180);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button likeBtn    = new Button("👍");
            private final Button dislikeBtn = new Button("👎");
            private final Button editBtn    = new Button("✏️");
            private final Button deleteBtn  = new Button("🗑️");
            private final HBox buttons      = new HBox(5, likeBtn, dislikeBtn, editBtn, deleteBtn);
            {
                likeBtn.setStyle("-fx-background-color:#2ecc71; -fx-text-fill:white; -fx-cursor:hand;");
                likeBtn.setOnAction(e -> incrementLikes(getTableView().getItems().get(getIndex())));

                dislikeBtn.setStyle("-fx-background-color:#e67e22; -fx-text-fill:white; -fx-cursor:hand;");
                dislikeBtn.setOnAction(e -> incrementDislikes(getTableView().getItems().get(getIndex())));

                editBtn.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; -fx-cursor:hand;");
                editBtn.setOnAction(e -> editPublication(getTableView().getItems().get(getIndex())));

                deleteBtn.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-cursor:hand;");
                deleteBtn.setOnAction(e -> deletePublication(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        publicationTable.getColumns().setAll(
                idCol, titreCol, descriptionCol, dateCol,
                categorieCol, vuesCol, likesCol, dislikesCol, actionsCol);
        publicationTable.setItems(publicationList);
    }

    private void loadPublications() {
        publicationList.clear();
        String sql = "SELECT * FROM publication ORDER BY date_creation DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                publicationList.add(new Publication(
                        rs.getInt("id"),              // ✅ corrigé : id pas id_publication
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("date_creation"),
                        rs.getString("categorie"),
                        rs.getInt("nombre_vues"),
                        rs.getInt("nombre_likes"),
                        rs.getInt("nombre_dislikes")));
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les publications: " + e.getMessage());
        }
    }

    @FXML
    private void savePublication() {
        if (titreField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le titre est obligatoire !"); titreField.requestFocus(); return;
        }
        if (titreField.getText().length() < 3) {
            showAlert("Erreur", "Le titre doit contenir au moins 3 caractères !"); titreField.requestFocus(); return;
        }
        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert("Erreur", "La description est obligatoire !"); descriptionArea.requestFocus(); return;
        }
        if (descriptionArea.getText().length() < 10) {
            showAlert("Erreur", "La description doit contenir au moins 10 caractères !"); descriptionArea.requestFocus(); return;
        }
        if (categorieCombo.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une catégorie !"); categorieCombo.requestFocus(); return;
        }

        String sql = "INSERT INTO publication (titre, description, date_creation, categorie, nombre_vues, nombre_likes, nombre_dislikes) VALUES (?, ?, ?, ?, 0, 0, 0)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, titreField.getText().trim());
            pstmt.setString(2, descriptionArea.getText().trim());
            pstmt.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setString(4, categorieCombo.getValue());
            pstmt.executeUpdate();
            showAlert("Succès", "Publication ajoutée avec succès !");
            clearForm();
            loadPublications();
            updateStatistics();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void incrementLikes(Publication publication) {
        // ✅ corrigé : id pas id_publication
        String sql = "UPDATE publication SET nombre_likes=nombre_likes+1, nombre_vues=nombre_vues+1 WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, publication.getId());
            pstmt.executeUpdate();
            loadPublications();
            updateStatistics();
            showAlert("Succès", "Merci pour votre like ! 👍");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du like: " + e.getMessage());
        }
    }

    private void incrementDislikes(Publication publication) {
        // ✅ corrigé : id pas id_publication
        String sql = "UPDATE publication SET nombre_dislikes=nombre_dislikes+1 WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, publication.getId());
            pstmt.executeUpdate();
            loadPublications();
            updateStatistics();
            showAlert("Succès", "Dislike enregistré ! 👎");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du dislike: " + e.getMessage());
        }
    }

    private void incrementView(int id) {
        // ✅ corrigé : id pas id_publication
        String sql = "UPDATE publication SET nombre_vues=nombre_vues+1 WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private void editPublication(Publication publication) {
        titreField.setText(publication.getTitre());
        descriptionArea.setText(publication.getDescription());
        categorieCombo.setValue(publication.getCategorie());
        Button saveBtn = findSaveButton();
        if (saveBtn != null) {
            saveBtn.setText("Mettre à jour");
            saveBtn.setOnAction(e -> updatePublication(publication.getId()));
        }
    }

    private void updatePublication(int id) {
        if (titreField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le titre est obligatoire !"); titreField.requestFocus(); return;
        }
        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert("Erreur", "La description est obligatoire !"); descriptionArea.requestFocus(); return;
        }
        // ✅ corrigé : id pas id_publication
        String sql = "UPDATE publication SET titre=?, description=?, categorie=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, titreField.getText().trim());
            pstmt.setString(2, descriptionArea.getText().trim());
            pstmt.setString(3, categorieCombo.getValue());
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
            showAlert("Succès", "Publication mise à jour !");
            clearForm();
            loadPublications();
            updateStatistics();
            restoreSaveButton();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    private void deletePublication(Publication publication) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la publication");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer : \"" + publication.getTitre() + "\" ?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            // ✅ corrigé : id pas id_publication
            String sql = "DELETE FROM publication WHERE id=?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, publication.getId());
                pstmt.executeUpdate();
                showAlert("Succès", "Publication supprimée !");
                loadPublications();
                updateStatistics();
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void searchPublications() {
        String keyword = searchField.getText().trim();
        String criteria = searchCriteriacombo.getValue();
        if (keyword.isEmpty()) { loadPublications(); return; }
        publicationList.clear();
        String sql;
        switch (criteria) {
            case "Description": sql = "SELECT * FROM publication WHERE description LIKE ? ORDER BY date_creation DESC"; break;
            case "Catégorie":   sql = "SELECT * FROM publication WHERE categorie = ? ORDER BY date_creation DESC"; break;
            default:            sql = "SELECT * FROM publication WHERE titre LIKE ? ORDER BY date_creation DESC"; break;
        }
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, criteria.equals("Catégorie") ? keyword : "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                publicationList.add(new Publication(
                        rs.getInt("id"),              // ✅ corrigé
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("date_creation"),
                        rs.getString("categorie"),
                        rs.getInt("nombre_vues"),
                        rs.getInt("nombre_likes"),
                        rs.getInt("nombre_dislikes")));
            }
            if (publicationList.isEmpty())
                showAlert("Information", "Aucune publication trouvée pour : " + keyword);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    @FXML private void sortByDate()  { publicationList.sort((p1, p2) -> p2.getDateCreation().compareTo(p1.getDateCreation())); }
    @FXML private void sortByViews() { publicationList.sort((p1, p2) -> Integer.compare(p2.getNombreVues(), p1.getNombreVues())); }
    @FXML private void sortByLikes() { publicationList.sort((p1, p2) -> Integer.compare(p2.getNombreLikes(), p1.getNombreLikes())); }

    private void updateStatistics() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) as total FROM publication");
            rs1.next(); int total = rs1.getInt("total");
            ResultSet rs2 = stmt.executeQuery("SELECT SUM(nombre_vues) as totalVues FROM publication");
            rs2.next(); int totalVues = rs2.getInt("totalVues");
            ResultSet rs3 = stmt.executeQuery("SELECT SUM(nombre_likes) as totalLikes FROM publication");
            rs3.next(); int totalLikes = rs3.getInt("totalLikes");
            ResultSet rs4 = stmt.executeQuery("SELECT SUM(nombre_dislikes) as totalDislikes FROM publication");
            rs4.next(); int totalDislikes = rs4.getInt("totalDislikes");
            double moyenne = total > 0 ? (double) totalVues / total : 0;
            statsLabel.setText(String.format(
                    "📊 %d publications | %d vues | 👍 %d | 👎 %d | %.1f vues/pub",
                    total, totalVues, totalLikes, totalDislikes, moyenne));
        } catch (SQLException e) {
            statsLabel.setText("📊 Statistiques: erreur de chargement");
        }
    }

    @FXML private void exportToPDF() { showAlert("Export PDF", "Fonctionnalité d'export PDF en cours de développement."); }

    @FXML private void clearForm() {
        titreField.clear(); descriptionArea.clear();
        categorieCombo.setValue(null); titreField.requestFocus();
    }

    private Button findSaveButton() {
        try { return (Button) titreField.getScene().lookup("#saveButton"); } catch (Exception e) { return null; }
    }

    private void restoreSaveButton() {
        Button saveBtn = findSaveButton();
        if (saveBtn != null) { saveBtn.setText("Publier"); saveBtn.setOnAction(e -> savePublication()); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(content); alert.showAndWait();
    }

    // ===== MODÈLE =====
    public static class Publication {
        private final int id, nombreVues, nombreLikes, nombreDislikes;
        private final String titre, description, dateCreation, categorie;

        public Publication(int id, String titre, String description, String dateCreation,
                           String categorie, int nombreVues, int nombreLikes, int nombreDislikes) {
            this.id = id; this.titre = titre; this.description = description;
            this.dateCreation = dateCreation; this.categorie = categorie;
            this.nombreVues = nombreVues; this.nombreLikes = nombreLikes;
            this.nombreDislikes = nombreDislikes;
        }

        public int getId() { return id; }
        public String getTitre() { return titre; }
        public String getDescription() { return description; }
        public String getDateCreation() { return dateCreation; }
        public String getCategorie() { return categorie; }
        public int getNombreVues() { return nombreVues; }
        public int getNombreLikes() { return nombreLikes; }
        public int getNombreDislikes() { return nombreDislikes; }
        @Override public String toString() { return titre; }
    }
}
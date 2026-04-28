package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FrontPublicationController {

    @FXML private FlowPane cardsContainer;
    @FXML private Label statsLabel;
    @FXML private Label noDataLabel;
    @FXML private ComboBox<String> categorieFilter;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private TextField searchField;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView logo;
    @FXML private Label formTitle;
    @FXML private Button saveBtn;
    @FXML private Label titreError;
    @FXML private Label descError;
    @FXML private Label categorieError;

    private ObservableList<Publication> publicationList = FXCollections.observableArrayList();
    private Integer editingId = null;

    @FXML
    public void initialize() {
        try {
            logo.setImage(new Image(
                    getClass().getResourceAsStream("/org/example/images/logo1.png")));
        } catch (Exception ignored) {}

        categorieCombo.getItems().addAll(
                "Problème technique", "Demande d'information",
                "Suggestion", "Réclamation", "Autre");

        categorieFilter.getItems().addAll(
                "Toutes", "Problème technique", "Demande d'information",
                "Suggestion", "Réclamation", "Autre");
        categorieFilter.setValue("Toutes");
        categorieFilter.setOnAction(e -> loadPublications());

        loadPublications();
        updateStatistics();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/learnflexplus", "root", "");
    }

    // ===== VALIDATION =====
    private boolean validate() {
        boolean valid = true;

        // Titre
        String titre = titreField.getText().trim();
        if (titre.isEmpty()) {
            showError(titreError, "Le titre est obligatoire !");
            valid = false;
        } else if (titre.length() < 3) {
            showError(titreError, "Le titre doit contenir au moins 3 caractères !");
            valid = false;
        } else if (titre.length() > 100) {
            showError(titreError, "Le titre ne doit pas dépasser 100 caractères !");
            valid = false;
        } else {
            hideError(titreError);
        }

        // Description
        String desc = descriptionArea.getText().trim();
        if (desc.isEmpty()) {
            showError(descError, "La description est obligatoire !");
            valid = false;
        } else if (desc.length() < 10) {
            showError(descError, "La description doit contenir au moins 10 caractères !");
            valid = false;
        } else if (desc.length() > 1000) {
            showError(descError, "La description ne doit pas dépasser 1000 caractères !");
            valid = false;
        } else {
            hideError(descError);
        }

        // Catégorie
        if (categorieCombo.getValue() == null) {
            showError(categorieError, "Veuillez sélectionner une catégorie !");
            valid = false;
        } else {
            hideError(categorieError);
        }

        return valid;
    }

    private void showError(Label label, String message) {
        label.setText("⚠ " + message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    // ===== CRUD =====
    @FXML
    private void savePublication() {
        if (!validate()) return;

        if (editingId == null) {
            insertPublication();
        } else {
            updatePublication();
        }
    }

    private void insertPublication() {
        String sql = "INSERT INTO publication (titre, description, date_creation, categorie, nombre_vues, nombre_likes) VALUES (?, ?, ?, ?, 0, 0)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, titreField.getText().trim());
            pstmt.setString(2, descriptionArea.getText().trim());
            pstmt.setString(3, LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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

    private void updatePublication() {
        String sql = "UPDATE publication SET titre=?, description=?, categorie=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, titreField.getText().trim());
            pstmt.setString(2, descriptionArea.getText().trim());
            pstmt.setString(3, categorieCombo.getValue());
            pstmt.setInt(4, editingId);
            pstmt.executeUpdate();
            showAlert("Succès", "Publication mise à jour !");
            clearForm();
            loadPublications();
            updateStatistics();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    private void deletePublication(Publication pub) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer \"" + pub.getTitre() + "\" ?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            String sql = "DELETE FROM publication WHERE id=?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, pub.getId());
                pstmt.executeUpdate();
                showAlert("Succès", "Publication supprimée !");
                loadPublications();
                updateStatistics();
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void incrementLikes(Publication pub, Label likesLabel, Button likeBtn) {
        String sql = "UPDATE publication SET nombre_likes=nombre_likes+1, nombre_vues=nombre_vues+1 WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pub.getId());
            pstmt.executeUpdate();
            likesLabel.setText("👍 " + (pub.getNombreLikes() + 1));
            likeBtn.setDisable(true);
            likeBtn.setText("👍 Aimé");
            updateStatistics();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du like: " + e.getMessage());
        }
    }

    // ===== CHARGEMENT =====
    private void loadPublications() {
        publicationList.clear();
        String sql = "SELECT * FROM publication ORDER BY date_creation DESC";
        if (categorieFilter.getValue() != null && !categorieFilter.getValue().equals("Toutes")) {
            sql = "SELECT * FROM publication WHERE categorie=? ORDER BY date_creation DESC";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, categorieFilter.getValue());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) addToList(rs);
                displayCards();
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage());
            }
            return;
        }
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) addToList(rs);
            displayCards();
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void addToList(ResultSet rs) throws SQLException {
        publicationList.add(new Publication(
                rs.getInt("id"), rs.getString("titre"),
                rs.getString("description"), rs.getString("date_creation"),
                rs.getString("categorie"), rs.getInt("nombre_vues"),
                rs.getInt("nombre_likes")
        ));
    }

    private void displayCards() {
        cardsContainer.getChildren().clear();
        if (publicationList.isEmpty()) {
            noDataLabel.setVisible(true);
            noDataLabel.setManaged(true);
            return;
        }
        noDataLabel.setVisible(false);
        noDataLabel.setManaged(false);
        for (Publication pub : publicationList) {
            cardsContainer.getChildren().add(createCard(pub));
        }
    }

    private VBox createCard(Publication pub) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color:white; -fx-background-radius:15; " +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);");
        card.setPrefWidth(350);
        card.setPadding(new Insets(15));

        // Badge catégorie
        Label catLabel = new Label(pub.getCategorie());
        catLabel.setStyle("-fx-background-color:" + getCategorieColor(pub.getCategorie()) +
                "; -fx-text-fill:white; -fx-background-radius:15; " +
                "-fx-padding:4 12; -fx-font-size:11px;");

        // Titre
        Label titreLabel = new Label(pub.getTitre());
        titreLabel.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#2c3e50; -fx-wrap-text:true;");
        titreLabel.setWrapText(true);

        // Description
        Label descLabel = new Label(pub.getDescription());
        descLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#7f8c8d; -fx-wrap-text:true;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(60);

        // Date
        Label dateLabel = new Label("📅 " + pub.getDateCreation());
        dateLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#95a5a6;");

        // Boutons like + edit + delete
        Label likesLabel = new Label("👍 " + pub.getNombreLikes());
        likesLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#e74c3c; -fx-font-weight:bold;");

        Button likeBtn = new Button("J'aime");
        likeBtn.setStyle("-fx-background-color:#2ecc71; -fx-text-fill:white; " +
                "-fx-background-radius:20; -fx-padding:5 15; -fx-cursor:hand;");
        likeBtn.setOnAction(e -> incrementLikes(pub, likesLabel, likeBtn));

        Button editBtn = new Button("✏️");
        editBtn.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; " +
                "-fx-background-radius:20; -fx-padding:5 10; -fx-cursor:hand;");
        editBtn.setOnAction(e -> fillFormForEdit(pub));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; " +
                "-fx-background-radius:20; -fx-padding:5 10; -fx-cursor:hand;");
        deleteBtn.setOnAction(e -> deletePublication(pub));

        HBox actions = new HBox(10, likesLabel, likeBtn, editBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(catLabel, titreLabel, descLabel, dateLabel, actions);
        return card;
    }

    private void fillFormForEdit(Publication pub) {
        editingId = pub.getId();
        titreField.setText(pub.getTitre());
        descriptionArea.setText(pub.getDescription());
        categorieCombo.setValue(pub.getCategorie());
        formTitle.setText("✏️ Modifier la publication");
        saveBtn.setText("💾 Mettre à jour");
        titreField.requestFocus();
    }

    @FXML
    private void clearForm() {
        editingId = null;
        titreField.clear();
        descriptionArea.clear();
        categorieCombo.setValue(null);
        formTitle.setText("✏️ Nouvelle publication");
        saveBtn.setText("💾 Publier");
        hideError(titreError);
        hideError(descError);
        hideError(categorieError);
    }

    @FXML
    private void searchPublications() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadPublications(); return; }
        publicationList.clear();
        String sql = "SELECT * FROM publication WHERE titre LIKE ? OR description LIKE ? ORDER BY date_creation DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) addToList(rs);
            displayCards();
            if (publicationList.isEmpty())
                showAlert("Information", "Aucune publication trouvée pour : " + keyword);
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void updateStatistics() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) as total, SUM(nombre_likes) as totalLikes FROM publication");
            rs.next();
            statsLabel.setText(String.format("📊 %d publications | %d likes",
                    rs.getInt("total"), rs.getInt("totalLikes")));
        } catch (SQLException e) {
            statsLabel.setText("📊 Statistiques indisponibles");
        }
    }

    private String getCategorieColor(String categorie) {
        switch (categorie) {
            case "Problème technique":    return "#e74c3c";
            case "Demande d'information": return "#3498db";
            case "Suggestion":            return "#2ecc71";
            case "Réclamation":           return "#f39c12";
            default:                      return "#95a5a6";
        }
    }

    // ===== NAVIGATION =====
    @FXML private void goToAccueil()       { navigateTo("/org/example/fxml/front.fxml", "LearnFlex+"); }
    @FXML private void goToPublications()  { navigateTo("/org/example/fxml/PublicationsFrontPage.fxml", "Publications"); }
    @FXML private void goToCommunications(){ navigateTo("/org/example/fxml/CommunicationsFrontPage.fxml", "Communications"); }
    @FXML private void goToCours()         { navigateTo("/org/example/fxml/cours.fxml", "Cours"); }
    @FXML private void goToQuiz()          { navigateTo("/org/example/fxml/ExamenView.fxml", "Quiz"); }
    @FXML private void goToConnexion()     { navigateTo("/org/example/fxml/login.fxml", "Connexion"); }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ===== MODÈLE =====
    public static class Publication {
        private int id, nombreVues, nombreLikes;
        private String titre, description, dateCreation, categorie;

        public Publication(int id, String titre, String description,
                           String dateCreation, String categorie,
                           int nombreVues, int nombreLikes) {
            this.id = id; this.titre = titre; this.description = description;
            this.dateCreation = dateCreation; this.categorie = categorie;
            this.nombreVues = nombreVues; this.nombreLikes = nombreLikes;
        }

        public int getId() { return id; }
        public String getTitre() { return titre; }
        public String getDescription() { return description; }
        public String getDateCreation() { return dateCreation; }
        public String getCategorie() { return categorie; }
        public int getNombreVues() { return nombreVues; }
        public int getNombreLikes() { return nombreLikes; }
        public void setNombreLikes(int likes) { this.nombreLikes = likes; }
    }
}
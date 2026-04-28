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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;

public class FrontCommunicationController {

    // ===== ÉLÉMENTS FXML =====
    @FXML private FlowPane cardsContainer;
    @FXML private Label statsLabel;
    @FXML private Label noDataLabel;
    @FXML private ComboBox<String> typeFilter;
    @FXML private TextField searchField;
    @FXML private ImageView logo;

    private ObservableList<Communication> communicationList = FXCollections.observableArrayList();

    // ===== INITIALISATION =====
    @FXML
    public void initialize() {
        System.out.println("✅ FrontCommunicationController initialisé");

        try {
            logo.setImage(new Image(getClass().getResourceAsStream("/org/example/images/logo1.png")));
        } catch (Exception e) {
            System.err.println("Logo non chargé");
        }

        typeFilter.getItems().addAll("Tous", "live", "record");
        typeFilter.setValue("Tous");
        typeFilter.setOnAction(e -> loadCommunications());

        loadCommunications();
        updateStatistics();
    }

    // ===== CONNEXION DB =====
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/learnflexplus", "root", "");
    }

    // ===== LECTURE =====
    private void loadCommunications() {
        communicationList.clear();
        String sql;
        String filterValue = typeFilter.getValue();

        if (filterValue == null || filterValue.equals("Tous")) {
            sql = "SELECT * FROM communication ORDER BY date_heure DESC";
        } else {
            sql = "SELECT * FROM communication WHERE type = ? ORDER BY date_heure DESC";
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (filterValue != null && !filterValue.equals("Tous")) {
                pstmt.setString(1, filterValue);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                communicationList.add(new Communication(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("lien"),
                        rs.getString("date_heure"),
                        rs.getInt("duree"),
                        rs.getString("etat"),
                        rs.getString("description_detaillee"),
                        rs.getInt("publication_id")
                ));
            }
            displayCards();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les communications: " + e.getMessage());
        }
    }

    private void displayCards() {
        cardsContainer.getChildren().clear();

        if (communicationList.isEmpty()) {
            noDataLabel.setVisible(true);
            return;
        }

        noDataLabel.setVisible(false);

        for (Communication comm : communicationList) {
            VBox card = createCommunicationCard(comm);
            cardsContainer.getChildren().add(card);
        }
    }

    // ===== CARTE COMMUNICATION (avec boutons CRUD) =====
    private VBox createCommunicationCard(Communication comm) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");
        card.setPrefWidth(320);
        card.setPadding(new Insets(15));
        card.setSpacing(10);

        // Badge type
        Label typeLabel = new Label(comm.getType().toUpperCase());
        String typeColor = comm.getType().equals("live") ? "#3498db" : "#9b59b6";
        typeLabel.setStyle("-fx-background-color: " + typeColor
                + "; -fx-text-fill: white; -fx-background-radius: 15;"
                + " -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        // Badge état
        Label etatLabel = new Label(comm.getEtat());
        String etatColor = getEtatColor(comm.getEtat());
        etatLabel.setStyle("-fx-background-color: " + etatColor
                + "; -fx-text-fill: white; -fx-background-radius: 15;"
                + " -fx-padding: 4 12; -fx-font-size: 11px;");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(typeLabel, etatLabel);

        // Lien
        Label lienLabel = new Label(comm.getLien());
        lienLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db; -fx-wrap-text: true;");
        lienLabel.setWrapText(true);
        lienLabel.setMaxWidth(290);

        // Date & durée
        Label dateLabel = new Label("📅 " + comm.getDateHeure());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");

        Label dureeLabel = new Label("⏱️ " + comm.getDuree() + " min");
        dureeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");

        HBox infoBox = new HBox(15);
        infoBox.getChildren().addAll(dateLabel, dureeLabel);

        // Description (si disponible)
        if (comm.getDescription() != null && !comm.getDescription().isEmpty()) {
            Label descLabel = new Label(comm.getDescription());
            descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(290);
            card.getChildren().addAll(headerBox, lienLabel, infoBox, descLabel);
        } else {
            card.getChildren().addAll(headerBox, lienLabel, infoBox);
        }

        // Bouton rejoindre
        Button joinBtn = new Button(comm.getType().equals("live")
                ? "🎥 Rejoindre le live" : "📹 Voir l'enregistrement");
        joinBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"
                + " -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-weight: bold;");
        joinBtn.setMaxWidth(Double.MAX_VALUE);
        joinBtn.setOnAction(e -> openLink(comm.getLien()));

        // Séparateur
        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.3;");

        // Boutons Modifier / Supprimer
        Button editBtn = new Button("✏️ Modifier");
        editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;"
                + " -fx-background-radius: 15; -fx-padding: 6 14; -fx-cursor: hand; -fx-font-size: 11px;");
        editBtn.setOnAction(e -> openEditDialog(comm));

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;"
                + " -fx-background-radius: 15; -fx-padding: 6 14; -fx-cursor: hand; -fx-font-size: 11px;");
        deleteBtn.setOnAction(e -> deleteCommunication(comm.getId()));

        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(joinBtn, sep, actionBox);
        return card;
    }

    // ===== CRUD - CRÉER =====
    @FXML
    private void openCreateDialog() {
        showCommunicationDialog(null);
    }

    // ===== CRUD - MODIFIER =====
    private void openEditDialog(Communication comm) {
        showCommunicationDialog(comm);
    }

    // ===== DIALOG PARTAGÉ CRÉER / MODIFIER =====
    private void showCommunicationDialog(Communication comm) {
        boolean isEdit = (comm != null);

        Stage dialog = new Stage();
        dialog.setTitle(isEdit ? "Modifier la communication" : "Créer une communication");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        // ---- Formulaire ----
        VBox form = new VBox(12);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: #f8fafc;");
        form.setPrefWidth(430);

        Label titleLbl = new Label(isEdit ? "✏️ Modifier la Communication" : "➕ Nouvelle Communication");
        titleLbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1f4f65;");

        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.4;");

        // Type
        Label typeLabel = new Label("Type *");
        styleFormLabel(typeLabel);
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("live", "record");
        typeCombo.setPromptText("Sélectionner le type");
        typeCombo.setPrefWidth(380);
        styleComboBox(typeCombo);
        if (isEdit) typeCombo.setValue(comm.getType());

        // Lien
        Label lienLabel = new Label("Lien (URL) *");
        styleFormLabel(lienLabel);
        TextField lienField = new TextField(isEdit ? comm.getLien() : "");
        lienField.setPromptText("https://meet.google.com/...");
        lienField.setPrefWidth(380);
        styleTextField(lienField);

        // Date/Heure
        Label dateLabel = new Label("Date et heure * (format : 2025-01-15 14:30:00)");
        styleFormLabel(dateLabel);
        TextField dateField = new TextField(isEdit ? comm.getDateHeure() : "");
        dateField.setPromptText("2025-01-15 14:30:00");
        dateField.setPrefWidth(380);
        styleTextField(dateField);

        // Durée
        Label dureeLabel = new Label("Durée (minutes) *");
        styleFormLabel(dureeLabel);
        TextField dureeField = new TextField(isEdit ? String.valueOf(comm.getDuree()) : "");
        dureeField.setPromptText("60");
        dureeField.setPrefWidth(380);
        styleTextField(dureeField);

        // État
        Label etatLabel = new Label("État *");
        styleFormLabel(etatLabel);
        ComboBox<String> etatCombo = new ComboBox<>();
        etatCombo.getItems().addAll("planifié", "actif", "terminé", "annulé");
        etatCombo.setPromptText("Sélectionner l'état");
        etatCombo.setPrefWidth(380);
        styleComboBox(etatCombo);
        if (isEdit) etatCombo.setValue(comm.getEtat());

        // Description
        Label descLabel = new Label("Description détaillée");
        styleFormLabel(descLabel);
        TextArea descArea = new TextArea(isEdit ? comm.getDescription() : "");
        descArea.setPromptText("Description optionnelle...");
        descArea.setPrefRowCount(3);
        descArea.setPrefWidth(380);
        descArea.setWrapText(true);
        descArea.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;"
                + " -fx-border-color: #ddd; -fx-font-size: 13px;");

        // Publication ID
        Label pubLabel = new Label("Publication ID (optionnel)");
        styleFormLabel(pubLabel);
        TextField pubField = new TextField(isEdit && comm.getPublicationId() > 0
                ? String.valueOf(comm.getPublicationId()) : "");
        pubField.setPromptText("Laisser vide si aucune");
        pubField.setPrefWidth(380);
        styleTextField(pubField);

        // Message d'erreur
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Boutons
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;"
                + " -fx-background-radius: 8; -fx-padding: 9 22; -fx-cursor: hand; -fx-font-size: 13px;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button(isEdit ? "💾 Enregistrer" : "✅ Créer");
        saveBtn.setStyle("-fx-background-color: #1f4f65; -fx-text-fill: white;"
                + " -fx-background-radius: 8; -fx-padding: 9 22; -fx-cursor: hand;"
                + " -fx-font-weight: bold; -fx-font-size: 13px;");

        saveBtn.setOnAction(e -> {
            // Validation
            if (typeCombo.getValue() == null
                    || lienField.getText().trim().isEmpty()
                    || dateField.getText().trim().isEmpty()
                    || dureeField.getText().trim().isEmpty()
                    || etatCombo.getValue() == null) {
                errorLabel.setText("⚠️ Veuillez remplir tous les champs obligatoires (*)");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            int duree;
            try {
                duree = Integer.parseInt(dureeField.getText().trim());
                if (duree <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                errorLabel.setText("⚠️ La durée doit être un entier positif.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            int pubId = 0;
            if (!pubField.getText().trim().isEmpty()) {
                try {
                    pubId = Integer.parseInt(pubField.getText().trim());
                } catch (NumberFormatException ex) {
                    errorLabel.setText("⚠️ L'ID publication doit être un entier.");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                    return;
                }
            }

            if (isEdit) {
                updateCommunication(
                        comm.getId(),
                        typeCombo.getValue(),
                        lienField.getText().trim(),
                        dateField.getText().trim(),
                        duree,
                        etatCombo.getValue(),
                        descArea.getText().trim(),
                        pubId
                );
            } else {
                createCommunication(
                        typeCombo.getValue(),
                        lienField.getText().trim(),
                        dateField.getText().trim(),
                        duree,
                        etatCombo.getValue(),
                        descArea.getText().trim(),
                        pubId
                );
            }
            dialog.close();
        });

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.getChildren().addAll(cancelBtn, saveBtn);

        form.getChildren().addAll(
                titleLbl, sep,
                typeLabel, typeCombo,
                lienLabel, lienField,
                dateLabel, dateField,
                dureeLabel, dureeField,
                etatLabel, etatCombo,
                descLabel, descArea,
                pubLabel, pubField,
                errorLabel,
                buttonsBox
        );

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        Scene scene = new Scene(scrollPane, 460, 620);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ===== CRUD - INSERT =====
    private void createCommunication(String type, String lien, String dateHeure,
                                     int duree, String etat, String description, int publicationId) {
        String sql = "INSERT INTO communication (type, lien, date_heure, duree, etat, description_detaillee, publication_id)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setString(2, lien);
            pstmt.setString(3, dateHeure);
            pstmt.setInt(4, duree);
            pstmt.setString(5, etat);
            pstmt.setString(6, description);
            pstmt.setInt(7, publicationId);
            pstmt.executeUpdate();
            showAlert("Succès", "✅ Communication créée avec succès !");
            loadCommunications();
            updateStatistics();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de créer la communication: " + e.getMessage());
        }
    }

    // ===== CRUD - UPDATE =====
    private void updateCommunication(int id, String type, String lien, String dateHeure,
                                     int duree, String etat, String description, int publicationId) {
        String sql = "UPDATE communication SET type=?, lien=?, date_heure=?, duree=?, etat=?,"
                + " description_detaillee=?, publication_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setString(2, lien);
            pstmt.setString(3, dateHeure);
            pstmt.setInt(4, duree);
            pstmt.setString(5, etat);
            pstmt.setString(6, description);
            pstmt.setInt(7, publicationId);
            pstmt.setInt(8, id);
            pstmt.executeUpdate();
            showAlert("Succès", "✅ Communication modifiée avec succès !");
            loadCommunications();
            updateStatistics();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de modifier la communication: " + e.getMessage());
        }
    }

    // ===== CRUD - DELETE =====
    private void deleteCommunication(int id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer cette communication ?");
        confirm.setContentText("Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "DELETE FROM communication WHERE id=?")) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                    showAlert("Succès", "🗑️ Communication supprimée avec succès.");
                    loadCommunications();
                    updateStatistics();
                } catch (SQLException e) {
                    showAlert("Erreur", "Impossible de supprimer: " + e.getMessage());
                }
            }
        });
    }

    // ===== RECHERCHE =====
    @FXML
    private void searchCommunications() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadCommunications();
            return;
        }

        communicationList.clear();
        String sql = "SELECT * FROM communication WHERE type LIKE ? OR lien LIKE ?"
                + " OR description_detaillee LIKE ? ORDER BY date_heure DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            pstmt.setString(1, like);
            pstmt.setString(2, like);
            pstmt.setString(3, like);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                communicationList.add(new Communication(
                        rs.getInt("id"), rs.getString("type"), rs.getString("lien"),
                        rs.getString("date_heure"), rs.getInt("duree"), rs.getString("etat"),
                        rs.getString("description_detaillee"), rs.getInt("publication_id")
                ));
            }
            displayCards();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    // ===== STATISTIQUES =====
    private void updateStatistics() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) AS total FROM communication");
            rs1.next();
            int total = rs1.getInt("total");

            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) AS live FROM communication WHERE type='live'");
            rs2.next();
            int live = rs2.getInt("live");

            ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) AS record FROM communication WHERE type='record'");
            rs3.next();
            int record = rs3.getInt("record");

            statsLabel.setText(String.format("📊 %d communications | Live: %d | Record: %d", total, live, record));
        } catch (SQLException e) {
            statsLabel.setText("📊 Statistiques indisponibles");
        }
    }

    // ===== COULEUR ÉTAT =====
    private String getEtatColor(String etat) {
        if (etat == null) return "#95a5a6";
        switch (etat) {
            case "actif":    return "#2ecc71";
            case "terminé":  return "#95a5a6";
            case "annulé":   return "#e74c3c";
            case "planifié": return "#f39c12";
            default:         return "#95a5a6";
        }
    }

    // ===== OUVRIR UN LIEN =====
    private void openLink(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le lien: " + url);
        }
    }

    // ===== HELPERS STYLE =====
    private void styleTextField(TextField field) {
        field.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;"
                + " -fx-border-color: #ddd; -fx-padding: 8 10; -fx-font-size: 13px;");
    }

    private void styleComboBox(ComboBox<?> combo) {
        combo.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;"
                + " -fx-border-color: #ddd; -fx-padding: 4 6; -fx-font-size: 13px;");
    }

    private void styleFormLabel(Label label) {
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");
    }

    // ===== ALERTE =====
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ===== NAVIGATION =====
    @FXML private void goToAccueil()        { navigateTo("/org/example/fxml/front.fxml", "LearnFlex+"); }
    @FXML private void goToPublications()   { navigateTo("/org/example/fxml/PublicationsFrontPage.fxml", "Publications"); }
    @FXML private void goToCommunications() { navigateTo("/org/example/fxml/CommunicationsFrontPage.fxml", "Communications"); }
    @FXML private void goToCours()          { navigateTo("/org/example/fxml/cours.fxml", "Cours"); }
    @FXML private void goToQuiz()           { navigateTo("/org/example/fxml/ExamenView.fxml", "Quiz"); }
    @FXML private void goToConnexion()      { navigateTo("/org/example/fxml/login.fxml", "Connexion"); }

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

    // ===== CLASSE MODÈLE =====
    public static class Communication {
        private final int id, duree, publicationId;
        private final String type, lien, dateHeure, etat, description;

        public Communication(int id, String type, String lien, String dateHeure,
                             int duree, String etat, String description, int publicationId) {
            this.id = id;
            this.type = type;
            this.lien = lien;
            this.dateHeure = dateHeure;
            this.duree = duree;
            this.etat = etat;
            this.description = description;
            this.publicationId = publicationId;
        }

        public int getId()             { return id; }
        public String getType()        { return type; }
        public String getLien()        { return lien; }
        public String getDateHeure()   { return dateHeure; }
        public int getDuree()          { return duree; }
        public String getEtat()        { return etat; }
        public String getDescription() { return description; }
        public int getPublicationId()  { return publicationId; }
    }
}

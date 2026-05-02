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
<<<<<<< HEAD

import java.sql.*;
=======
import javafx.application.Platform;

import javax.mail.*;
import javax.mail.internet.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.Properties;
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))

public class FrontCommunicationController {

    // ===== ÉLÉMENTS FXML =====
    @FXML private FlowPane cardsContainer;
    @FXML private Label statsLabel;
    @FXML private Label noDataLabel;
    @FXML private ComboBox<String> typeFilter;
    @FXML private TextField searchField;
    @FXML private ImageView logo;

<<<<<<< HEAD
=======
    // ===== CONFIG EMAIL =====
    private static final String EMAIL_UTILISATEUR  = "aouamriamal0000@gmail.com";
    private static final String EMAIL_MOT_DE_PASSE = "ziwu piqz iync fgvl";

    // ===== CONFIG ONESIGNAL =====
    private static final String ONESIGNAL_APP_ID = "d122c439-6307-434a-a4e3-c7f3ab78de39";
    private static final String ONESIGNAL_API_KEY = "bcfhhaq7gu3qmh53mfaclqpgc";
    private static final String ONESIGNAL_URL     = "https://onesignal.com/api/v1/notifications";

>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
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

<<<<<<< HEAD
    // ===== CARTE COMMUNICATION (avec boutons CRUD) =====
=======
    // ===== CARTE COMMUNICATION =====
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
    private VBox createCommunicationCard(Communication comm) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");
        card.setPrefWidth(320);
        card.setPadding(new Insets(15));
        card.setSpacing(10);

<<<<<<< HEAD
        // Badge type
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Label typeLabel = new Label(comm.getType().toUpperCase());
        String typeColor = comm.getType().equals("live") ? "#3498db" : "#9b59b6";
        typeLabel.setStyle("-fx-background-color: " + typeColor
                + "; -fx-text-fill: white; -fx-background-radius: 15;"
                + " -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;");

<<<<<<< HEAD
        // Badge état
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Label etatLabel = new Label(comm.getEtat());
        String etatColor = getEtatColor(comm.getEtat());
        etatLabel.setStyle("-fx-background-color: " + etatColor
                + "; -fx-text-fill: white; -fx-background-radius: 15;"
                + " -fx-padding: 4 12; -fx-font-size: 11px;");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(typeLabel, etatLabel);

<<<<<<< HEAD
        // Lien
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Label lienLabel = new Label(comm.getLien());
        lienLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db; -fx-wrap-text: true;");
        lienLabel.setWrapText(true);
        lienLabel.setMaxWidth(290);

<<<<<<< HEAD
        // Date & durée
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Label dateLabel = new Label("📅 " + comm.getDateHeure());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");

        Label dureeLabel = new Label("⏱️ " + comm.getDuree() + " min");
        dureeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");

        HBox infoBox = new HBox(15);
        infoBox.getChildren().addAll(dateLabel, dureeLabel);

<<<<<<< HEAD
        // Description (si disponible)
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        if (comm.getDescription() != null && !comm.getDescription().isEmpty()) {
            Label descLabel = new Label(comm.getDescription());
            descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(290);
            card.getChildren().addAll(headerBox, lienLabel, infoBox, descLabel);
        } else {
            card.getChildren().addAll(headerBox, lienLabel, infoBox);
        }

<<<<<<< HEAD
        // Bouton rejoindre
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Button joinBtn = new Button(comm.getType().equals("live")
                ? "🎥 Rejoindre le live" : "📹 Voir l'enregistrement");
        joinBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"
                + " -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-weight: bold;");
        joinBtn.setMaxWidth(Double.MAX_VALUE);
        joinBtn.setOnAction(e -> openLink(comm.getLien()));

<<<<<<< HEAD
        // Séparateur
        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.3;");

        // Boutons Modifier / Supprimer
=======
        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.3;");

>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
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

<<<<<<< HEAD
    // ===== DIALOG PARTAGÉ CRÉER / MODIFIER =====
=======
    // ===== DIALOG PARTAGÉ =====
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
    private void showCommunicationDialog(Communication comm) {
        boolean isEdit = (comm != null);

        Stage dialog = new Stage();
        dialog.setTitle(isEdit ? "Modifier la communication" : "Créer une communication");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

<<<<<<< HEAD
        // ---- Formulaire ----
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        VBox form = new VBox(12);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: #f8fafc;");
        form.setPrefWidth(430);

        Label titleLbl = new Label(isEdit ? "✏️ Modifier la Communication" : "➕ Nouvelle Communication");
        titleLbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1f4f65;");

        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.4;");

<<<<<<< HEAD
        // Type
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Label typeLabel = new Label("Type *");
        styleFormLabel(typeLabel);
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("live", "record");
        typeCombo.setPromptText("Sélectionner le type");
        typeCombo.setPrefWidth(380);
        styleComboBox(typeCombo);
        if (isEdit) typeCombo.setValue(comm.getType());

<<<<<<< HEAD
        // Lien
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Label lienLabel = new Label("Lien (URL) *");
        styleFormLabel(lienLabel);
        TextField lienField = new TextField(isEdit ? comm.getLien() : "");
        lienField.setPromptText("https://meet.google.com/...");
        lienField.setPrefWidth(380);
        styleTextField(lienField);

<<<<<<< HEAD
        // Date/Heure
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Label dateLabel = new Label("Date et heure * (format : 2025-01-15 14:30:00)");
        styleFormLabel(dateLabel);
        TextField dateField = new TextField(isEdit ? comm.getDateHeure() : "");
        dateField.setPromptText("2025-01-15 14:30:00");
        dateField.setPrefWidth(380);
        styleTextField(dateField);

<<<<<<< HEAD
        // Durée
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Label dureeLabel = new Label("Durée (minutes) *");
        styleFormLabel(dureeLabel);
        TextField dureeField = new TextField(isEdit ? String.valueOf(comm.getDuree()) : "");
        dureeField.setPromptText("60");
        dureeField.setPrefWidth(380);
        styleTextField(dureeField);

<<<<<<< HEAD
        // État
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Label etatLabel = new Label("État *");
        styleFormLabel(etatLabel);
        ComboBox<String> etatCombo = new ComboBox<>();
        etatCombo.getItems().addAll("planifié", "actif", "terminé", "annulé");
        etatCombo.setPromptText("Sélectionner l'état");
        etatCombo.setPrefWidth(380);
        styleComboBox(etatCombo);
        if (isEdit) etatCombo.setValue(comm.getEtat());

<<<<<<< HEAD
        // Description
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Label descLabel = new Label("Description détaillée");
        styleFormLabel(descLabel);
        TextArea descArea = new TextArea(isEdit ? comm.getDescription() : "");
        descArea.setPromptText("Description optionnelle...");
        descArea.setPrefRowCount(3);
        descArea.setPrefWidth(380);
        descArea.setWrapText(true);
        descArea.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;"
                + " -fx-border-color: #ddd; -fx-font-size: 13px;");

<<<<<<< HEAD
        // Publication ID
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Label pubLabel = new Label("Publication ID (optionnel)");
        styleFormLabel(pubLabel);
        TextField pubField = new TextField(isEdit && comm.getPublicationId() > 0
                ? String.valueOf(comm.getPublicationId()) : "");
        pubField.setPromptText("Laisser vide si aucune");
        pubField.setPrefWidth(380);
        styleTextField(pubField);

<<<<<<< HEAD
        // Message d'erreur
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

<<<<<<< HEAD
        // Boutons
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;"
                + " -fx-background-radius: 8; -fx-padding: 9 22; -fx-cursor: hand; -fx-font-size: 13px;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button(isEdit ? "💾 Enregistrer" : "✅ Créer");
        saveBtn.setStyle("-fx-background-color: #1f4f65; -fx-text-fill: white;"
                + " -fx-background-radius: 8; -fx-padding: 9 22; -fx-cursor: hand;"
                + " -fx-font-weight: bold; -fx-font-size: 13px;");

        saveBtn.setOnAction(e -> {
<<<<<<< HEAD
            // Validation
=======
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
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
<<<<<<< HEAD
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
=======
                        comm.getId(), typeCombo.getValue(), lienField.getText().trim(),
                        dateField.getText().trim(), duree, etatCombo.getValue(),
                        descArea.getText().trim(), pubId
                );
            } else {
                createCommunication(
                        typeCombo.getValue(), lienField.getText().trim(),
                        dateField.getText().trim(), duree, etatCombo.getValue(),
                        descArea.getText().trim(), pubId
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
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
<<<<<<< HEAD
                errorLabel,
                buttonsBox
=======
                errorLabel, buttonsBox
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        );

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        Scene scene = new Scene(scrollPane, 460, 620);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

<<<<<<< HEAD
    // ===== CRUD - INSERT =====
=======
    // ===== CRUD - INSERT + NOTIFICATIONS EMAIL + ONESIGNAL =====
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
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
<<<<<<< HEAD
            showAlert("Succès", "✅ Communication créée avec succès !");
            loadCommunications();
            updateStatistics();
=======

            showAlert("Succès", "✅ Communication créée avec succès !");
            loadCommunications();
            updateStatistics();

            // ✅ ENVOI EMAIL dans un Thread séparé
            final String typeF = type;
            final String lienF = lien;
            final String dateF = dateHeure;
            final String descF = description;
            Thread emailThread = new Thread(() ->
                    envoyerNotificationEmail(typeF, lienF, dateF, duree, etat, descF)
            );
            emailThread.setDaemon(true);
            emailThread.start();

            // 🔔 ENVOI NOTIFICATION ONESIGNAL
            String icone = type.equals("live") ? "🎥" : "📹";
            envoyerNotificationOneSignal(
                    icone + " Nouveau " + type.toUpperCase() + " sur LearnFlex+",
                    "Une nouvelle communication a été créée : " + lien
            );

>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de créer la communication: " + e.getMessage());
        }
    }

<<<<<<< HEAD
=======
    // ═══════════════════════════════════════════════════════════════
    //  🔔 ENVOI NOTIFICATION ONESIGNAL (REST API)
    // ═══════════════════════════════════════════════════════════════
    private void envoyerNotificationOneSignal(String titre, String message) {
        Thread thread = new Thread(() -> {
            try {
                String jsonBody = String.format("""
                    {
                        "app_id": "%s",
                        "included_segments": ["All"],
                        "headings": {"en": "%s"},
                        "contents": {"en": "%s"}
                    }
                    """, ONESIGNAL_APP_ID, titre, message);

                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ONESIGNAL_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Basic " + ONESIGNAL_API_KEY)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = client.send(
                        request, HttpResponse.BodyHandlers.ofString()
                );

                System.out.println("✅ OneSignal réponse : " + response.body());

                Platform.runLater(() ->
                        showAlert("🔔 Notification envoyée",
                                "La notification push OneSignal a été envoyée avec succès !")
                );

            } catch (Exception e) {
                System.err.println("❌ Erreur OneSignal : " + e.getMessage());
                Platform.runLater(() ->
                        showAlert("⚠️ OneSignal échoué",
                                "Notification non envoyée : " + e.getMessage())
                );
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ═══════════════════════════════════════════════════════════════
    //  📧 ENVOI DE L'EMAIL DE NOTIFICATION (Jakarta Mail)
    // ═══════════════════════════════════════════════════════════════
    private void envoyerNotificationEmail(String type, String lien, String dateHeure,
                                          int duree, String etat, String description) {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_UTILISATEUR, EMAIL_MOT_DE_PASSE);
            }
        });

        try {
            String icone   = type.equals("live") ? "🎥" : "📹";
            String typeTxt = type.equals("live") ? "SESSION LIVE" : "ENREGISTREMENT";

            String htmlContent =
                    "<!DOCTYPE html>" +
                            "<html><body style='font-family: Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px;'>" +
                            "<div style='max-width: 550px; margin: auto; background: white; border-radius: 15px;" +
                            "     box-shadow: 0 4px 15px rgba(0,0,0,0.1); overflow: hidden;'>" +
                            "<div style='background: linear-gradient(135deg, #1f4f65, #2980b9);" +
                            "     padding: 30px; text-align: center;'>" +
                            "<h1 style='color: white; margin: 0; font-size: 26px;'>LearnFlex+ 📚</h1>" +
                            "<p style='color: rgba(255,255,255,0.85); margin: 8px 0 0;'>Nouvelle communication disponible</p>" +
                            "</div>" +
                            "<div style='padding: 25px 30px 10px;'>" +
                            "<div style='display: inline-block; background-color: " +
                            (type.equals("live") ? "#3498db" : "#9b59b6") +
                            "; color: white; border-radius: 20px; padding: 6px 18px;" +
                            "     font-size: 13px; font-weight: bold; margin-bottom: 15px;'>" +
                            icone + " " + typeTxt +
                            "</div>" +
                            "<table style='width: 100%; border-collapse: collapse;'>" +
                            ligne("📅 Date", dateHeure) +
                            ligne("⏱️ Durée", duree + " minutes") +
                            ligne("📌 État", etat) +
                            (description != null && !description.isEmpty() ? ligne("📝 Description", description) : "") +
                            "</table>" +
                            "<div style='text-align: center; margin: 25px 0;'>" +
                            "<a href='" + lien + "' style='background-color: #e74c3c; color: white;" +
                            "   text-decoration: none; padding: 12px 30px; border-radius: 25px;" +
                            "   font-weight: bold; font-size: 15px;'>" +
                            icone + " " + (type.equals("live") ? "Rejoindre le live" : "Voir l'enregistrement") +
                            "</a></div></div>" +
                            "<div style='background: #f1f3f4; padding: 15px 30px; text-align: center;" +
                            "     color: #95a5a6; font-size: 11px;'>" +
                            "Cet email a été envoyé automatiquement par LearnFlex+. Ne pas répondre." +
                            "</div></div></body></html>";

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_UTILISATEUR, "LearnFlex+"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL_UTILISATEUR));
            message.setSubject(icone + " Nouvelle communication " + typeTxt + " — LearnFlex+");
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Email de notification envoyé à : " + EMAIL_UTILISATEUR);

            Platform.runLater(() ->
                    showAlert("📧 Email envoyé", "Une notification a été envoyée à " + EMAIL_UTILISATEUR)
            );

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email : " + e.getMessage());
            Platform.runLater(() ->
                    showAlert("⚠️ Email non envoyé",
                            "La communication a été créée mais l'email a échoué : " + e.getMessage())
            );
        }
    }

    /** Génère une ligne de tableau HTML pour l'email */
    private String ligne(String label, String valeur) {
        return "<tr>" +
                "<td style='padding: 8px 0; color: #7f8c8d; font-size: 13px; width: 35%;'>" + label + "</td>" +
                "<td style='padding: 8px 0; color: #2c3e50; font-size: 13px; font-weight: bold;'>" + valeur + "</td>" +
                "</tr>";
    }

>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
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
<<<<<<< HEAD
            showAlert("Succès", "✅ Communication modifiée avec succès !");
            loadCommunications();
            updateStatistics();
=======

            showAlert("Succès", "✅ Communication modifiée avec succès !");
            loadCommunications();
            updateStatistics();

            // 🔔 NOTIFICATION ONESIGNAL — modification
            String icone = type.equals("live") ? "🎥" : "📹";
            envoyerNotificationOneSignal(
                    icone + " Communication modifiée — LearnFlex+",
                    "Une communication " + type.toUpperCase() + " a été mise à jour : " + lien
            );

>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
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
<<<<<<< HEAD
                    showAlert("Succès", "🗑️ Communication supprimée avec succès.");
                    loadCommunications();
                    updateStatistics();
=======

                    showAlert("Succès", "🗑️ Communication supprimée avec succès.");
                    loadCommunications();
                    updateStatistics();

                    // 🔔 NOTIFICATION ONESIGNAL — suppression
                    envoyerNotificationOneSignal(
                            "🗑️ Communication supprimée — LearnFlex+",
                            "Une communication a été supprimée de la plateforme."
                    );

>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
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
<<<<<<< HEAD
        if (keyword.isEmpty()) {
            loadCommunications();
            return;
        }
=======
        if (keyword.isEmpty()) { loadCommunications(); return; }
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))

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
<<<<<<< HEAD

            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) AS total FROM communication");
            rs1.next();
            int total = rs1.getInt("total");

            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) AS live FROM communication WHERE type='live'");
            rs2.next();
            int live = rs2.getInt("live");

            ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) AS record FROM communication WHERE type='record'");
            rs3.next();
            int record = rs3.getInt("record");

=======
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) AS total FROM communication");
            rs1.next(); int total = rs1.getInt("total");
            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) AS live FROM communication WHERE type='live'");
            rs2.next(); int live = rs2.getInt("live");
            ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) AS record FROM communication WHERE type='record'");
            rs3.next(); int record = rs3.getInt("record");
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))
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
<<<<<<< HEAD
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
=======
            this.id = id; this.type = type; this.lien = lien;
            this.dateHeure = dateHeure; this.duree = duree;
            this.etat = etat; this.description = description;
            this.publicationId = publicationId;
        }

        public int    getId()             { return id; }
        public String getType()           { return type; }
        public String getLien()           { return lien; }
        public String getDateHeure()      { return dateHeure; }
        public int    getDuree()          { return duree; }
        public String getEtat()           { return etat; }
        public String getDescription()    { return description; }
        public int    getPublicationId()  { return publicationId; }
    }
}
>>>>>>> 27ea260 (Ajout des fonctionnalités LearnFlexPlus (génération IA, photos Base64, FYP, traduction, audio))

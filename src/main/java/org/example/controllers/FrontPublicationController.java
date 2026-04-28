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
import javafx.stage.FileChooser;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.application.Platform;

import org.example.utils.BadWordFilter;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.util.Base64;

import javax.sound.sampled.*;

public class FrontPublicationController {

    // ========================================================================
    // 1. COMPOSANTS FXML
    // ========================================================================

    @FXML private FlowPane cardsContainer;
    @FXML private FlowPane aiCardsContainer;
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
    @FXML private VBox imagePreviewBox;
    @FXML private ImageView imagePreview;
    @FXML private VBox aiGeneratedSection;
    @FXML private Button generateAIBtn;
    @FXML private Button addPhotoBtn;
    @FXML private TextField imagePromptField;
    @FXML private Button generateImageBtn;
    @FXML private Label imageGenStatus;

    // ========================================================================
    // 2. CONSTANTES ET VARIABLES
    // ========================================================================

    private static final String VOICERSS_API_KEY = "c7e8fa99f11747b3b3acab6a9879c40e";
    private static final String VOICERSS_URL = "https://api.voicerss.org/";
    private static final String MYMEMORY_URL = "https://api.mymemory.translated.net/get";

    private static final String USER_EMAIL = "aouamriamal0000@gmail.com";
    private static final String USER_PRENOM = "Amal Aouamri";

    private String currentImageBase64;
    private boolean isAIGenerated = false;
    private Integer editingId = null;

    private ObservableList<Publication> publicationList = FXCollections.observableArrayList();
    private ObservableList<Publication> aiPublicationList = FXCollections.observableArrayList();

    private Clip currentClip;

    // ========================================================================
    // 3. INITIALISATION
    // ========================================================================

    @FXML
    public void initialize() {
        chargerLogo();
        initialiserComboBoxes();
        initialiserSections();
        verifierColonnes();
        chargerDonnees();
    }

    private void chargerLogo() {
        try {
            logo.setImage(new Image(getClass().getResourceAsStream("/org/example/images/logo1.png")));
        } catch (Exception ignored) {}
    }

    private void initialiserComboBoxes() {
        String[] categories = {"Problème technique", "Demande d'information", "Suggestion", "Réclamation", "Autre"};
        categorieCombo.getItems().addAll(categories);
        categorieFilter.getItems().addAll("Toutes");
        categorieFilter.getItems().addAll(categories);
        categorieFilter.setValue("Toutes");
        categorieFilter.setOnAction(e -> loadPublications());
    }

    private void initialiserSections() {
        if (aiGeneratedSection != null) {
            aiGeneratedSection.setVisible(false);
            aiGeneratedSection.setManaged(false);
        }
    }

    private void verifierColonnes() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            try { stmt.execute("ALTER TABLE publication ADD COLUMN image_base64 LONGTEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE publication ADD COLUMN is_ai_generated TINYINT DEFAULT 0"); } catch (SQLException ignored) {}
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void chargerDonnees() {
        loadPublications();
        loadAIPublications();
        updateStatistics();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/learnflexplus", "root", "");
    }

    // ========================================================================
    // 4. GESTION DES PHOTOS
    // ========================================================================

    @FXML
    private void addPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                String base64Image = Base64.getEncoder().encodeToString(fileBytes);

                String fileName = selectedFile.getName().toLowerCase();
                String mimeType = "image/png";
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) mimeType = "image/jpeg";
                else if (fileName.endsWith(".gif")) mimeType = "image/gif";
                else if (fileName.endsWith(".bmp")) mimeType = "image/bmp";

                currentImageBase64 = "data:" + mimeType + ";base64," + base64Image;
                Image image = new Image(currentImageBase64);
                imagePreview.setImage(image);
                imagePreviewBox.setVisible(true);
                imagePreviewBox.setManaged(true);

                showInfoAlert("Succès", "✅ Image ajoutée avec succès !");
            } catch (IOException e) {
                showErrorAlert("Erreur", "Erreur lors du chargement de l'image : " + e.getMessage());
            }
        }
    }

    @FXML
    private void removePhoto() {
        imagePreview.setImage(null);
        imagePreviewBox.setVisible(false);
        imagePreviewBox.setManaged(false);
        currentImageBase64 = null;
    }

    // ========================================================================
    // 5. GÉNÉRATION IA (TEXTE)
    // ========================================================================

    @FXML
    private void generateWithAI() {
        titreField.setText(genererTitreParIA());
        descriptionArea.setText(genererDescriptionParIA());
        categorieCombo.setValue(genererCategorieParIA());
        isAIGenerated = true;

        titreField.setStyle("-fx-border-color: #9b59b6; -fx-border-radius: 8;");
        descriptionArea.setStyle("-fx-border-color: #9b59b6; -fx-border-radius: 8;");

        showInfoAlert("Succès", "✨ Publication générée par IA !");
        animerBouton(generateAIBtn);
    }

    private String genererTitreParIA() {
        String[] titres = {
                "🤖 5 astuces pour réussir en programmation",
                "✨ Les meilleures pratiques de développement JavaFX",
                "💡 Comment apprendre plus efficacement en 2026",
                "🚀 Les tendances tech à suivre cette année",
                "🎯 Guide complet pour débuter avec Spring Boot"
        };
        return titres[(int)(Math.random() * titres.length)];
    }

    private String genererDescriptionParIA() {
        String[] descriptions = {
                "Découvrez des conseils pratiques pour améliorer vos compétences. Contenu optimisé par IA.",
                "Voici les meilleures ressources pour progresser rapidement. Généré intelligemment.",
                "L'IA a analysé des milliers de publications pour vous proposer ce contenu de qualité."
        };
        return descriptions[(int)(Math.random() * descriptions.length)];
    }

    private String genererCategorieParIA() {
        String[] categories = {"Problème technique", "Demande d'information", "Suggestion", "Réclamation", "Autre"};
        return categories[(int)(Math.random() * categories.length)];
    }

    private void animerBouton(Button btn) {
        btn.setStyle("-fx-background-color:#8e44ad; -fx-text-fill:white; -fx-background-radius:20;");
        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> btn.setStyle("-fx-background-color:#9b59b6; -fx-text-fill:white; -fx-background-radius:20;"));
        pause.play();
    }

    // ========================================================================
    // 6. GÉNÉRATION IMAGE IA (POLLINATIONS)
    // ========================================================================

    @FXML
    private void generateImageWithAI() {
        String prompt = imagePromptField.getText().trim();
        if (prompt.isEmpty()) {
            afficherStatutImage("⚠ Décrivez l'image avant de générer !", "#e74c3c");
            return;
        }

        generateImageBtn.setDisable(true);
        generateImageBtn.setText("⏳ Génération...");
        afficherStatutImage("🎨 Génération en cours... (5-10 secondes)", "#6c3483");

        new Thread(() -> {
            try {
                String encodedPrompt = URLEncoder.encode(prompt, "UTF-8");
                String imageUrl = "https://image.pollinations.ai/prompt/" + encodedPrompt + "?width=800&height=400&nologo=true";

                HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                byte[] imageBytes = conn.getInputStream().readAllBytes();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                String imageBase64Data = "data:image/png;base64," + base64Image;

                Platform.runLater(() -> {
                    Image image = new Image(imageBase64Data);
                    imagePreview.setImage(image);
                    imagePreviewBox.setVisible(true);
                    imagePreviewBox.setManaged(true);
                    currentImageBase64 = imageBase64Data;
                    afficherStatutImage("✅ Image générée avec succès !", "#27ae60");
                    generateImageBtn.setDisable(false);
                    generateImageBtn.setText("🎨 Générer");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    afficherStatutImage("❌ Erreur : " + e.getMessage(), "#e74c3c");
                    generateImageBtn.setDisable(false);
                    generateImageBtn.setText("🎨 Générer");
                });
            }
        }).start();
    }

    private void afficherStatutImage(String message, String couleur) {
        imageGenStatus.setText(message);
        imageGenStatus.setStyle("-fx-text-fill:" + couleur + "; -fx-font-size:11px;");
        imageGenStatus.setVisible(true);
        imageGenStatus.setManaged(true);
    }

    // ========================================================================
    // 7. TRADUCTION MYMEMORY API
    // ========================================================================

    private String traduireEnAnglais(String texte) {
        try {
            String encodedText = URLEncoder.encode(texte, "UTF-8");
            String urlStr = MYMEMORY_URL + "?q=" + encodedText + "&langpair=fr%7Cen";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlStr)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            int idx = body.indexOf("\"translatedText\":\"");
            if (idx != -1) {
                int start = idx + 18;
                int end = body.indexOf("\"", start);
                if (end != -1) return body.substring(start, end);
            }
            return texte;
        } catch (Exception e) {
            return texte;
        }
    }

    // ========================================================================
    // 8. TEXT-TO-SPEECH VOICERSS
    // ========================================================================

    private void lireDescriptionAudio(String description) {
        arreterLectureAudio();

        new Thread(() -> {
            try {
                String texte = description.length() > 500 ? description.substring(0, 500) : description;
                String encodedText = URLEncoder.encode(texte, "UTF-8");
                String urlStr = VOICERSS_URL + "?key=" + VOICERSS_API_KEY + "&hl=fr-fr&src=" + encodedText + "&c=WAV&f=16khz_16bit_stereo";

                URL url = new URL(urlStr);
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                byte[] audioData = conn.getInputStream().readAllBytes();

                ByteArrayInputStream byteStream = new ByteArrayInputStream(audioData);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(byteStream);

                AudioFormat sourceFormat = audioStream.getFormat();
                AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        sourceFormat.getSampleRate(), 16, sourceFormat.getChannels(),
                        sourceFormat.getChannels() * 2, sourceFormat.getSampleRate(), false);

                AudioInputStream pcmStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                currentClip = AudioSystem.getClip();
                currentClip.open(pcmStream);
                currentClip.start();

                currentClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) currentClip.close();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showErrorAlert("⚠️ Audio", "Impossible de lire: " + e.getMessage()));
            }
        }).start();
    }

    private void arreterLectureAudio() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }

    // ========================================================================
    // 9. CHARGEMENT DES PUBLICATIONS (FYP - FOR YOU PAGE)
    // ========================================================================

    private void loadPublications() {
        publicationList.clear();

        String sql = "SELECT p.*, " +
                "(SELECT COUNT(*) FROM commentaire c WHERE c.publication_id = p.id) as nb_commentaires, " +
                "((p.nombre_likes * 2) + ((SELECT COUNT(*) FROM commentaire c WHERE c.publication_id = p.id) * 1.5) + (p.nombre_vues * 0.5)) as score_popularite " +
                "FROM publication p " +
                "ORDER BY score_popularite DESC, p.nombre_likes DESC, nb_commentaires DESC";

        if (categorieFilter.getValue() != null && !categorieFilter.getValue().equals("Toutes")) {
            sql = "SELECT p.*, " +
                    "(SELECT COUNT(*) FROM commentaire c WHERE c.publication_id = p.id) as nb_commentaires, " +
                    "((p.nombre_likes * 2) + ((SELECT COUNT(*) FROM commentaire c WHERE c.publication_id = p.id) * 1.5) + (p.nombre_vues * 0.5)) as score_popularite " +
                    "FROM publication p " +
                    "WHERE p.categorie = ? " +
                    "ORDER BY score_popularite DESC, p.nombre_likes DESC, nb_commentaires DESC";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, categorieFilter.getValue());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) addToList(rs);
                displayCards();
            } catch (SQLException e) { showErrorAlert("Erreur", e.getMessage()); }
            return;
        }

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) addToList(rs);
            displayCards();
        } catch (SQLException e) { showErrorAlert("Erreur", e.getMessage()); }
    }

    private void addToList(ResultSet rs) throws SQLException {
        Publication pub = new Publication(
                rs.getInt("id"), rs.getString("titre"), rs.getString("description"),
                rs.getString("date_creation"), rs.getString("categorie"),
                rs.getInt("nombre_vues"), rs.getInt("nombre_likes"), rs.getInt("nombre_dislikes"));
        pub.setImageBase64(rs.getString("image_base64"));
        pub.setNombreCommentaires(rs.getInt("nb_commentaires"));
        pub.setScorePopularite(rs.getDouble("score_popularite"));
        publicationList.add(pub);
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

    private void loadAIPublications() {
        if (aiCardsContainer == null) return;
        aiPublicationList.clear();
        String sql = "SELECT * FROM publication WHERE is_ai_generated = 1 ORDER BY date_creation DESC LIMIT 5";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Publication pub = new Publication(
                        rs.getInt("id"), rs.getString("titre"), rs.getString("description"),
                        rs.getString("date_creation"), rs.getString("categorie"),
                        rs.getInt("nombre_vues"), rs.getInt("nombre_likes"), rs.getInt("nombre_dislikes"));
                pub.setImageBase64(rs.getString("image_base64"));
                aiPublicationList.add(pub);
            }
            displayAICards();
        } catch (SQLException e) { System.err.println("Erreur IA: " + e.getMessage()); }
    }

    private void displayAICards() {
        if (aiCardsContainer == null) return;
        aiCardsContainer.getChildren().clear();
        if (aiPublicationList.isEmpty()) {
            aiGeneratedSection.setVisible(false);
            aiGeneratedSection.setManaged(false);
            return;
        }
        aiGeneratedSection.setVisible(true);
        aiGeneratedSection.setManaged(true);
        for (Publication pub : aiPublicationList) {
            aiCardsContainer.getChildren().add(createAIPublicationCard(pub));
        }
    }

    // ========================================================================
    // 10. CRÉATION DES CARTES
    // ========================================================================

    private VBox createCard(Publication pub) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color:white; -fx-background-radius:15; " +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);");
        card.setPrefWidth(420);
        card.setPadding(new Insets(15));

        // Badges
        HBox badgesBox = new HBox(5);
        badgesBox.setAlignment(Pos.CENTER_LEFT);
        if (pub.getScorePopularite() > 50) {
            Label trendingBadge = new Label("🔥 Tendance");
            trendingBadge.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:12; -fx-padding:2 8; -fx-font-size:10px;");
            badgesBox.getChildren().add(trendingBadge);
        } else if (pub.getScorePopularite() > 20) {
            Label popularBadge = new Label("⭐ Populaire");
            popularBadge.setStyle("-fx-background-color:#f39c12; -fx-text-fill:white; -fx-background-radius:12; -fx-padding:2 8; -fx-font-size:10px;");
            badgesBox.getChildren().add(popularBadge);
        }

        HBox topBox = new HBox();
        topBox.setAlignment(Pos.CENTER_RIGHT);
        Label catLabel = new Label(pub.getCategorie());
        catLabel.setStyle("-fx-background-color:" + getCategorieColor(pub.getCategorie()) +
                "; -fx-text-fill:white; -fx-background-radius:15; -fx-padding:4 12; -fx-font-size:11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBox.getChildren().addAll(catLabel, spacer);

        Label titreLabel = new Label(pub.getTitre());
        titreLabel.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");
        titreLabel.setWrapText(true);

        Label descLabel = new Label(pub.getDescription());
        descLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#7f8c8d;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(60);

        // Image
        if (pub.getImageBase64() != null && !pub.getImageBase64().isEmpty()) {
            try {
                Image image = new Image(pub.getImageBase64());
                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(150);
                    imageView.setFitWidth(390);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    card.getChildren().add(imageView);
                }
            } catch (Exception e) {}
        }

        Label dateLabel = new Label("📅 " + pub.getDateCreation());
        dateLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#95a5a6;");

        Label likesLabel = new Label("👍 " + pub.getNombreLikes());
        likesLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#2ecc71; -fx-font-weight:bold;");

        Button likeBtn = new Button("J'aime");
        likeBtn.setStyle("-fx-background-color:#2ecc71; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:5 12; -fx-cursor:hand; -fx-font-size:11px;");
        likeBtn.setOnAction(e -> incrementLikes(pub, likesLabel, likeBtn));

        Label dislikesLabel = new Label("👎 " + pub.getNombreDislikes());
        dislikesLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#e67e22; -fx-font-weight:bold;");

        Button dislikeBtn = new Button("Dislike");
        dislikeBtn.setStyle("-fx-background-color:#e67e22; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:5 12; -fx-cursor:hand; -fx-font-size:11px;");
        dislikeBtn.setOnAction(e -> incrementDislikes(pub, dislikesLabel, dislikeBtn));

        Button commentBtn = new Button("💬 " + getNombreCommentaires(pub.getId()));
        commentBtn.setStyle("-fx-background-color:#9b59b6; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:5 12; -fx-cursor:hand; -fx-font-size:11px;");
        commentBtn.setOnAction(e -> ouvrirCommentaires(pub, commentBtn));

        HBox ligne1 = new HBox(8, likesLabel, likeBtn, dislikesLabel, dislikeBtn, commentBtn);
        ligne1.setAlignment(Pos.CENTER_LEFT);

        Button translateBtn = new Button("🌍 Traduire EN");
        translateBtn.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:5 12; -fx-cursor:hand; -fx-font-size:11px;");
        translateBtn.setOnAction(e -> {
            translateBtn.setDisable(true);
            translateBtn.setText("⏳...");
            new Thread(() -> {
                String descTraduite = traduireEnAnglais(pub.getDescription());
                String titreTraduit = traduireEnAnglais(pub.getTitre());
                Platform.runLater(() -> {
                    descLabel.setText(descTraduite);
                    titreLabel.setText(titreTraduit);
                    translateBtn.setText("✅ Traduit");
                    translateBtn.setStyle("-fx-background-color:#27ae60; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:5 12;");
                });
            }).start();
        });

        Button listenBtn = new Button("🔊 Écouter");
        listenBtn.setStyle("-fx-background-color:#e67e22; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:5 12; -fx-cursor:hand; -fx-font-size:11px;");
        listenBtn.setOnAction(e -> {
            listenBtn.setText("⏳...");
            listenBtn.setDisable(true);
            lireDescriptionAudio(pub.getDescription());
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> { listenBtn.setText("🔊 Écouter"); listenBtn.setDisable(false); });
            pause.play();
        });

        Button editBtn = new Button("✏️ Modifier");
        editBtn.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:5 12; -fx-cursor:hand; -fx-font-size:11px;");
        editBtn.setOnAction(e -> fillFormForEdit(pub));

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:5 12; -fx-cursor:hand; -fx-font-size:11px;");
        deleteBtn.setOnAction(e -> deletePublication(pub));

        HBox ligne2 = new HBox(8, translateBtn, listenBtn, editBtn, deleteBtn);
        ligne2.setAlignment(Pos.CENTER_RIGHT);

        Separator sep = new Separator();

        card.getChildren().addAll(badgesBox, topBox, titreLabel, descLabel, dateLabel, sep, ligne1, ligne2);
        return card;
    }

    private VBox createAIPublicationCard(Publication pub) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: linear-gradient(135deg, #f5f0ff 0%, #e8d5f5 100%); " +
                "-fx-background-radius: 12; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(155,89,182,0.2), 8, 0, 0, 2); " +
                "-fx-border-color: #d4afd4; -fx-border-radius: 12; -fx-border-width: 1;");
        card.setPrefWidth(350);

        HBox badgeBox = new HBox();
        badgeBox.setAlignment(Pos.CENTER_RIGHT);
        Label aiBadge = new Label("🤖 Généré par IA");
        aiBadge.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; " +
                "-fx-background-radius: 15; -fx-padding: 3 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        badgeBox.getChildren().add(aiBadge);

        Label catLabel = new Label(pub.getCategorie());
        catLabel.setStyle("-fx-background-color:" + getCategorieColor(pub.getCategorie()) +
                "; -fx-text-fill:white; -fx-background-radius:15; -fx-padding:4 12; -fx-font-size:11px;");

        Label titreLabel = new Label(pub.getTitre());
        titreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titreLabel.setWrapText(true);

        String descText = pub.getDescription();
        if (descText.length() > 100) descText = descText.substring(0, 100) + "...";
        Label descriptionLabel = new Label(descText);
        descriptionLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        descriptionLabel.setWrapText(true);

        if (pub.getImageBase64() != null && !pub.getImageBase64().isEmpty()) {
            try {
                ImageView imageView = new ImageView(new Image(pub.getImageBase64()));
                imageView.setFitHeight(120);
                imageView.setFitWidth(330);
                imageView.setPreserveRatio(true);
                card.getChildren().add(imageView);
            } catch (Exception e) {}
        }

        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        Label likesLabel = new Label("👍 " + pub.getNombreLikes());
        likesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #2ecc71;");
        Label dislikesLabel = new Label("👎 " + pub.getNombreDislikes());
        dislikesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e67e22;");
        Label viewsLabel = new Label("👁️ " + pub.getNombreVues());
        viewsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #3498db;");
        statsBox.getChildren().addAll(likesLabel, dislikesLabel, viewsLabel);

        HBox actionBtns = new HBox(8);
        actionBtns.setAlignment(Pos.CENTER_LEFT);

        Button translateBtn = new Button("🌍 Traduire EN");
        translateBtn.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; -fx-background-radius:15; -fx-padding:5 12; -fx-cursor:hand; -fx-font-size:11px;");
        translateBtn.setOnAction(e -> {
            translateBtn.setDisable(true);
            translateBtn.setText("⏳...");
            new Thread(() -> {
                String descTraduite = traduireEnAnglais(pub.getDescription());
                String titreTraduit = traduireEnAnglais(pub.getTitre());
                Platform.runLater(() -> {
                    descriptionLabel.setText(descTraduite.length() > 100 ? descTraduite.substring(0, 100) + "..." : descTraduite);
                    titreLabel.setText(titreTraduit);
                    translateBtn.setText("✅");
                    translateBtn.setStyle("-fx-background-color:#27ae60; -fx-text-fill:white; -fx-background-radius:15; -fx-padding:5 12;");
                });
            }).start();
        });

        Button listenBtn = new Button("🔊 Écouter");
        listenBtn.setStyle("-fx-background-color:#e67e22; -fx-text-fill:white; -fx-background-radius:15; -fx-padding:5 12; -fx-cursor:hand; -fx-font-size:11px;");
        listenBtn.setOnAction(e -> {
            listenBtn.setText("⏳...");
            listenBtn.setDisable(true);
            lireDescriptionAudio(pub.getDescription());
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> { listenBtn.setText("🔊 Écouter"); listenBtn.setDisable(false); });
            pause.play();
        });

        actionBtns.getChildren().addAll(translateBtn, listenBtn);

        card.getChildren().addAll(badgeBox, catLabel, titreLabel, descriptionLabel, statsBox, actionBtns);
        card.setOnMouseClicked(e -> ouvrirDetailPublication(pub));
        card.setCursor(javafx.scene.Cursor.HAND);
        return card;
    }

    // ========================================================================
    // 11. LIKES / DISLIKES
    // ========================================================================

    private void incrementLikes(Publication pub, Label likesLabel, Button likeBtn) {
        String sql = "UPDATE publication SET nombre_likes=nombre_likes+1, nombre_vues=nombre_vues+1 WHERE id=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pub.getId());
            pstmt.executeUpdate();
            likesLabel.setText("👍 " + (pub.getNombreLikes() + 1));
            likeBtn.setDisable(true);
            likeBtn.setText("👍 Aimé");
            updateStatistics();
            loadPublications();
            loadAIPublications();
        } catch (SQLException e) { showErrorAlert("Erreur", e.getMessage()); }
    }

    private void incrementDislikes(Publication pub, Label dislikesLabel, Button dislikeBtn) {
        String sql = "UPDATE publication SET nombre_dislikes=nombre_dislikes+1 WHERE id=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pub.getId());
            pstmt.executeUpdate();
            dislikesLabel.setText("👎 " + (pub.getNombreDislikes() + 1));
            dislikeBtn.setDisable(true);
            dislikeBtn.setText("👎 Noté");
            loadPublications();
            loadAIPublications();
        } catch (SQLException e) { showErrorAlert("Erreur", e.getMessage()); }
    }

    // ========================================================================
    // 12. CRUD PUBLICATIONS
    // ========================================================================

    @FXML
    private void savePublication() {
        if (!validate()) return;
        if (editingId == null) insertPublication();
        else updatePublication();
    }

    private void insertPublication() {
        String sql = "INSERT INTO publication (titre, description, date_creation, categorie, nombre_vues, nombre_likes, nombre_dislikes, image_base64, is_ai_generated) VALUES (?, ?, ?, ?, 0, 0, 0, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, titreField.getText().trim());
            pstmt.setString(2, descriptionArea.getText().trim());
            pstmt.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setString(4, categorieCombo.getValue());
            pstmt.setString(5, currentImageBase64);
            pstmt.setInt(6, isAIGenerated ? 1 : 0);
            pstmt.executeUpdate();
            showInfoAlert("Succès", "Publication ajoutée avec succès !");
            clearForm();
            loadPublications();
            loadAIPublications();
            updateStatistics();
        } catch (SQLException e) { showErrorAlert("Erreur", e.getMessage()); }
    }

    private void updatePublication() {
        String sql = "UPDATE publication SET titre=?, description=?, categorie=?, image_base64=?, is_ai_generated=? WHERE id=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, titreField.getText().trim());
            pstmt.setString(2, descriptionArea.getText().trim());
            pstmt.setString(3, categorieCombo.getValue());
            pstmt.setString(4, currentImageBase64);
            pstmt.setInt(5, isAIGenerated ? 1 : 0);
            pstmt.setInt(6, editingId);
            pstmt.executeUpdate();
            showInfoAlert("Succès", "Publication mise à jour !");
            clearForm();
            loadPublications();
            loadAIPublications();
            updateStatistics();
        } catch (SQLException e) { showErrorAlert("Erreur", e.getMessage()); }
    }

    private void deletePublication(Publication pub) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer \"" + pub.getTitre() + "\" ?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            String sql = "DELETE FROM publication WHERE id=?";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, pub.getId());
                pstmt.executeUpdate();
                showInfoAlert("Succès", "Publication supprimée !");
                loadPublications();
                loadAIPublications();
                updateStatistics();
            } catch (SQLException e) { showErrorAlert("Erreur", e.getMessage()); }
        }
    }

    // ========================================================================
    // 13. COMMENTAIRES
    // ========================================================================

    private int getNombreCommentaires(int publicationId) {
        String sql = "SELECT COUNT(*) as total FROM commentaire WHERE publication_id=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, publicationId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException ignored) {}
        return 0;
    }

    private void ouvrirCommentaires(Publication pub, Button commentBtn) {
        Stage stage = new Stage();
        stage.setTitle("💬 Commentaires — " + pub.getTitre());

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color:#f8fafc;");

        Label titre = new Label("💬 Commentaires : " + pub.getTitre());
        titre.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");
        titre.setWrapText(true);

        VBox listeCommentaires = new VBox(8);
        chargerCommentaires(pub.getId(), listeCommentaires);

        ScrollPane scroll = new ScrollPane(listeCommentaires);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(250);
        scroll.setStyle("-fx-background:transparent; -fx-background-color:transparent;");

        Separator sep = new Separator();

        Label labelSaisie = new Label("✏️ Ajouter un commentaire :");
        labelSaisie.setStyle("-fx-font-weight:bold; -fx-text-fill:#2c3e50;");

        TextArea champCommentaire = new TextArea();
        champCommentaire.setPromptText("Écrivez votre commentaire... (min. 3, max. 500 caractères)");
        champCommentaire.setPrefHeight(80);
        champCommentaire.setWrapText(true);
        champCommentaire.setStyle("-fx-background-radius:8; -fx-border-color:#e0e0e0; -fx-border-radius:8;");

        Label erreurLabel = new Label("");
        erreurLabel.setStyle("-fx-text-fill:#e74c3c; -fx-font-size:11px;");

        Button publierBtn = new Button("📤 Publier le commentaire");
        publierBtn.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; " +
                "-fx-background-radius:20; -fx-padding:8 20; -fx-cursor:hand; -fx-font-weight:bold;");

        publierBtn.setOnAction(e -> {
            String contenu = champCommentaire.getText().trim();

            if (contenu.isEmpty()) {
                erreurLabel.setText("⚠ Le commentaire ne peut pas être vide !");
                return;
            }
            if (contenu.length() < 3) {
                erreurLabel.setText("⚠ Minimum 3 caractères !");
                return;
            }
            if (contenu.length() > 500) {
                erreurLabel.setText("⚠ Maximum 500 caractères !");
                return;
            }

            String motInterdit = BadWordFilter.detecterMotInterdit(contenu);
            if (motInterdit != null) {
                erreurLabel.setText("⚠ Mot inapproprié détecté : \""
                        + BadWordFilter.masquerMot(motInterdit)
                        + "\". Un avertissement a été envoyé à votre email.");
                BadWordFilter.envoyerAvertissement(
                        USER_EMAIL, USER_PRENOM, motInterdit, "commentaire");
                return;
            }

            ajouterCommentaire(pub.getId(), contenu);
            champCommentaire.clear();
            erreurLabel.setText("");
            chargerCommentaires(pub.getId(), listeCommentaires);
            commentBtn.setText("💬 " + getNombreCommentaires(pub.getId()));
            loadPublications();
        });

        HBox actionsForm = new HBox(publierBtn);
        actionsForm.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                titre, scroll, sep,
                labelSaisie, champCommentaire,
                erreurLabel, actionsForm);

        Scene scene = new Scene(root, 500, 500);
        stage.setScene(scene);
        stage.show();
    }

    private void chargerCommentaires(int publicationId, VBox container) {
        container.getChildren().clear();
        String sql = "SELECT * FROM commentaire WHERE publication_id=? ORDER BY date_commentaire DESC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, publicationId);
            ResultSet rs = pstmt.executeQuery();
            boolean hasComments = false;
            while (rs.next()) {
                hasComments = true;
                VBox commentBox = new VBox(4);
                commentBox.setStyle("-fx-background-color:white; -fx-background-radius:10; " +
                        "-fx-padding:10; -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

                Label contenuLabel = new Label(rs.getString("contenu"));
                contenuLabel.setWrapText(true);
                contenuLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#2c3e50;");

                Label dateLabel = new Label("📅 " + rs.getString("date_commentaire"));
                dateLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#95a5a6;");

                Button suppBtn = new Button("🗑️");
                suppBtn.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; " +
                        "-fx-background-radius:15; -fx-padding:3 8; -fx-cursor:hand;");
                int commentId = rs.getInt("id");
                suppBtn.setOnAction(ev -> {
                    supprimerCommentaire(commentId, publicationId, container);
                    loadPublications();
                });

                HBox header = new HBox(10, dateLabel, suppBtn);
                header.setAlignment(Pos.CENTER_LEFT);

                commentBox.getChildren().addAll(contenuLabel, header);
                container.getChildren().add(commentBox);
            }
            if (!hasComments) {
                Label vide = new Label("💬 Aucun commentaire. Soyez le premier !");
                vide.setStyle("-fx-text-fill:#95a5a6; -fx-font-size:13px;");
                container.getChildren().add(vide);
            }
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur chargement commentaires: " + e.getMessage());
        }
    }

    private void ajouterCommentaire(int publicationId, String contenu) {
        String sql = "INSERT INTO commentaire (contenu, date_commentaire, publication_id) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, contenu);
            pstmt.setString(2, LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setInt(3, publicationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur ajout commentaire: " + e.getMessage());
        }
    }

    private void supprimerCommentaire(int commentId, int publicationId, VBox container) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer ce commentaire ?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            String sql = "DELETE FROM commentaire WHERE id=?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, commentId);
                pstmt.executeUpdate();
                chargerCommentaires(publicationId, container);
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Erreur suppression: " + e.getMessage());
            }
        }
    }

    // ========================================================================
    // 14. VALIDATION ET FORMULAIRES
    // ========================================================================

    private boolean validate() {
        boolean valid = true;

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

    private void fillFormForEdit(Publication pub) {
        editingId = pub.getId();
        titreField.setText(pub.getTitre());
        descriptionArea.setText(pub.getDescription());
        categorieCombo.setValue(pub.getCategorie());
        currentImageBase64 = pub.getImageBase64();
        if (currentImageBase64 != null && !currentImageBase64.isEmpty()) {
            try {
                imagePreview.setImage(new Image(currentImageBase64));
                imagePreviewBox.setVisible(true);
                imagePreviewBox.setManaged(true);
            } catch (Exception e) { e.printStackTrace(); }
        }
        formTitle.setText("✏️ Modifier la publication");
        saveBtn.setText("💾 Mettre à jour");
        titreField.requestFocus();
        titreField.setStyle("");
        descriptionArea.setStyle("");
        isAIGenerated = false;
    }

    @FXML
    private void clearForm() {
        editingId = null;
        titreField.clear();
        descriptionArea.clear();
        categorieCombo.setValue(null);
        imagePromptField.clear();
        imageGenStatus.setVisible(false);
        imageGenStatus.setManaged(false);
        removePhoto();
        formTitle.setText("✏️ Nouvelle publication");
        saveBtn.setText("💾 Publier");
        hideError(titreError);
        hideError(descError);
        hideError(categorieError);
        titreField.setStyle("");
        descriptionArea.setStyle("");
        isAIGenerated = false;
    }

    @FXML
    private void searchPublications() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadPublications(); return; }
        publicationList.clear();
        String sql = "SELECT p.*, " +
                "(SELECT COUNT(*) FROM commentaire c WHERE c.publication_id = p.id) as nb_commentaires, " +
                "((p.nombre_likes * 2) + ((SELECT COUNT(*) FROM commentaire c WHERE c.publication_id = p.id) * 1.5) + (p.nombre_vues * 0.5)) as score_popularite " +
                "FROM publication p " +
                "WHERE p.titre LIKE ? OR p.description LIKE ? " +
                "ORDER BY score_popularite DESC, p.nombre_likes DESC, nb_commentaires DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) addToList(rs);
            displayCards();
            if (publicationList.isEmpty())
                showInfoAlert("Information", "Aucune publication trouvée pour : " + keyword);
        } catch (SQLException e) {
            showErrorAlert("Erreur", e.getMessage());
        }
    }

    // ========================================================================
    // 15. UTILITAIRES
    // ========================================================================

    @FXML
    private void openChatBot() {
        ChatBotController.openChatBot();
    }

    private void updateStatistics() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) as total, SUM(nombre_likes) as totalLikes, " +
                            "SUM(nombre_dislikes) as totalDislikes FROM publication");
            rs.next();
            statsLabel.setText(String.format("📊 %d publications | 👍 %d | 👎 %d",
                    rs.getInt("total"),
                    rs.getInt("totalLikes"),
                    rs.getInt("totalDislikes")));
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

    private void ouvrirDetailPublication(Publication pub) {
        Alert detailAlert = new Alert(Alert.AlertType.INFORMATION);
        detailAlert.setTitle("Détail de la publication");
        detailAlert.setHeaderText(pub.getTitre());

        String content = "📁 Catégorie : " + pub.getCategorie() + "\n\n" +
                "📝 Description :\n" + pub.getDescription() + "\n\n" +
                "📅 Date : " + pub.getDateCreation() + "\n\n" +
                "👍 Likes : " + pub.getNombreLikes() + "\n" +
                "👎 Dislikes : " + pub.getNombreDislikes() + "\n" +
                "👁️ Vues : " + pub.getNombreVues();

        detailAlert.setContentText(content);
        detailAlert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ========================================================================
    // 16. NAVIGATION
    // ========================================================================

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

    // ========================================================================
    // 17. MODÈLE PUBLICATION
    // ========================================================================

    public static class Publication {
        private int id, nombreVues, nombreLikes, nombreDislikes, nombreCommentaires;
        private double scorePopularite;
        private String titre, description, dateCreation, categorie, imageBase64;

        public Publication(int id, String titre, String description,
                           String dateCreation, String categorie,
                           int nombreVues, int nombreLikes, int nombreDislikes) {
            this.id = id;
            this.titre = titre;
            this.description = description;
            this.dateCreation = dateCreation;
            this.categorie = categorie;
            this.nombreVues = nombreVues;
            this.nombreLikes = nombreLikes;
            this.nombreDislikes = nombreDislikes;
            this.nombreCommentaires = 0;
            this.scorePopularite = 0;
        }

        public int getId() { return id; }
        public String getTitre() { return titre; }
        public String getDescription() { return description; }
        public String getDateCreation() { return dateCreation; }
        public String getCategorie() { return categorie; }
        public int getNombreVues() { return nombreVues; }
        public int getNombreLikes() { return nombreLikes; }
        public int getNombreDislikes() { return nombreDislikes; }
        public int getNombreCommentaires() { return nombreCommentaires; }
        public double getScorePopularite() { return scorePopularite; }
        public String getImageBase64() { return imageBase64; }
        public boolean isAIGenerated() { return false; }

        public void setNombreLikes(int likes) { this.nombreLikes = likes; }
        public void setNombreDislikes(int dislikes) { this.nombreDislikes = dislikes; }
        public void setNombreCommentaires(int nombreCommentaires) { this.nombreCommentaires = nombreCommentaires; }
        public void setScorePopularite(double scorePopularite) { this.scorePopularite = scorePopularite; }
        public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    }
}
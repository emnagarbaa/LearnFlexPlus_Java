package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.Services.ServiceReponseExamen;
import org.example.entities.Examen;
import org.example.entities.ReponseExamen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ExamenFrontController implements Initializable {

    @FXML
    private Label lblTitre, lblMatiere, lblNiveau, lblDuree, lblNbQuestions, lblDescription;

    @FXML
    private VBox questionContainer, heroSection, startSection;

    @FXML
    private Button btnCommencer;

    @FXML
    private ScrollPane mainScrollPane, questionsScrollPane;

    private Examen examen;
    private List<TextArea> reponsesFields = new ArrayList<>();
    private boolean examenSoumis = false;
    private int currentUserId = -1;  // Initialisé à -1
    private String currentUserEmail = "";  // Ajouté pour le débogage

    public void setExamen(Examen examen) {
        this.examen = examen;
        lblTitre.setText(examen.getTitre());
        lblMatiere.setText(examen.getMatiere());
        lblNiveau.setText(examen.getNiveauexamen());
        lblDuree.setText(examen.getDuree() + " min");
        lblNbQuestions.setText(String.valueOf(examen.getNbquestion()));
        lblDescription.setText(examen.getDescription());

        // Récupérer l'ID de l'utilisateur connecté
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getId();
            currentUserEmail = currentUser.getEmail();
            System.out.println("✅ Utilisateur connecté: " + currentUserEmail + " (ID: " + currentUserId + ", Rôle: " + currentUser.getRole() + ")");
        } else {
            System.err.println("⚠️ Aucun utilisateur connecté!");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour passer un examen.");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (mainScrollPane != null) {
            mainScrollPane.setFitToWidth(true);
            mainScrollPane.setFitToHeight(true);
        }
        if (questionsScrollPane != null) {
            questionsScrollPane.setFitToWidth(true);
        }
    }

    @FXML
    private void handleCommencer() {
        if (startSection != null) {
            startSection.setVisible(false);
            startSection.setManaged(false);
        }

        if (questionsScrollPane != null) {
            questionsScrollPane.setVisible(true);
            questionsScrollPane.setManaged(true);
        }

        if (questionContainer != null) {
            questionContainer.getChildren().clear();
            afficherPDF();
            buildQuestions();
        }

        if (mainScrollPane != null) {
            mainScrollPane.setVvalue(0);
        }
    }

    @FXML
    private void handleFermer(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void afficherPDF() {
        if (examen == null || examen.getPdf() == null || examen.getPdf().trim().isEmpty()) {
            return;
        }

        File pdfFile = new File(examen.getPdf());

        if (!pdfFile.exists()) {
            Label erreurLabel = new Label("❌ Fichier PDF introuvable : " + examen.getPdf());
            erreurLabel.setStyle("-fx-text-fill: red; -fx-padding: 10;");
            questionContainer.getChildren().add(erreurLabel);
            return;
        }

        TitledPane pdfTitledPane = new TitledPane();
        pdfTitledPane.setText("📄 Document PDF");
        pdfTitledPane.setExpanded(true);
        pdfTitledPane.setStyle("-fx-font-weight: bold;");

        VBox pdfBox = new VBox(10);
        pdfBox.setPadding(new Insets(10));

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int maxPages = Math.min(document.getNumberOfPages(), 15);

            for (int i = 0; i < maxPages; i++) {
                BufferedImage img = renderer.renderImageWithDPI(i, 100);
                javafx.scene.image.Image fxImage = javafx.embed.swing.SwingFXUtils.toFXImage(img, null);
                ImageView iv = new ImageView(fxImage);
                iv.setFitWidth(750);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                pdfBox.getChildren().add(iv);
            }

            pdfTitledPane.setContent(pdfBox);
            questionContainer.getChildren().add(pdfTitledPane);

            Separator separator = new Separator();
            separator.setPadding(new Insets(20, 0, 20, 0));
            questionContainer.getChildren().add(separator);

        } catch (Exception e) {
            e.printStackTrace();
            Label erreurLabel = new Label("❌ Erreur PDF : " + e.getMessage());
            erreurLabel.setStyle("-fx-text-fill: red;");
            pdfBox.getChildren().add(erreurLabel);
            pdfTitledPane.setContent(pdfBox);
            questionContainer.getChildren().add(pdfTitledPane);
        }
    }

    private void buildQuestions() {
        reponsesFields.clear();

        int nb = examen.getNbquestion();

        if (nb <= 0) {
            Label noQuestions = new Label("⚠️ Aucune question pour cet examen");
            noQuestions.setStyle("-fx-text-fill: orange; -fx-padding: 20; -fx-font-size: 14px;");
            questionContainer.getChildren().add(noQuestions);
            return;
        }

        Label sectionTitle = new Label("📝 QUESTIONS DE L'EXAMEN");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1f4f65;");
        sectionTitle.setPadding(new Insets(10, 0, 10, 0));
        questionContainer.getChildren().add(sectionTitle);

        for (int i = 1; i <= nb; i++) {
            VBox questionBox = new VBox(10);
            questionBox.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 12px;" +
                            "-fx-padding: 20px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
            );

            Label qNumber = new Label("Question " + i + " / " + nb);
            qNumber.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1f4f65;");

            TextArea ta = new TextArea();
            ta.setPromptText("Écrivez votre réponse ici...");
            ta.setPrefRowCount(4);
            ta.setWrapText(true);
            ta.setStyle("-fx-font-size: 13px; -fx-font-family: 'Segoe UI';");

            reponsesFields.add(ta);
            questionBox.getChildren().addAll(qNumber, ta);
            questionContainer.getChildren().add(questionBox);
        }

        Button btnSubmit = new Button("📤 SOUMETTRE L'EXAMEN");
        btnSubmit.setStyle(
                "-fx-background-color: linear-gradient(#1f4f65, #163d4f);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 15px;" +
                        "-fx-padding: 15px 30px;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-cursor: hand;"
        );
        btnSubmit.setOnAction(e -> handleSubmit());
        btnSubmit.setMaxWidth(Double.MAX_VALUE);

        VBox.setMargin(btnSubmit, new Insets(20, 0, 10, 0));
        questionContainer.getChildren().add(btnSubmit);
    }

    private void handleSubmit() {
        if (examenSoumis) {
            showAlert(Alert.AlertType.WARNING, "Examen déjà soumis", "Cet examen a déjà été soumis !");
            return;
        }

        // Vérifier que l'utilisateur est connecté
        if (currentUserId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour soumettre un examen.");
            return;
        }

        boolean hasAnswer = reponsesFields.stream().anyMatch(ta -> !ta.getText().trim().isEmpty());

        if (!hasAnswer) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez répondre à au moins une question.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Soumettre l'examen ?");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir soumettre vos réponses ?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== RÉPONSES EXAMEN ===\n");
        sb.append("Titre: ").append(examen.getTitre()).append("\n");
        sb.append("Date: ").append(new Timestamp(System.currentTimeMillis())).append("\n\n");

        for (int i = 0; i < reponsesFields.size(); i++) {
            String reponse = reponsesFields.get(i).getText().trim();
            if (!reponse.isEmpty()) {
                sb.append("Q").append(i + 1).append(":\n");
                sb.append(reponse).append("\n");
                sb.append("─".repeat(50)).append("\n\n");
            }
        }

        try {
            ServiceReponseExamen service = new ServiceReponseExamen();
            // Utilisation de l'ID de l'utilisateur connecté
            ReponseExamen r = new ReponseExamen(examen.getId(), currentUserId, sb.toString());
            r.setDateSoumission(new Timestamp(System.currentTimeMillis()));
            service.soumettre(r);

            examenSoumis = true;
            System.out.println("✅ Examen soumis avec succès par l'utilisateur ID: " + currentUserId + " (" + currentUserEmail + ")");
            afficherResultat();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder : " + e.getMessage());
        }
    }

    private void afficherResultat() {
        if (questionContainer != null) {
            questionContainer.getChildren().clear();
        }

        VBox resultBox = new VBox(20);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(60));
        resultBox.setStyle("-fx-background-color: white; -fx-background-radius: 16px;");

        Label icon = new Label("✅");
        icon.setStyle("-fx-font-size: 64px;");

        Label title = new Label("Examen soumis avec succès !");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #2e7d32;");

        Label message = new Label("Vos réponses ont bien été enregistrées.");
        message.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle(
                "-fx-background-color: #1f4f65;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10px 25px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            if (stage != null) stage.close();
        });

        resultBox.getChildren().addAll(icon, title, message, closeBtn);

        if (questionContainer != null) {
            questionContainer.getChildren().add(resultBox);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
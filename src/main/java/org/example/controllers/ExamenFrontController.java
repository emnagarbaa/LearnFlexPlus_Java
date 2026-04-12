package org.example.controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.Services.ServiceCommentaire;
import org.example.entities.Commentaire;
import org.example.entities.Examen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ExamenFrontController implements Initializable {

    @FXML private Label  lblTitre;
    @FXML private Label  lblMatiere;
    @FXML private Label  lblNiveau;
    @FXML private Label  lblDuree;
    @FXML private Label  lblNbQuestions;
    @FXML private Label  lblDescription;
    @FXML private VBox   questionContainer;
    @FXML private Button btnCommencer;
    @FXML private VBox   heroSection;
    @FXML private VBox   startSection;

    private Examen examen;

    public void setExamen(Examen examen) {
        this.examen = examen;
        lblTitre.setText(examen.getTitre());
        lblMatiere.setText(examen.getMatiere() != null ? examen.getMatiere() : "—");
        lblNiveau.setText(examen.getNiveauexamen() != null ? examen.getNiveauexamen() : "—");
        lblDuree.setText(examen.getDuree() + " min");
        lblNbQuestions.setText(String.valueOf(examen.getNbquestion()));
        lblDescription.setText(examen.getDescription() != null ? examen.getDescription() : "");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Supprimer les logs PDFBox
        java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.fontbox").setLevel(java.util.logging.Level.OFF);
    }

    @FXML
    private void handleCommencer() {
        startSection.setVisible(false);
        startSection.setManaged(false);
        questionContainer.setVisible(true);
        questionContainer.setManaged(true);

        // ── Afficher le PDF via PDFBox 3.x ───────────────────
        if (examen.getPdf() != null && !examen.getPdf().isBlank()) {
            File pdfFile = new File(examen.getPdf());

            Label pdfTitre = new Label("📄  Support de l'examen");
            pdfTitre.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#1f4f65;");

            VBox pdfBox = new VBox(12);
            pdfBox.setStyle(
                    "-fx-background-color:white; -fx-background-radius:16; " +
                            "-fx-padding:25; -fx-border-color:#e0e0e0; " +
                            "-fx-border-radius:16; -fx-border-width:1;");
            pdfBox.getChildren().add(pdfTitre);

            if (pdfFile.exists()) {
                try (PDDocument document = Loader.loadPDF(pdfFile)) {
                    PDFRenderer renderer = new PDFRenderer(document);
                    int nbPages = document.getNumberOfPages();

                    for (int page = 0; page < nbPages; page++) {
                        BufferedImage buffImage = renderer.renderImageWithDPI(page, 150);
                        Image fxImage = SwingFXUtils.toFXImage(buffImage, null);

                        ImageView iv = new ImageView(fxImage);
                        iv.setPreserveRatio(true);
                        iv.setFitWidth(820);
                        iv.setSmooth(true);

                        if (nbPages > 1) {
                            Label pageLabel = new Label("Page " + (page + 1) + " / " + nbPages);
                            pageLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#999; -fx-alignment:center;");
                            pdfBox.getChildren().addAll(iv, pageLabel);
                        } else {
                            pdfBox.getChildren().add(iv);
                        }
                    }

                } catch (Exception ex) {
                    Label errPdf = new Label("⚠️  Erreur lors du chargement du PDF : " + ex.getMessage());
                    errPdf.setStyle("-fx-text-fill:#dc2626; -fx-font-size:12px;");
                    pdfBox.getChildren().add(errPdf);
                    ex.printStackTrace();
                }

            } else {
                Label errPdf = new Label("⚠️  PDF introuvable : " + examen.getPdf());
                errPdf.setStyle("-fx-text-fill:#dc2626; -fx-font-size:12px;");
                pdfBox.setStyle(
                        "-fx-background-color:white; -fx-background-radius:16; " +
                                "-fx-padding:25; -fx-border-color:#fca5a5; " +
                                "-fx-border-radius:16; -fx-border-width:1;");
                pdfBox.getChildren().add(errPdf);
            }

            questionContainer.getChildren().add(pdfBox);
        }

        buildQuestions();
    }

    private void buildQuestions() {
        int nb = examen != null ? examen.getNbquestion() : 5;
        for (int i = 1; i <= nb; i++) {
            VBox card = new VBox(12);
            card.setStyle(
                    "-fx-background-color:white; -fx-background-radius:16; " +
                            "-fx-padding:25; -fx-border-color:#e0e0e0; -fx-border-radius:16; " +
                            "-fx-border-width:1;");

            Label qLabel = new Label("Question " + i);
            qLabel.setStyle(
                    "-fx-font-size:13px; -fx-font-weight:bold; " +
                            "-fx-text-fill:#97c3a2; -fx-background-color:#f0f7f4; " +
                            "-fx-background-radius:20; -fx-padding:4 12;");

            Label qText = new Label("Répondez à la question " + i + " de l'examen.");
            qText.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#1f4f65;");
            qText.setWrapText(true);

            TextArea ta = new TextArea();
            ta.setPromptText("Écrivez votre réponse ici...");
            ta.setPrefRowCount(3);
            ta.setStyle(
                    "-fx-font-size:13px; -fx-background-radius:10; " +
                            "-fx-border-color:#d1d5db; -fx-border-radius:10; -fx-border-width:1;");

            card.getChildren().addAll(qLabel, qText, ta);
            questionContainer.getChildren().add(card);
        }

        Button btnSubmit = new Button("📨  Soumettre l'examen");
        btnSubmit.setStyle(
                "-fx-background-color:#1f4f65; -fx-text-fill:white; " +
                        "-fx-font-size:15px; -fx-font-weight:bold; -fx-background-radius:12; " +
                        "-fx-padding:13 35; -fx-cursor:hand;");
        btnSubmit.setOnAction(e -> handleSubmit());
        VBox.setMargin(btnSubmit, new Insets(10, 0, 0, 0));
        questionContainer.getChildren().add(btnSubmit);
    }

    private void handleSubmit() {
        questionContainer.getChildren().clear();

        // ── Message de succès ─────────────────────────────────
        VBox result = new VBox(20);
        result.setAlignment(Pos.CENTER);
        result.setPadding(new Insets(40));
        result.setStyle("-fx-background-color:white; -fx-background-radius:16; -fx-padding:40;");

        Label ico = new Label("✅");
        ico.setStyle("-fx-font-size:60px;");

        Label titre = new Label("Examen soumis avec succès !");
        titre.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#1f4f65;");

        Label msg = new Label("Vos réponses ont été enregistrées. Bonne chance !");
        msg.setStyle("-fx-font-size:14px; -fx-text-fill:#666;");

        Button btnFermer = new Button("✖  Fermer");
        btnFermer.setStyle(
                "-fx-background-color:#e5e7eb; -fx-text-fill:#374151; " +
                        "-fx-font-size:14px; -fx-background-radius:10; -fx-padding:10 30; -fx-cursor:hand;");
        btnFermer.setOnAction(e ->
                ((Stage) questionContainer.getScene().getWindow()).close()
        );

        result.getChildren().addAll(ico, titre, msg, btnFermer);
        questionContainer.getChildren().add(result);

        // ── Commentaires du prof ──────────────────────────────
        try {
            ServiceCommentaire serviceCommentaire = new ServiceCommentaire();
            List<Commentaire> commentaires = serviceCommentaire.recupererParExamenId(examen.getId());

            if (!commentaires.isEmpty()) {

                Label comTitre = new Label("💬  Commentaires du professeur");
                comTitre.setStyle(
                        "-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#1f4f65;");

                VBox comContainer = new VBox(12);
                comContainer.setStyle(
                        "-fx-background-color:white; -fx-background-radius:16; " +
                                "-fx-padding:25; -fx-border-color:#e0e0e0; " +
                                "-fx-border-radius:16; -fx-border-width:1;");
                comContainer.getChildren().add(comTitre);

                for (Commentaire c : commentaires) {
                    VBox comCard = new VBox(6);
                    comCard.setStyle(
                            "-fx-background-color:#f0f7f4; -fx-background-radius:12; " +
                                    "-fx-padding:15; -fx-border-color:#97c3a2; " +
                                    "-fx-border-radius:12; -fx-border-width:1;");

                    // En-tête : auteur + date
                    HBox header = new HBox(10);
                    header.setAlignment(Pos.CENTER_LEFT);

                    Label auteur = new Label("👤  " + c.getAuteur());
                    auteur.setStyle(
                            "-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1f4f65;");

                    Label date = new Label(c.getDatecre() != null ? c.getDatecre().toString() : "");
                    date.setStyle("-fx-font-size:11px; -fx-text-fill:#999;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    header.getChildren().addAll(auteur, spacer, date);

                    // Contenu du commentaire
                    Label contenu = new Label(c.getContenu());
                    contenu.setStyle("-fx-font-size:13px; -fx-text-fill:#374151;");
                    contenu.setWrapText(true);

                    // Likes
                    Label likes = new Label("👍  " + c.getLikes());
                    likes.setStyle("-fx-font-size:11px; -fx-text-fill:#97c3a2;");

                    comCard.getChildren().addAll(header, contenu, likes);
                    comContainer.getChildren().add(comCard);
                }

                questionContainer.getChildren().add(comContainer);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleFermer() {
        ((Stage) lblTitre.getScene().getWindow()).close();
    }
}
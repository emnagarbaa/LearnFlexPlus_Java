package org.example.controllers;
import org.example.Services.ServiceIA;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.Services.ServiceExamen;
import org.example.Services.ServiceReponseExamen;
import org.example.entities.Examen;
import org.example.entities.ReponseExamen;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class EnseignantExamenController implements Initializable {

    @FXML private ComboBox<Examen> cmbExamen;
    @FXML private ListView<ReponseExamen> listReponses;
    @FXML private VBox detailsContainer;
    @FXML private ScrollPane pdfScrollPane;
    @FXML private TextArea txtReponseEtudiant;
    @FXML private TextField txtNote;
    @FXML private TextArea txtCommentaire;
    @FXML private Label lblEtudiantInfo;
    @FXML private Label lblDateSoumission;
    @FXML private Label lblStatut;
    @FXML private Label lblMoyenne;
    @FXML private Label lblNbSoumissions;
    @FXML private Label lblNoteMax;
    @FXML private Label lblNoteMin;
    @FXML private TabPane tabPane;
    @FXML private Tab tabCorrection;
    @FXML private Tab tabStatistiques;

    private ServiceExamen serviceExamen;
    private ServiceReponseExamen serviceReponse;
    private List<Examen> examens;
    private ReponseExamen selectedReponse;
    private File currentPdfFile;
    private User currentUser;
    private final ServiceIA serviceIA = new ServiceIA();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        serviceExamen = new ServiceExamen();
        serviceReponse = new ServiceReponseExamen();
        currentUser = SessionManager.getCurrentUser();

        // Initialiser les labels des statistiques
        resetStatistiquesLabels();

        chargerExamens();
        setupListeners();

        if (currentUser != null && !isEnseignant(currentUser.getRole())) {
            showAlert("Accès refusé", "Cette page est réservée aux enseignants.");
            retourFront();
        }
    }

    private void resetStatistiquesLabels() {
        if (lblNbSoumissions != null) lblNbSoumissions.setText("0");
        if (lblMoyenne != null) lblMoyenne.setText("0/20");
        if (lblNoteMax != null) lblNoteMax.setText("0/20");
        if (lblNoteMin != null) lblNoteMin.setText("0/20");
    }

    private void chargerExamens() {
        try {
            examens = serviceExamen.recuperer();
            cmbExamen.getItems().addAll(examens);
            cmbExamen.setCellFactory(lv -> new ListCell<Examen>() {
                @Override
                protected void updateItem(Examen examen, boolean empty) {
                    super.updateItem(examen, empty);
                    setText(empty || examen == null ? "" : examen.getTitre());
                }
            });
            cmbExamen.setButtonCell(new ListCell<Examen>() {
                @Override
                protected void updateItem(Examen examen, boolean empty) {
                    super.updateItem(examen, empty);
                    setText(empty || examen == null ? "📚 Sélectionnez un examen" : examen.getTitre());
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les examens: " + e.getMessage());
        }
    }

    private void setupListeners() {
        cmbExamen.setOnAction(e -> {
            if (cmbExamen.getSelectionModel().getSelectedItem() != null) {
                chargerReponses();
                chargerStatistiques();
            }
        });

        listReponses.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedReponse = newVal;
                        afficherDetailsReponse();
                        if (tabPane != null) {
                            Platform.runLater(() -> tabPane.getSelectionModel().select(tabCorrection));
                        }
                    }
                }
        );
    }
    @FXML
    private void detecterIA() {
        String texte = txtReponseEtudiant.getText();
        if (texte == null || texte.trim().isEmpty()) {
            showAlert("Attention", "Aucune réponse à analyser.");
            return;
        }

        // Désactiver le bouton pendant l'analyse
        showAlert("Analyse en cours...", "⏳ L'IA analyse la réponse, veuillez patienter.");

        new Thread(() -> {
            try {
                String result = serviceIA.detecterIA(texte);
                JSONObject json = new JSONObject(result);

                int proba       = json.optInt("probabilite", 0);
                String verdict  = json.optString("verdict", "Inconnu");
                String expl     = json.optString("explication", "");
                JSONArray indices = json.optJSONArray("indices");

                StringBuilder sb = new StringBuilder();
                sb.append("🤖 Verdict : ").append(verdict).append("\n");
                sb.append("📊 Probabilité IA : ").append(proba).append("%\n\n");
                sb.append("📝 Explication : ").append(expl).append("\n\n");

                if (indices != null && indices.length() > 0) {
                    sb.append("🔍 Indices détectés :\n");
                    for (int i = 0; i < indices.length(); i++) {
                        sb.append("  • ").append(indices.getString(i)).append("\n");
                    }
                }

                String message = sb.toString();
                Platform.runLater(() -> showAlert("Résultat Détection IA", message));

            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Erreur", "Impossible d'analyser : " + e.getMessage()));
            }
        }).start();
    }
    @FXML
    private void chargerReponses() {
        Examen selected = cmbExamen.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            List<ReponseExamen> reponses = serviceReponse.getReponsesByExamenId(selected.getId());
            listReponses.getItems().clear();
            listReponses.getItems().addAll(reponses);

            listReponses.setCellFactory(lv -> new ListCell<ReponseExamen>() {
                @Override
                protected void updateItem(ReponseExamen item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(10);
                        hbox.setAlignment(Pos.CENTER_LEFT);
                        hbox.setPadding(new Insets(10));

                        // Récupérer le nom de l'étudiant si disponible
                        String nomEtudiant = "Étudiant #" + item.getUserId();
                        try {
                            Map<String, Object> details = serviceReponse.getReponseWithDetails(item.getId());
                            String prenom = (String) details.get("etudiant_prenom");
                            String nom = (String) details.get("etudiant_nom");
                            if (prenom != null && nom != null) {
                                nomEtudiant = prenom + " " + nom;
                            }
                        } catch (SQLException e) {
                            // Ignorer, garder l'ID par défaut
                        }

                        Label nomLabel = new Label("👤 " + nomEtudiant);
                        nomLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f4f65;");

                        String statutText = item.getNote() != null ? "✅ Noté: " + item.getNote() + "/20" : "⏳ En attente";
                        Label statutLabel = new Label(statutText);
                        statutLabel.setStyle("-fx-text-fill: " + (item.getNote() != null ? "#27ae60" : "#f39c12") + "; -fx-font-size: 11px;");

                        Label dateLabel = new Label("📅 " + (item.getDateSoumission() != null ?
                                item.getDateSoumission().toString().substring(0, 10) : "N/A"));
                        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        hbox.getChildren().addAll(nomLabel, spacer, statutLabel, dateLabel);
                        setGraphic(hbox);
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les réponses: " + e.getMessage());
        }
    }

    @FXML
    public void chargerStatistiques() {
        Examen selected = cmbExamen.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("⚠️ Aucun examen sélectionné pour les statistiques");
            return;
        }

        // 🔍 DÉBOGAGE CRITIQUE - Vérifier si les labels sont bien injectés
        System.out.println("=== VÉRIFICATION DES LABELS ===");
        System.out.println("lblNbSoumissions = " + lblNbSoumissions);
        System.out.println("lblMoyenne = " + lblMoyenne);
        System.out.println("lblNoteMax = " + lblNoteMax);
        System.out.println("lblNoteMin = " + lblNoteMin);
        System.out.println("==============================");

        if (lblNbSoumissions == null) {
            System.err.println("❌ ERREUR CRITIQUE: lblNbSoumissions est NULL! Vérifiez le fx:id dans votre FXML.");
            return;
        }

        System.out.println("📊 Chargement des statistiques pour l'examen ID: " + selected.getId());

        try {
            Map<String, Object> stats = serviceReponse.getStatistiquesExamen(selected.getId());

            System.out.println("📊 Stats reçues: " + stats);

            int total = stats.get("total") != null ? (int) stats.get("total") : 0;
            double moyenne = stats.get("moyenne") != null ? (double) stats.get("moyenne") : 0;
            double max = stats.get("max") != null ? (double) stats.get("max") : 0;
            double min = stats.get("min") != null ? (double) stats.get("min") : 0;

            // Mise à jour directe des labels (sans Platform.runLater pour tester)
            lblNbSoumissions.setText(String.valueOf(total));
            lblMoyenne.setText(String.format("%.2f / 20", moyenne));
            lblNoteMax.setText(String.format("%.2f / 20", max));
            lblNoteMin.setText(String.format("%.2f / 20", min));

            // Forcer le rafraîchissement visuel
            lblNbSoumissions.setVisible(true);
            lblMoyenne.setVisible(true);
            lblNoteMax.setVisible(true);
            lblNoteMin.setVisible(true);

            System.out.println("✅ Statistiques mises à jour: Total=" + total + ", Moyenne=" + moyenne);
            System.out.println("   lblNbSoumissions texte = " + lblNbSoumissions.getText());
            System.out.println("   lblMoyenne texte = " + lblMoyenne.getText());

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors du chargement des stats: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les statistiques: " + e.getMessage());
        }
    }
    private void afficherDetailsReponse() {
        if (selectedReponse == null) return;

        try {
            Map<String, Object> details = serviceReponse.getReponseWithDetails(selectedReponse.getId());

            String etudiantNom = (String) details.get("etudiant_nom");
            String etudiantPrenom = (String) details.get("etudiant_prenom");
            String examenPdf = (String) details.get("examen_pdf");

            if (etudiantNom != null && etudiantPrenom != null) {
                lblEtudiantInfo.setText("👤 " + etudiantPrenom + " " + etudiantNom);
            } else {
                lblEtudiantInfo.setText("👤 Étudiant #" + selectedReponse.getUserId());
            }

            lblDateSoumission.setText("📅 Soumis le: " + selectedReponse.getDateSoumission());

            if (selectedReponse.getNote() != null) {
                lblStatut.setText("✅ Statut: Corrigé - Note: " + selectedReponse.getNote() + "/20");
            } else {
                lblStatut.setText("⏳ Statut: En attente de correction");
            }

            txtReponseEtudiant.setText(selectedReponse.getContenu());

            if (selectedReponse.getNote() != null) {
                txtNote.setText(String.valueOf(selectedReponse.getNote()));
            } else {
                txtNote.clear();
            }

            if (selectedReponse.getCommentaireCorrection() != null) {
                txtCommentaire.setText(selectedReponse.getCommentaireCorrection());
            } else {
                txtCommentaire.clear();
            }

            if (examenPdf != null && !examenPdf.isEmpty()) {
                chargerPDF(examenPdf);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails: " + e.getMessage());
        }
    }

    private void chargerPDF(String pdfPath) {
        currentPdfFile = new File(pdfPath);
        if (!currentPdfFile.exists()) {
            VBox errorBox = new VBox();
            errorBox.setAlignment(Pos.CENTER);
            errorBox.getChildren().add(new Label("❌ Fichier PDF introuvable: " + pdfPath));
            pdfScrollPane.setContent(errorBox);
            return;
        }

        VBox pdfBox = new VBox(10);
        pdfBox.setPadding(new Insets(10));
        pdfBox.setAlignment(Pos.TOP_CENTER);

        try (PDDocument document = Loader.loadPDF(currentPdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int maxPages = Math.min(document.getNumberOfPages(), 15);

            for (int i = 0; i < maxPages; i++) {
                BufferedImage img = renderer.renderImageWithDPI(i, 100);
                javafx.scene.image.Image fxImage = javafx.embed.swing.SwingFXUtils.toFXImage(img, null);
                ImageView iv = new ImageView(fxImage);
                iv.setFitWidth(650);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                pdfBox.getChildren().add(iv);
            }

            Label pageInfo = new Label("📄 " + maxPages + " page(s) sur " + document.getNumberOfPages());
            pageInfo.setStyle("-fx-text-fill: #666; -fx-padding: 10;");
            pdfBox.getChildren().add(0, pageInfo);

            Platform.runLater(() -> pdfScrollPane.setContent(pdfBox));

        } catch (Exception e) {
            e.printStackTrace();
            VBox errorBox = new VBox();
            errorBox.setAlignment(Pos.CENTER);
            errorBox.getChildren().add(new Label("❌ Erreur chargement PDF: " + e.getMessage()));
            Platform.runLater(() -> pdfScrollPane.setContent(errorBox));
        }
    }

    @FXML
    private void enregistrerCorrection() {
        if (selectedReponse == null) {
            showAlert("Attention", "Veuillez sélectionner une réponse à corriger.");
            return;
        }

        String noteText = txtNote.getText();
        if (noteText.isEmpty()) {
            showAlert("Attention", "Veuillez entrer une note.");
            return;
        }

        try {
            double note = Double.parseDouble(noteText);
            if (note < 0 || note > 20) {
                showAlert("Attention", "La note doit être comprise entre 0 et 20.");
                return;
            }

            String commentaire = txtCommentaire.getText();

            serviceReponse.corrigerReponseAvecEmail(selectedReponse.getId(), note, commentaire);

            showAlert("Succès", "✅ Correction enregistrée avec succès !\n" +
                    "Un email a été envoyé à l'étudiant.\nNote: " + note + "/20");

            chargerReponses();
            chargerStatistiques();

            selectedReponse = null;
            txtNote.clear();
            txtCommentaire.clear();
            txtReponseEtudiant.clear();
            lblEtudiantInfo.setText("Aucune réponse sélectionnée");
            lblStatut.setText("");

        } catch (NumberFormatException e) {
            showAlert("Erreur", "La note doit être un nombre valide.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'enregistrer la correction: " + e.getMessage());
        }
    }

    @FXML
    private void retourFront() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/front.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 900);
            Stage stage = (Stage) cmbExamen.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("LearnFlex+");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isEnseignant(String role) {
        return role != null && (role.equalsIgnoreCase("Enseignant") ||
                role.equalsIgnoreCase("ENSEIGNANT") ||
                role.equalsIgnoreCase("Teacher"));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

package org.example.controllers;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Services.ServiceQuiz;
import org.example.entities.Quiz;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QuizController {

    // ── Injections FXML ──────────────────────────────────────────────────────
    //variables sont connectées à ton fichier FXML
    @FXML private TableView<Quiz>            tableQuiz;
    @FXML private TableColumn<Quiz, Integer> colId;
    @FXML private TableColumn<Quiz, String>  colTitre;
    @FXML private TableColumn<Quiz, String>  colQuestion;
    @FXML private TableColumn<Quiz, Integer> colDuree;
    @FXML private TableColumn<Quiz, String>  colEtat;
    @FXML private TableColumn<Quiz, Void>    colActions;
    @FXML private TextField                  searchField;
    @FXML private Label                      messageLabel;

    // ── Service & données ────────────────────────────────────────────────────
    //parle avec la base de données (CRUD)
    private final ServiceQuiz service = new ServiceQuiz();
    //contient les quiz affichés dans la table
    private final ObservableList<Quiz> masterList = FXCollections.observableArrayList();

    // =========================================================================
    //  VALIDATION — constantes
    // =========================================================================
    private static final int TITRE_MIN       = 3;
    private static final int TITRE_MAX       = 100;
    private static final int QUESTION_MIN    = 10;
    private static final int QUESTION_MAX    = 500;
    private static final int DESCRIPTION_MAX = 1000;
    private static final int DUREE_MIN       = 1;
    private static final int DUREE_MAX       = 300;

    // =========================================================================
    //  VALIDATION — méthodes
    // =========================================================================

    private boolean validerFormulaire(TextField fTitre,
                                      TextField fQuestion,
                                      TextField fDesc,
                                      TextField fDuree,
                                      ComboBox<String> fEtat,
                                      Label[] errLabels) {
        boolean valide = true;

        String titre = fTitre.getText().trim();
        if (titre.isEmpty()) {
            setErreur(fTitre, errLabels[0], "Le titre est obligatoire.");
            valide = false;
        } else if (titre.length() < TITRE_MIN || titre.length() > TITRE_MAX) {
            setErreur(fTitre, errLabels[0],
                    "Le titre doit contenir entre " + TITRE_MIN + " et " + TITRE_MAX + " caractères.");
            valide = false;
        } else {
            clearErreur(fTitre, errLabels[0]);
        }

        String question = fQuestion.getText().trim();
        if (question.isEmpty()) {
            setErreur(fQuestion, errLabels[1], "La question est obligatoire.");
            valide = false;
        } else if (question.length() < QUESTION_MIN || question.length() > QUESTION_MAX) {
            setErreur(fQuestion, errLabels[1],
                    "La question doit contenir entre " + QUESTION_MIN + " et " + QUESTION_MAX + " caractères.");
            valide = false;
        } else {
            clearErreur(fQuestion, errLabels[1]);
        }

        String desc = fDesc.getText().trim();
        if (!desc.isEmpty() && desc.length() > DESCRIPTION_MAX) {
            setErreur(fDesc, errLabels[2],
                    "La description ne peut pas dépasser " + DESCRIPTION_MAX + " caractères.");
            valide = false;
        } else {
            clearErreur(fDesc, errLabels[2]);
        }

        String dureeStr = fDuree.getText().trim();
        if (dureeStr.isEmpty()) {
            setErreur(fDuree, errLabels[3], "La durée est obligatoire.");
            valide = false;
        } else {
            try {
                int duree = Integer.parseInt(dureeStr);
                if (duree < DUREE_MIN || duree > DUREE_MAX) {
                    setErreur(fDuree, errLabels[3],
                            "La durée doit être entre " + DUREE_MIN + " et " + DUREE_MAX + " minutes.");
                    valide = false;
                } else {
                    clearErreur(fDuree, errLabels[3]);
                }
            } catch (NumberFormatException e) {
                setErreur(fDuree, errLabels[3], "La durée doit être un nombre entier.");
                valide = false;
            }
        }

        String etat = fEtat.getValue();
        if (etat == null || (!etat.equals("active") && !etat.equals("inactive"))) {
            setErreur(fEtat, errLabels[4], "Sélectionnez un état valide (active / inactive).");
            valide = false;
        } else {
            clearErreur(fEtat, errLabels[4]);
        }

        return valide;
    }

    private void setErreur(TextField field, Label errLabel, String message) {
        field.setStyle(field.getStyle().replace("-fx-border-color:#ccc;", "")
                + "-fx-border-color:#e24b4a;-fx-border-width:1.5;");
        errLabel.setText(message);
        errLabel.setVisible(true);
        errLabel.setManaged(true);
    }

    private void setErreur(ComboBox<?> combo, Label errLabel, String message) {
        combo.setStyle("-fx-border-color:#e24b4a;-fx-border-width:1.5;-fx-border-radius:6;");
        errLabel.setText(message);
        errLabel.setVisible(true);
        errLabel.setManaged(true);
    }

    private void clearErreur(TextField field, Label errLabel) {
        field.setStyle("-fx-background-radius:6;-fx-border-color:#ccc;"
                + "-fx-border-radius:6;-fx-font-size:13px;-fx-padding:7 10;");
        errLabel.setVisible(false);
        errLabel.setManaged(false);
    }

    private void clearErreur(ComboBox<?> combo, Label errLabel) {
        combo.setStyle("");
        errLabel.setVisible(false);
        errLabel.setManaged(false);
    }

    private Label creerLabelErreur() {
        Label lbl = new Label();
        lbl.setStyle("-fx-text-fill:#a32d2d;-fx-font-size:11px;");
        lbl.setVisible(false);
        lbl.setManaged(false);
        lbl.setWrapText(true);
        return lbl;
    }

    // =========================================================================
    //  INITIALIZE
    // =========================================================================
//exécuté automatiquement au démarrage
    @FXML
    public void initialize() {
        configurerColonnes();
        configurerColonneActions();
        chargerDonnees();
    }

    // =========================================================================
    //  COLONNES
    // =========================================================================

    private void configurerColonnes() {
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getId()).asObject());
        colTitre.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getTitre()));
        colQuestion.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getQuestion()));
        colDuree.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getDuree()).asObject());
        colEtat.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getEtat()));

        colEtat.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val);
                badge.setStyle("active".equalsIgnoreCase(val)
                        ? "-fx-background-color:#eaf3de;-fx-text-fill:#3b6d11;-fx-padding:3 10;-fx-background-radius:10;-fx-font-size:11px;-fx-font-weight:bold;"
                        : "-fx-background-color:#fcebeb;-fx-text-fill:#a32d2d;-fx-padding:3 10;-fx-background-radius:10;-fx-font-size:11px;-fx-font-weight:bold;"
                );
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void configurerColonneActions() {
        colActions.setCellFactory(tc -> new TableCell<>() {
            final Button btnEdit     = creerBouton("Éditer",    "#77af84");
            final Button btnReponses = creerBouton("Réponses",  "#7777af");
            final Button btnDelete   = creerBouton("Supprimer", "#e24b4a");
            final HBox   box         = new HBox(6, btnEdit, btnReponses, btnDelete);

            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setOnAction(e -> {
                    Quiz q = getTableRow().getItem();
                    if (q == null) return;
                    ouvrirModalModifier(q);
                });
                btnReponses.setOnAction(e -> {
                    Quiz q = getTableRow().getItem();
                    if (q == null) return;
                    ouvrirPageReponses(q);
                });
                btnDelete.setOnAction(e -> {
                    Quiz q = getTableRow().getItem();
                    if (q == null) return;
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Supprimer \"" + q.getTitre() + "\" ?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.YES) {
                            service.supprimer(q);
                            chargerDonnees();
                            afficherMessage("Quiz supprimé.", true);
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // =========================================================================
    //  ACTIONS
    // =========================================================================

    private void ouvrirPageReponses(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/reponse.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            ReponseController ctrl = loader.getController();
            ctrl.initAvecQuiz(quiz);
            Stage stage = (Stage) tableQuiz.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("LearnFlex Admin – Réponses");
        } catch (Exception e) {
            e.printStackTrace();
            afficherMessage("Erreur lors de l'ouverture des réponses.", false);
        }
    }

    private void chargerDonnees() {
        masterList.setAll(service.recuperer());
        tableQuiz.setItems(masterList);
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText().trim().toLowerCase();
        tableQuiz.setItems(q.isEmpty() ? masterList : masterList.filtered(quiz ->
                quiz.getTitre().toLowerCase().contains(q) ||
                        quiz.getQuestion().toLowerCase().contains(q)));
    }

    @FXML void ajouterQuiz()  { ouvrirModalAjouter(); }

    @FXML void supprimerQuiz() {
        Quiz s = tableQuiz.getSelectionModel().getSelectedItem();
        if (s == null) { afficherMessage("Sélectionnez un quiz.", false); return; }
        service.supprimer(s);
        chargerDonnees();
        afficherMessage("Quiz supprimé.", true);
    }

    @FXML void modifierQuiz() {
        Quiz s = tableQuiz.getSelectionModel().getSelectedItem();
        if (s == null) { afficherMessage("Sélectionnez un quiz.", false); return; }
        ouvrirModalModifier(s);
    }

    // =========================================================================
    //  EXPORT PDF
    // =========================================================================

    @FXML
    void exporterPDF() {
        ObservableList<Quiz> listeAExporter = tableQuiz.getItems();
        if (listeAExporter == null || listeAExporter.isEmpty()) {
            afficherMessage("Aucun quiz à exporter.", false);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.setInitialFileName("rapport_quiz_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));

        Stage stage = (Stage) tableQuiz.getScene().getWindow();
        File fichier = fileChooser.showSaveDialog(stage);
        if (fichier == null) return;

        try {
            genererPDFQuiz(fichier.getAbsolutePath(), listeAExporter);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export réussi");
            alert.setHeaderText(null);
            alert.setContentText("Rapport exporté avec succès :\n" + fichier.getAbsolutePath());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'export");
            alert.setHeaderText("Impossible de générer le PDF");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void genererPDFQuiz(String cheminFichier,
                                ObservableList<Quiz> quizzes) throws Exception {

        DeviceRgb BLEU        = new DeviceRgb(24,  144, 255);
        DeviceRgb GRIS_FOND   = new DeviceRgb(250, 251, 252);
        DeviceRgb BLANC       = new DeviceRgb(255, 255, 255);  // ← DeviceRgb, pas ColorConstants.WHITE
        DeviceRgb VERT_TEXTE  = new DeviceRgb(59,  109, 17);
        DeviceRgb ROUGE_TEXTE = new DeviceRgb(163, 45,  45);
        DeviceRgb VERT_BADGE  = new DeviceRgb(234, 243, 222);
        DeviceRgb ROUGE_BADGE = new DeviceRgb(252, 235, 235);

        try (PdfDocument pdf = new PdfDocument(new PdfWriter(cheminFichier));
             Document doc = new Document(pdf, PageSize.A4.rotate())) {

            doc.setMargins(36, 36, 36, 36);

            // En-tête
            doc.add(new Paragraph("Rapport — Liste des Quiz")
                    .setFontSize(22).setBold()
                    .setFontColor(BLEU)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4));

            String dateStr = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy a HH:mm"));
            doc.add(new Paragraph("Genere le " + dateStr + "  -  " + quizzes.size() + " quiz")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Tableau : # | Titre | Question | Durée | État
            Table table = new Table(new float[]{0.5f, 2f, 5f, 1f, 1f})
                    .setWidth(UnitValue.createPercentValue(100));

            // En-têtes colonnes
            for (String h : new String[]{"#", "Titre", "Question", "Duree (min)", "Etat"}) {
                table.addHeaderCell(
                        new Cell()
                                .add(new Paragraph(h).setBold().setFontSize(11)
                                        .setFontColor(ColorConstants.WHITE))
                                .setBackgroundColor(BLEU)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setPadding(8)
                );
            }

            // Lignes
            for (int i = 0; i < quizzes.size(); i++) {
                Quiz q = quizzes.get(i);
                DeviceRgb fondLigne = (i % 2 == 0) ? BLANC : GRIS_FOND;

                table.addCell(cellule(String.valueOf(i + 1), fondLigne, TextAlignment.CENTER, 10, null));
                table.addCell(cellule(q.getTitre(), fondLigne, TextAlignment.LEFT, 10, null));

                String questionAffichee = q.getQuestion().length() > 120
                        ? q.getQuestion().substring(0, 117) + "..."
                        : q.getQuestion();
                table.addCell(cellule(questionAffichee, fondLigne, TextAlignment.LEFT, 9, null));

                table.addCell(cellule(String.valueOf(q.getDuree()), fondLigne, TextAlignment.CENTER, 10, null));

                // Badge état
                boolean actif = "active".equalsIgnoreCase(q.getEtat());
                table.addCell(
                        new Cell()
                                .add(new Paragraph(q.getEtat())
                                        .setFontSize(9).setBold()
                                        .setFontColor(actif ? VERT_TEXTE : ROUGE_TEXTE))
                                .setBackgroundColor(actif ? VERT_BADGE : ROUGE_BADGE)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setPadding(5)
                );
            }

            doc.add(table);

            doc.add(new Paragraph("- Fin du rapport -")
                    .setFontSize(9)
                    .setFontColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(16));
        }
    }

    private Cell cellule(String texte, DeviceRgb fond,
                         TextAlignment align, float fontSize, DeviceRgb couleurTexte) {
        Paragraph p = new Paragraph(texte).setFontSize(fontSize);
        if (couleurTexte != null) p.setFontColor(couleurTexte);
        return new Cell().add(p)
                .setBackgroundColor(fond)
                .setTextAlignment(align)
                .setPadding(5);
    }

    // =========================================================================
    //  MODALS
    // =========================================================================

    private void ouvrirModalAjouter() {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Ajouter un nouveau Quiz");

        TextField fTitre    = champTexte("");
        TextField fQuestion = champTexte("");
        TextField fDesc     = champTexte("");
        TextField fDuree    = champTexte("30");
        ComboBox<String> fEtat = new ComboBox<>(
                FXCollections.observableArrayList("active", "inactive"));
        fEtat.setValue("active");
        fEtat.setMaxWidth(Double.MAX_VALUE);

        Label[] erreurs = new Label[]{
                creerLabelErreur(), creerLabelErreur(),
                creerLabelErreur(), creerLabelErreur(), creerLabelErreur()
        };

        Button btnSave   = creerBouton("Ajouter",  "#142341");
        Button btnCancel = creerBouton("Annuler",  "#888888");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setMaxWidth(Double.MAX_VALUE);

        btnSave.setOnAction(e -> {
            if (!validerFormulaire(fTitre, fQuestion, fDesc, fDuree, fEtat, erreurs)) return;
            try {
                service.ajouter(new Quiz(
                        fTitre.getText().trim(),
                        fQuestion.getText().trim(),
                        Integer.parseInt(fDuree.getText().trim()),
                        fEtat.getValue(),
                        fDesc.getText().trim()));
                chargerDonnees();
                afficherMessage("Quiz ajouté avec succès !", true);
                modal.close();
            } catch (Exception ex) {
                afficherMessage("Erreur lors de l'ajout : " + ex.getMessage(), false);
            }
        });
        btnCancel.setOnAction(e -> modal.close());

        modal.setScene(new Scene(
                construireFormulaireAvecErreurs(
                        "Ajouter un nouveau Quiz",
                        new String[]{"Titre *", "Question *", "Description", "Durée (min) *", "État *"},
                        new javafx.scene.Node[]{fTitre, fQuestion, fDesc, fDuree, fEtat},
                        erreurs, btnSave, btnCancel),
                420, 530));
        modal.showAndWait();
    }

    private void ouvrirModalModifier(Quiz quiz) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Modifier le Quiz");

        TextField fTitre    = champTexte(quiz.getTitre());
        TextField fQuestion = champTexte(quiz.getQuestion());
        TextField fDesc     = champTexte(quiz.getDescription() != null ? quiz.getDescription() : "");
        TextField fDuree    = champTexte(String.valueOf(quiz.getDuree()));
        ComboBox<String> fEtat = new ComboBox<>(
                FXCollections.observableArrayList("active", "inactive"));
        fEtat.setValue(quiz.getEtat());
        fEtat.setMaxWidth(Double.MAX_VALUE);

        Label[] erreurs = new Label[]{
                creerLabelErreur(), creerLabelErreur(),
                creerLabelErreur(), creerLabelErreur(), creerLabelErreur()
        };

        Button btnSave   = creerBouton("Enregistrer", "#142341");
        Button btnCancel = creerBouton("Annuler",     "#888888");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setMaxWidth(Double.MAX_VALUE);

        btnSave.setOnAction(e -> {
            if (!validerFormulaire(fTitre, fQuestion, fDesc, fDuree, fEtat, erreurs)) return;
            try {
                quiz.setTitre(fTitre.getText().trim());
                quiz.setQuestion(fQuestion.getText().trim());
                quiz.setDescription(fDesc.getText().trim());
                quiz.setDuree(Integer.parseInt(fDuree.getText().trim()));
                quiz.setEtat(fEtat.getValue());
                service.modifier(quiz);
                chargerDonnees();
                afficherMessage("Quiz modifié avec succès !", true);
                modal.close();
            } catch (Exception ex) {
                afficherMessage("Erreur lors de la modification : " + ex.getMessage(), false);
            }
        });
        btnCancel.setOnAction(e -> modal.close());

        modal.setScene(new Scene(
                construireFormulaireAvecErreurs(
                        "Modifier le Quiz",
                        new String[]{"Titre *", "Question *", "Description", "Durée (min) *", "État *"},
                        new javafx.scene.Node[]{fTitre, fQuestion, fDesc, fDuree, fEtat},
                        erreurs, btnSave, btnCancel),
                420, 530));
        modal.showAndWait();
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private VBox construireFormulaireAvecErreurs(String titre,
                                                 String[] labels,
                                                 javafx.scene.Node[] fields,
                                                 Label[] erreurs,
                                                 Button btnSave,
                                                 Button btnCancel) {
        VBox form = new VBox(10);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: white;");

        Label heading = new Label(titre);
        heading.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#142341;");
        heading.setMaxWidth(Double.MAX_VALUE);
        heading.setAlignment(Pos.CENTER);

        Label hint = new Label("* Champs obligatoires");
        hint.setStyle("-fx-font-size:11px;-fx-text-fill:#888;");
        hint.setMaxWidth(Double.MAX_VALUE);
        hint.setAlignment(Pos.CENTER_RIGHT);

        form.getChildren().addAll(heading, hint);

        for (int i = 0; i < labels.length; i++) {
            Label lbl = new Label(labels[i]);
            lbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#444;");
            VBox groupe = new VBox(3, lbl, fields[i], erreurs[i]);
            form.getChildren().add(groupe);
        }

        HBox actions = new HBox(10, btnSave, btnCancel);
        HBox.setHgrow(btnSave, Priority.ALWAYS);
        HBox.setHgrow(btnCancel, Priority.ALWAYS);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(6, 0, 0, 0));
        form.getChildren().add(actions);

        return form;
    }

    private Button creerBouton(String texte, String couleur) {
        Button b = new Button(texte);
        b.setStyle("-fx-background-color:" + couleur + ";-fx-text-fill:white;" +
                "-fx-background-radius:6;-fx-font-size:12px;" +
                "-fx-padding:6 12;-fx-cursor:hand;-fx-border-width:0;");
        return b;
    }

    private TextField champTexte(String texte) {
        TextField tf = new TextField(texte);
        tf.setStyle("-fx-background-radius:6;-fx-border-color:#ccc;" +
                "-fx-border-radius:6;-fx-font-size:13px;-fx-padding:7 10;");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private void afficherMessage(String msg, boolean succes) {
        messageLabel.setText(msg);
        messageLabel.setStyle(succes
                ? "-fx-text-fill:#3b6d11;-fx-font-size:13px;-fx-font-weight:bold;"
                : "-fx-text-fill:#a32d2d;-fx-font-size:13px;-fx-font-weight:bold;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void afficherInfo(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
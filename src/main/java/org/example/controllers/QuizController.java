package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.entities.Quiz;
import org.example.Services.ServiceQuiz;

public class QuizController {

    // ── Injections FXML ──────────────────────────────────────────────────────
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
    private final ServiceQuiz service = new ServiceQuiz();
    private final ObservableList<Quiz> masterList = FXCollections.observableArrayList();

    // =========================================================================
    //  VALIDATION — constantes
    // =========================================================================
    private static final int TITRE_MIN      = 3;
    private static final int TITRE_MAX      = 100;
    private static final int QUESTION_MIN   = 10;
    private static final int QUESTION_MAX   = 500;
    private static final int DESCRIPTION_MAX = 1000;
    private static final int DUREE_MIN      = 1;
    private static final int DUREE_MAX      = 300;

    // =========================================================================
    //  VALIDATION — méthodes
    // =========================================================================

    /**
     * Valide tous les champs du formulaire Quiz.
     * Affiche les erreurs en ligne sous chaque champ invalide.
     * @return true si tout est valide, false si au moins un champ est en erreur.
     */
    private boolean validerFormulaire(TextField fTitre,
                                      TextField fQuestion,
                                      TextField fDesc,
                                      TextField fDuree,
                                      ComboBox<String> fEtat,
                                      Label[] errLabels) {
        boolean valide = true;

        // — Titre —
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

        // — Question —
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

        // — Description (optionnelle) —
        String desc = fDesc.getText().trim();
        if (!desc.isEmpty() && desc.length() > DESCRIPTION_MAX) {
            setErreur(fDesc, errLabels[2],
                    "La description ne peut pas dépasser " + DESCRIPTION_MAX + " caractères.");
            valide = false;
        } else {
            clearErreur(fDesc, errLabels[2]);
        }

        // — Durée —
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

        // — État —
        String etat = fEtat.getValue();
        if (etat == null || (!etat.equals("active") && !etat.equals("inactive"))) {
            setErreur(fEtat, errLabels[4], "Sélectionnez un état valide (active / inactive).");
            valide = false;
        } else {
            clearErreur(fEtat, errLabels[4]);
        }

        return valide;
    }

    /** Applique le style erreur sur un TextField et affiche le label d'erreur. */
    private void setErreur(TextField field, Label errLabel, String message) {
        field.setStyle(field.getStyle().replace("-fx-border-color:#ccc;", "")
                + "-fx-border-color:#e24b4a;-fx-border-width:1.5;");
        errLabel.setText(message);
        errLabel.setVisible(true);
        errLabel.setManaged(true);
    }

    /** Applique le style erreur sur une ComboBox et affiche le label d'erreur. */
    private void setErreur(ComboBox<?> combo, Label errLabel, String message) {
        combo.setStyle("-fx-border-color:#e24b4a;-fx-border-width:1.5;-fx-border-radius:6;");
        errLabel.setText(message);
        errLabel.setVisible(true);
        errLabel.setManaged(true);
    }

    /** Efface le style erreur sur un TextField. */
    private void clearErreur(TextField field, Label errLabel) {
        field.setStyle("-fx-background-radius:6;-fx-border-color:#ccc;"
                + "-fx-border-radius:6;-fx-font-size:13px;-fx-padding:7 10;");
        errLabel.setVisible(false);
        errLabel.setManaged(false);
    }

    /** Efface le style erreur sur une ComboBox. */
    private void clearErreur(ComboBox<?> combo, Label errLabel) {
        combo.setStyle("");
        errLabel.setVisible(false);
        errLabel.setManaged(false);
    }

    /** Crée un Label d'erreur préconfiguré (caché par défaut). */
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

    @FXML void ajouterQuiz()   { ouvrirModalAjouter(); }
    @FXML void exporterPDF()   { afficherInfo("Export PDF", "Export PDF déclenché !"); }

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

        // Labels d'erreur (un par champ)
        Label[] erreurs = new Label[]{
                creerLabelErreur(), creerLabelErreur(),
                creerLabelErreur(), creerLabelErreur(), creerLabelErreur()
        };

        Button btnSave   = creerBouton("Ajouter",  "#142341");
        Button btnCancel = creerBouton("Annuler",  "#888888");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setMaxWidth(Double.MAX_VALUE);

        btnSave.setOnAction(e -> {
            // ── CONTRÔLE DE SAISIE ──
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
            // ── CONTRÔLE DE SAISIE ──
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
    //  HELPERS — FORMULAIRE
    // =========================================================================

    /**
     * Construit le VBox du formulaire en intercalant chaque champ
     * avec son label d'erreur dédié.
     */
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

        // Sous-titre champs obligatoires
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
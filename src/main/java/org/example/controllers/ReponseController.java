package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.entities.Quiz;
import org.example.entities.Reponse;
import org.example.Services.ServiceReponse;

import java.util.Optional;

public class ReponseController {

    // ── Injections FXML ──────────────────────────────────────────────────────
    @FXML private TableView<Reponse>           tableReponse;
    @FXML private TableColumn<Reponse,Integer> colId;
    @FXML private TableColumn<Reponse,String>  colTexte;
    @FXML private TableColumn<Reponse,String>  colEstCorrecte;
    @FXML private TableColumn<Reponse,Void>    colActions;
    @FXML private TextField                     searchField;
    @FXML private Label                         messageLabel;
    @FXML private Label                         quizTitreLabel;

    // ── Données ──────────────────────────────────────────────────────────────
    private final ServiceReponse service = new ServiceReponse();
    private final ObservableList<Reponse> masterList = FXCollections.observableArrayList();

    // Quiz courant (passé depuis QuizController)
    private Quiz quizCourant;

    // ── Appelé par QuizController avant d'afficher la page ───────────────────
    public void initAvecQuiz(Quiz quiz) {
        this.quizCourant = quiz;
        quizTitreLabel.setText("Quiz : " + quiz.getTitre());
        chargerDonnees();
    }

    // ── initialize() ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        configurerColonnes();
        configurerColonneActions();
    }

    // ── Configuration colonnes ────────────────────────────────────────────────
    private void configurerColonnes() {
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getId()).asObject());

        colTexte.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getTexte()));

        // Colonne Correcte : badge ✅ / ❌
        colEstCorrecte.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().isEstCorrecte() ? "true" : "false"));

        colEstCorrecte.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }

                Label badge = new Label("true".equals(val) ? "✅ Oui" : "❌ Non");
                badge.setStyle("true".equals(val)
                        ? "-fx-background-color:#eaf3de; -fx-text-fill:#3b6d11;" +
                          "-fx-padding:3 10; -fx-background-radius:10; -fx-font-size:11px;"
                        : "-fx-background-color:#fcebeb; -fx-text-fill:#a32d2d;" +
                          "-fx-padding:3 10; -fx-background-radius:10; -fx-font-size:11px;"
                );
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });
    }

    // ── Colonne Actions ───────────────────────────────────────────────────────
    private void configurerColonneActions() {
        colActions.setCellFactory(tc -> new TableCell<>() {

            final Button btnVoir     = creerBouton("Voir",      "#378add");
            final Button btnModifier = creerBouton("Modifier",  "#77af84");
            final Button btnSuppr    = creerBouton("Supprimer", "#e24b4a");
            final HBox   box         = new HBox(6, btnVoir, btnModifier, btnSuppr);

            {
                box.setAlignment(Pos.CENTER);

                btnVoir.setOnAction(e -> {
                    Reponse r = (Reponse) getTableRow().getItem(); // ← getItem() au lieu de getIndex()
                    if (r == null) return;
                    afficherInfo("Détail Réponse",
                            "ID : "       + r.getId()                          + "\n" +
                                    "Texte : "    + r.getTexte()                       + "\n" +
                                    "Correcte : " + (r.isEstCorrecte() ? "Oui" : "Non")
                    );
                });

                btnModifier.setOnAction(e -> {
                    Reponse r = (Reponse) getTableRow().getItem(); // ← getItem()
                    if (r == null) return;
                    ouvrirModalModifier(r);
                });

                btnSuppr.setOnAction(e -> {
                    Reponse r = (Reponse) getTableRow().getItem(); // ✅ CORRECT

                    if (r == null) return;

                    System.out.println(">>> SUPPRIMER id = " + r.getId());

                    Alert confirm = new Alert(
                            Alert.AlertType.CONFIRMATION,
                            "Supprimer cette réponse ?",
                            ButtonType.YES, ButtonType.NO
                    );
                    confirm.setHeaderText(null);

                    Optional<ButtonType> result = confirm.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.YES) {
                        service.supprimer(r);
                        masterList.remove(r);
                        tableReponse.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ── Chargement ────────────────────────────────────────────────────────────
    private void chargerDonnees() {
        if (quizCourant != null) {
            masterList.setAll(service.recupererParQuiz(quizCourant.getId()));
            tableReponse.setItems(masterList);
        }
    }

    // ── Recherche ─────────────────────────────────────────────────────────────
    @FXML
    private void handleSearch() {
        String q = searchField.getText().trim().toLowerCase();
        tableReponse.setItems(q.isEmpty() ? masterList :
                masterList.filtered(r -> r.getTexte().toLowerCase().contains(q))
        );
    }

    // ── Ajouter ───────────────────────────────────────────────────────────────
    @FXML
    void ajouterReponse() {
        ouvrirModalAjouter();
    }

    // ── Retour à la liste des Quiz ────────────────────────────────────────────
    @FXML
    void retourQuiz() {
        try {
            // récupérer le dashboard actuel
            Stage stage = (Stage) tableReponse.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/dashboard.fxml")
            );

            Scene scene = new Scene(loader.load(), 1200, 800);

            DashboardController controller = loader.getController();

            // charger directement quiz dans le mainContent
            controller.showQuestionnaire();

            stage.setScene(scene);
            stage.setTitle("LearnFlex Admin");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Modal Ajouter ─────────────────────────────────────────────────────────
    private void ouvrirModalAjouter() {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Nouvelle Réponse");

        TextField fTexte = champTexte("");
        CheckBox  fCorrecte = new CheckBox("Cette réponse est correcte");
        fCorrecte.setStyle("-fx-font-size: 13px;");

        Button btnSave   = creerBouton("Ajouter",  "#142341");
        Button btnCancel = creerBouton("Annuler",  "#888888");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setMaxWidth(Double.MAX_VALUE);

        btnSave.setOnAction(e -> {
            String texte = fTexte.getText().trim();

            // ❌ Texte vide
            if (texte.isEmpty()) {
                afficherMessage("Le texte est obligatoire.", false);
                return;
            }

            // ❌ Texte trop court
            if (texte.length() < 3) {
                afficherMessage("Le texte doit contenir au moins 3 caractères.", false);
                return;
            }

            // ❌ Vérifier qu’un quiz est sélectionné
            if (quizCourant == null) {
                afficherMessage("Erreur : aucun quiz associé.", false);
                return;
            }

            // ❌ Vérifier qu’il n’existe pas déjà
            boolean existe = masterList.stream()
                    .anyMatch(r -> r.getTexte().equalsIgnoreCase(texte));

            if (existe) {
                afficherMessage("Cette réponse existe déjà.", false);
                return;
            }

            // ✅ Création
            Reponse r = new Reponse(
                    texte,
                    fCorrecte.isSelected(),
                    quizCourant
            );

            service.ajouter(r);
            chargerDonnees();
            afficherMessage("Réponse ajoutée avec succès !", true);
            modal.close();
        });
        btnCancel.setOnAction(e -> modal.close());

        VBox form = new VBox(12);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: white;");
        form.setPrefWidth(420);

        Label heading = new Label("Nouvelle Réponse");
        heading.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#142341;");
        heading.setMaxWidth(Double.MAX_VALUE);
        heading.setAlignment(Pos.CENTER);

        Label lblTexte = new Label("Texte de la réponse");
        lblTexte.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#444;");

        HBox actions = new HBox(10, btnSave, btnCancel);
        HBox.setHgrow(btnSave,   Priority.ALWAYS);
        HBox.setHgrow(btnCancel, Priority.ALWAYS);

        form.getChildren().addAll(
                heading,
                new VBox(4, lblTexte, fTexte),
                fCorrecte,
                actions
        );

        modal.setScene(new Scene(form, 420, 260));
        modal.showAndWait();
    }

    // ── Modal Modifier ────────────────────────────────────────────────────────
    private void ouvrirModalModifier(Reponse reponse) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Modifier la Réponse");

        TextField fTexte = champTexte(reponse.getTexte());
        CheckBox fCorrecte = new CheckBox("Cette réponse est correcte");
        fCorrecte.setSelected(reponse.isEstCorrecte());
        fCorrecte.setStyle("-fx-font-size: 13px;");

        Button btnSave   = creerBouton("Enregistrer", "#142341");
        Button btnCancel = creerBouton("Annuler",     "#888888");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setMaxWidth(Double.MAX_VALUE);

        btnSave.setOnAction(e -> {
            String texte = fTexte.getText().trim();

            // ❌ Texte vide
            if (texte.isEmpty()) {
                afficherMessage("Le texte est obligatoire.", false);
                return;
            }

            // ❌ Texte trop court
            if (texte.length() < 3) {
                afficherMessage("Le texte doit contenir au moins 3 caractères.", false);
                return;
            }

            // ❌ Vérifier doublon (sauf lui-même)
            boolean existe = masterList.stream()
                    .anyMatch(r -> r.getTexte().equalsIgnoreCase(texte)
                            && r.getId() != reponse.getId());

            if (existe) {
                afficherMessage("Une autre réponse avec ce texte existe déjà.", false);
                return;
            }

            try {
                reponse.setTexte(texte);
                reponse.setEstCorrecte(fCorrecte.isSelected());
                reponse.setQuiz(quizCourant);

                service.modifier(reponse);
                chargerDonnees();
                tableReponse.refresh();

                afficherMessage("Réponse modifiée avec succès !", true);
                modal.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                afficherMessage("Erreur lors de la modification.", false);
            }
        });

        btnCancel.setOnAction(e -> modal.close());

        VBox form = new VBox(12);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: white;");
        form.setPrefWidth(420);

        Label heading = new Label("Modifier la Réponse");
        heading.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#142341;");
        heading.setMaxWidth(Double.MAX_VALUE);
        heading.setAlignment(Pos.CENTER);

        Label lblTexte = new Label("Texte de la réponse");
        lblTexte.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#444;");

        HBox actions = new HBox(10, btnSave, btnCancel);
        HBox.setHgrow(btnSave,   Priority.ALWAYS);
        HBox.setHgrow(btnCancel, Priority.ALWAYS);

        form.getChildren().addAll(
                heading,
                new VBox(4, lblTexte, fTexte),
                fCorrecte,
                actions
        );

        modal.setScene(new Scene(form, 420, 260));
        modal.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Button creerBouton(String texte, String couleur) {
        Button b = new Button(texte);
        b.setStyle(
                "-fx-background-color:" + couleur + "; -fx-text-fill:white;" +
                        "-fx-background-radius:6; -fx-font-size:12px;" +
                        "-fx-padding:6 12; -fx-border-width:0; -fx-cursor:hand;"
        );
        return b;
    }

    private TextField champTexte(String texte) {
        TextField tf = new TextField(texte);
        tf.setStyle(
                "-fx-background-radius:6; -fx-border-color:#ccc;" +
                        "-fx-border-radius:6; -fx-font-size:13px; -fx-padding:7 10;"
        );
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private void afficherMessage(String msg, boolean succes) {
        messageLabel.setText(msg);
        messageLabel.setStyle(succes
                ? "-fx-text-fill:#3b6d11; -fx-font-size:13px; -fx-font-weight:bold;"
                : "-fx-text-fill:#a32d2d; -fx-font-size:13px; -fx-font-weight:bold;"
        );
        messageLabel.setVisible(true);
    }

    private void afficherInfo(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.showAndWait();
    }
}

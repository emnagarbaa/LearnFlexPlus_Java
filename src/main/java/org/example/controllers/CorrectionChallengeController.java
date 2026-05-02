package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.Services.ServiceReponseChallenge;
import org.example.entities.ReponseChallenge;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class CorrectionChallengeController implements Initializable {

    @FXML private TableView<ReponseChallenge>              table;
    @FXML private TableColumn<ReponseChallenge, String>    colId;
    @FXML private TableColumn<ReponseChallenge, String>    colUser;
    @FXML private TableColumn<ReponseChallenge, String>    colChallenge;
    @FXML private TableColumn<ReponseChallenge, String>    colDate;
    @FXML private TableColumn<ReponseChallenge, String>    colStatut;

    @FXML private TextArea   taReponse;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Spinner<Double>  spinnerNote;
    @FXML private TextArea   taCommentaire;
    @FXML private Button     btnValider;
    @FXML private Label      lblInfo;

    private final ServiceReponseChallenge service = new ServiceReponseChallenge();
    private ReponseChallenge selected;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupForm();
        chargerReponses();
    }

    private void setupTable() {
        colId.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colUser.setCellValueFactory(d ->
                new SimpleStringProperty("Utilisateur " + d.getValue().getUserId()));
        colChallenge.setCellValueFactory(d ->
                new SimpleStringProperty("Challenge #" + d.getValue().getChallengeId()));
        colDate.setCellValueFactory(d -> {
            var ts = d.getValue().getDateSoumission();
            if (ts == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH).format(ts));
        });
        colStatut.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatut()));

        // Coloriser le statut
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(switch (item) {
                    case "Validé"     -> "-fx-text-fill:#1D9E75; -fx-font-weight:bold;";
                    case "Rejeté"     -> "-fx-text-fill:#A32D2D; -fx-font-weight:bold;";
                    default           -> "-fx-text-fill:#BA7517; -fx-font-weight:bold;";
                });
            }
        });

        // Sélection → remplir le formulaire
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) remplirFormulaire(val);
        });
    }

    private void setupForm() {
        cbStatut.setItems(FXCollections.observableArrayList("En attente", "Validé", "Rejeté"));
        cbStatut.setValue("En attente");

        SpinnerValueFactory<Double> factory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 20, 0, 0.5);
        spinnerNote.setValueFactory(factory);
        spinnerNote.setEditable(true);

        setFormDisabled(true);
    }

    private void chargerReponses() {
        try {
            List<ReponseChallenge> list = service.getEnAttente();
            table.setItems(FXCollections.observableArrayList(list));
            lblInfo.setText(list.size() + " réponse(s) en attente");
        } catch (SQLException e) {
            showAlert("Erreur chargement : " + e.getMessage());
        }
    }

    private void remplirFormulaire(ReponseChallenge r) {
        selected = r;
        taReponse.setText(r.getReponseTexte());
        cbStatut.setValue(r.getStatut());
        spinnerNote.getValueFactory().setValue(r.getNote() != null ? r.getNote().doubleValue() : 0.0);
        taCommentaire.setText(r.getCommentaire() != null ? r.getCommentaire() : "");
        setFormDisabled(false);
    }

    @FXML
    private void sauvegarderCorrection() {
        if (selected == null) return;
        try {
            String statut      = cbStatut.getValue();
            Float  note        = spinnerNote.getValue().floatValue();
            String commentaire = taCommentaire.getText().trim();

            service.corriger(selected.getId(), statut, note, commentaire);

            lblInfo.setText("✅ Correction enregistrée pour l'utilisateur " + selected.getUserId());
            chargerReponses();
            viderFormulaire();
        } catch (SQLException e) {
            showAlert("Erreur sauvegarde : " + e.getMessage());
        }
    }

    @FXML
    private void voirToutes() {
        try {
            // Charger toutes les réponses (pas seulement "En attente")
            // On réutilise getEnAttente() + un filtre "tout" à ajouter dans le service si besoin
            lblInfo.setText("Affichage de toutes les réponses");
        } catch (Exception e) {
            showAlert(e.getMessage());
        }
    }

    private void viderFormulaire() {
        selected = null;
        taReponse.clear();
        cbStatut.setValue("En attente");
        spinnerNote.getValueFactory().setValue(0.0);
        taCommentaire.clear();
        setFormDisabled(true);
        table.getSelectionModel().clearSelection();
    }

    private void setFormDisabled(boolean disabled) {
        cbStatut.setDisable(disabled);
        spinnerNote.setDisable(disabled);
        taCommentaire.setDisable(disabled);
        btnValider.setDisable(disabled);
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}

package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Services.ServiceChallenge;
import org.example.entities.Challenge;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class AjouterChallengeController implements Initializable {

    @FXML private TextField        fieldTitre;
    @FXML private TextArea         fieldDescription;
    @FXML private ComboBox<String> cbNiveau;
    @FXML private ComboBox<String> cbEtat;
    @FXML private TextField        fieldObjectif;
    @FXML private TextField        fieldProgression;
    @FXML private TextField        fieldTypeRecompense;
    @FXML private TextField        fieldContenuRecompense;
    @FXML private TextField        fieldExamenId;
    @FXML private Label            lblErreur;

    private Challenge challengeAModifier = null;
    private Consumer<Challenge> onSuccessCallback;
    private final ServiceChallenge serviceChallenge = new ServiceChallenge();

    public void setOnSuccessCallback(Consumer<Challenge> callback) {
        this.onSuccessCallback = callback;
    }

    public void setChallenge(Challenge c) {
        this.challengeAModifier = c;
        prefillFields();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbNiveau.getItems().addAll("Facile", "Moyen", "Difficile");
        cbEtat.getItems().addAll("Actif", "En attente", "Termine");
    }

    private void prefillFields() {
        if (challengeAModifier == null) return;
        fieldTitre.setText(nvl(challengeAModifier.getTitrec()));
        fieldDescription.setText(nvl(challengeAModifier.getDescriptionc()));
        if (challengeAModifier.getNiveaudifficulte() != null)
            cbNiveau.setValue(challengeAModifier.getNiveaudifficulte());
        if (challengeAModifier.getEtat() != null)
            cbEtat.setValue(challengeAModifier.getEtat());
        fieldObjectif.setText(String.valueOf(challengeAModifier.getObjectifscore()));
        fieldProgression.setText(String.valueOf(challengeAModifier.getProgressionactuelle()));
        fieldTypeRecompense.setText(nvl(challengeAModifier.getTyperecomponse()));
        fieldContenuRecompense.setText(nvl(challengeAModifier.getContenurecompense()));
        fieldExamenId.setText(String.valueOf(challengeAModifier.getExamen_id()));
    }

    private String nvl(String s) { return s != null ? s : ""; }

    @FXML
    private void handleEnregistrer() {
        lblErreur.setText("");

        if (fieldTitre.getText().trim().isEmpty()) {
            lblErreur.setText("Le titre est obligatoire."); return;
        }
        if (cbNiveau.getValue() == null) {
            lblErreur.setText("Veuillez choisir un niveau."); return;
        }
        if (cbEtat.getValue() == null) {
            lblErreur.setText("Veuillez choisir un état."); return;
        }

        double objectif, progression;
        int examenId;

        try { objectif = Double.parseDouble(fieldObjectif.getText().trim()); }
        catch (NumberFormatException e) { lblErreur.setText("L'objectif doit être un nombre."); return; }

        try { progression = Double.parseDouble(fieldProgression.getText().trim()); }
        catch (NumberFormatException e) { lblErreur.setText("La progression doit être un nombre."); return; }

        try { examenId = Integer.parseInt(fieldExamenId.getText().trim()); }
        catch (NumberFormatException e) { lblErreur.setText("L'ID examen doit être un entier."); return; }

        Challenge c = (challengeAModifier != null) ? challengeAModifier : new Challenge();
        c.setTitrec(fieldTitre.getText().trim());
        c.setDescriptionc(fieldDescription.getText().trim());
        c.setNiveaudifficulte(cbNiveau.getValue());
        c.setEtat(cbEtat.getValue());
        c.setObjectifscore(objectif);
        c.setProgressionactuelle(progression);
        c.setTyperecomponse(fieldTypeRecompense.getText().trim());
        c.setContenurecompense(fieldContenuRecompense.getText().trim());
        c.setExamen_id(examenId);
        c.setNiveauatteint(challengeAModifier != null ? challengeAModifier.getNiveauatteint() : "Débutant");
        c.setAlerte(challengeAModifier != null && challengeAModifier.isAlerte());
        c.setDernier_score(challengeAModifier != null ? challengeAModifier.getDernier_score() : 0);
        c.setDernier_niveau(challengeAModifier != null ? challengeAModifier.getDernier_niveau() : "Débutant");
        c.setReponses(challengeAModifier != null ? challengeAModifier.getReponses() : "[]");

        try {
            if (challengeAModifier != null) {
                serviceChallenge.modifier(c);
            } else {
                serviceChallenge.ajouter(c);
            }
            if (onSuccessCallback != null) onSuccessCallback.accept(c);
            ((Stage) fieldTitre.getScene().getWindow()).close();
        } catch (SQLException ex) {
            lblErreur.setText("Erreur BDD : " + ex.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        ((Stage) fieldTitre.getScene().getWindow()).close();
    }
}
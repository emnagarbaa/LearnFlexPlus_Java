package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Services.ServiceExamen;
import org.example.entities.Examen;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class AjouterExamenController implements Initializable {

    @FXML private TextField        fieldTitre;
    @FXML private TextArea         fieldDescription;
    @FXML private TextField        fieldMatiere;
    @FXML private ComboBox<String> cbNiveau;
    @FXML private DatePicker       dpDebut;
    @FXML private DatePicker       dpFin;
    @FXML private TextField        fieldDuree;
    @FXML private TextField        fieldNbQuestions;
    @FXML private TextField        fieldScoreTotal;
    @FXML private TextField        fieldCoefficient;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<String> cbEtat;
    @FXML private TextField        fieldPdf;
    @FXML private Label            lblErreur;

    // null  = mode AJOUTER  |  non-null = mode MODIFIER
    private Examen examenAModifier = null;

    private Consumer<Examen> onSuccessCallback;
    private final ServiceExamen serviceExamen = new ServiceExamen();

    public void setOnSuccessCallback(Consumer<Examen> callback) {
        this.onSuccessCallback = callback;
    }

    // ── Appelé depuis ExamenController pour le mode MODIFIER ─
    public void setExamen(Examen examen) {
        this.examenAModifier = examen;
        prefillFields();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbNiveau.getItems().addAll("facile", "moyen", "difficile");
        cbType.getItems().addAll("QCM", "Redaction", "QCM+Redac.", "Oral", "Pratique");
        cbEtat.getItems().addAll("Actif", "En attente", "Termine");
    }

    // ── Pré-remplissage des champs (mode MODIFIER) ────────────
    private void prefillFields() {
        if (examenAModifier == null) return;

        fieldTitre.setText(nvl(examenAModifier.getTitre()));
        fieldDescription.setText(nvl(examenAModifier.getDescription()));
        fieldMatiere.setText(nvl(examenAModifier.getMatiere()));

        if (examenAModifier.getNiveauexamen() != null)
            cbNiveau.setValue(examenAModifier.getNiveauexamen());
        if (examenAModifier.getDatedebut() != null)
            dpDebut.setValue(examenAModifier.getDatedebut().toLocalDate());
        if (examenAModifier.getDatefin() != null)
            dpFin.setValue(examenAModifier.getDatefin().toLocalDate());

        fieldDuree.setText(String.valueOf(examenAModifier.getDuree()));
        fieldNbQuestions.setText(String.valueOf(examenAModifier.getNbquestion()));
        fieldScoreTotal.setText(String.valueOf(examenAModifier.getScoretotal()));
        fieldCoefficient.setText(String.valueOf(examenAModifier.getCoefficient()));

        if (examenAModifier.getTypeexamen() != null)
            cbType.setValue(examenAModifier.getTypeexamen());
        if (examenAModifier.getEtat() != null)
            cbEtat.setValue(examenAModifier.getEtat());

        fieldPdf.setText(nvl(examenAModifier.getPdf()));
    }

    private String nvl(String s) { return s != null ? s : ""; }

    // ── Enregistrer : INSERT ou UPDATE selon le mode ──────────
    @FXML
    private void handleEnregistrer() {
        lblErreur.setText("");

        // ── Validation ────────────────────────────────────────
        if (fieldTitre.getText().trim().isEmpty()) {
            lblErreur.setText("Le titre est obligatoire."); return;
        }
        if (fieldMatiere.getText().trim().isEmpty()) {
            lblErreur.setText("La matiere est obligatoire."); return;
        }
        if (cbNiveau.getValue() == null) {
            lblErreur.setText("Veuillez choisir un niveau."); return;
        }
        if (dpDebut.getValue() == null || dpFin.getValue() == null) {
            lblErreur.setText("Les dates sont obligatoires."); return;
        }
        if (dpFin.getValue().isBefore(dpDebut.getValue())) {
            lblErreur.setText("La date de fin doit etre apres la date de debut."); return;
        }
        if (cbType.getValue() == null) {
            lblErreur.setText("Veuillez choisir un type d'examen."); return;
        }
        if (cbEtat.getValue() == null) {
            lblErreur.setText("Veuillez choisir un etat."); return;
        }

        int duree, nbQuestions;
        double scoreTotal, coefficient;

        try { duree = Integer.parseInt(fieldDuree.getText().trim()); }
        catch (NumberFormatException e) { lblErreur.setText("La duree doit etre un entier."); return; }

        try { nbQuestions = Integer.parseInt(fieldNbQuestions.getText().trim()); }
        catch (NumberFormatException e) { lblErreur.setText("Nb questions doit etre un entier."); return; }

        try { scoreTotal = Double.parseDouble(fieldScoreTotal.getText().trim()); }
        catch (NumberFormatException e) { lblErreur.setText("Le score total doit etre un nombre."); return; }

        try {
            String c = fieldCoefficient.getText().trim();
            coefficient = c.isEmpty() ? 1.0 : Double.parseDouble(c);
        } catch (NumberFormatException e) { lblErreur.setText("Le coefficient doit etre un nombre."); return; }

        // ── Construction de l'objet ───────────────────────────
        // Mode MODIFIER : on réutilise l'objet existant (conserve l'id)
        // Mode AJOUTER  : on crée un nouvel objet
        Examen examen = (examenAModifier != null) ? examenAModifier : new Examen();

        examen.setTitre(fieldTitre.getText().trim());
        examen.setDescription(fieldDescription.getText().trim());
        examen.setMatiere(fieldMatiere.getText().trim());
        examen.setNiveauexamen(cbNiveau.getValue());
        examen.setDatedebut(Date.valueOf(dpDebut.getValue()));
        examen.setDatefin(Date.valueOf(dpFin.getValue()));
        examen.setDuree(duree);
        examen.setNbquestion(nbQuestions);
        examen.setScoretotal(scoreTotal);
        examen.setCoefficient(coefficient);
        examen.setTypeexamen(cbType.getValue());
        examen.setEtat(cbEtat.getValue());
        examen.setPdf(fieldPdf.getText().trim());

        // ── Appel service ─────────────────────────────────────
        try {
            if (examenAModifier != null) serviceExamen.modifier(examen);
            else                         serviceExamen.ajouter(examen);

            if (onSuccessCallback != null) onSuccessCallback.accept(examen);
            ((Stage) fieldTitre.getScene().getWindow()).close();

        } catch (IllegalArgumentException ex) {
            lblErreur.setText(ex.getMessage());        // erreur métier venant du service
        } catch (SQLException ex) {
            lblErreur.setText("Erreur BDD : " + ex.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        ((Stage) fieldTitre.getScene().getWindow()).close();
    }
}
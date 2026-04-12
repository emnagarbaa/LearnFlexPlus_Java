package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Services.ServiceExamen;
import org.example.entities.Examen;

import java.io.File;
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
    @FXML private Label            lblPdfNom;

    private Examen examenAModifier = null;
    private Consumer<Examen> onSuccessCallback;
    private final ServiceExamen serviceExamen = new ServiceExamen();

    public void setOnSuccessCallback(Consumer<Examen> callback) {
        this.onSuccessCallback = callback;
    }

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
        if (examenAModifier.getPdf() != null && !examenAModifier.getPdf().isBlank()) {
            fieldPdf.setText(examenAModifier.getPdf());
            File f = new File(examenAModifier.getPdf());
            lblPdfNom.setText("📄 " + f.getName());
        }
    }

    private String nvl(String s) { return s != null ? s : ""; }

    // ── Parcourir PDF ─────────────────────────────────────────
    @FXML
    private void handleParcourirPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );

        Stage stage = (Stage) fieldTitre.getScene().getWindow();
        File fichier = fileChooser.showOpenDialog(stage);

        if (fichier != null) {
            try {
                String destDir = "src/main/resources/org/example/pdfs/";
                new File(destDir).mkdirs();
                File dest = new File(destDir + fichier.getName());
                java.nio.file.Files.copy(
                        fichier.toPath(),
                        dest.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                fieldPdf.setText(dest.getAbsolutePath());
                lblPdfNom.setText("✅ " + fichier.getName());
                lblPdfNom.setStyle("-fx-text-fill:#16a34a; -fx-font-size:11px;");
            } catch (Exception e) {
                lblPdfNom.setText("❌ Erreur : " + e.getMessage());
                lblPdfNom.setStyle("-fx-text-fill:#dc2626; -fx-font-size:11px;");
            }
        }
    }

    // ── Enregistrer ───────────────────────────────────────────
    @FXML
    private void handleEnregistrer() {
        lblErreur.setText("");

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

        try {
            if (examenAModifier != null) serviceExamen.modifier(examen);
            else                         serviceExamen.ajouter(examen);
            if (onSuccessCallback != null) onSuccessCallback.accept(examen);
            ((Stage) fieldTitre.getScene().getWindow()).close();
        } catch (IllegalArgumentException ex) {
            lblErreur.setText(ex.getMessage());
        } catch (SQLException ex) {
            lblErreur.setText("Erreur BDD : " + ex.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        ((Stage) fieldTitre.getScene().getWindow()).close();
    }
}
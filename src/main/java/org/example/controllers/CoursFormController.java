package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.example.Services.ServiceCours;
import org.example.Services.ServiceMatiere;
import org.example.entities.Cours;
import org.example.entities.Matiere;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.UUID;

public class CoursFormController {

    @FXML private Label formTitle;
    @FXML private TextField tfTitre;
    @FXML private TextArea taDesc;
    @FXML private TextField tfSection;
    @FXML private TextField tfDuree;
    @FXML private TextField tfPrix;
    @FXML private ComboBox<String> cbLangue;
    @FXML private ComboBox<Matiere> cbMatiere;
    @FXML private Label lblImageName;
    @FXML private Label lblPdfName;

    @FXML private Label errTitre;
    @FXML private Label errDesc;
    @FXML private Label errSection;
    @FXML private Label errDuree;
    @FXML private Label errPrix;
    @FXML private Label errLangue;
    @FXML private Label errMatiere;

    private Cours editingCours = null;
    //matière actuelle sélectionnée
    private Matiere currentMatiere = null;
    private String selectedImagePath = null;
    private String selectedPdfPath = null;

    private final ServiceCours serviceCours = new ServiceCours();
    private final ServiceMatiere serviceMatiere = new ServiceMatiere();
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/images/";

    // Initialise les listes de langues et de matières, et prépare le dossier d'upload
    @FXML
    public void initialize() {
        cbLangue.getItems().addAll("Français", "Anglais", "Arabe", "Espagnol");
        try {
            cbMatiere.getItems().addAll(serviceMatiere.recuperer());
        } catch (SQLException ignored) {}
        new File(UPLOAD_DIR).mkdirs();
    }

    // Configure le mode du formulaire (Ajout ou Modification) et remplit les champs si un cours est fourni
    public void setCours(Cours c, Matiere m) {
        this.editingCours = c;
        this.currentMatiere = m;
        if (m != null) cbMatiere.setValue(m);
        
        if (c != null) {
            formTitle.setText("Modifier le cours");
            tfTitre.setText(c.getTitre());
            taDesc.setText(c.getDescription());
            tfSection.setText(c.getSection());
            tfDuree.setText(c.getDureeTotale());
            tfPrix.setText(String.valueOf(c.getPrix()));
            cbLangue.setValue(c.getLangue());
            lblImageName.setText(c.getImage() != null ? c.getImage() : "Aucune image");
            lblPdfName.setText(c.getPdfFile() != null ? c.getPdfFile() : "Aucun PDF");
        }
    }

    // Ouvre un sélecteur de fichier pour choisir une image de couverture
    @FXML
    public void chooseImage() {
        FileChooser fc = new FileChooser();
        File f = fc.showOpenDialog(tfTitre.getScene().getWindow());
        if (f != null) { selectedImagePath = f.getAbsolutePath(); lblImageName.setText(f.getName()); }
    }

    // Ouvre un sélecteur de fichier pour choisir un document PDF
    @FXML
    public void choosePdf() {
        FileChooser fc = new FileChooser();
        File f = fc.showOpenDialog(tfTitre.getScene().getWindow());
        if (f != null) { selectedPdfPath = f.getAbsolutePath(); lblPdfName.setText(f.getName()); }
    }

    // Ferme la fenêtre popup actuelle sans enregistrer
    @FXML
    public void goBack() {
        // Obtenir le Stage actuel et le fermer
        javafx.stage.Stage stage = (javafx.stage.Stage) tfTitre.getScene().getWindow();
        stage.close();
    }

    // Valide les données saisies, télécharge les fichiers et enregistre le cours (Ajout ou Update)
    @FXML
    public void saveCours() {
        resetErrors();
        boolean valid = true;

        if (tfTitre.getText().trim().isEmpty()) { showError(errTitre); valid = false; }
        if (taDesc.getText().trim().isEmpty()) { showError(errDesc); valid = false; }
        if (tfSection.getText().trim().isEmpty()) { showError(errSection); valid = false; }
        if (tfDuree.getText().trim().isEmpty()) { showError(errDuree); valid = false; }
        if (cbLangue.getValue() == null) { showError(errLangue); valid = false; }
        if (cbMatiere.getValue() == null) { showError(errMatiere); valid = false; }
        
        double prix = 0;
        try {
            prix = Double.parseDouble(tfPrix.getText());
            if (prix < 0) throw new Exception();
        } catch (Exception e) { showError(errPrix); valid = false; }

        if (!valid) return;

        Cours c = (editingCours != null) ? editingCours : new Cours();
        c.setTitre(tfTitre.getText().trim());
        c.setDescription(taDesc.getText().trim());
        c.setSection(tfSection.getText().trim());
        c.setDureeTotale(tfDuree.getText().trim());
        c.setPrix(prix);
        c.setLangue(cbLangue.getValue());
        c.setMatiereId(cbMatiere.getValue().getId());

        if (selectedImagePath != null) {
            String fn = UUID.randomUUID().toString() + ".png";
            try { Files.copy(Path.of(selectedImagePath), Path.of(UPLOAD_DIR + fn), StandardCopyOption.REPLACE_EXISTING); c.setImage(fn); } catch (Exception e) {}
        }
        if (selectedPdfPath != null) {
            String fn = UUID.randomUUID().toString() + ".pdf";
            try { Files.copy(Path.of(selectedPdfPath), Path.of(UPLOAD_DIR + fn), StandardCopyOption.REPLACE_EXISTING); c.setPdfFile(fn); } catch (Exception e) {}
        }

        try {
            if (editingCours == null) serviceCours.ajouter(c);
            else serviceCours.modifier(c);
            goBack();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur DB: " + e.getMessage());
            alert.show();
        }
    }

    // Cache tous les messages d'erreur de validation
    private void resetErrors() {
        errTitre.setVisible(false); errTitre.setManaged(false);
        errDesc.setVisible(false); errDesc.setManaged(false);
        errSection.setVisible(false); errSection.setManaged(false);
        errDuree.setVisible(false); errDuree.setManaged(false);
        errPrix.setVisible(false); errPrix.setManaged(false);
        errLangue.setVisible(false); errLangue.setManaged(false);
        errMatiere.setVisible(false); errMatiere.setManaged(false);
    }

    // Affiche un label d'erreur spécifique
    private void showError(Label lbl) { lbl.setVisible(true); lbl.setManaged(true); }
}

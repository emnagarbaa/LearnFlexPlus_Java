package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Services.ServiceMatiere;
import org.example.entities.Matiere;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.UUID;

public class MatiereFormController {

    @FXML private Label formTitle;
    @FXML private TextField tfNom;
    @FXML private TextArea taDesc;
    @FXML private TextField tfSection;
    @FXML private TextField tfCode;
    @FXML private ComboBox<String> cbNiveau;
    @FXML private Label lblImageName;

    @FXML private Label errNom;
    @FXML private Label errDesc;
    @FXML private Label errSection;
    @FXML private Label errCode;
    @FXML private Label errNiveau;

    private Matiere editingMatiere = null;
    private String selectedImagePath = null;
    private final ServiceMatiere serviceMatiere = new ServiceMatiere();
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/images/";

    // Initialise le menu des niveaux et s'assure que le dossier d'images existe
    @FXML
    public void initialize() {
        cbNiveau.getItems().addAll("Bac 1ère année", "Bac 2ème année", "Bac 3ème année", "Licence", "Master");
        new File(UPLOAD_DIR).mkdirs();
    }

    // Remplit le formulaire si on est en mode modification d'une matière existante
    public void setMatiere(Matiere m) {
        this.editingMatiere = m;
        if (m != null) {
            formTitle.setText("Modifier la matière");
            tfNom.setText(m.getNomMatiere());
            taDesc.setText(m.getDescription());
            tfSection.setText(m.getSection());
            tfCode.setText(m.getCodeMatiere());
            cbNiveau.setValue(m.getNiveau());
            lblImageName.setText(m.getImage() != null ? m.getImage() : "Aucun fichier sélectionné");
        }
    }

    // Propose à l'utilisateur de choisir une image sur son ordinateur
    @FXML
    public void chooseImage() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(tfNom.getScene().getWindow());
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            lblImageName.setText(file.getName());
        }
    }

    // Ferme la fenêtre du formulaire
    @FXML
    public void goBack() {
        Stage stage = (Stage) tfNom.getScene().getWindow();
        stage.close();
    }

    // Vérifie les entrées, enregistre l'image et sauvegarde la matière en base de données
    @FXML
    public void saveMatiere() {
        resetErrors();
        boolean valid = true;

        if (tfNom.getText().trim().length() < 3) { showError(errNom); valid = false; }
        if (taDesc.getText().trim().isEmpty()) { showError(errDesc); valid = false; }
        if (tfSection.getText().trim().isEmpty()) { showError(errSection); valid = false; }
        if (tfCode.getText().trim().isEmpty()) { showError(errCode); valid = false; }
        if (cbNiveau.getValue() == null) { showError(errNiveau); valid = false; }

        if (!valid) return;

        Matiere m = (editingMatiere != null) ? editingMatiere : new Matiere();
        m.setNomMatiere(tfNom.getText().trim());
        m.setDescription(taDesc.getText().trim());
        m.setSection(tfSection.getText().trim());
        m.setCodeMatiere(tfCode.getText().trim());
        m.setNiveau(cbNiveau.getValue());

        if (selectedImagePath != null) {
            String fileName = UUID.randomUUID().toString() + ".png";
            try {
                Files.copy(Path.of(selectedImagePath), Path.of(UPLOAD_DIR + fileName), StandardCopyOption.REPLACE_EXISTING);
                m.setImage(fileName);
            } catch (IOException ex) { ex.printStackTrace(); }
        }

        try {
            if (editingMatiere == null) serviceMatiere.ajouter(m);
            else serviceMatiere.modifier(m);
            goBack();
        } catch (SQLException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur DB: " + ex.getMessage());
            alert.show();
        }
    }

    // Réinitialise l'affichage des alertes d'erreur
    private void resetErrors() {
        errNom.setVisible(false); errNom.setManaged(false);
        errDesc.setVisible(false); errDesc.setManaged(false);
        errSection.setVisible(false); errSection.setManaged(false);
        errCode.setVisible(false); errCode.setManaged(false);
        errNiveau.setVisible(false); errNiveau.setManaged(false);
    }

    // Affiche un texte d'erreur sous un champ spécifique
    private void showError(Label lbl) {
        lbl.setVisible(true);
        lbl.setManaged(true);
    }
}

package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entities.Organisme;
import org.example.Services.OrganismeService;
import org.example.utils.ValidationUtil;

import java.io.File;
import java.sql.SQLException;

public class AddOrganismeController {

    // ── Champs FXML ───────────────────────────────────────────────────────────
    @FXML private TextField        fNom;
    @FXML private ComboBox<String> fType;
    @FXML private TextField        fVille;
    @FXML private TextField        fEmail;
    @FXML private TextField        fTelephone;
    @FXML private TextField        fSiteWeb;
    @FXML private TextField        fFraisMin;
    @FXML private ComboBox<String> fLangue;
    @FXML private TextArea         fDescription;
    @FXML private CheckBox         fActif;
    @FXML private CheckBox         fOpportunitesStage;
    @FXML private CheckBox         fOpportunitesEmploi;
    @FXML private Label            errorLabel;

    // ── Photo ─────────────────────────────────────────────────────────────────
    @FXML private ImageView photoPreview;
    @FXML private Label     photoPlaceholder;
    @FXML private Label     photoPathLabel;
    @FXML private Button    btnSupprimerPhoto;

    // Stocke le chemin absolu du fichier sélectionné
    private String selectedPhotoPath = null;

    // ── Labels d'erreur par champ ─────────────────────────────────────────────
    @FXML private Label errNom;
    @FXML private Label errType;
    @FXML private Label errVille;
    @FXML private Label errEmail;
    @FXML private Label errTelephone;
    @FXML private Label errSiteWeb;
    @FXML private Label errFraisMin;
    @FXML private Label errDescription;

    // ── Service ───────────────────────────────────────────────────────────────
    private final OrganismeService service = new OrganismeService();
    private DashboardController    dashboardController;

    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    // ── Initialisation ────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        fType.setItems(FXCollections.observableArrayList(
                "Université", "École", "Centre de formation",
                "Entreprise", "ONG", "Autre"));
        fLangue.setItems(FXCollections.observableArrayList(
                "Arabe", "Français", "Anglais", "Bilingue", "Multilingue"));
        fActif.setSelected(true);

        // Validation en temps réel à la perte du focus
        fNom.focusedProperty().addListener((obs, was, is) -> {
            if (!is) {
                ValidationUtil.required(fNom, errNom, "Le nom est obligatoire.");
                ValidationUtil.minLength(fNom, errNom, 2, "Au moins 2 caractères.");
                ValidationUtil.maxLength(fNom, errNom, 100, "100 caractères max.");
            }
        });
        fVille.focusedProperty().addListener((obs, was, is) -> {
            if (!is) ValidationUtil.minLength(fVille, errVille, 2, "Au moins 2 caractères.");
        });
        fEmail.focusedProperty().addListener((obs, was, is) -> {
            if (!is) ValidationUtil.isEmail(fEmail, errEmail, "Format email invalide.");
        });
        fTelephone.focusedProperty().addListener((obs, was, is) -> {
            if (!is) ValidationUtil.isPhone(fTelephone, errTelephone, "Format téléphone invalide.");
        });
        fSiteWeb.focusedProperty().addListener((obs, was, is) -> {
            if (!is) ValidationUtil.isUrl(fSiteWeb, errSiteWeb, "Format URL invalide. Ex: https://...");
        });
        fFraisMin.focusedProperty().addListener((obs, was, is) -> {
            if (!is) ValidationUtil.isPositiveDouble(fFraisMin, errFraisMin, "Nombre positif requis.");
        });
    }

    // ── Photo : choisir ───────────────────────────────────────────────────────
    @FXML
    private void choisirPhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Images (PNG, JPG, GIF)", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));

        Stage stage = (Stage) fNom.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);

        if (file != null) {
            selectedPhotoPath = file.getAbsolutePath();
            afficherApercu(file);
        }
    }

    // ── Photo : supprimer ─────────────────────────────────────────────────────
    @FXML
    private void supprimerPhoto() {
        selectedPhotoPath = null;
        photoPreview.setImage(null);
        photoPlaceholder.setVisible(true);
        photoPlaceholder.setManaged(true);
        photoPathLabel.setVisible(false);
        photoPathLabel.setManaged(false);
        btnSupprimerPhoto.setVisible(false);
        btnSupprimerPhoto.setManaged(false);
    }

    // ── Affiche l'aperçu dans l'ImageView ─────────────────────────────────────
    private void afficherApercu(File file) {
        try {
            Image img = new Image(file.toURI().toString(), 252, 156, true, true);
            photoPreview.setImage(img);

            // Masquer le placeholder, afficher le chemin et le bouton supprimer
            photoPlaceholder.setVisible(false);
            photoPlaceholder.setManaged(false);

            // Afficher un chemin raccourci
            String path = file.getAbsolutePath();
            photoPathLabel.setText(path.length() > 45 ? "…" + path.substring(path.length() - 45) : path);
            photoPathLabel.setVisible(true);
            photoPathLabel.setManaged(true);

            btnSupprimerPhoto.setVisible(true);
            btnSupprimerPhoto.setManaged(true);

        } catch (Exception e) {
            System.err.println("❌ Impossible de charger l'image : " + e.getMessage());
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML
    private void goBack() {
        if (dashboardController != null) dashboardController.showOrganisme();
    }

    // ── Sauvegarde ────────────────────────────────────────────────────────────
    @FXML
    private void saveOrganisme() {
        if (!validateAll()) return;

        Organisme o = new Organisme();
        o.setNom(fNom.getText().trim());
        o.setType(fType.getValue());
        o.setVille(fVille.getText().trim());
        o.setEmail(fEmail.getText().trim());
        o.setTelephone(fTelephone.getText().trim());
        o.setSiteWeb(fSiteWeb.getText().trim());
        try   { o.setFraisMin(Double.parseDouble(fFraisMin.getText().trim().replace(",", "."))); }
        catch (NumberFormatException ex) { o.setFraisMin(0); }
        o.setLangue(fLangue.getValue());
        o.setDescription(fDescription.getText().trim());
        o.setActif(fActif.isSelected());
        o.setOpportunitesStage(fOpportunitesStage.isSelected());
        o.setOpportunitesEmploi(fOpportunitesEmploi.isSelected());

        // ← chemin de la photo (null si aucune sélectionnée)
        o.setPhoto(selectedPhotoPath);

        try {
            service.create(o);
            if (dashboardController != null) dashboardController.showOrganisme();
        } catch (SQLException e) {
            ValidationUtil.showFieldError(errorLabel,
                    "Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    // ── Validation complète au submit ─────────────────────────────────────────
    private boolean validateAll() {
        boolean ok = true;

        ok &= ValidationUtil.required(fNom, errNom, "Le nom de l'organisme est obligatoire.");
        ok &= ValidationUtil.required(fType, errType, "Veuillez sélectionner un type d'organisme.");
        ok &= ValidationUtil.minLength(fNom, errNom, 2, "Le nom doit contenir au moins 2 caractères.");
        ok &= ValidationUtil.maxLength(fNom, errNom, 100, "Le nom ne peut pas dépasser 100 caractères.");
        ok &= ValidationUtil.minLength(fVille, errVille, 2, "La ville doit contenir au moins 2 caractères.");

        ok &= ValidationUtil.minLength(
                fDescription.getText() != null
                        ? new javafx.scene.control.TextField(fDescription.getText())
                        : new javafx.scene.control.TextField(""),
                errDescription, 10, "La description doit contenir au moins 10 caractères.");

        ok &= ValidationUtil.isPositiveDouble(fFraisMin, errFraisMin,
                "Les frais doivent être un nombre positif. Ex: 150.00");
        ok &= ValidationUtil.isEmail(fEmail, errEmail, "Format email invalide. Ex: nom@domaine.com");
        ok &= ValidationUtil.isPhone(fTelephone, errTelephone,
                "Format téléphone invalide. Ex: +216 22 345 678");
        ok &= ValidationUtil.isUrl(fSiteWeb, errSiteWeb,
                "Format URL invalide. Ex: https://exemple.com");

        if (!ok) {
            ValidationUtil.showFieldError(errorLabel, "Veuillez corriger les erreurs avant d'enregistrer.");
        } else {
            ValidationUtil.hideFieldError(errorLabel);
        }
        return ok;
    }
}
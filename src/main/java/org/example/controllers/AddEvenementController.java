package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.entities.Evenement;
import org.example.entities.Organisme;
import org.example.Services.EvenementService;
import org.example.Services.OrganismeService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddEvenementController {

    // ── Champs FXML ───────────────────────────────────────────────────────────
    @FXML private TextField        fTitre;
    @FXML private TextField        fLieu;
    @FXML private ComboBox<String> fMode;
    @FXML private TextField        fCapaciteMax;
    @FXML private TextField        fDateDebut;
    @FXML private TextField        fDateFin;
    @FXML private TextField        fPublicCible;
    @FXML private ComboBox<String> fOrganisme;
    @FXML private TextField        fContactEmail;
    @FXML private TextField        fContactTelephone;
    @FXML private TextField        fLienInscription;
    @FXML private TextArea         fDescription;
    @FXML private CheckBox         fActif;
    @FXML private CheckBox         fInscriptionRequise;
    @FXML private CheckBox         fGratuit;
    @FXML private Label            errorLabel;


    // ── Labels d'erreur par champ (à ajouter dans le FXML) ───────────────────
    @FXML private Label errTitre;
    @FXML private Label errMode;
    @FXML private Label errCapaciteMax;
    @FXML private Label errDateDebut;
    @FXML private Label errDateFin;
    @FXML private Label errContactEmail;
    @FXML private Label errContactTelephone;
    @FXML private Label errLienInscription;
    @FXML private Label errDescription;

    // ── Services ──────────────────────────────────────────────────────────────
    private final EvenementService service    = new EvenementService();
    private final OrganismeService orgService = new OrganismeService();
    private DashboardController    dashboardController;

    private static final DateTimeFormatter FMT     = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String            DATE_PAT = "yyyy-MM-dd HH:mm";

    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    // ── Initialisation ────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        fMode.setItems(FXCollections.observableArrayList(
                "Présentiel", "En ligne", "Hybride"));

        try {
            ObservableList<String> orgNames = FXCollections.observableArrayList();
            orgNames.add("-- Aucun --");
            orgService.findAll().forEach(o -> orgNames.add(o.getId() + " - " + o.getNom()));
            fOrganisme.setItems(orgNames);
            fOrganisme.setValue("-- Aucun --");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Validation en temps réel à la perte du focus
        fTitre.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ValidationUtil.required(fTitre, errTitre, "Le titre est obligatoire.");
        });
        fCapaciteMax.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ValidationUtil.isPositiveInteger(fCapaciteMax, errCapaciteMax,
                    "La capacité doit être un nombre entier positif.");
        });
        fDateDebut.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ValidationUtil.isDateTime(fDateDebut, errDateDebut,
                    DATE_PAT, "Format attendu : yyyy-MM-dd HH:mm");
        });
        fDateFin.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                ValidationUtil.isDateTime(fDateFin, errDateFin,
                        DATE_PAT, "Format attendu : yyyy-MM-dd HH:mm");
                ValidationUtil.isDateBeforeOrEqual(fDateDebut, fDateFin, errDateFin,
                        DATE_PAT, "La date de fin doit être après la date de début.");
            }
        });
        fContactEmail.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ValidationUtil.isEmail(fContactEmail, errContactEmail,
                    "Format email invalide. Ex: nom@domaine.com");
        });
        fContactTelephone.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ValidationUtil.isPhone(fContactTelephone, errContactTelephone,
                    "Format téléphone invalide. Ex: +216 22 345 678");
        });
        fLienInscription.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ValidationUtil.isUrl(fLienInscription, errLienInscription,
                    "Format URL invalide. Ex: https://exemple.com");
        });
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML
    private void goBack() {
        if (dashboardController != null) dashboardController.showEvenement();
    }

    // ── Sauvegarde ────────────────────────────────────────────────────────────
    @FXML
    private void saveEvenement() {
        if (!validateAll()) return;

        Evenement e = new Evenement();
        e.setTitre(fTitre.getText().trim());
        e.setLieu(fLieu.getText().trim());
        e.setMode(fMode.getValue());
        e.setPublicCible(fPublicCible.getText().trim());
        e.setContactEmail(fContactEmail.getText().trim());
        e.setContactTelephone(fContactTelephone.getText().trim());
        e.setLienInscription(fLienInscription.getText().trim());
        e.setDescription(fDescription.getText().trim());

        try   { e.setCapaciteMax(Integer.parseInt(fCapaciteMax.getText().trim())); }
        catch (NumberFormatException ex) { e.setCapaciteMax(0); }

        try   { e.setDateDebut(LocalDateTime.parse(fDateDebut.getText().trim(), FMT)); }
        catch (DateTimeParseException ex) { e.setDateDebut(null); }

        try   { e.setDateFin(LocalDateTime.parse(fDateFin.getText().trim(), FMT)); }
        catch (DateTimeParseException ex) { e.setDateFin(null); }

        String orgVal = fOrganisme.getValue();
        if (orgVal != null && !orgVal.startsWith("--")) {
            try {
                int orgId = Integer.parseInt(orgVal.split(" - ")[0].trim());
                Organisme org = new Organisme();
                org.setId(orgId);
                e.setOrganisme(org);
            } catch (Exception ex) { e.setOrganisme(null); }
        } else {
            e.setOrganisme(null);
        }

        try {
            service.create(e);
            if (dashboardController != null) dashboardController.showEvenement();
        } catch (SQLException ex) {
            ValidationUtil.showFieldError(errorLabel,
                    "Erreur lors de la sauvegarde : " + ex.getMessage());
        }
    }

    // ── Validation complète au submit ─────────────────────────────────────────
    private boolean validateAll() {
        boolean ok = true;

        // Champs obligatoires
        ok &= ValidationUtil.required(fTitre, errTitre,
                "Le titre est obligatoire.");
        ok &= ValidationUtil.required(fMode, errMode,
                "Veuillez sélectionner un mode.");

        // Longueur titre
        ok &= ValidationUtil.minLength(fTitre, errTitre, 3,
                "Le titre doit contenir au moins 3 caractères.");
        ok &= ValidationUtil.maxLength(fTitre, errTitre, 100,
                "Le titre ne peut pas dépasser 100 caractères.");

        // Description
        ok &= ValidationUtil.minLength(fDescription.getText() != null ?
                        new javafx.scene.control.TextField(fDescription.getText()) :
                        new javafx.scene.control.TextField(""), errDescription, 10,
                "La description doit contenir au moins 10 caractères.");

        // Capacité
        ok &= ValidationUtil.isPositiveInteger(fCapaciteMax, errCapaciteMax,
                "La capacité doit être un nombre entier positif.");

        // Dates
        ok &= ValidationUtil.isDateTime(fDateDebut, errDateDebut,
                DATE_PAT, "Format attendu : yyyy-MM-dd HH:mm");
        ok &= ValidationUtil.isDateTime(fDateFin, errDateFin,
                DATE_PAT, "Format attendu : yyyy-MM-dd HH:mm");
        ok &= ValidationUtil.isDateBeforeOrEqual(fDateDebut, fDateFin, errDateFin,
                DATE_PAT, "La date de fin doit être après la date de début.");

        // Contact
        ok &= ValidationUtil.isEmail(fContactEmail, errContactEmail,
                "Format email invalide. Ex: nom@domaine.com");
        ok &= ValidationUtil.isPhone(fContactTelephone, errContactTelephone,
                "Format téléphone invalide. Ex: +216 22 345 678");
        ok &= ValidationUtil.isUrl(fLienInscription, errLienInscription,
                "Format URL invalide. Ex: https://exemple.com");

        if (!ok) {
            ValidationUtil.showFieldError(errorLabel,
                    "Veuillez corriger les erreurs avant d'enregistrer.");
        } else {
            ValidationUtil.hideFieldError(errorLabel);
        }

        return ok;
    }
}
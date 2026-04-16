package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.entities.Evenement;

import java.time.format.DateTimeFormatter;

public class EvenementDetailController {

    @FXML private Label  heroTitre;
    @FXML private Label  lblDescription;
    @FXML private Label  lblDateDebut;
    @FXML private Label  lblDateFin;
    @FXML private Label  lblLieu;
    @FXML private Label  lblMode;
    @FXML private Label  lblCapacite;
    @FXML private Label  lblPublicCible;
    @FXML private Label  lblLienInscription;
    @FXML private Label  lblEmail;
    @FXML private Label  lblTelephone;
    @FXML private Label  lblOrganisme;
    @FXML private Label  sideDate;
    @FXML private Label  sideHeure;
    @FXML private Label  sideLieu;
    @FXML private Label  sideCapacite;
    @FXML private Button btnInscrire;

    private Evenement evenement;

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY    = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_ONLY    = DateTimeFormatter.ofPattern("HH:mm");

    // Appelé depuis OrientationFrontController pour passer l'événement
    public void setEvenement(Evenement e) {
        this.evenement = e;
        fillPage(e);
    }

    private void fillPage(Evenement e) {
        // Hero
        heroTitre.setText(e.getTitre() != null ? e.getTitre() : "");

        // Description
        lblDescription.setText(e.getDescription() != null ? e.getDescription() : "Aucune description disponible.");

        // Dates
        lblDateDebut.setText(e.getDateDebut() != null ? e.getDateDebut().format(DATE_FMT) : "-");
        lblDateFin.setText(e.getDateFin()     != null ? e.getDateFin().format(DATE_FMT)   : "-");

        // Lieu
        lblLieu.setText("Lieu : " + (e.getLieu() != null ? e.getLieu() : "-"));

        // Mode + Capacité
        lblMode.setText(e.getMode() != null ? e.getMode() : "-");
        lblCapacite.setText(e.getCapaciteMax() + " places");

        // Public cible
        lblPublicCible.setText("Public cible : " + (e.getPublicCible() != null ? e.getPublicCible() : "-"));

        // Lien inscription
        String lien = e.getLienInscription();
        if (lien != null && !lien.isBlank()) {
            lblLienInscription.setText("Formulaire d'inscription : " + lien);
            btnInscrire.setDisable(false);
        } else {
            lblLienInscription.setText("Lien d'inscription non disponible.");
            btnInscrire.setDisable(true);
            btnInscrire.setText("Lien d'inscription indisponible");
            btnInscrire.setStyle(btnInscrire.getStyle() +
                    "-fx-background-color:#ccc; -fx-cursor:default;");
        }

        // Contact
        lblEmail.setText("✉  Email : " + (e.getContactEmail() != null ? e.getContactEmail() : "-"));
        lblTelephone.setText("☎  Téléphone : " + (e.getContactTelephone() != null ? e.getContactTelephone() : "-"));

        // Organisme
        lblOrganisme.setText(e.getOrganisme() != null && e.getOrganisme().getNom() != null
                ? e.getOrganisme().getNom() : "Organisme non spécifié");

        // Sidebar
        sideDate.setText(e.getDateDebut()     != null ? e.getDateDebut().format(DATE_ONLY) : "-");
        sideHeure.setText(e.getDateDebut()    != null ? e.getDateDebut().format(TIME_ONLY) : "-");
        sideLieu.setText(e.getLieu()          != null ? e.getLieu() : "-");
        sideCapacite.setText(String.valueOf(e.getCapaciteMax()));
    }

    @FXML
    private void goBack() {
        // Ferme la fenêtre détail
        Stage stage = (Stage) heroTitre.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void inscrire() {
        if (evenement != null && evenement.getLienInscription() != null) {
            // Ouvre le lien dans le navigateur
            try {
                java.awt.Desktop.getDesktop().browse(
                        new java.net.URI(evenement.getLienInscription()));
            } catch (Exception ex) {
                System.err.println("❌ Impossible d'ouvrir le lien : " + ex.getMessage());
            }
        }
    }
}
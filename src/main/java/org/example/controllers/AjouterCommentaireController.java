package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Services.EmailService;
import org.example.Services.ServiceCommentaire;
import org.example.entities.Commentaire;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class AjouterCommentaireController implements Initializable {

    @FXML private TextArea  fieldContenu;
    @FXML private TextField fieldAuteur;
    @FXML private Label     lblErreur;

    private Commentaire              commentaireAModifier = null;
    private int                      examenId;
    private String                   titreExamen          = "";
    private Consumer<Commentaire>    onSuccessCallback;
    private final ServiceCommentaire serviceCommentaire   = new ServiceCommentaire();

    public void setExamenId(int examenId) {
        this.examenId = examenId;
    }

    public void setTitreExamen(String titreExamen) {
        this.titreExamen = (titreExamen != null) ? titreExamen : "";
    }

    public void setOnSuccessCallback(Consumer<Commentaire> callback) {
        this.onSuccessCallback = callback;
    }

    public void setCommentaire(Commentaire c) {
        this.commentaireAModifier = c;
        fieldContenu.setText(c.getContenu());
        fieldAuteur.setText(c.getAuteur());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    @FXML
    private void handleEnregistrer() {
        lblErreur.setText("");

        if (fieldContenu.getText().trim().isEmpty()) {
            lblErreur.setText("Le contenu est obligatoire."); return;
        }
        if (fieldAuteur.getText().trim().isEmpty()) {
            lblErreur.setText("L'auteur est obligatoire."); return;
        }

        boolean isNew = (commentaireAModifier == null);

        Commentaire c = isNew ? new Commentaire() : commentaireAModifier;
        c.setContenu(fieldContenu.getText().trim());
        c.setAuteur(fieldAuteur.getText().trim());
        c.setDatecre(new Date(System.currentTimeMillis()));
        c.setNbvue(0);
        c.setLikes(isNew ? 0 : commentaireAModifier.getLikes());
        c.setExamen_id(examenId);

        try {
            if (!isNew) {
                serviceCommentaire.modifier(c);
            } else {
                serviceCommentaire.ajouter(c);

                // ── Email envoyé uniquement lors d'un AJOUT ──────
                new Thread(() ->
                        EmailService.envoyerFeedback(
                                "emnagarbaa200@gmail.com",
                                titreExamen
                        )
                ).start();
            }

            if (onSuccessCallback != null) onSuccessCallback.accept(c);
            ((Stage) fieldContenu.getScene().getWindow()).close();

        } catch (IllegalArgumentException ex) {
            lblErreur.setText(ex.getMessage());
        } catch (SQLException ex) {
            lblErreur.setText("Erreur BDD : " + ex.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        ((Stage) fieldContenu.getScene().getWindow()).close();
    }
}
package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Services.ServiceChallenge;
import org.example.entities.Challenge;
import org.example.utils.MyDatabase;

import java.net.URL;
import java.sql.*;
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
    @FXML private ComboBox<String> cbExamenId;   // ← remplacé
    @FXML private Label            lblErreur;

    private Challenge challengeAModifier = null;
    private Consumer<Challenge> onSuccessCallback;
    private final ServiceChallenge serviceChallenge = new ServiceChallenge();

    public void setOnSuccessCallback(Consumer<Challenge> callback) {
        this.onSuccessCallback = callback;
    }

    public void setChallenge(Challenge c) {
        this.challengeAModifier = c;
        //remplir le formulaire
        prefillFields();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbNiveau.getItems().addAll("Facile", "Moyen", "Difficile");
        cbEtat.getItems().addAll("Actif", "En attente", "Termine");
        chargerExamens();
    }

    private void chargerExamens() {
        //vider la comboBox
        cbExamenId.getItems().clear();
        String sql = "SELECT id, titre FROM examen ORDER BY id";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            //Parcourir les résultats et remplir la ComboBox
            while (rs.next()) {
                int id = rs.getInt("id");
                String titre = rs.getString("titre");
                // Affiche "1 - Mathématiques" dans la ComboBox
                cbExamenId.getItems().add(id + " - " + titre);
            }

            if (cbExamenId.getItems().isEmpty()) {
                cbExamenId.getItems().add("Aucun examen disponible");
                cbExamenId.setDisable(true);
            }

        } catch (SQLException e) {
            lblErreur.setText("Erreur chargement examens : " + e.getMessage());
        }
    }
    //remplit automatiquement tous les champs
    private void prefillFields() {
        //Remplir les champs texte simples
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

        // Sélectionne automatiquement l'examen existant dans la ComboBox
        int examenId = challengeAModifier.getExamen_id();
        cbExamenId.getItems().stream()
                .filter(item -> item.startsWith(examenId + " -"))
                .findFirst()
                .ifPresent(cbExamenId::setValue);
    }
    //retourne :la chaîne telle quelle si elle n'est pas null une chaîne vide "" si elle est null
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
        if (cbExamenId.getValue() == null || cbExamenId.isDisable()) {
            lblErreur.setText("Veuillez choisir un examen."); return;
        }

        double objectif, progression;

        try { objectif = Double.parseDouble(fieldObjectif.getText().trim()); }
        catch (NumberFormatException e) { lblErreur.setText("L'objectif doit être un nombre."); return; }

        try { progression = Double.parseDouble(fieldProgression.getText().trim()); }
        catch (NumberFormatException e) { lblErreur.setText("La progression doit être un nombre."); return; }

        // Extrait juste le numéro avant le " - "
        int examenId = Integer.parseInt(cbExamenId.getValue().split(" - ")[0].trim());

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
            if (challengeAModifier != null) serviceChallenge.modifier(c);
            else                            serviceChallenge.ajouter(c);

            if (onSuccessCallback != null) onSuccessCallback.accept(c);
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

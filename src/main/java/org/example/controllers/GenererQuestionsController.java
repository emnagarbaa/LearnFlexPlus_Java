package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Services.GroqService;
import org.example.Services.ServiceChallenge;
import org.example.entities.Challenge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class GenererQuestionsController implements Initializable {

    @FXML private Label             lblTitreChallenge;
    @FXML private Spinner<Integer>  spinnerNbQuestions;
    @FXML private Button            btnGenerer;
    @FXML private Button            btnSauvegarder;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private VBox              vboxQuestions;
    @FXML private Label             lblStatut;
    @FXML private ScrollPane        scrollPane;

    private Challenge              challenge;
    private String                 jsonGenere       = null;
    private final GroqService      groqService      = new GroqService();
    private final ServiceChallenge serviceChallenge = new ServiceChallenge();

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
        lblTitreChallenge.setText("Challenge : " + challenge.getTitrec()
                + "  |  Niveau : " + challenge.getNiveaudifficulte());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SpinnerValueFactory<Integer> factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 5);
        spinnerNbQuestions.setValueFactory(factory);
        progressIndicator.setVisible(false);
        lblStatut.setText("");
        btnSauvegarder.setDisable(true);
    }

    // ── Générer ───────────────────────────────────────────────
    @FXML
    private void handleGenerer() {
        if (challenge == null) return;

        int nbQuestions = spinnerNbQuestions.getValue();

        btnGenerer.setDisable(true);
        btnSauvegarder.setDisable(true);
        progressIndicator.setVisible(true);
        vboxQuestions.getChildren().clear();
        lblStatut.setText("⏳ Génération en cours...");

        new Thread(() -> {
            try {
                String json = groqService.genererQuestionsJson(
                        challenge.getTitrec(),
                        challenge.getDescriptionc(),
                        challenge.getNiveaudifficulte(),
                        nbQuestions
                );

                Platform.runLater(() -> {
                    jsonGenere = json;
                    afficherQuestions(json);
                    btnGenerer.setDisable(false);
                    btnSauvegarder.setDisable(false);
                    progressIndicator.setVisible(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatut.setText("❌ Erreur : " + e.getMessage());
                    btnGenerer.setDisable(false);
                    progressIndicator.setVisible(false);
                });
            }
        }).start();
    }

    // ── Sauvegarder en BDD ────────────────────────────────────
    @FXML
    private void handleSauvegarder() {
        if (jsonGenere == null || jsonGenere.isEmpty()) return;
        try {
            serviceChallenge.mettreAJourQuestions(challenge.getId(), jsonGenere);
            challenge.setQuestion(jsonGenere);
            lblStatut.setText("✅ Questions sauvegardées ! Disponibles dans le front.");
            btnSauvegarder.setDisable(true);
        } catch (SQLException e) {
            lblStatut.setText("❌ Erreur sauvegarde : " + e.getMessage());
        }
    }

    // ── Affichage avec type coloré ────────────────────────────
    private void afficherQuestions(String json) {
        vboxQuestions.getChildren().clear();
        try {
            JSONObject root      = new JSONObject(json);
            JSONArray  questions = root.getJSONArray("questions");

            int qcm = 0, tf = 0, open = 0;

            for (int i = 0; i < questions.length(); i++) {
                JSONObject q    = questions.getJSONObject(i);
                String     type = q.optString("type", "open");

                // ── Couleur et label par type ─────────────────
                String bgColor, typeLabel;
                switch (type) {
                    case "qcm" -> {
                        bgColor   = "#eef6ff";
                        typeLabel = "📝 QCM";
                        qcm++;
                    }
                    case "truefalse" -> {
                        bgColor   = "#f0fdf4";
                        typeLabel = "✅ Vrai / Faux";
                        tf++;
                    }
                    default -> {
                        bgColor   = "#f8f5ff";
                        typeLabel = "💬 Question ouverte";
                        open++;
                    }
                }

                VBox card = new VBox(6);
                card.setStyle(
                        "-fx-background-color:" + bgColor + ";" +
                                "-fx-border-color:#d0d0d0;" +
                                "-fx-border-radius:8;" +
                                "-fx-background-radius:8;" +
                                "-fx-padding:12 16;"
                );

                // Numéro + type
                Label typeTag = new Label("Question " + (i + 1) + "  —  " + typeLabel);
                typeTag.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#6f42c1;");

                // Texte de la question
                Label questionLabel = new Label(q.optString("question", ""));
                questionLabel.setWrapText(true);
                questionLabel.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#212529;");

                card.getChildren().addAll(typeTag, questionLabel);

                // ── Détails selon type ────────────────────────
                if ("qcm".equals(type) && q.has("choices")) {
                    JSONArray choices = q.getJSONArray("choices");
                    for (int j = 0; j < choices.length(); j++) {
                        String choice = choices.getString(j);
                        String correct = q.optString("correct_answer", "");
                        boolean isCorrect = choice.startsWith(correct);
                        Label choiceLabel = new Label((isCorrect ? "✔ " : "   ") + choice);
                        choiceLabel.setStyle("-fx-font-size:12px; -fx-text-fill:"
                                + (isCorrect ? "#16a34a" : "#555") + ";");
                        card.getChildren().add(choiceLabel);
                    }
                }

                if ("truefalse".equals(type)) {
                    boolean correct = q.optBoolean("correct_answer", true);
                    Label rep = new Label("Réponse : " + (correct ? "✅ Vrai" : "❌ Faux"));
                    rep.setStyle("-fx-font-size:12px; -fx-text-fill:#16a34a; -fx-font-weight:bold;");
                    card.getChildren().add(rep);
                }

                if (q.has("explanation")) {
                    Label exp = new Label("💡 " + q.getString("explanation"));
                    exp.setWrapText(true);
                    exp.setStyle("-fx-font-size:11px; -fx-text-fill:#666; -fx-font-style:italic;");
                    card.getChildren().add(exp);
                }

                if ("open".equals(type) && q.has("model_answer")) {
                    Label rep = new Label("📖 Réponse modèle : " + q.getString("model_answer"));
                    rep.setWrapText(true);
                    rep.setStyle("-fx-font-size:11px; -fx-text-fill:#1f4f65;");
                    card.getChildren().add(rep);
                }

                vboxQuestions.getChildren().add(card);
            }

            // ── Résumé des types générés ──────────────────────
            lblStatut.setText("✅ " + questions.length() + " questions générées : "
                    + qcm + " QCM  |  " + tf + " Vrai/Faux  |  " + open + " Ouvertes"
                    + "  — Cliquez 💾 Sauvegarder pour les utiliser dans le front");

        } catch (Exception e) {
            lblStatut.setText("❌ Erreur parsing JSON : " + e.getMessage());
        }
    }

    @FXML
    private void handleFermer() {
        ((Stage) btnGenerer.getScene().getWindow()).close();
    }
}
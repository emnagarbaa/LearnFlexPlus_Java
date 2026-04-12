package org.example.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Services.ServiceExamen;
import org.example.entities.Challenge;
import org.example.entities.Examen;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class ChallengeUIController implements Initializable {

    @FXML private Label lblTitre, lblDescription, lblObjectif, lblScore, lblTimer, lblQuestion;
    @FXML private VBox  questionContainer;

    private Challenge challenge;
    private List<JsonNode> questions = new ArrayList<>();
    private int current    = 0;
    private int totalScore = 0;
    private int timeLeft   = 60;
    private Timeline timer;
    private boolean finished = false;

    private final ServiceExamen serviceExamen = new ServiceExamen();

    public void setChallenge(Challenge c) {
        this.challenge = c;
        lblTitre.setText(c.getTitrec());
        lblDescription.setText(nvl(c.getDescriptionc()));
        lblObjectif.setText((int) c.getObjectifscore() + "%");
        parseQuestions(c.getQuestion());
        startTimer();
        renderQuestion();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    // ── Parse JSON ────────────────────────────────────────────
    private void parseQuestions(String json) {
        if (json == null || json.isBlank()) return;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            if (root.has("questions")) {
                root.get("questions").forEach(questions::add);
            } else if (root.isArray()) {
                root.forEach(questions::add);
            }
            lblQuestion.setText("1/" + questions.size());
        } catch (Exception e) {
            System.err.println("Erreur parsing questions : " + e.getMessage());
        }
    }

    // ── Timer ─────────────────────────────────────────────────
    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            lblTimer.setText(timeLeft + "s");
            if (timeLeft <= 10)
                lblTimer.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#f87171;");
            if (timeLeft <= 0) { timer.stop(); terminer(); }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    // ── Render question ───────────────────────────────────────
    private void renderQuestion() {
        questionContainer.getChildren().clear();
        if (current >= questions.size()) { terminer(); return; }

        JsonNode q    = questions.get(current);
        String   type = q.has("type") ? q.get("type").asText() : "qcm";
        lblQuestion.setText((current + 1) + "/" + questions.size());

        switch (type) {
            case "qcm"       -> renderQCM(q);
            case "truefalse" -> renderTrueFalse(q);
            case "open"      -> renderOpen(q);
            default          -> next();
        }
    }

    // ── QCM ───────────────────────────────────────────────────
    private void renderQCM(JsonNode q) {
        questionContainer.getChildren().add(questionLabel(q.get("question").asText()));

        ToggleGroup group  = new ToggleGroup();
        List<RadioButton> radios = new ArrayList<>();

        if (q.has("choices")) {
            q.get("choices").forEach(choice -> {
                RadioButton rb = new RadioButton(choice.asText());
                rb.setToggleGroup(group);
                rb.setStyle("-fx-font-size:14px; -fx-padding:10 15; " +
                        "-fx-background-color:#f0f7f4; -fx-background-radius:10;");
                rb.setPrefWidth(600);
                radios.add(rb);
                questionContainer.getChildren().add(rb);
            });
        }

        Button btnValider = btnValider();
        Label  lblMsg     = msgLabel();
        questionContainer.getChildren().addAll(btnValider, lblMsg);

        btnValider.setOnAction(e -> {
            RadioButton sel = (RadioButton) group.getSelectedToggle();
            if (sel == null) { lblMsg.setText("⚠️ Choisis une réponse !"); return; }

            String  correct   = q.has("correct_answer") ? q.get("correct_answer").asText() : "";
            boolean isCorrect = sel.getText().startsWith(correct);

            radios.forEach(rb -> {
                if (rb.getText().startsWith(correct)) {
                    rb.setStyle("-fx-font-size:14px; -fx-padding:10 15; " +
                            "-fx-background-color:#bbf7d0; -fx-background-radius:10;");
                } else if (rb == sel && !isCorrect) {
                    rb.setStyle("-fx-font-size:14px; -fx-padding:10 15; " +
                            "-fx-background-color:#fecaca; -fx-background-radius:10;");
                }
                rb.setDisable(true);
            });

            if (isCorrect) {
                totalScore += 20;
                lblMsg.setText("✅ Bonne réponse ! +20 pts");
                lblMsg.setStyle("-fx-text-fill:#16a34a; -fx-font-size:13px; -fx-font-weight:bold;");
            } else {
                lblMsg.setText("❌ Mauvaise réponse !");
                lblMsg.setStyle("-fx-text-fill:#dc2626; -fx-font-size:13px; -fx-font-weight:bold;");
            }

            if (q.has("explanation")) {
                Label exp = new Label("💡 " + q.get("explanation").asText());
                exp.setStyle("-fx-font-size:12px; -fx-text-fill:#1f4f65; -fx-wrap-text:true;");
                exp.setWrapText(true);
                questionContainer.getChildren().add(exp);
            }

            updateScore();
            btnValider.setDisable(true);
            autoNext(1500);
        });
    }

    // ── Vrai / Faux ───────────────────────────────────────────
    private void renderTrueFalse(JsonNode q) {
        questionContainer.getChildren().add(questionLabel(q.get("question").asText()));

        Button btnVrai = new Button("✅  Vrai");
        Button btnFaux = new Button("❌  Faux");
        String s = "-fx-font-size:15px; -fx-font-weight:bold; -fx-padding:12 40; " +
                "-fx-background-radius:10; -fx-cursor:hand; ";
        btnVrai.setStyle(s + "-fx-background-color:#bbf7d0; -fx-text-fill:#15803d;");
        btnFaux.setStyle(s + "-fx-background-color:#fecaca; -fx-text-fill:#dc2626;");

        HBox btns = new HBox(20, btnVrai, btnFaux);
        btns.setAlignment(Pos.CENTER);

        Label lblMsg = msgLabel();
        questionContainer.getChildren().addAll(btns, lblMsg);

        boolean correct = q.has("correct_answer") && q.get("correct_answer").asBoolean();
        btnVrai.setOnAction(e -> handleTF(true,  correct, lblMsg, btnVrai, btnFaux, q));
        btnFaux.setOnAction(e -> handleTF(false, correct, lblMsg, btnVrai, btnFaux, q));
    }

    private void handleTF(boolean answer, boolean correct, Label lblMsg,
                          Button btnVrai, Button btnFaux, JsonNode q) {
        boolean isCorrect = (answer == correct);
        if (isCorrect) {
            totalScore += 20;
            lblMsg.setText("✅ Bonne réponse ! +20 pts");
            lblMsg.setStyle("-fx-text-fill:#16a34a; -fx-font-size:13px; -fx-font-weight:bold;");
        } else {
            lblMsg.setText("❌ Mauvaise réponse !");
            lblMsg.setStyle("-fx-text-fill:#dc2626; -fx-font-size:13px; -fx-font-weight:bold;");
        }
        if (q.has("explanation")) {
            Label exp = new Label("💡 " + q.get("explanation").asText());
            exp.setStyle("-fx-font-size:12px; -fx-text-fill:#1f4f65; -fx-wrap-text:true;");
            exp.setWrapText(true);
            questionContainer.getChildren().add(exp);
        }
        btnVrai.setDisable(true);
        btnFaux.setDisable(true);
        updateScore();
        autoNext(1800);
    }

    // ── Open ──────────────────────────────────────────────────
    private void renderOpen(JsonNode q) {
        questionContainer.getChildren().add(questionLabel(q.get("question").asText()));

        TextArea ta = new TextArea();
        ta.setPromptText("Écris ta réponse ici...");
        ta.setPrefRowCount(4);
        ta.setStyle("-fx-font-size:13px; -fx-background-radius:10; " +
                "-fx-border-color:#1f4f65; -fx-border-radius:10;");

        Button btnValider = btnValider();
        Label  lblMsg     = msgLabel();
        questionContainer.getChildren().addAll(ta, btnValider, lblMsg);

        btnValider.setOnAction(e -> {
            String answer = ta.getText().trim().toLowerCase();
            if (answer.isEmpty()) { lblMsg.setText("⚠️ Écris ta réponse !"); return; }

            List<String> keywords = new ArrayList<>();
            if (q.has("expected_keywords"))
                q.get("expected_keywords").forEach(kw -> keywords.add(kw.asText().toLowerCase()));

            long found  = keywords.stream().filter(answer::contains).count();
            int  points = keywords.isEmpty() ? 10 :
                    (int) Math.round((double) found / keywords.size() * 20);
            totalScore += points;

            lblMsg.setText("📝 +" + points + " pts — Mots-clés : " + found + "/" + keywords.size());
            lblMsg.setStyle("-fx-text-fill:#1f4f65; -fx-font-size:12px;");

            if (q.has("model_answer")) {
                Label model = new Label("✅ Réponse modèle : " + q.get("model_answer").asText());
                model.setStyle("-fx-font-size:12px; -fx-text-fill:#555; -fx-wrap-text:true;");
                model.setWrapText(true);
                questionContainer.getChildren().add(model);
            }

            updateScore();
            ta.setDisable(true);
            btnValider.setDisable(true);
            autoNext(2500);
        });
    }

    // ── Terminer ──────────────────────────────────────────────
    private void terminer() {
        if (finished) return;
        finished = true;
        if (timer != null) timer.stop();

        int    percent = Math.min(totalScore, 100);
        String niveau  = percent >= 81 ? "difficile" : percent >= 50 ? "moyen" : "facile";
        String badge   = percent >= 80 ? "🏆 Expert"   :
                percent >= 60 ? "🥇 Compétent" :
                percent >= 40 ? "🥈 Apprenti"  : "🥉 Novice";
        String couleur = percent >= (int) challenge.getObjectifscore() ? "#16a34a" : "#dc2626";

        // Chercher l'examen recommandé
        Examen examenRecommande = trouverExamen(niveau);

        questionContainer.getChildren().clear();

        VBox victory = new VBox(20);
        victory.setAlignment(Pos.CENTER);
        victory.setPadding(new Insets(30));

        Label ico   = new Label("🏆");
        ico.setStyle("-fx-font-size:60px;");

        Label titre = new Label("Challenge terminé !");
        titre.setStyle("-fx-font-size:24px; -fx-font-weight:bold; -fx-text-fill:#1a3d4f;");

        Label score = new Label("Score final : " + percent + "%");
        score.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:" + couleur + ";");

        Label bdg   = new Label(badge);
        bdg.setStyle("-fx-font-size:18px; -fx-text-fill:#1f4f65;");

        Label obj   = new Label("Objectif : " + (int) challenge.getObjectifscore() + "% — " +
                (percent >= (int) challenge.getObjectifscore() ? "✅ Atteint !" : "❌ Non atteint"));
        obj.setStyle("-fx-font-size:14px; -fx-text-fill:#555;");

        // ── Niveau recommandé ──────────────────────────────
        Label niveauLabel = new Label("📊 Niveau recommandé : " + niveau.toUpperCase());
        niveauLabel.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1f4f65; " +
                "-fx-background-color:" + niveauCouleur(niveau) + "; " +
                "-fx-background-radius:20; -fx-padding:8 20;");

        // ── Examen recommandé ──────────────────────────────
        VBox examenBox = new VBox(8);
        examenBox.setAlignment(Pos.CENTER);
        examenBox.setStyle("-fx-background-color:#f0f7f4; -fx-background-radius:14; -fx-padding:20;");

        if (examenRecommande != null) {
            Label exTitre = new Label("📝 Examen recommandé :");
            exTitre.setStyle("-fx-font-size:13px; -fx-text-fill:#555;");

            Label exNom = new Label(examenRecommande.getTitre());
            exNom.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#1a3d4f;");

            Label exNiveau = new Label("Niveau : " + examenRecommande.getNiveauexamen() +
                    " | Matière : " + nvl(examenRecommande.getMatiere()));
            exNiveau.setStyle("-fx-font-size:12px; -fx-text-fill:#666;");

            Button btnExamen = new Button("📝  Passer l'examen " + niveau);
            btnExamen.setStyle("-fx-background-color:#1D9E75; -fx-text-fill:white; " +
                    "-fx-font-size:14px; -fx-font-weight:bold; " +
                    "-fx-background-radius:10; -fx-padding:10 30; -fx-cursor:hand;");
            btnExamen.setOnAction(e -> ouvrirExamen(examenRecommande));

            examenBox.getChildren().addAll(exTitre, exNom, exNiveau, btnExamen);
        } else {
            Label noExam = new Label("ℹ️ Aucun examen " + niveau + " disponible pour le moment.");
            noExam.setStyle("-fx-font-size:13px; -fx-text-fill:#888;");
            examenBox.getChildren().add(noExam);
        }

        // ── Boutons ────────────────────────────────────────
        Button btnReessayer = new Button("🔄  Réessayer");
        btnReessayer.setStyle("-fx-background-color:#3b82f6; -fx-text-fill:white; " +
                "-fx-font-size:14px; -fx-font-weight:bold; " +
                "-fx-background-radius:10; -fx-padding:10 30; -fx-cursor:hand;");
        btnReessayer.setOnAction(e -> {
            current = 0; totalScore = 0; timeLeft = 60; finished = false;
            lblScore.setText("0");
            lblTimer.setText("60s");
            lblTimer.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#facc15;");
            startTimer();
            renderQuestion();
        });

        Button btnFermer = new Button("✖  Fermer");
        btnFermer.setStyle("-fx-background-color:#e5e7eb; -fx-text-fill:#374151; " +
                "-fx-font-size:14px; -fx-background-radius:10; -fx-padding:10 30; -fx-cursor:hand;");
        btnFermer.setOnAction(e ->
                ((Stage) questionContainer.getScene().getWindow()).close()
        );

        HBox btns = new HBox(15, btnReessayer, btnFermer);
        btns.setAlignment(Pos.CENTER);

        victory.getChildren().addAll(ico, titre, score, bdg, obj, niveauLabel, examenBox, btns);
        questionContainer.getChildren().add(victory);
    }

    // ── Trouver examen selon niveau ───────────────────────────
    private Examen trouverExamen(String niveau) {
        try {
            List<Examen> examens = serviceExamen.recuperer();
            // Cherche d'abord par niveau exact
            for (Examen ex : examens) {
                if (niveau.equalsIgnoreCase(ex.getNiveauexamen())) {
                    return ex;
                }
            }
            // Si rien trouvé, retourne le premier disponible
            return examens.isEmpty() ? null : examens.get(0);
        } catch (SQLException e) {
            System.err.println("Erreur chargement examens : " + e.getMessage());
            return null;
        }
    }

    // ── Ouvrir la page examen ─────────────────────────────────
    private void ouvrirExamen(Examen examen) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/ExamenFront.fxml"));
            Parent root = loader.load();
            ExamenFrontController ctrl = loader.getController();
            ctrl.setExamen(examen);
            Stage stage = new Stage();
            stage.setTitle("Examen : " + examen.getTitre());
            stage.setScene(new Scene(root, 900, 700));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur ouverture examen : " + e.getMessage());
        }
    }

    // ── Couleur par niveau ────────────────────────────────────
    private String niveauCouleur(String niveau) {
        return switch (niveau) {
            case "facile"    -> "#EAF3DE";
            case "moyen"     -> "#FAEEDA";
            case "difficile" -> "#FCEBEB";
            default          -> "#f0f0f0";
        };
    }

    // ── Helpers ───────────────────────────────────────────────
    private void next() {
        current++;
        Platform.runLater(this::renderQuestion);
    }

    private void autoNext(int ms) {
        new Timeline(new KeyFrame(Duration.millis(ms), e -> next())).play();
    }

    private void updateScore() {
        lblScore.setText(String.valueOf(Math.min(totalScore, 100)));
    }

    private Label questionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:17px; -fx-font-weight:bold; " +
                "-fx-text-fill:#1a3d4f; -fx-wrap-text:true;");
        l.setWrapText(true);
        l.setMaxWidth(650);
        VBox.setMargin(l, new Insets(0, 0, 15, 0));
        return l;
    }

    private Button btnValider() {
        Button b = new Button("Valider →");
        b.setStyle("-fx-background-color:#1D9E75; -fx-text-fill:white; " +
                "-fx-font-size:14px; -fx-font-weight:bold; " +
                "-fx-background-radius:10; -fx-padding:10 30; -fx-cursor:hand;");
        return b;
    }

    private Label msgLabel() {
        Label l = new Label("");
        l.setStyle("-fx-font-size:13px;");
        return l;
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
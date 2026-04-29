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
import org.example.Services.ServiceIA;
import org.example.Services.ServiceLeaderboard;
import org.example.Services.ServiceReponseChallenge;
import org.example.entities.Challenge;
import org.example.entities.Examen;
import org.example.entities.LeaderboardEntry;
import org.example.entities.ReponseChallenge;
import org.example.entities.User;
import org.example.utils.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class ChallengeUIController implements Initializable {

    @FXML private Label lblTitre, lblDescription, lblObjectif, lblScore, lblTimer, lblQuestion;
    @FXML private VBox  questionContainer;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblProgress;

    private Challenge      challenge;
    private List<JsonNode> questions  = new ArrayList<>();
    private int            current    = 0;
    private int            totalScore = 0;
    private int            timeLeft   = 60;
    private Timeline       timer;
    private boolean        finished   = false;

    // ID utilisateur connecté
    private int currentUserId = -1;

    private final ServiceExamen             serviceExamen      = new ServiceExamen();
    private final ServiceLeaderboard        serviceLeaderboard = new ServiceLeaderboard();
    private final ServiceIA                 serviceIA          = new ServiceIA();
    private final ServiceReponseChallenge   serviceReponse     = new ServiceReponseChallenge();

    // Palette couleurs
    private static final String C_PRIMARY    = "#0f3d4f";
    private static final String C_GREEN      = "#1D9E75";
    private static final String C_GREEN_LIGHT= "#bbf7d0";
    private static final String C_GREEN_TEXT = "#15803d";
    private static final String C_RED_LIGHT  = "#fecaca";
    private static final String C_RED_TEXT   = "#dc2626";
    private static final String C_BLUE_TEXT  = "#1f4f65";
    private static final String C_GRAY_BG    = "#f0f4f5";
    private static final String C_CARD_BG    = "#f8fafb";
    private static final String C_BORDER     = "#e2e8ec";
    private static final String C_TEXT_MAIN  = "#1a3d4f";
    private static final String C_TEXT_MUTED = "#6b7280";
    private static final String C_AMBER      = "#f59e0b";
    private static final String C_AMBER_LIGHT= "#fef3c7";

    // Enregistrement des réponses pour analyse
    private List<Map<String, Object>> reponsesEnregistrees = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    public void setChallenge(Challenge c) {
        this.challenge = c;
        lblTitre.setText(c.getTitrec());
        lblDescription.setText(nvl(c.getDescriptionc()));
        lblObjectif.setText((int) c.getObjectifscore() + "%");
        parseQuestions(c.getQuestion());
        startTimer();
        renderQuestion();
    }

    public void setUserId(int userId) {
        this.currentUserId = userId;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Récupérer l'utilisateur connecté
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getId();
            System.out.println("✅ Utilisateur connecté pour le challenge: " + currentUser.getEmail());
        }
    }

    // ── Parse JSON ────────────────────────────────────────────
    private void parseQuestions(String json) {
        if (json == null || json.isBlank()) return;
        questions.clear();
        reponsesEnregistrees.clear();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            if (root.has("questions")) {
                root.get("questions").forEach(questions::add);
            } else if (root.isArray()) {
                root.forEach(questions::add);
            }
            lblQuestion.setText("1/" + questions.size());
            updateProgress();
        } catch (Exception e) {
            System.err.println("Erreur parsing questions : " + e.getMessage());
        }
    }

    // ── Timer ─────────────────────────────────────────────────
    private void startTimer() {
        if (timer != null) timer.stop();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            lblTimer.setText(timeLeft + "s");
            if (timeLeft <= 10) {
                lblTimer.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#f87171;");
            }
            if (timeLeft <= 0) {
                timer.stop();
                terminer();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    // ── Progression ───────────────────────────────────────────
    private void updateProgress() {
        if (questions.isEmpty()) return;
        double p = (double) current / questions.size();
        if (progressBar != null) progressBar.setProgress(p);
        if (lblProgress != null) lblProgress.setText((int)(p * 100) + "% complété");
    }

    // ── Render question ───────────────────────────────────────
    private void renderQuestion() {
        questionContainer.getChildren().clear();
        if (current >= questions.size()) {
            terminer();
            return;
        }

        JsonNode q = questions.get(current);
        String type = q.has("type") ? q.get("type").asText() : "qcm";
        lblQuestion.setText((current + 1) + "/" + questions.size());
        updateProgress();

        switch (type) {
            case "qcm" -> renderQCM(q);
            case "truefalse" -> renderTrueFalse(q);
            case "open" -> renderOpen(q);
            default -> next();
        }
    }

    // ── QCM ───────────────────────────────────────────────────
    private void renderQCM(JsonNode q) {
        questionContainer.getChildren().add(questionLabel(q.get("question").asText()));

        ToggleGroup group = new ToggleGroup();
        List<RadioButton> radios = new ArrayList<>();
        String[] letters = {"A", "B", "C", "D", "E"};
        int[] idx = {0};

        if (q.has("choices")) {
            q.get("choices").forEach(choice -> {
                RadioButton rb = new RadioButton(choice.asText());
                rb.setToggleGroup(group);
                rb.setStyle(choiceStyle("default"));
                rb.setPrefWidth(680);
                Label badge = new Label(idx[0] < letters.length ? letters[idx[0]] : "•");
                badge.setStyle(
                        "-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:" + C_BLUE_TEXT + ";" +
                                "-fx-background-color:#dbeafe; -fx-background-radius:99;" +
                                "-fx-padding:2 7; -fx-min-width:22; -fx-alignment:center;");
                rb.setGraphic(badge);
                rb.setContentDisplay(ContentDisplay.LEFT);
                idx[0]++;
                radios.add(rb);
                questionContainer.getChildren().add(rb);
            });
        }

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:" + C_BORDER + "; -fx-padding:5 0;");

        Button btnValider = btnValider();
        Label lblMsg = msgLabel();
        questionContainer.getChildren().addAll(sep, btnValider, lblMsg);

        String correctAnswer = q.has("correct_answer") ? q.get("correct_answer").asText() : "";

        btnValider.setOnAction(e -> {
            RadioButton sel = (RadioButton) group.getSelectedToggle();
            if (sel == null) {
                lblMsg.setText("⚠️ Sélectionne une réponse avant de valider.");
                lblMsg.setStyle("-fx-text-fill:" + C_AMBER + "; -fx-font-size:13px;");
                return;
            }

            boolean isCorrect = sel.getText().startsWith(correctAnswer);

            // Enregistrer la réponse pour analyse
            Map<String, Object> reponseRecord = new HashMap<>();
            reponseRecord.put("question", q.get("question").asText());
            reponseRecord.put("reponse_etudiant", sel.getText());
            reponseRecord.put("reponse_correcte", correctAnswer);
            reponseRecord.put("est_correct", isCorrect);
            reponseRecord.put("categorie", q.has("categorie") ? q.get("categorie").asText() : "Général");
            reponsesEnregistrees.add(reponseRecord);

            radios.forEach(rb -> {
                if (rb.getText().startsWith(correctAnswer)) {
                    rb.setStyle(choiceStyle("correct"));
                } else if (rb == sel && !isCorrect) {
                    rb.setStyle(choiceStyle("wrong"));
                }
                rb.setDisable(true);
            });

            if (isCorrect) {
                totalScore += 20;
                setMsg(lblMsg, "✅ Bonne réponse ! +20 pts", C_GREEN_TEXT, true);
            } else {
                setMsg(lblMsg, "❌ Mauvaise réponse !", C_RED_TEXT, true);
            }

            if (q.has("explanation")) {
                questionContainer.getChildren().add(explanationBox(q.get("explanation").asText()));
            }

            updateScore();
            btnValider.setDisable(true);
            autoNext(1500);
        });
    }

    // ── Vrai / Faux ───────────────────────────────────────────
    private void renderTrueFalse(JsonNode q) {
        questionContainer.getChildren().add(questionLabel(q.get("question").asText()));

        Button btnVrai = tfButton("✔ Vrai", C_GREEN_LIGHT, C_GREEN_TEXT);
        Button btnFaux = tfButton("✘ Faux", C_RED_LIGHT, C_RED_TEXT);

        HBox btns = new HBox(16, btnVrai, btnFaux);
        btns.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(btns, new Insets(8, 0, 0, 0));

        Label lblMsg = msgLabel();
        questionContainer.getChildren().addAll(btns, lblMsg);

        boolean correct = q.has("correct_answer") && q.get("correct_answer").asBoolean();
        String categorie = q.has("categorie") ? q.get("categorie").asText() : "Général";

        btnVrai.setOnAction(e -> handleTF(true, correct, lblMsg, btnVrai, btnFaux, q, categorie));
        btnFaux.setOnAction(e -> handleTF(false, correct, lblMsg, btnVrai, btnFaux, q, categorie));
    }

    private void handleTF(boolean answer, boolean correct, Label lblMsg,
                          Button btnVrai, Button btnFaux, JsonNode q, String categorie) {
        boolean isCorrect = (answer == correct);

        // Enregistrer la réponse pour analyse
        Map<String, Object> reponseRecord = new HashMap<>();
        reponseRecord.put("question", q.get("question").asText());
        reponseRecord.put("reponse_etudiant", answer ? "Vrai" : "Faux");
        reponseRecord.put("reponse_correcte", correct ? "Vrai" : "Faux");
        reponseRecord.put("est_correct", isCorrect);
        reponseRecord.put("categorie", categorie);
        reponsesEnregistrees.add(reponseRecord);

        if (isCorrect) {
            totalScore += 20;
            setMsg(lblMsg, "✅ Bonne réponse ! +20 pts", C_GREEN_TEXT, true);
        } else {
            setMsg(lblMsg, "❌ Mauvaise réponse !", C_RED_TEXT, true);
        }

        if (q.has("explanation")) {
            questionContainer.getChildren().add(explanationBox(q.get("explanation").asText()));
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
        ta.setMaxWidth(680);
        ta.setStyle(
                "-fx-font-size:13px; -fx-font-family:'Segoe UI';" +
                        "-fx-background-color:white;" +
                        "-fx-background-radius:10; -fx-border-color:" + C_BORDER + ";" +
                        "-fx-border-radius:10; -fx-border-width:1.5;" +
                        "-fx-text-fill:" + C_TEXT_MAIN + ";");

        Button btnValider = btnValider();
        Label lblMsg = msgLabel();
        questionContainer.getChildren().addAll(ta, btnValider, lblMsg);

        String categorie = q.has("categorie") ? q.get("categorie").asText() : "Général";
        List<String> keywords = new ArrayList<>();
        if (q.has("expected_keywords")) {
            q.get("expected_keywords").forEach(kw -> keywords.add(kw.asText().toLowerCase()));
        }
        String modelAnswer = q.has("model_answer") ? q.get("model_answer").asText() : "";

        btnValider.setOnAction(e -> {
            String answer = ta.getText().trim().toLowerCase();
            if (answer.isEmpty()) {
                setMsg(lblMsg, "⚠️ Écris ta réponse avant de valider.", C_AMBER, false);
                return;
            }

            long found = keywords.stream().filter(answer::contains).count();
            int points = keywords.isEmpty() ? 10 :
                    (int) Math.round((double) found / keywords.size() * 20);
            totalScore += points;

            // Enregistrer la réponse pour analyse
            Map<String, Object> reponseRecord = new HashMap<>();
            reponseRecord.put("question", q.get("question").asText());
            reponseRecord.put("reponse_etudiant", answer);
            reponseRecord.put("reponse_correcte", modelAnswer);
            reponseRecord.put("est_correct", points >= 15);
            reponseRecord.put("categorie", categorie);
            reponseRecord.put("score_obtenu", points);
            reponsesEnregistrees.add(reponseRecord);

            setMsg(lblMsg,
                    "📝 +" + points + " pts — Mots-clés trouvés : " + found + "/" + keywords.size(),
                    C_BLUE_TEXT, false);

            if (!modelAnswer.isEmpty()) {
                questionContainer.getChildren().add(
                        explanationBox("Réponse modèle : " + modelAnswer));
            }

            updateScore();
            ta.setDisable(true);
            btnValider.setDisable(true);
            autoNext(2500);
        });
    }

    // ── Analyser les réponses par catégorie ───────────────────
    private Map<String, Object> analyserReponsesParCategorie() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> pointsParCategorie = new HashMap<>();
        Map<String, Integer> maxParCategorie = new HashMap<>();
        Map<String, List<String>> erreursParCategorie = new HashMap<>();

        for (Map<String, Object> rep : reponsesEnregistrees) {
            String categorie = (String) rep.get("categorie");
            boolean estCorrect = (boolean) rep.get("est_correct");

            if (!estCorrect) {
                erreursParCategorie.computeIfAbsent(categorie, k -> new ArrayList<>())
                        .add((String) rep.get("question"));
            }

            int points = estCorrect ? 20 : 0;
            if (rep.containsKey("score_obtenu")) {
                points = (int) rep.get("score_obtenu");
            }

            pointsParCategorie.put(categorie,
                    pointsParCategorie.getOrDefault(categorie, 0) + points);
            maxParCategorie.put(categorie,
                    maxParCategorie.getOrDefault(categorie, 0) + 20);
        }

        List<String> categoriesFaibles = new ArrayList<>();
        for (String cat : pointsParCategorie.keySet()) {
            int obtenus = pointsParCategorie.get(cat);
            int max = maxParCategorie.get(cat);
            double pourcentage = max > 0 ? (double) obtenus / max * 100 : 0;
            if (pourcentage < 60) {
                categoriesFaibles.add(cat);
            }
        }

        result.put("categories_faibles", categoriesFaibles);
        result.put("reussies", (int) reponsesEnregistrees.stream().filter(r -> (boolean) r.get("est_correct")).count());
        result.put("ratees", (int) reponsesEnregistrees.stream().filter(r -> !(boolean) r.get("est_correct")).count());
        result.put("erreurs_par_categorie", erreursParCategorie);
        result.put("points_par_categorie", pointsParCategorie);

        return result;
    }

    // ── Terminer ──────────────────────────────────────────────
    private void terminer() {
        if (finished) return;
        finished = true;
        if (timer != null) timer.stop();

        int percent = Math.min(totalScore, 100);
        String niveau = percent >= 81 ? "difficile" : percent >= 50 ? "moyen" : "facile";
        String badge = percent >= 80 ? "🏆 Expert" :
                percent >= 60 ? "🥇 Compétent" :
                percent >= 40 ? "🥈 Apprenti" : "🥉 Novice";
        boolean objectifAtteint = percent >= (int) challenge.getObjectifscore();
        String couleur = objectifAtteint ? C_GREEN_TEXT : C_RED_TEXT;

        // Analyser les réponses
        Map<String, Object> analyse = analyserReponsesParCategorie();

        // Sauvegarder dans le leaderboard
        try {
            LeaderboardEntry entry = new LeaderboardEntry(
                    percent, badge, challenge.getReponses(), 0, challenge.getId());
            serviceLeaderboard.ajouterScore(entry);
        } catch (SQLException e) {
            System.err.println("❌ Erreur sauvegarde leaderboard : " + e.getMessage());
        }

        // Sauvegarder la réponse de l'étudiant avec analyse
        try {
            JSONObject reponseDetail = new JSONObject();
            reponseDetail.put("score", percent);
            reponseDetail.put("badge", badge);
            reponseDetail.put("niveau", niveau);
            reponseDetail.put("questions_total", questions.size());
            reponseDetail.put("questions_reussies", analyse.get("reussies"));
            reponseDetail.put("questions_ratees", analyse.get("ratees"));

            JSONArray categoriesArray = new JSONArray();
            List<String> categoriesFaibles = (List<String>) analyse.get("categories_faibles");
            for (String cat : categoriesFaibles) {
                categoriesArray.put(cat);
            }
            reponseDetail.put("categories_faibles", categoriesArray);

            ReponseChallenge reponse = new ReponseChallenge(
                    challenge.getId(),
                    currentUserId,
                    reponseDetail.toString()
            );
            serviceReponse.soumettre(reponse);
            System.out.println("✅ Réponse détaillée sauvegardée pour userId=" + currentUserId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur sauvegarde réponse : " + e.getMessage());
        }

        challenge.setDernier_score(percent);
        challenge.setDernier_niveau(niveau);
        challenge.setNiveauatteint(badge);

        Examen examenRecommande = trouverExamen(niveau);
        questionContainer.getChildren().clear();

        // Conteneur principal
        VBox victory = new VBox(20);
        victory.setAlignment(Pos.CENTER);
        victory.setPadding(new Insets(10, 0, 20, 0));

        // Carte score
        VBox scoreCard = card();
        scoreCard.setAlignment(Pos.CENTER);
        scoreCard.setSpacing(8);

        Label ico = new Label(percent >= 60 ? "🏆" : percent >= 40 ? "🎯" : "📚");
        ico.setStyle("-fx-font-size:52px;");

        Label titre = new Label("Challenge terminé !");
        titre.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT_MAIN + ";");

        Label scoreLbl = new Label(percent + "%");
        scoreLbl.setStyle("-fx-font-size:42px; -fx-font-weight:bold; -fx-text-fill:" + couleur + ";");

        Label badgeLbl = new Label(badge);
        badgeLbl.setStyle("-fx-font-size:15px; -fx-text-fill:" + C_BLUE_TEXT + ";");

        String objBg = objectifAtteint ? "#dcfce7" : "#fee2e2";
        String objText = objectifAtteint ? C_GREEN_TEXT : C_RED_TEXT;
        Label objLbl = new Label(
                "Objectif " + (int) challenge.getObjectifscore() + "% — " +
                        (objectifAtteint ? "✅ Atteint !" : "❌ Non atteint"));
        objLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + objText + ";" +
                "-fx-background-color:" + objBg + "; -fx-background-radius:99;" +
                "-fx-padding:6 18;");

        Label niveauLbl = new Label("Niveau recommandé : " + niveau.toUpperCase());
        niveauLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + niveauTexte(niveau) + ";" +
                "-fx-background-color:" + niveauCouleur(niveau) + "; -fx-background-radius:99; -fx-padding:5 16;");

        scoreCard.getChildren().addAll(ico, titre, scoreLbl, badgeLbl, objLbl, niveauLbl);

        // Carte examen recommandé
        VBox examenCard = card();
        examenCard.setSpacing(10);
        if (examenRecommande != null) {
            Label exTitre = sectionTitle("📝 Examen recommandé");
            Label exNom = new Label(examenRecommande.getTitre());
            exNom.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT_MAIN + ";");
            Label exMeta = new Label(
                    "Niveau : " + examenRecommande.getNiveauexamen() +
                            " • Matière : " + nvl(examenRecommande.getMatiere()));
            exMeta.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_TEXT_MUTED + ";");
            Button btnExamen = primaryBtn("Passer l'examen →");
            btnExamen.setOnAction(e -> ouvrirExamen(examenRecommande));
            examenCard.getChildren().addAll(exTitre, exNom, exMeta, btnExamen);
        } else {
            Label noExam = new Label("ℹ️ Aucun examen disponible pour ce niveau.");
            noExam.setStyle("-fx-font-size:13px; -fx-text-fill:" + C_TEXT_MUTED + ";");
            examenCard.getChildren().add(noExam);
        }

        // Boutons d'action
        Button btnReessayer = secondaryBtn("🔄 Réessayer");
        Button btnLeaderboard = accentBtn("🏆 Leaderboard");
        Button btnFermer = ghostBtn("✖ Fermer");

        btnReessayer.setOnAction(e -> {
            current = 0;
            totalScore = 0;
            timeLeft = 60;
            finished = false;
            reponsesEnregistrees.clear();
            lblScore.setText("0");
            lblTimer.setText("60s");
            lblTimer.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#facc15;");
            updateProgress();
            startTimer();
            renderQuestion();
        });
        btnLeaderboard.setOnAction(e -> ouvrirLeaderboard());
        btnFermer.setOnAction(e -> ((Stage) questionContainer.getScene().getWindow()).close());

        HBox btns = new HBox(12, btnReessayer, btnLeaderboard, btnFermer);
        btns.setAlignment(Pos.CENTER);

        // Carte feedback IA
        VBox feedbackCard = card();
        feedbackCard.setStyle(feedbackCard.getStyle() +
                " -fx-border-color:" + C_GREEN + "; -fx-border-width:1.5; -fx-border-radius:14;");
        Label loader = new Label("⏳ Génération du feedback IA…");
        loader.setStyle("-fx-font-size:13px; -fx-text-fill:" + C_TEXT_MUTED + ";-fx-font-style:italic;");
        feedbackCard.getChildren().add(loader);

        victory.getChildren().addAll(scoreCard, examenCard, btns, feedbackCard);
        questionContainer.getChildren().add(victory);

        // Appel IA avec analyse détaillée
        final int fp = percent;
        final String fb2 = badge;
        final String fn = niveau;
        final int fno = questions.size();
        final int fob = (int) challenge.getObjectifscore();
        final Map<String, Object> analyseFinale = analyse;

        new Thread(() -> {
            try {
                String json = serviceIA.genererFeedbackAvecAnalyse(
                        challenge.getTitrec(), fp, fb2, fob, fn, fno, analyseFinale);
                JSONObject fb = new JSONObject(json);

                Platform.runLater(() -> afficherFeedbackDetaille(feedbackCard, fb, analyseFinale));

            } catch (Exception e) {
                Platform.runLater(() -> {
                    feedbackCard.getChildren().clear();
                    Label err = new Label("⚠️ Feedback IA indisponible : " + e.getMessage());
                    err.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_RED_TEXT + ";");
                    feedbackCard.getChildren().add(err);
                });
            }
        }).start();
    }

    private void afficherFeedbackDetaille(VBox feedbackCard, JSONObject fb, Map<String, Object> analyse) {
        feedbackCard.getChildren().clear();

        int percent = fb.optInt("score", 0);
        String titreIcon = percent >= 70 ? "🎉" : percent >= 40 ? "📈" : "📚";
        Label fbTitre = sectionTitle(titreIcon + " " + fb.optString("titre", "Analyse détaillée"));

        Label fbMsg = new Label(fb.optString("message", ""));
        fbMsg.setStyle("-fx-font-size:13px; -fx-text-fill:" + C_TEXT_MAIN + ";");
        fbMsg.setWrapText(true);

        // Points forts
        VBox pointsForts = new VBox(4);
        Label pfTitre = new Label("✅ Points forts");
        pfTitre.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + C_GREEN_TEXT + ";");
        pointsForts.getChildren().add(pfTitre);

        JSONArray pf = fb.optJSONArray("points_forts");
        if (pf != null && pf.length() > 0) {
            for (int i = 0; i < pf.length(); i++) {
                pointsForts.getChildren().add(bulletLabel(pf.getString(i), C_TEXT_MAIN));
            }
        } else {
            pointsForts.getChildren().add(bulletLabel("Bonne participation au challenge", C_TEXT_MAIN));
        }

        // Axes d'amélioration
        VBox axes = new VBox(5);
        Label axTitre = new Label("📈 Axes d'amélioration prioritaires");
        axTitre.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#b45309;");
        axes.getChildren().add(axTitre);

        @SuppressWarnings("unchecked")
        List<String> categoriesFaibles = (List<String>) analyse.get("categories_faibles");
        if (categoriesFaibles != null && !categoriesFaibles.isEmpty()) {
            Label catLabel = new Label("⚠️ Catégories à renforcer :");
            catLabel.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#e67e22;");
            axes.getChildren().add(catLabel);
            for (String cat : categoriesFaibles) {
                axes.getChildren().add(bulletLabel(cat + " - à revoir en priorité", "#e67e22"));
            }
        }

        JSONArray ax = fb.optJSONArray("axes_amelioration");
        if (ax != null && ax.length() > 0) {
            for (int i = 0; i < ax.length(); i++) {
                axes.getChildren().add(bulletLabel(ax.getString(i), C_TEXT_MAIN));
            }
        }

        // Statistiques
        VBox statsBox = new VBox(5);
        Label statsTitre = new Label("📊 Vos statistiques");
        statsTitre.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1f4f65;");
        statsBox.getChildren().add(statsTitre);

        int reussies = (int) analyse.getOrDefault("reussies", 0);
        int ratees = (int) analyse.getOrDefault("ratees", 0);
        int totalQuestions = reussies + ratees;
        int taux = totalQuestions > 0 ? (reussies * 100 / totalQuestions) : 0;

        Label statsText = new Label(String.format("✅ Réussies: %d   ❌ Ratées: %d   📈 Taux: %d%%", reussies, ratees, taux));
        statsText.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_TEXT_MAIN + ";");
        statsBox.getChildren().add(statsText);

        // Conseil final
        Label conseil = new Label("💡 " + fb.optString("conseil_final", "Continuez à pratiquer régulièrement pour améliorer vos compétences !"));
        conseil.setStyle("-fx-font-size:13px; -fx-font-style:italic; -fx-text-fill:" + C_BLUE_TEXT + ";" +
                "-fx-background-color:#e0f2fe; -fx-background-radius:10; -fx-padding:10 14;");
        conseil.setWrapText(true);

        // ===== NOUVEAU: RECOMMANDATIONS DE CHALLENGES =====
        VBox recommandationsBox = new VBox(8);
        Label recoTitre = new Label("🎯 Challenges recommandés pour progresser");
        recoTitre.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1f4f65; -fx-padding:5 0 0 0;");
        recommandationsBox.getChildren().add(recoTitre);

        JSONArray recos = fb.optJSONArray("recommandations_challenges");
        if (recos != null && recos.length() > 0) {
            for (int i = 0; i < recos.length(); i++) {
                JSONObject reco = recos.getJSONObject(i);

                // Créer une carte pour chaque challenge recommandé
                VBox challengeCard = new VBox(8);
                challengeCard.setStyle(
                        "-fx-background-color: #f8fafb;" +
                                "-fx-border-color: #e2e8ec;" +
                                "-fx-border-radius: 10;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 12 15;"
                );

                // Niveau avec badge de couleur
                String niveau = reco.optString("niveau", "moyen");
                String niveauColor = switch (niveau.toLowerCase()) {
                    case "facile" -> "#27ae60";
                    case "difficile" -> "#e74c3c";
                    default -> "#f39c12";
                };

                Label badgeNiveau = new Label("🏷️ " + niveau.toUpperCase());
                badgeNiveau.setStyle("-fx-font-size:10px; -fx-text-fill:" + niveauColor + "; -fx-font-weight:bold;");

                Label titreChallenge = new Label("📌 " + reco.optString("titre", "Challenge"));
                titreChallenge.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1a3d4f;");

                Label raison = new Label("💡 " + reco.optString("raison", "Pour progresser sur ce sujet"));
                raison.setStyle("-fx-font-size:11px; -fx-text-fill:#666;");
                raison.setWrapText(true);

                String thematique = reco.optString("thematique", "");
                Label thematiqueLabel = new Label("📚 " + thematique);
                thematiqueLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#1f4f65; -fx-font-style:italic;");

                Button btnJouer = new Button("🎮 Jouer ce challenge");
                btnJouer.setStyle(
                        "-fx-background-color: #1f4f65;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 11px;" +
                                "-fx-padding: 6 12;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;"
                );

                int challengeId = reco.optInt("id", -1);
                String challengeTitre = reco.optString("titre", "");

                btnJouer.setOnAction(e -> lancerChallengeParTitre(challengeTitre, challengeId));

                HBox headerBox = new HBox(10);
                headerBox.setAlignment(Pos.CENTER_LEFT);
                headerBox.getChildren().addAll(badgeNiveau, thematiqueLabel);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                headerBox.getChildren().add(spacer);

                challengeCard.getChildren().addAll(headerBox, titreChallenge, raison, btnJouer);
                recommandationsBox.getChildren().add(challengeCard);
            }
        } else {
            Label noReco = new Label("  •  Consultez les cours recommandés dans votre dashboard");
            noReco.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_TEXT_MUTED + ";");
            recommandationsBox.getChildren().add(noReco);
        }

        // Ajouter tous les composants
        feedbackCard.getChildren().addAll(fbTitre, fbMsg, pointsForts, axes, statsBox, conseil, recommandationsBox);
    }

    /**
     * Lancer un challenge par son titre ou ID
     */
    private void lancerChallengeParTitre(String titre, int id) {
        try {
            // Charger les challenges depuis la base
            org.example.Services.ServiceChallenge serviceChallenge = new org.example.Services.ServiceChallenge();
            List<Challenge> challenges = serviceChallenge.recuperer();

            Challenge challengeToPlay = null;

            // Chercher par ID d'abord
            if (id > 0) {
                for (Challenge c : challenges) {
                    if (c.getId() == id) {
                        challengeToPlay = c;
                        break;
                    }
                }
            }

            // Sinon chercher par titre
            if (challengeToPlay == null && titre != null && !titre.isEmpty()) {
                for (Challenge c : challenges) {
                    if (c.getTitrec() != null && c.getTitrec().toLowerCase().contains(titre.toLowerCase())) {
                        challengeToPlay = c;
                        break;
                    }
                }
            }

            if (challengeToPlay != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/ChallengeUI.fxml"));
                Parent root = loader.load();
                ChallengeUIController controller = loader.getController();
                controller.setChallenge(challengeToPlay);
                controller.setUserId(currentUserId);

                Stage stage = new Stage();
                stage.setTitle("Challenge: " + challengeToPlay.getTitrec());
                stage.setScene(new Scene(root, 900, 700));
                stage.show();

                // Fermer la fenêtre actuelle
                ((Stage) questionContainer.getScene().getWindow()).close();
            } else {
                showAlert("Challenge non trouvé", "Le challenge recommandé n'est pas disponible actuellement.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de lancer le challenge: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ── Ouvrir Leaderboard ────────────────────────────────────
    private void ouvrirLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/Leaderboard.fxml"));
            Parent root = loader.load();
            LeaderboardController ctrl = loader.getController();
            ctrl.setChallenge(challenge);
            Stage stage = new Stage();
            stage.setTitle("🏆 Leaderboard — " + challenge.getTitrec());
            stage.setScene(new Scene(root, 620, 500));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur ouverture leaderboard : " + e.getMessage());
        }
    }

    private Examen trouverExamen(String niveau) {
        try {
            List<Examen> examens = serviceExamen.recuperer();
            for (Examen ex : examens)
                if (niveau.equalsIgnoreCase(ex.getNiveauexamen())) return ex;
            return examens.isEmpty() ? null : examens.get(0);
        } catch (SQLException e) {
            System.err.println("Erreur chargement examens : " + e.getMessage());
            return null;
        }
    }

    private void ouvrirExamen(Examen examen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/ExamenFront.fxml"));
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

    // ════════════════════════════════════════════════════════
    // DESIGN HELPERS
    // ════════════════════════════════════════════════════════

    private VBox card() {
        VBox box = new VBox(12);
        box.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:14;" +
                        "-fx-border-color:" + C_BORDER + ";" +
                        "-fx-border-radius:14;" +
                        "-fx-border-width:0.5;" +
                        "-fx-padding:22 24;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,2);");
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private Label questionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT_MAIN + ";" +
                "-fx-font-family:'Segoe UI'; -fx-wrap-text:true;");
        l.setWrapText(true);
        l.setMaxWidth(680);
        VBox.setMargin(l, new Insets(0, 0, 10, 0));
        return l;
    }

    private String choiceStyle(String state) {
        return switch (state) {
            case "correct" ->
                    "-fx-font-size:14px; -fx-font-family:'Segoe UI';" +
                            "-fx-padding:11 16; -fx-background-color:" + C_GREEN_LIGHT + ";" +
                            "-fx-background-radius:10; -fx-border-color:#86efac;" +
                            "-fx-border-radius:10; -fx-border-width:1;" +
                            "-fx-text-fill:" + C_GREEN_TEXT + "; -fx-cursor:hand;";
            case "wrong" ->
                    "-fx-font-size:14px; -fx-font-family:'Segoe UI';" +
                            "-fx-padding:11 16; -fx-background-color:" + C_RED_LIGHT + ";" +
                            "-fx-background-radius:10; -fx-border-color:#fca5a5;" +
                            "-fx-border-radius:10; -fx-border-width:1;" +
                            "-fx-text-fill:" + C_RED_TEXT + "; -fx-cursor:hand;";
            default ->
                    "-fx-font-size:14px; -fx-font-family:'Segoe UI';" +
                            "-fx-padding:11 16; -fx-background-color:" + C_CARD_BG + ";" +
                            "-fx-background-radius:10; -fx-border-color:" + C_BORDER + ";" +
                            "-fx-border-radius:10; -fx-border-width:1;" +
                            "-fx-text-fill:" + C_TEXT_MAIN + "; -fx-cursor:hand;";
        };
    }

    private Button tfButton(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-font-family:'Segoe UI';" +
                "-fx-padding:12 36; -fx-background-radius:10; -fx-cursor:hand;" +
                "-fx-background-color:" + bg + "; -fx-text-fill:" + fg + ";" +
                "-fx-border-color:" + fg + "22; -fx-border-radius:10; -fx-border-width:1;");
        b.setPrefWidth(160);
        return b;
    }

    private Button btnValider() {
        Button b = new Button("Valider →");
        b.setStyle("-fx-background-color:" + C_GREEN + "; -fx-text-fill:white;" +
                "-fx-font-size:14px; -fx-font-weight:bold; -fx-font-family:'Segoe UI';" +
                "-fx-background-radius:10; -fx-padding:11 32; -fx-cursor:hand;");
        VBox.setMargin(b, new Insets(6, 0, 0, 0));
        return b;
    }

    private Button primaryBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + C_GREEN + "; -fx-text-fill:white;" +
                "-fx-font-size:13px; -fx-font-weight:bold; -fx-font-family:'Segoe UI';" +
                "-fx-background-radius:10; -fx-padding:10 28; -fx-cursor:hand;");
        return b;
    }

    private Button secondaryBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#dbeafe; -fx-text-fill:#1d4ed8;" +
                "-fx-font-size:13px; -fx-font-weight:bold; -fx-font-family:'Segoe UI';" +
                "-fx-background-radius:10; -fx-padding:10 22; -fx-cursor:hand;");
        return b;
    }

    private Button accentBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + C_AMBER_LIGHT + "; -fx-text-fill:#92400e;" +
                "-fx-font-size:13px; -fx-font-weight:bold; -fx-font-family:'Segoe UI';" +
                "-fx-background-radius:10; -fx-padding:10 22; -fx-cursor:hand;");
        return b;
    }

    private Button ghostBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#f3f4f6; -fx-text-fill:#6b7280;" +
                "-fx-font-size:13px; -fx-font-family:'Segoe UI';" +
                "-fx-background-radius:10; -fx-padding:10 22; -fx-cursor:hand;");
        return b;
    }

    private Label msgLabel() {
        Label l = new Label("");
        l.setStyle("-fx-font-size:13px; -fx-font-family:'Segoe UI';");
        VBox.setMargin(l, new Insets(4, 0, 0, 0));
        return l;
    }

    private void setMsg(Label l, String text, String color, boolean bold) {
        l.setText(text);
        l.setStyle("-fx-font-size:13px; -fx-font-family:'Segoe UI'; -fx-text-fill:" + color + ";" +
                (bold ? "-fx-font-weight:bold;" : ""));
    }

    private VBox explanationBox(String text) {
        Label l = new Label("💡 " + text);
        l.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_BLUE_TEXT + ";" +
                "-fx-font-family:'Segoe UI'; -fx-wrap-text:true;");
        l.setWrapText(true);
        VBox box = new VBox(l);
        box.setStyle("-fx-background-color:#f0f9ff; -fx-background-radius:10;" +
                "-fx-border-color:#bae6fd; -fx-border-radius:10; -fx-border-width:1;" +
                "-fx-padding:10 14;");
        VBox.setMargin(box, new Insets(6, 0, 0, 0));
        return box;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:15px; -fx-font-weight:bold;" +
                "-fx-text-fill:" + C_TEXT_MAIN + "; -fx-font-family:'Segoe UI';");
        l.setWrapText(true);
        return l;
    }

    private Label bulletLabel(String text, String color) {
        Label l = new Label("  •  " + text);
        l.setStyle("-fx-font-size:12px; -fx-text-fill:" + color + "; -fx-font-family:'Segoe UI';");
        l.setWrapText(true);
        return l;
    }

    private String niveauCouleur(String niveau) {
        return switch (niveau) {
            case "facile" -> "#dcfce7";
            case "moyen" -> "#fef3c7";
            case "difficile" -> "#fee2e2";
            default -> "#f3f4f6";
        };
    }

    private String niveauTexte(String niveau) {
        return switch (niveau) {
            case "facile" -> C_GREEN_TEXT;
            case "moyen" -> "#92400e";
            case "difficile" -> C_RED_TEXT;
            default -> C_TEXT_MUTED;
        };
    }

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

    private String nvl(String s) {
        return s != null ? s : "";
    }
}
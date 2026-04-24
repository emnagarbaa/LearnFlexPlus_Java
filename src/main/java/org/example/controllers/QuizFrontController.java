package org.example.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.example.Services.CertificatGenerator;
import org.example.Services.ServiceQuiz;
import org.example.Services.ServiceReponse;
import org.example.entities.Quiz;
import org.example.entities.Reponse;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class QuizFrontController {

    // ── FXML BINDINGS ──────────────────────────────────────────────
    // ── FXML BINDINGS ──────────────────────────────────────────────
    @FXML private Label       lblTitre;
    @FXML private Label       lblTimer;
    @FXML private Label       lblProgression;
    @FXML private Label       lblScore;
    @FXML private Label       lblAlerteTemps;
    @FXML private Label       lblQuestion;
    @FXML private ProgressBar progressBar;
    @FXML private VBox        reponseContainer;
    @FXML private VBox        historiqueContainer;
    @FXML private Button      btnValider;
    @FXML private VBox        colonneHistorique;
    // ✅ NOUVEAUX
    @FXML private HBox        blocFiltres;
    @FXML private VBox        blocHero;
    @FXML private VBox        blocQuestion;
    @FXML private VBox        ecranCorrectionContainer;
    @FXML private ScrollPane  scrollPrincipal;
    // ── SERVICES ────────────────────────────────────────────────────
    private final ServiceQuiz    serviceQuiz    = new ServiceQuiz();
    private final ServiceReponse serviceReponse = new ServiceReponse();
    private static final String GROQ_API_KEY = System.getenv("GROQ_API_KEY");    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<Quiz> allQuiz = new ArrayList<>();
    // Cache simple pour éviter les appels redondants
    private final java.util.Map<String, String> explicationCache = new java.util.HashMap<>();
    // ── ÉTAT ────────────────────────────────────────────────────────
    private List<Quiz>    quizList          = new ArrayList<>();
    private List<Quiz>    quizHistorique    = new ArrayList<>();   // ← NOUVEAU
    private List<Reponse> reponsesChoisies  = new ArrayList<>();   // ← NOUVEAU
    private Quiz          quizCourant;
    private List<Reponse> reponses;
    private int           currentIndex = 0;
    private int           score        = 0;
    private ToggleGroup   toggleGroup;
    private Timeline      timer;
    private int           secondesRestantes;
    private int totalQuestions;
    private boolean quizTermine = false;
    private String nomEtudiant = "Étudiant";
    // ── INITIALISATION ───────────────────────────────────────────────
    @FXML
    public void initialize() {

        allQuiz = serviceQuiz.recuperer();

        quizList = allQuiz.stream()
                .filter(q -> "active".equalsIgnoreCase(q.getEtat()))
                .toList();

        totalQuestions = quizList.size();

        if (quizList.isEmpty()) {
            lblQuestion.setText("Aucun quiz disponible.");
            btnValider.setDisable(true);
            return;
        }

        quizCourant = quizList.get(0);
        reponses = serviceReponse.recupererParQuiz(quizCourant.getId());

        lblTitre.setText(quizCourant.getTitre());

        afficherQuestion();

        int duree = quizCourant.getDuree() * 60;
        demarrerTimer(duree > 0 ? duree : 180);
    }
    // ── APPEL API GROQ ───────────────────────────────────────────────
    private String getExplication(String question, String reponseEtudiant,
                                  String bonneReponse, boolean estCorrecte, int quizId) {
        if (reponseEtudiant == null || reponseEtudiant.isBlank()) {
            return "Tu n'as pas sélectionné de réponse.";
        }

        // Clé de cache
        String cacheKey = "explication_quiz_" + quizId + "_"
                + Integer.toHexString((reponseEtudiant + question + bonneReponse).hashCode());

        if (explicationCache.containsKey(cacheKey)) {
            return explicationCache.get(cacheKey);
        }

        String prompt = """
            Tu es un enseignant qui corrige un quiz scolaire.

            IMPORTANT :
            - La bonne réponse fournie est la référence officielle.
            - L'étudiant peut écrire n'importe quoi (mot, nombre, phrase).
            - Tu ne dois JAMAIS inventer une autre interprétation.

            QUESTION :
            %s

            REPONSE ETUDIANT :
            %s

            BONNE REPONSE :
            %s

            Ta tâche :
            1) Dire clairement si la réponse est correcte ou incorrecte.
            2) Si incorrecte : expliquer pourquoi elle ne correspond pas à la bonne réponse.
            3) Donner la bonne information sous forme simple (comme un professeur).
            4) Donner une courte explication pédagogique liée au cours.

            Contraintes :
            - Maximum 5 lignes
            - Français simple
            - Pas de calcul inventé
            - Pas d'hypothèse
            - Ne pas interpréter la réponse de l'étudiant autrement que littéralement
            """.formatted(question, reponseEtudiant, bonneReponse);

        try {
            // Construire le JSON manuellement (sans dépendance externe)
            String requestBody = """
                {
                  "model": "llama-3.1-8b-instant",
                  "messages": [
                    {
                      "role": "system",
                      "content": "Tu es un professeur qui explique les réponses d'un quiz à un étudiant de manière simple et pédagogique."
                    },
                    {
                      "role": "user",
                      "content": %s
                    }
                  ],
                  "temperature": 0.4,
                  "max_tokens": 300
                }
                """.formatted(objectMapper.writeValueAsString(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_API_URL))
                    .header("Authorization", "Bearer " + GROQ_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Parser la réponse JSON
            var jsonNode = objectMapper.readTree(response.body());
            String explication = jsonNode
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText("Aucune explication disponible.");

            explication = explication.trim();
            explicationCache.put(cacheKey, explication);
            return explication;

        } catch (Exception e) {
            return "Erreur IA : " + e.getMessage();
        }
    }
    // ── AFFICHAGE D'UNE QUESTION ─────────────────────────────────────
    private void afficherQuestion() {

        if (currentIndex >= quizList.size()) {
            terminerQuiz();
            return;
        }

        quizCourant = quizList.get(currentIndex);
        reponses    = serviceReponse.recupererParQuiz(quizCourant.getId());

        double pct = (double) currentIndex / quizList.size();
        progressBar.setProgress(pct);
        lblProgression.setText((currentIndex + 1) + " / " + quizList.size());
        lblQuestion.setText(quizCourant.getQuestion());

        reponseContainer.getChildren().clear();
        toggleGroup = new ToggleGroup();

        for (Reponse rep : reponses) {
            RadioButton rb = new RadioButton(rep.getTexte());
            rb.setToggleGroup(toggleGroup);
            rb.setUserData(rep);
            rb.setStyle(styleRbNormal());
            rb.setMaxWidth(Double.MAX_VALUE);

            rb.selectedProperty().addListener((obs, old, selected) -> {
                if (selected) {
                    rb.setStyle(styleRbSelected());
                    btnValider.setDisable(false);
                } else {
                    rb.setStyle(styleRbNormal());
                }
            });

            reponseContainer.getChildren().add(rb);
        }

        btnValider.setDisable(true);
    }

    // ── VALIDATION DE LA RÉPONSE ─────────────────────────────────────
    @FXML
    private void validerReponse() {

        if (quizTermine) return; // ❌ bloque tout après fin

        RadioButton selected = (RadioButton) toggleGroup.getSelectedToggle();
        if (selected == null) return;

        Reponse reponseChoisie = (Reponse) selected.getUserData();
        boolean correct = reponseChoisie.isEstCorrecte();

        if (correct) score++;

        reponsesChoisies.add(reponseChoisie);
        quizHistorique.add(quizCourant);

        currentIndex++;

        if (currentIndex >= quizList.size()) {
            quizTermine = true;   // 🔥 IMPORTANT
            terminerQuiz();
        } else {
            afficherQuestion();
        }
    }

    // ── HISTORIQUE LATÉRAL ───────────────────────────────────────────
    private void ajouterAuHistorique(Quiz quiz, Reponse reponseChoisie, boolean correct) {
        VBox carte = new VBox(6);
        carte.setPadding(new Insets(12, 14, 12, 14));
        carte.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-radius:12;" +
                        "-fx-border-color:" + (correct ? "#52c41a" : "#ff4d4f") + ";" +
                        "-fx-border-width:0 0 0 4;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);"
        );

        Label lblQ = new Label("Q" + (currentIndex + 1) + " — " + quiz.getQuestion());
        lblQ.setWrapText(true);
        lblQ.setStyle("-fx-font-size:12px; -fx-text-fill:#555; -fx-font-weight:500;");

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label icone = new Label(correct ? "✓" : "✗");
        icone.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"
                + (correct ? "#389e0d" : "#cf1322") + ";");

        Label lblRep = new Label(reponseChoisie.getTexte());
        lblRep.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"
                + (correct ? "#389e0d" : "#cf1322") + ";");

        Label badge = new Label(correct ? "Correct" : "Incorrect");
        badge.setStyle("-fx-font-size:10px; -fx-padding:2 8; -fx-background-radius:10;" +
                (correct
                        ? "-fx-background-color:#f6ffed; -fx-text-fill:#389e0d; -fx-border-color:#b7eb8f; -fx-border-radius:10;"
                        : "-fx-background-color:#fff1f0; -fx-text-fill:#cf1322; -fx-border-color:#ffa39e; -fx-border-radius:10;"));

        row.getChildren().addAll(icone, lblRep, badge);
        carte.getChildren().addAll(lblQ, row);

        if (!correct) {
            reponses.stream()
                    .filter(Reponse::isEstCorrecte)
                    .findFirst()
                    .ifPresent(bonne -> {
                        Label lblBonne = new Label("Bonne réponse : " + bonne.getTexte());
                        lblBonne.setWrapText(true);
                        lblBonne.setStyle("-fx-font-size:12px; -fx-text-fill:#888;");
                        carte.getChildren().add(lblBonne);
                    });
        }

        historiqueContainer.getChildren().add(0, carte);
    }

    // ── TIMER ────────────────────────────────────────────────────────
    private void demarrerTimer(int secondes) {
        secondesRestantes = secondes;
        if (timer != null) timer.stop();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondesRestantes--;
            int m = secondesRestantes / 60;
            int s = secondesRestantes % 60;
            lblTimer.setText(String.format("%02d:%02d", m, s));

            double pct    = (double) secondesRestantes / secondes;
            String couleur = pct <= 0.2 ? "#cf1322" : pct <= 0.5 ? "#d46b08" : "#ff4d4f";
            lblTimer.setStyle(
                    "-fx-font-size:18px; -fx-font-weight:bold;" +
                            "-fx-text-fill:" + couleur + ";" +
                            "-fx-background-color:white;" +
                            "-fx-padding:5 14; -fx-background-radius:20;"
            );

            if (secondesRestantes <= 0) {
                timer.stop();
                lblAlerteTemps.setVisible(true);
                lblAlerteTemps.setManaged(true);
                terminerQuiz();
            }
        }));

        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    // ── FIN DU QUIZ + CORRECTION COMPLÈTE ───────────────────────────
    private void terminerQuiz() {
        quizTermine = true;
        if (timer != null) timer.stop();

        // ── CACHER les blocs quiz ──────────────────────────────────
        blocFiltres.setVisible(false);  blocFiltres.setManaged(false);
        blocHero.setVisible(false);     blocHero.setManaged(false);
        blocQuestion.setVisible(false); blocQuestion.setManaged(false);

        // ── SCORE ──────────────────────────────────────────────────
        int total   = quizList.size();
        double pct  = total == 0 ? 0 : (double) score / total;
        String couleurScore = pct >= 0.8 ? "#389e0d" : pct >= 0.5 ? "#d46b08" : "#cf1322";
        String emoji        = pct >= 0.8 ? "🏆"      : pct >= 0.5 ? "👍"      : "💪";

        // ── CONSTRUIRE L'ÉCRAN CORRECTION ─────────────────────────
        ecranCorrectionContainer.getChildren().clear();

        Label lblFin = new Label(emoji + " Quiz terminé !");
        lblFin.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#142341;");

        Label lblScoreFinal = new Label("Score : " + score + " / " + total);
        lblScoreFinal.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:" + couleurScore + ";");

        ProgressBar barreScore = new ProgressBar(pct);
        barreScore.setMaxWidth(Double.MAX_VALUE);
        barreScore.setStyle("-fx-accent:" + couleurScore + ";");

        ecranCorrectionContainer.getChildren().addAll(lblFin, lblScoreFinal, barreScore);

        // ── BLOC CERTIFICAT ────────────────────────────────────────
        afficherBlocCertificat(ecranCorrectionContainer, quizCourant.getTitre());

        // ── CARTES CORRECTION PAR QUESTION ────────────────────────
        int size = Math.min(quizHistorique.size(), reponsesChoisies.size());
        for (int i = 0; i < size; i++) {

            Quiz    q       = quizHistorique.get(i);
            Reponse choisie = reponsesChoisies.get(i);
            boolean correct = choisie.isEstCorrecte();

            List<Reponse> toutesRep = serviceReponse.recupererParQuiz(q.getId());
            String bonneReponseTexte = toutesRep.stream()
                    .filter(Reponse::isEstCorrecte)
                    .map(Reponse::getTexte)
                    .findFirst().orElse("N/A");

            VBox carte = new VBox(8);
            carte.setPadding(new Insets(14));
            carte.setStyle(
                    "-fx-background-color:white;" +
                            "-fx-background-radius:12;" +
                            "-fx-border-radius:12;" +
                            "-fx-border-color:" + (correct ? "#52c41a" : "#ff4d4f") + ";" +
                            "-fx-border-width:2;" +
                            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);"
            );

            Label lblQ = new Label("Q" + (i + 1) + " — " + q.getQuestion());
            lblQ.setWrapText(true);
            lblQ.setStyle("-fx-font-weight:bold; -fx-font-size:14px; -fx-text-fill:#142341;");

            Label lblRep = new Label((correct ? "✅" : "❌") + " Votre réponse : " + choisie.getTexte());
            lblRep.setStyle("-fx-text-fill:" + (correct ? "#389e0d" : "#cf1322") + "; -fx-font-size:13px;");

            Label lblBonne = new Label("💡 Bonne réponse : " + bonneReponseTexte);
            lblBonne.setStyle("-fx-text-fill:#555; -fx-font-size:13px;");

            Label lblExplication = new Label("⏳ Génération IA...");
            lblExplication.setWrapText(true);
            lblExplication.setStyle("-fx-text-fill:#888; -fx-font-size:12px; -fx-font-style:italic;");

            carte.getChildren().addAll(lblQ, lblRep, lblBonne, lblExplication);
            ecranCorrectionContainer.getChildren().add(carte);

            // async IA
            final String bonneRep = bonneReponseTexte;
            Thread thread = new Thread(() -> {
                String explication = getExplication(
                        q.getQuestion(), choisie.getTexte(), bonneRep, correct, q.getId()
                );
                javafx.application.Platform.runLater(() ->
                        lblExplication.setText("🤖 " + explication)
                );
            });
            thread.setDaemon(true);
            thread.start();
        }

        // ── AFFICHER l'écran correction (colonne gauche) ───────────
        ecranCorrectionContainer.setVisible(true);
        ecranCorrectionContainer.setManaged(true);

        // ── AFFICHER la colonne droite avec le score ───────────────
        lblScore.setText(score + " / " + total);


        // ── Scroll vers le haut ────────────────────────────────────
        javafx.application.Platform.runLater(() -> scrollPrincipal.setVvalue(0));
    }

    // ── NAVIGATION ───────────────────────────────────────────────────
    @FXML private void goToAccueil()     { /* navigation */ }
    @FXML private void goToCours()       { /* navigation */ }
    @FXML private void goToEvaluation()  { /* navigation */ }
    @FXML private void goToOrientation() { /* navigation */ }
    @FXML private void goToForum()       { /* navigation */ }
    @FXML private void goToConnexion()   { /* navigation */ }

    // ── STYLES RadioButton ───────────────────────────────────────────
    private String styleRbNormal() {
        return "-fx-font-size:14px; -fx-padding:12 18;" +
                "-fx-background-color:#fafbfc; -fx-border-color:#e8ecf0;" +
                "-fx-border-radius:10; -fx-background-radius:10; -fx-text-fill:#142341;";
    }
    private String styleRbSelected() {
        return "-fx-font-size:14px; -fx-padding:12 18;" +
                "-fx-background-color:#e6f4ff; -fx-border-color:#1890ff;" +
                "-fx-border-radius:10; -fx-background-radius:10; -fx-text-fill:#003d8f;";
    }
    private void filtrerQuiz(String texte) {

        quizList = allQuiz.stream()
                .filter(q ->
                        "active".equalsIgnoreCase(q.getEtat()) &&
                                q.getTitre().toLowerCase().contains(texte.toLowerCase())
                )
                .toList();

        currentIndex = 0;

        if (!quizList.isEmpty()) {
            quizCourant = quizList.get(0);
            afficherQuestion();
        } else {
            lblQuestion.setText("Aucun quiz trouvé");
            reponseContainer.getChildren().clear();
        }
    }
    @FXML
    private void filtrerJava() {
        filtrerQuiz("java");
    }

    @FXML
    private void filtrerSQL() {
        filtrerQuiz("sql");
    }

    @FXML
    private void filtrerTous() {
        quizList = allQuiz.stream()
                .filter(q -> "active".equalsIgnoreCase(q.getEtat()))
                .toList();

        currentIndex = 0;

        if (!quizList.isEmpty()) {
            quizCourant = quizList.get(0);
            afficherQuestion();
        }
    }
    private void afficherBlocCertificat(VBox ecranCorrection, String titreQuiz) {

        double pct = quizList.isEmpty() ? 0 : (double) score / quizList.size();

        if (pct < 0.5) {
            // ── Pas de certificat ──────────────────────
            VBox blocEchec = new VBox(8);
            blocEchec.setPadding(new Insets(16));
            blocEchec.setStyle(
                    "-fx-background-color:#fff1f0;" +
                            "-fx-border-color:#ffa39e;" +
                            "-fx-border-radius:12;" +
                            "-fx-background-radius:12;"
            );
            Label lbl = new Label("❌  Score insuffisant pour obtenir un certificat (minimum 50%).");
            lbl.setWrapText(true);
            lbl.setStyle("-fx-text-fill:#cf1322; -fx-font-size:13px;");
            blocEchec.getChildren().add(lbl);
            ecranCorrection.getChildren().add(blocEchec);
            return;
        }

        // ── Bloc certificat ────────────────────────────
        VBox blocCert = new VBox(12);
        blocCert.setPadding(new Insets(20));
        blocCert.setAlignment(Pos.CENTER);
        blocCert.setStyle(
                "-fx-background-color:linear-gradient(to right, #fffbe6, #fff7cd);" +
                        "-fx-border-color:#faad14;" +
                        "-fx-border-width:2;" +
                        "-fx-border-radius:14;" +
                        "-fx-background-radius:14;" +
                        "-fx-effect:dropshadow(gaussian,rgba(250,173,20,0.3),12,0,0,4);"
        );

        Label lblTitreCert = new Label("🏅 Félicitations ! Vous avez obtenu votre certificat.");
        lblTitreCert.setWrapText(true);
        lblTitreCert.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#7c5000;");

        Label lblInfo = new Label("Nom : " + nomEtudiant + "   •   Quiz : " + titreQuiz +
                "   •   Score : " + score + "/" + quizList.size());
        lblInfo.setStyle("-fx-font-size:12px; -fx-text-fill:#ad6800;");

        Button btnTelecharger = new Button("⬇  Télécharger le certificat PDF");
        btnTelecharger.setStyle(
                "-fx-background-color:#faad14;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-weight:bold;" +
                        "-fx-font-size:13px;" +
                        "-fx-padding:10 20;" +
                        "-fx-background-radius:10;" +
                        "-fx-cursor:hand;"
        );

        btnTelecharger.setOnAction(e -> {
            btnTelecharger.setDisable(true);
            btnTelecharger.setText("⏳ Génération...");

            Thread t = new Thread(() -> {
                try {
                    File pdf = CertificatGenerator.generer(
                            nomEtudiant, titreQuiz, score, quizList.size()
                    );
                    javafx.application.Platform.runLater(() -> {
                        btnTelecharger.setText("✅ Certificat enregistré !");
                        // Ouvrir le dossier ou le fichier directement
                        try {
                            java.awt.Desktop.getDesktop().open(pdf);
                        } catch (Exception ex) {
                            btnTelecharger.setText("✅ Saved: " + pdf.getName());
                        }
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        btnTelecharger.setText("❌ Erreur : " + ex.getMessage());
                        btnTelecharger.setDisable(false);
                    });
                }
            });
            t.setDaemon(true);
            t.start();
        });

        blocCert.getChildren().addAll(lblTitreCert, lblInfo, btnTelecharger);
        ecranCorrection.getChildren().add(blocCert);
    }
}
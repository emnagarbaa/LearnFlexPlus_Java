package org.example.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Services.ServiceQuiz;
import org.example.Services.ServiceReponse;
import org.example.entities.Quiz;
import org.example.entities.Reponse;

import java.net.URL;
import java.util.List;

public class QuizFrontController {

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

    // ── SERVICES ────────────────────────────────────────────────────
    private final ServiceQuiz    serviceQuiz    = new ServiceQuiz();
    private final ServiceReponse serviceReponse = new ServiceReponse();

    // ── ÉTAT ────────────────────────────────────────────────────────
    private List<Quiz>    quizList;
    private Quiz          quizCourant;
    private List<Reponse> reponses;
    private int           currentIndex = 0;
    private int           score        = 0;
    private ToggleGroup   toggleGroup;
    private Timeline      timer;
    private int           secondesRestantes;

    // ── INITIALISATION ───────────────────────────────────────────────
    @FXML
    public void initialize() {
        quizList = serviceQuiz.recuperer()
                .stream()
                .filter(q -> "active".equalsIgnoreCase(q.getEtat()))
                .toList();

        if (quizList == null || quizList.isEmpty()) {
            lblQuestion.setText("Aucun quiz disponible.");
            btnValider.setDisable(true);
            return;
        }

        quizCourant = quizList.get(0);
        reponses    = serviceReponse.recupererParQuiz(quizCourant.getId());

        if (reponses == null || reponses.isEmpty()) {
            lblQuestion.setText("Ce quiz n'a pas encore de réponses.");
            btnValider.setDisable(true);
            return;
        }

        lblTitre.setText(quizCourant.getTitre());
        afficherQuestion();

        int dureeSecondes = quizCourant.getDuree() * 60;
        demarrerTimer(dureeSecondes > 0 ? dureeSecondes : 180);
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
        RadioButton selected = (RadioButton) toggleGroup.getSelectedToggle();
        if (selected == null) return;

        Reponse reponseChoisie = (Reponse) selected.getUserData();
        boolean correct = reponseChoisie.isEstCorrecte();

        if (correct) score++;

        ajouterAuHistorique(quizCourant, reponseChoisie, correct);
        lblScore.setText(score + " / " + quizList.size());

        currentIndex++;
        afficherQuestion();
    }

    // ── HISTORIQUE DES RÉPONSES ──────────────────────────────────────
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

        Label lblQ = new Label("Q" + currentIndex + " — " + quiz.getQuestion());
        lblQ.setWrapText(true);
        lblQ.setStyle("-fx-font-size:12px; -fx-text-fill:#555; -fx-font-weight:500;");

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label icone = new Label(correct ? "✓" : "✗");
        icone.setStyle("-fx-font-size:14px; -fx-font-weight:bold; " +
                "-fx-text-fill:" + (correct ? "#389e0d" : "#cf1322") + ";");

        Label lblRep = new Label(reponseChoisie.getTexte());
        lblRep.setStyle("-fx-font-size:13px; -fx-font-weight:bold; " +
                "-fx-text-fill:" + (correct ? "#389e0d" : "#cf1322") + ";");

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

            double pct = (double) secondesRestantes / secondes;
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

    // ── FIN DU QUIZ ──────────────────────────────────────────────────
    private void terminerQuiz() {
        if (timer != null) timer.stop();
        progressBar.setProgress(1.0);
        lblProgression.setText(quizList.size() + " / " + quizList.size());
        lblScore.setText(score + " / " + quizList.size());
        reponseContainer.getChildren().clear();
        btnValider.setDisable(true);
        lblQuestion.setText("🎉 Quiz terminé ! Score : " + score + " / " + quizList.size());
    }

    // ── NAVIGATION ───────────────────────────────────────────────────
    @FXML private void goToAccueil()       { navigateTo("/org/example/fxml/front.fxml",           "Accueil"); }
    @FXML private void goToCours()         { pageEnDeveloppement("Cours"); }
    @FXML private void explorerCours()     { pageEnDeveloppement("Cours"); }
    @FXML private void voirCours()         { pageEnDeveloppement("Cours"); }
    @FXML private void passerQuiz()        { navigateTo("/org/example/fxml/ExamenView.fxml",       "Examens"); }
    @FXML private void accederForum()      { pageEnDeveloppement("Forum"); }
    @FXML private void goToConnexion()     { navigateTo("/org/example/fxml/home.fxml",             "Connexion"); }
    @FXML private void goToEvaluation()    { navigateTo("/org/example/fxml/EvaluationFront.fxml",  "Évaluation"); }
    @FXML private void goToQuestionnaire() { navigateTo("/org/example/fxml/quiz_front.fxml",       "Questionnaire"); }
    @FXML private void goToOrientation()   { pageEnDeveloppement("Orientation"); }
    @FXML private void goToForum()         { pageEnDeveloppement("Forum"); }

    // ✅ méthode navigateTo — manquait dans l'ancien code
    private void navigateTo(String fxmlPath, String title) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + fxmlPath);
                new Alert(Alert.AlertType.ERROR,
                        "Page introuvable : " + fxmlPath, ButtonType.OK).showAndWait();
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = (Stage) reponseContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation vers " + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ méthode pageEnDeveloppement — manquait dans l'ancien code
    private void pageEnDeveloppement(String nomPage) {
        new Alert(Alert.AlertType.INFORMATION,
                "La page « " + nomPage + " » est en cours de développement.",
                ButtonType.OK).showAndWait();
    }

    // ── STYLES RadioButton ───────────────────────────────────────────
    private String styleRbNormal() {
        return "-fx-font-size:14px; -fx-padding:12 18;" +
                "-fx-background-color:#fafbfc; -fx-border-color:#e8ecf0;" +
                "-fx-border-radius:10; -fx-background-radius:10;" +
                "-fx-text-fill:#142341;";
    }

    private String styleRbSelected() {
        return "-fx-font-size:14px; -fx-padding:12 18;" +
                "-fx-background-color:#e6f4ff; -fx-border-color:#1890ff;" +
                "-fx-border-radius:10; -fx-background-radius:10;" +
                "-fx-text-fill:#003d8f;";
    }
}

package org.example.controllers;

import javafx.animation.RotateTransition;
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
import org.example.Services.ServiceChallenge;
import org.example.entities.Challenge;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class EvaluationFrontController implements Initializable {
    @FXML private Label lblTotalChallenges;

    @FXML private FlowPane cardsContainer;
    @FXML private Button btnTous, btnFacile, btnMoyen, btnDifficile;

    private List<Challenge> allChallenges;
    private final ServiceChallenge service = new ServiceChallenge();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            allChallenges = service.recuperer()
                    .stream()
                    .filter(c -> "Actif".equals(c.getEtat()))
                    .toList();
            afficherCartes(allChallenges);
        } catch (SQLException e) {
            System.err.println("Erreur chargement challenges : " + e.getMessage());
            afficherMessageErreur("Impossible de charger les challenges.");
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
            afficherMessageErreur("Une erreur inattendue s'est produite.");
        }
    }

    private void afficherMessageErreur(String msg) {
        Label lbl = new Label("⚠️  " + msg);
        lbl.setStyle("-fx-font-size:14px; -fx-text-fill:#A32D2D; -fx-padding:20;");
        cardsContainer.getChildren().add(lbl);
    }

    private void afficherCartes(List<Challenge> liste) {
        if (lblTotalChallenges != null)
            lblTotalChallenges.setText(String.valueOf(allChallenges.size()));
        cardsContainer.getChildren().clear();
        for (Challenge c : liste) {
            cardsContainer.getChildren().add(creerCarte(c));
        }
    }

    private StackPane creerCarte(Challenge challenge) {

        // ── FACE AVANT ────────────────────────────────────────
        VBox front = new VBox(12);
        front.setPrefSize(260, 300);
        front.setPadding(new Insets(20));
        front.setAlignment(Pos.TOP_LEFT);
        front.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:14;" +
                        "-fx-border-color:#e0e0e0;" +
                        "-fx-border-radius:14;" +
                        "-fx-border-width:1;"
        );

        Label badge = new Label(challenge.getNiveaudifficulte());
        badge.setStyle(badgeStyle(challenge.getNiveaudifficulte()));

        Label titre = new Label(challenge.getTitrec());
        titre.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#1a3d4f;");
        titre.setWrapText(true);
        titre.setMaxWidth(220);

        Label desc = new Label(nvl(challenge.getDescriptionc()));
        desc.setStyle("-fx-font-size:12px; -fx-text-fill:#666;");
        desc.setWrapText(true);
        desc.setMaxWidth(220);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label hint = new Label("↻  Clique pour voir les détails");
        hint.setStyle("-fx-font-size:11px; -fx-text-fill:#aaa;");

        front.getChildren().addAll(badge, titre, desc, spacer, hint);

        // ── FACE ARRIÈRE ──────────────────────────────────────
        VBox back = new VBox(10);
        back.setPrefSize(260, 300);
        back.setPadding(new Insets(20));
        back.setAlignment(Pos.TOP_LEFT);
        back.setStyle(
                "-fx-background-color:#f0faf5;" +
                        "-fx-background-radius:14;" +
                        "-fx-border-color:#1D9E75;" +
                        "-fx-border-radius:14;" +
                        "-fx-border-width:1.5;"
        );
        back.setVisible(false);

        Label titrBack = new Label(challenge.getTitrec());
        titrBack.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1a3d4f;");
        titrBack.setWrapText(true);

        Label lblExamen = new Label("📋  Examen : #" + challenge.getExamen_id());
        lblExamen.setStyle("-fx-font-size:12px; -fx-text-fill:#444;");

        Label lblObjectif = new Label("🎯  Objectif : " + (int) challenge.getObjectifscore() + " pts");
        lblObjectif.setStyle("-fx-font-size:12px; -fx-text-fill:#444;");

        Label lblRecompense = new Label("🎁  Récompense : " + nvl(challenge.getTyperecomponse()));
        lblRecompense.setStyle("-fx-font-size:12px; -fx-text-fill:#444;");

        Label lblProg = new Label("Progression : " + (int) challenge.getProgressionactuelle() + "%");
        lblProg.setStyle("-fx-font-size:11px; -fx-text-fill:#666;");

        ProgressBar pb = new ProgressBar(challenge.getProgressionactuelle() / 100.0);
        pb.setPrefWidth(220);
        pb.setStyle("-fx-accent:#1D9E75;");

        Region spacerBack = new Region();
        VBox.setVgrow(spacerBack, Priority.ALWAYS);

        Button btnChoisir = new Button("✅  Choisir ce challenge");
        btnChoisir.setStyle(
                "-fx-background-color:#1D9E75; -fx-text-fill:white;" +
                        "-fx-font-size:13px; -fx-font-weight:bold;" +
                        "-fx-background-radius:8; -fx-cursor:hand;" +
                        "-fx-pref-width:220px; -fx-pref-height:36px;"
        );
        btnChoisir.setOnAction(e -> choisirChallenge(challenge));
        btnChoisir.setOnMouseClicked(e -> e.consume());

        back.getChildren().addAll(titrBack, lblExamen, lblObjectif, lblRecompense, lblProg, pb, spacerBack, btnChoisir);

        // ── STACK ─────────────────────────────────────────────
        StackPane card = new StackPane(front, back);
        card.setPrefSize(260, 300);
        card.setStyle("-fx-cursor:hand;");

        // ── ANIMATION FLIP ────────────────────────────────────
        final boolean[] flipped = {false};

        card.setOnMouseClicked(e -> {
            if (flipped[0]) {
                RotateTransition r1 = new RotateTransition(Duration.millis(150), back);
                r1.setFromAngle(0); r1.setToAngle(90);
                r1.setOnFinished(ev -> {
                    back.setVisible(false);
                    front.setVisible(true);
                    RotateTransition r2 = new RotateTransition(Duration.millis(150), front);
                    r2.setFromAngle(-90); r2.setToAngle(0);
                    r2.play();
                });
                r1.play();
            } else {
                RotateTransition r1 = new RotateTransition(Duration.millis(150), front);
                r1.setFromAngle(0); r1.setToAngle(90);
                r1.setOnFinished(ev -> {
                    front.setVisible(false);
                    back.setVisible(true);
                    RotateTransition r2 = new RotateTransition(Duration.millis(150), back);
                    r2.setFromAngle(-90); r2.setToAngle(0);
                    r2.play();
                });
                r1.play();
            }
            flipped[0] = !flipped[0];
        });

        return card;
    }

    // ── Filtres ───────────────────────────────────────────────
    @FXML private void filtrerTous() {
        resetFiltres(btnTous);
        afficherCartes(allChallenges);
    }
    @FXML private void filtrerFacile() {
        resetFiltres(btnFacile);
        afficherCartes(allChallenges.stream().filter(c -> "Facile".equals(c.getNiveaudifficulte())).toList());
    }
    @FXML private void filtrerMoyen() {
        resetFiltres(btnMoyen);
        afficherCartes(allChallenges.stream().filter(c -> "Moyen".equals(c.getNiveaudifficulte())).toList());
    }
    @FXML private void filtrerDifficile() {
        resetFiltres(btnDifficile);
        afficherCartes(allChallenges.stream().filter(c -> "Difficile".equals(c.getNiveaudifficulte())).toList());
    }

    private void resetFiltres(Button actif) {
        for (Button b : new Button[]{btnTous, btnFacile, btnMoyen, btnDifficile}) {
            b.setStyle("-fx-background-color:white; -fx-text-fill:#1a3d4f; -fx-border-color:#ccc;" +
                    " -fx-border-radius:20; -fx-background-radius:20; -fx-padding:6 16; -fx-cursor:hand;");
        }
        actif.setStyle("-fx-background-color:#1D9E75; -fx-text-fill:white; -fx-border-color:#1D9E75;" +
                " -fx-border-radius:20; -fx-background-radius:20; -fx-padding:6 16; -fx-cursor:hand;");
    }

    // ── Action choisir ────────────────────────────────────────
    private void choisirChallenge(Challenge c) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/ChallengeUI.fxml")
            );
            Parent root = loader.load();

            ChallengeUIController ctrl = loader.getController();
            ctrl.setChallenge(c);

            Stage stage = new Stage();
            stage.setTitle("Challenge : " + c.getTitrec());
            stage.setScene(new Scene(root));
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur ouverture challenge : " + e.getMessage());
        }
    }

    // ── Navigation ────────────────────────────────────────────
    // Fichiers FXML disponibles :
    // front.fxml, EvaluationFront.fxml, ExamenFront.fxml, ExamenView.fxml,
    // quiz.fxml, quiz_front.fxml, reponse.fxml, dashboard.fxml, home.fxml

    @FXML private void goToAccueil()       { naviguer("/org/example/fxml/front.fxml"); }
    @FXML private void goToCours()         { pageEnDeveloppement("Cours"); }           // cours.fxml n'existe pas
    @FXML private void goToEvaluation()    { naviguer("/org/example/fxml/EvaluationFront.fxml"); }
    @FXML private void goToQuestionnaire() { naviguer("/org/example/fxml/quiz_front.fxml"); }  // ✅ quiz_front.fxml
    @FXML private void goToOrientation()   { pageEnDeveloppement("Orientation"); }     // orientation.fxml n'existe pas
    @FXML private void goToForum()         { pageEnDeveloppement("Forum"); }            // forum.fxml n'existe pas
    @FXML private void goToConnexion()     { naviguer("/org/example/fxml/home.fxml"); } // ✅ home.fxml

    private void naviguer(String fxml) {
        try {
            // ✅ FIX : vérification null avant FXMLLoader.load()
            URL url = getClass().getResource(fxml);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + fxml);
                new Alert(Alert.AlertType.ERROR,
                        "Page introuvable : " + fxml, ButtonType.OK).showAndWait();
                return;
            }
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Erreur navigation vers " + fxml + " : " + e.getMessage());
            new Alert(Alert.AlertType.ERROR,
                    "Erreur lors de la navigation : " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private void pageEnDeveloppement(String nomPage) {
        new Alert(Alert.AlertType.INFORMATION,
                "La page « " + nomPage + " » est en cours de développement.",
                ButtonType.OK).showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────
    private String nvl(String s) { return s != null ? s : "—"; }

    private String badgeStyle(String niveau) {
        return switch (niveau != null ? niveau : "") {
            case "Facile"    -> "-fx-background-color:#EAF3DE; -fx-text-fill:#3B6D11; -fx-background-radius:20; -fx-padding:3 10; -fx-font-size:12px;";
            case "Moyen"     -> "-fx-background-color:#FAEEDA; -fx-text-fill:#854F0B; -fx-background-radius:20; -fx-padding:3 10; -fx-font-size:12px;";
            case "Difficile" -> "-fx-background-color:#FCEBEB; -fx-text-fill:#A32D2D; -fx-background-radius:20; -fx-padding:3 10; -fx-font-size:12px;";
            default          -> "-fx-background-color:#eee;    -fx-text-fill:#555;    -fx-background-radius:20; -fx-padding:3 10; -fx-font-size:12px;";
        };
    }
}
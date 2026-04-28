package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.Services.ServiceLeaderboard;
import org.example.entities.Challenge;
import org.example.entities.LeaderboardEntry;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class LeaderboardController implements Initializable {

    @FXML private Label                                    lblTitre;
    @FXML private Label                                    lblSousTitre;
    @FXML private VBox                                     podiumContainer;
    @FXML private VBox                                     listContainer;
    @FXML private TableView<LeaderboardEntry>              leaderboardTable;
    @FXML private TableColumn<LeaderboardEntry, Integer>   colRang;
    @FXML private TableColumn<LeaderboardEntry, Integer>   colUserId;
    @FXML private TableColumn<LeaderboardEntry, Integer>   colScore;
    @FXML private TableColumn<LeaderboardEntry, String>    colBadge;
    @FXML private TableColumn<LeaderboardEntry, String>    colDate;

    private final ServiceLeaderboard serviceLeaderboard = new ServiceLeaderboard();
    private Challenge challenge;

    public void setChallenge(Challenge c) {
        this.challenge = c;
        lblTitre.setText("Leaderboard");
        lblSousTitre.setText("Challenge : " + c.getTitrec());
        loadData();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
    }

    // ── Chargement et dispatch ────────────────────────────
    private void loadData() {
        try {
            List<LeaderboardEntry> list = serviceLeaderboard.getTop10(challenge.getId());
            if (list.isEmpty()) {
                podiumContainer.getChildren().add(emptyLabel());
                return;
            }
            buildPodium(list);
            buildList(list);
        } catch (SQLException e) {
            System.err.println("Erreur chargement leaderboard : " + e.getMessage());
        }
    }

    // ── Podium top 3 ──────────────────────────────────────
    private void buildPodium(List<LeaderboardEntry> list) {
        podiumContainer.getChildren().clear();

        // Ordre visuel : 2 - 1 - 3
        int[] order = list.size() >= 3 ? new int[]{1, 0, 2} :
                list.size() == 2 ? new int[]{1, 0}    : new int[]{0};

        HBox podiumRow = new HBox(16);
        podiumRow.setAlignment(Pos.BOTTOM_CENTER);

        for (int idx : order) {
            if (idx >= list.size()) continue;
            LeaderboardEntry entry = list.get(idx);
            int rang = idx + 1;
            podiumRow.getChildren().add(buildSlot(entry, rang));
        }

        podiumContainer.getChildren().add(podiumRow);
    }

    private VBox buildSlot(LeaderboardEntry entry, int rang) {
        VBox slot = new VBox(0);
        slot.setAlignment(Pos.BOTTOM_CENTER);

        // ── Avatar ────────────────────────────────────────
        StackPane avatarWrap = new StackPane();
        avatarWrap.setPrefSize(rang == 1 ? 70 : rang == 2 ? 60 : 54,
                rang == 1 ? 70 : rang == 2 ? 60 : 54);
        VBox.setMargin(avatarWrap, new javafx.geometry.Insets(0, 0, 8, 0));

        Label avatar = new Label("U" + entry.getUserId());
        double size = rang == 1 ? 64 : rang == 2 ? 54 : 50;
        avatar.setPrefSize(size, size);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle(
                "-fx-background-radius:99; -fx-font-weight:bold;" +
                        "-fx-font-size:" + (rang == 1 ? 17 : 13) + "px;" +
                        "-fx-border-radius:99; -fx-border-width:2.5;" +
                        switch (rang) {
                            case 1 -> "-fx-background-color:#FAEEDA; -fx-text-fill:#854F0B; -fx-border-color:#FAC775;";
                            case 2 -> "-fx-background-color:#E6F1FB; -fx-text-fill:#185FA5; -fx-border-color:#85B7EB;";
                            default -> "-fx-background-color:#FAECE7; -fx-text-fill:#712B13; -fx-border-color:#F0997B;";
                        }
        );

        Label medal = new Label(String.valueOf(rang));
        medal.setPrefSize(20, 20);
        medal.setAlignment(Pos.CENTER);
        medal.setStyle(
                "-fx-background-radius:99; -fx-font-size:11px; -fx-font-weight:bold;" +
                        switch (rang) {
                            case 1 -> "-fx-background-color:#FAC775; -fx-text-fill:#633806;";
                            case 2 -> "-fx-background-color:#D3D1C7; -fx-text-fill:#2C2C2A;";
                            default -> "-fx-background-color:#F5C4B3; -fx-text-fill:#4A1B0C;";
                        }
        );
        StackPane.setAlignment(medal, Pos.BOTTOM_RIGHT);

        avatarWrap.getChildren().addAll(avatar, medal);

        // ── Nom ───────────────────────────────────────────
        Label name = new Label("Utilisateur " + entry.getUserId());
        name.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:-color-text-primary;");
        name.setAlignment(Pos.CENTER);
        VBox.setMargin(name, new javafx.geometry.Insets(0, 0, 4, 0));

        // ── Score ─────────────────────────────────────────
        Label score = new Label(entry.getScore() + "%");
        score.setStyle(
                "-fx-font-size:12px; -fx-font-weight:bold;" +
                        switch (rang) {
                            case 1 -> "-fx-text-fill:#854F0B;";
                            case 2 -> "-fx-text-fill:#185FA5;";
                            default -> "-fx-text-fill:#712B13;";
                        }
        );
        score.setAlignment(Pos.CENTER);
        VBox.setMargin(score, new javafx.geometry.Insets(0, 0, 8, 0));

        // ── Piédestal ─────────────────────────────────────
        StackPane pedestal = new StackPane();
        double pw = rang == 1 ? 110 : rang == 2 ? 96 : 88;
        double ph = rang == 1 ? 80  : rang == 2 ? 60 : 44;
        pedestal.setPrefSize(pw, ph);
        pedestal.setStyle(
                "-fx-background-radius:8 8 0 0;" +
                        switch (rang) {
                            case 1 -> "-fx-background-color:#FAEEDA;";
                            case 2 -> "-fx-background-color:#E6F1FB;";
                            default -> "-fx-background-color:#FAECE7;";
                        }
        );

        Label numPedestal = new Label(String.valueOf(rang));
        numPedestal.setStyle(
                "-fx-font-size:22px; -fx-font-weight:bold; -fx-padding:0 0 10 0;" +
                        switch (rang) {
                            case 1 -> "-fx-text-fill:#854F0B;";
                            case 2 -> "-fx-text-fill:#185FA5;";
                            default -> "-fx-text-fill:#712B13;";
                        }
        );
        StackPane.setAlignment(numPedestal, Pos.BOTTOM_CENTER);
        pedestal.getChildren().add(numPedestal);

        slot.getChildren().addAll(avatarWrap, name, score, pedestal);
        return slot;
    }

    // ── Liste rang 4+ ─────────────────────────────────────
    private void buildList(List<LeaderboardEntry> list) {
        listContainer.getChildren().clear();
        if (list.size() <= 3) return;

        Separator sep = new Separator();
        VBox.setMargin(sep, new javafx.geometry.Insets(0, 0, 12, 0));
        listContainer.getChildren().add(sep);

        String[] avatarColors = {
                "-fx-background-color:#E1F5EE; -fx-text-fill:#0F6E56;",
                "-fx-background-color:#F1EFE8; -fx-text-fill:#5F5E5A;",
                "-fx-background-color:#EEEDFE; -fx-text-fill:#3C3489;",
                "-fx-background-color:#FBEAF0; -fx-text-fill:#72243E;",
                "-fx-background-color:#EAF3DE; -fx-text-fill:#27500A;",
                "-fx-background-color:#FCEBEB; -fx-text-fill:#791F1F;",
                "-fx-background-color:#FAEEDA; -fx-text-fill:#633806;",
        };

        for (int i = 3; i < list.size(); i++) {
            LeaderboardEntry entry = list.get(i);
            int rang = i + 1;

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                    "-fx-padding:10 14; -fx-background-radius:8;" +
                            "-fx-border-radius:8; -fx-border-width:0.5;" +
                            "-fx-border-color:-color-border-tertiary;" +
                            "-fx-background-color:-color-background-primary;"
            );
            VBox.setMargin(row, new javafx.geometry.Insets(0, 0, 8, 0));

            // Rang
            Label rangLbl = new Label(String.valueOf(rang));
            rangLbl.setPrefWidth(24);
            rangLbl.setAlignment(Pos.CENTER);
            rangLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:-color-text-secondary;");

            // Avatar
            Label av = new Label("U" + entry.getUserId());
            av.setPrefSize(34, 34);
            av.setAlignment(Pos.CENTER);
            av.setStyle("-fx-background-radius:99; -fx-font-size:12px; -fx-font-weight:bold; " +
                    avatarColors[(i - 3) % avatarColors.length]);

            // Nom
            Label name = new Label("Utilisateur " + entry.getUserId());
            name.setStyle("-fx-font-size:13px; -fx-font-weight:bold;");
            HBox.setHgrow(name, Priority.ALWAYS);

            // Barre score
            int score = entry.getScore();
            String barColor = score >= 80 ? "#1D9E75" :
                    score >= 60 ? "#185FA5" :
                    score >= 40 ? "#BA7517" : "#888780";

            ProgressBar bar = new ProgressBar(score / 100.0);
            bar.setPrefWidth(90);
            bar.setPrefHeight(6);
            bar.setStyle("-fx-accent:" + barColor + ";");

            Label scoreLbl = new Label(score + "%");
            scoreLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + barColor + ";");
            scoreLbl.setPrefWidth(34);

            // Badge
            String badgeText = entry.getBadges().replaceAll("[^\\p{L}\\p{N} ]", "").trim();
            Label badge = badgeLabel(badgeText);

            row.getChildren().addAll(rangLbl, av, name, bar, scoreLbl, badge);
            listContainer.getChildren().add(row);
        }
    }

    // ── Badge pill ────────────────────────────────────────
    private Label badgeLabel(String badge) {
        Label l = new Label(badge);
        l.setStyle(
                "-fx-font-size:11px; -fx-font-weight:bold;" +
                        "-fx-padding:2 10; -fx-background-radius:99;" +
                        (badge.contains("Expert")    ? "-fx-background-color:#FAEEDA; -fx-text-fill:#854F0B;" :
                                badge.contains("Compétent") ? "-fx-background-color:#E1F5EE; -fx-text-fill:#0F6E56;" :
                                badge.contains("Apprenti")  ? "-fx-background-color:#E6F1FB; -fx-text-fill:#185FA5;" :
                                "-fx-background-color:#F1EFE8; -fx-text-fill:#5F5E5A;")
        );
        return l;
    }

    // ── Setup tableau (fallback si pas de podiumContainer) ─
    private void setupTableColumns() {
        if (colRang == null) return;

        colRang.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                setText(String.valueOf(getIndex() + 1));
                setAlignment(Pos.CENTER);
            }
        });

        colUserId.setCellValueFactory(d ->
                new javafx.beans.property.SimpleIntegerProperty(d.getValue().getUserId()).asObject());
        colScore.setCellValueFactory(d ->
                new javafx.beans.property.SimpleIntegerProperty(d.getValue().getScore()).asObject());
        colBadge.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getBadges()));
        colDate.setCellValueFactory(d -> {
            var ts = d.getValue().getCreatedAt();
            if (ts == null) return new javafx.beans.property.SimpleStringProperty("—");
            return new javafx.beans.property.SimpleStringProperty(
                    new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH).format(ts));
        });
    }

    private Label emptyLabel() {
        Label l = new Label("Aucun score enregistré pour ce challenge.");
        l.setStyle("-fx-text-fill:-color-text-secondary; -fx-font-size:13px; -fx-padding:40 0;");
        l.setAlignment(Pos.CENTER);
        return l;
    }

    @FXML
    private void handleFermer() {
        ((Stage) podiumContainer.getScene().getWindow()).close();
    }
}
package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.Services.ServiceCours;
import org.example.Services.ServiceMatiere;
import org.example.entities.Cours;
import org.example.entities.Matiere;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for the student (étudiant) view.
 * Shows matière catalog and course listings in a read-only, browsing mode.
 */
public class EtudiantCoursController {

    @FXML private VBox contentArea;
    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private ImageView logo;

    private final ServiceMatiere serviceMatiere = new ServiceMatiere();
    private final ServiceCours serviceCours = new ServiceCours();

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/images/";
//Cette méthode s’exécute automatiquement au lancement
    @FXML
    public void initialize() {
        try {
            logo.setImage(new Image(getClass().getResourceAsStream("/org/example/images/logo1.png")));
        } catch (Exception ignored) {}
        loadMatiereCatalog(); //Affiche les matières dès le début
    }

    @FXML
    private void goToAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/front.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 900));
            stage.setTitle("LearnFlex+");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void goToConnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 900));
            stage.setTitle("Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════
    //  MATIÈRE CATALOG (Student browsing)
    // ══════════════════════════════════════════════
    private void loadMatiereCatalog() {
        contentArea.getChildren().clear();

        // Hero section
        VBox hero = new VBox(12);
        hero.setAlignment(Pos.CENTER);
        hero.setPadding(new Insets(50, 40, 50, 40));
        hero.setStyle("-fx-background-color: linear-gradient(to right, #065f46, #10b981); -fx-background-radius: 20;");

        Label heroTitle = new Label("📚 Catalogue des Matières");
        heroTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label heroSub = new Label("Explorez nos matières et découvrez les cours disponibles");
        heroSub.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.85);");

        Button backToHomeBtn = new Button("← Retour à l'accueil");
        backToHomeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 0;");
        backToHomeBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/front.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setScene(new Scene(root, 1200, 900));
                stage.setTitle("LearnFlex+");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        hero.getChildren().addAll(backToHomeBtn, heroTitle, heroSub);

        // Search bar
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER);
        searchContainer.setMaxWidth(500);
        searchContainer.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-padding: 8 20;");
        Label searchIcon = new Label("🔍");
        TextField searchInput = new TextField();
        searchInput.setPromptText("Rechercher une matière...");
        searchInput.setPrefWidth(400);
        searchInput.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        searchContainer.getChildren().addAll(searchIcon, searchInput);
        hero.getChildren().add(searchContainer);

        contentArea.getChildren().add(hero);

        // Grid
        FlowPane grid = new FlowPane();
        grid.setHgap(25);
        grid.setVgap(25);
        grid.setPadding(new Insets(25, 0, 25, 0));
        grid.setPrefWrapLength(900);

        try {
            List<Matiere> allMatieres = serviceMatiere.recuperer();
            
            searchInput.setOnKeyReleased(e -> {
                String query = searchInput.getText().toLowerCase().trim();
                grid.getChildren().clear();
                List<Matiere> filtered = allMatieres.stream()
                    .filter(m -> m.getNomMatiere().toLowerCase().contains(query) || m.getSection().toLowerCase().contains(query))
                    .toList();
                
                if (filtered.isEmpty()) {
                    Label noResult = new Label("Aucune matière correspondante.");
                    noResult.setStyle("-fx-text-fill: white; -fx-font-style: italic;");
                    grid.getChildren().add(noResult);
                } else {
                    for (Matiere m : filtered) grid.getChildren().add(createMatiereCatalogCard(m));
                }
            });

            if (allMatieres.isEmpty()) {
                VBox emptyBox = new VBox(15);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(50));
                emptyBox.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
                Label icon = new Label("📂");
                icon.setStyle("-fx-font-size: 48px;");
                Label msg = new Label("Aucune matière disponible pour le moment.");
                msg.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b;");
                emptyBox.getChildren().addAll(icon, msg);
                grid.getChildren().add(emptyBox);
            } else {
                for (Matiere m : allMatieres) {
                    grid.getChildren().add(createMatiereCatalogCard(m));
                }
            }
        } catch (SQLException e) {
            Label err = new Label("Erreur: " + e.getMessage());
            err.setStyle("-fx-text-fill: #e11d48;");
            grid.getChildren().add(err);
        }

        contentArea.getChildren().add(grid);
    }

    private VBox createMatiereCatalogCard(Matiere matiere) {
        VBox card = new VBox();
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3); -fx-cursor: hand;");

        // Image
        StackPane imageBox = new StackPane();
        imageBox.setPrefHeight(200);
        imageBox.setMinHeight(200);
        imageBox.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 16 16 0 0;");

        if (matiere.getImage() != null && !matiere.getImage().isEmpty()) {
            try {
                File imgFile = new File(UPLOAD_DIR + matiere.getImage());
                if (imgFile.exists()) {
                    ImageView iv = new ImageView(new Image(imgFile.toURI().toString()));
                    iv.setFitWidth(300);
                    iv.setFitHeight(200);
                    iv.setPreserveRatio(false);
                    iv.setSmooth(true);
                    imageBox.getChildren().add(iv);
                }
            } catch (Exception ignored) {}
        } else {
            Label placeholder = new Label("🖼");
            placeholder.setStyle("-fx-font-size: 48px; -fx-text-fill: #94a3b8;");
            imageBox.getChildren().add(placeholder);
        }

        // Section badge
        Label sectionBadge = new Label(matiere.getSection());
        sectionBadge.setStyle("-fx-background-color: rgba(151,195,162,0.9); -fx-text-fill: white; " +
                "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        StackPane.setAlignment(sectionBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(sectionBadge, new Insets(12, 12, 0, 0));
        imageBox.getChildren().add(sectionBadge);

        // Content
        VBox content = new VBox(8);
        content.setPadding(new Insets(20));

        Label title = new Label(matiere.getNomMatiere());
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        title.setWrapText(true);

        String descText = matiere.getDescription();
        if (descText != null && descText.length() > 100) descText = descText.substring(0, 100) + "...";
        Label desc = new Label(descText);
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-line-spacing: 2;");
        desc.setWrapText(true);

        content.getChildren().addAll(title, desc);

        // Footer
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(14, 20, 14, 20));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #f1f5f9; -fx-border-width: 1 0 0 0;");

        Label niveau = new Label("🎓 " + matiere.getNiveau());
        niveau.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        // Cours count
        int coursCount = 0;
        try { coursCount = serviceMatiere.compterCours(matiere.getId()); } catch (Exception ignored) {}
        Label countLabel = new Label("📚 " + coursCount + " cours");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("Voir les cours →");
        arrow.setStyle("-fx-text-fill: #97c3a2; -fx-font-weight: bold; -fx-font-size: 12px;");

        footer.getChildren().addAll(niveau, countLabel, spacer, arrow);

        card.getChildren().addAll(imageBox, content, footer);

        // Click handler - show courses for this matiere
        card.setOnMouseClicked(e -> showCoursForMatiere(matiere));

        // Hover
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + " -fx-translate-y: -8;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace(" -fx-translate-y: -8;", "")));

        return card;
    }

    // ══════════════════════════════════════════════
    //  COURSE LISTING (Student view)
    // ══════════════════════════════════════════════
    private void showCoursForMatiere(Matiere matiere) {
        contentArea.getChildren().clear();

        // Hero
        VBox hero = new VBox(12);
        hero.setAlignment(Pos.CENTER);
        hero.setPadding(new Insets(50, 40, 50, 40));
        hero.setStyle("-fx-background-color: linear-gradient(to right, #065f46, #10b981); -fx-background-radius: 20;");

        Label badge = new Label(matiere.getSection());
        badge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-padding: 6 16; -fx-background-radius: 12; -fx-font-size: 12px;");

        Label heroTitle = new Label(matiere.getNomMatiere());
        heroTitle.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label heroDesc = new Label(matiere.getDescription());
        heroDesc.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.85); -fx-wrap-text: true; -fx-text-alignment: center;");
        heroDesc.setWrapText(true);
        heroDesc.setMaxWidth(600);

        hero.getChildren().addAll(badge, heroTitle, heroDesc);

        // Search bar
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER);
        searchContainer.setMaxWidth(500);
        searchContainer.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-padding: 8 20;");
        Label searchIcon = new Label("🔍");
        TextField searchInput = new TextField();
        searchInput.setPromptText("Rechercher un cours...");
        searchInput.setPrefWidth(400);
        searchInput.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        searchContainer.getChildren().addAll(searchIcon, searchInput);
        hero.getChildren().add(searchContainer);

        // Container for course list to update on search
        VBox courseListContainer = new VBox(20);

        // Back button
        Button backBtn = new Button("← Retour au catalogue");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-cursor: hand; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 0;");
        backBtn.setOnAction(e -> loadMatiereCatalog());

        contentArea.getChildren().addAll(hero, backBtn, courseListContainer);

        // Course list
        try {
            List<Cours> allCours = serviceCours.recupererParMatiere(matiere.getId());

            searchInput.setOnKeyReleased(e -> {
                String query = searchInput.getText().toLowerCase().trim();
                courseListContainer.getChildren().clear();
                List<Cours> filtered = allCours.stream()
                    .filter(c -> c.getTitre().toLowerCase().contains(query) || c.getDescription().toLowerCase().contains(query))
                    .toList();
                
                if (filtered.isEmpty()) {
                    Label noResult = new Label("Aucun cours ne correspond à votre recherche.");
                    noResult.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                    courseListContainer.getChildren().add(noResult);
                } else {
                    for (Cours c : filtered) courseListContainer.getChildren().add(createCourseListItem(c));
                }
            });

            if (allCours.isEmpty()) {
                VBox emptyBox = new VBox(15);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(60));
                emptyBox.setStyle("-fx-background-color: white; -fx-background-radius: 20;");

                Label icon = new Label("🎓");
                icon.setStyle("-fx-font-size: 48px;");
                Label msg = new Label("Aucun cours n'est encore disponible pour cette matière.");
                msg.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b;");

                emptyBox.getChildren().addAll(icon, msg);
                courseListContainer.getChildren().add(emptyBox);
            } else {
                for (Cours cours : allCours) {
                    courseListContainer.getChildren().add(createCourseListItem(cours));
                }
            }
        } catch (SQLException e) {
            Label err = new Label("Erreur: " + e.getMessage());
            err.setStyle("-fx-text-fill: #e11d48;");
            contentArea.getChildren().add(err);
        }
    }

    private HBox createCourseListItem(Cours cours) {
        HBox card = new HBox(25);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2); " +
                "-fx-border-color: transparent; -fx-border-width: 1; -fx-border-radius: 20;");

        // Image
        StackPane imageBox = new StackPane();
        imageBox.setPrefSize(200, 130);
        imageBox.setMinSize(200, 130);
        imageBox.setMaxSize(200, 130);
        imageBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12;");

        if (cours.getImage() != null && !cours.getImage().isEmpty()) {
            try {
                File imgFile = new File(UPLOAD_DIR + cours.getImage());
                if (imgFile.exists()) {
                    ImageView iv = new ImageView(new Image(imgFile.toURI().toString()));
                    iv.setFitWidth(200);
                    iv.setFitHeight(130);
                    iv.setPreserveRatio(false);
                    iv.setSmooth(true);
                    iv.setStyle("-fx-background-radius: 12;");
                    imageBox.getChildren().add(iv);
                }
            } catch (Exception ignored) {}
        } else {
            Label placeholder = new Label("▶");
            placeholder.setStyle("-fx-font-size: 36px; -fx-text-fill: #cbd5e1;");
            imageBox.getChildren().add(placeholder);
        }

        // Info
        VBox info = new VBox(8);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(cours.getTitre());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        title.setWrapText(true);

        Label desc = new Label(cours.getDescription());
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-line-spacing: 2;");
        desc.setWrapText(true);
        desc.setMaxHeight(50);

        HBox meta = new HBox(20);
        meta.setPadding(new Insets(8, 0, 0, 0));

        Label duration = new Label("⏱ " + (cours.getDureeTotale() != null ? cours.getDureeTotale() : "N/A"));
        duration.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label langue = new Label("🌐 " + (cours.getLangue() != null ? cours.getLangue() : "N/A"));
        langue.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label prix = new Label("💰 " + String.format("%.2f USD", cours.getPrix()));
        prix.setStyle("-fx-font-size: 13px; -fx-text-fill: #97c3a2; -fx-font-weight: bold;");

        meta.getChildren().addAll(duration, langue, prix);

        info.getChildren().addAll(title, desc, meta);

        // Action buttons
        VBox actionBox = new VBox(10);
        actionBox.setAlignment(Pos.CENTER);

        Button viewBtn = new Button("📖 Voir le cours");
        viewBtn.setStyle("-fx-background-color: #97c3a2; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-padding: 12 24; -fx-cursor: hand; -fx-font-size: 12px;");
        viewBtn.setOnAction(e -> showCoursDetail(cours));

        actionBox.getChildren().add(viewBtn);

        if (cours.getPdfFile() != null && !cours.getPdfFile().isEmpty()) {
            Button pdfBtn = new Button("📄 PDF");
            pdfBtn.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #7c3aed); -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-size: 11px;");
            pdfBtn.setOnAction(e -> openPdf(cours.getPdfFile()));
            actionBox.getChildren().add(pdfBtn);
        }

        card.getChildren().addAll(imageBox, info, actionBox);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() +
                " -fx-border-color: #97c3a2; -fx-translate-x: 3;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle()
                .replace(" -fx-border-color: #97c3a2; -fx-translate-x: 3;", "")));

        return card;
    }

    private void showCoursDetail(Cours cours) {
        contentArea.getChildren().clear();

        Matiere matiere = null;
        try {
            matiere = serviceMatiere.recupererParId(cours.getMatiereId());
        } catch(Exception ignored) {}
        Matiere finalMatiere = matiere;

        // 1. Bouton Retour
        Button backBtn = new Button("← Retour aux cours");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-cursor: hand; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0 20 0;");
        backBtn.setOnAction(e -> {
            if(finalMatiere != null) showCoursForMatiere(finalMatiere);
            else loadMatiereCatalog();
        });

        // 2. Grille principale (HBox divisée en Left Hero et Right Sidebar)
        HBox mainLayout = new HBox(40);
        mainLayout.setAlignment(Pos.TOP_LEFT);

        // --- Colonne de gauche (Hero info de type Udemy) ---
        VBox heroInfo = new VBox(20);
        heroInfo.setPrefWidth(700);
        heroInfo.setStyle("-fx-background-color: #1e293b; -fx-padding: 40; -fx-background-radius: 12;");

        Label sectionBadge = new Label(cours.getSection() != null ? cours.getSection() : "Cours");
        sectionBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-padding: 6 12; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label title = new Label(cours.getTitre());
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-wrap-text: true;");

        Label desc = new Label(cours.getDescription());
        desc.setStyle("-fx-font-size: 16px; -fx-text-fill: #cbd5e1; -fx-wrap-text: true; -fx-line-spacing: 4;");

        HBox meta = new HBox(25);
        Label duration = new Label("⏱ Durée: " + (cours.getDureeTotale() != null ? cours.getDureeTotale() : "N/A"));
        duration.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label langue = new Label("🌐 Langue: " + (cours.getLangue() != null ? cours.getLangue() : "N/A"));
        langue.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        meta.getChildren().addAll(duration, langue);

        heroInfo.getChildren().addAll(sectionBadge, title, desc, meta);

        // --- Colonne de droite (Sidebar Sticky) ---
        VBox sidebarCard = new VBox(20);
        sidebarCard.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5); -fx-border-color: #f1f5f9; -fx-border-width: 1; -fx-border-radius: 12;");
        sidebarCard.setPrefWidth(350);

        StackPane imgCover = new StackPane();
        imgCover.setPrefHeight(180);
        imgCover.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8;");
        if (cours.getImage() != null && !cours.getImage().isEmpty()) {
            try {
                File imgFile = new File(UPLOAD_DIR + cours.getImage());
                if (imgFile.exists()) {
                    ImageView iv = new ImageView(new Image(imgFile.toURI().toString()));
                    iv.setFitWidth(310);
                    iv.setFitHeight(180);
                    iv.setPreserveRatio(false);
                    iv.setSmooth(true);
                    imgCover.getChildren().add(iv);
                }
            } catch (Exception ignored) {}
        } else {
            Label placeholder = new Label("▶");
            placeholder.setStyle("-fx-font-size: 48px; -fx-text-fill: #cbd5e1;");
            imgCover.getChildren().add(placeholder);
        }

        Label price = new Label(String.format("%.2f USD", cours.getPrix()));
        price.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Button buyBtn = new Button("🛒 Ajouter au panier");
        buyBtn.setStyle("-fx-background-color: #97c3a2; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-padding: 15; -fx-background-radius: 8; -fx-cursor: hand;");
        buyBtn.setMaxWidth(Double.MAX_VALUE);

        VBox sidebarContent = new VBox(20, imgCover, price, buyBtn);
        sidebarContent.setAlignment(Pos.CENTER_LEFT);

        if (cours.getPdfFile() != null && !cours.getPdfFile().isEmpty()) {
            Button pdfBtn = new Button("📄 Consulter les Ressources PDF");
            pdfBtn.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #7c3aed); -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;");
            pdfBtn.setMaxWidth(Double.MAX_VALUE);
            pdfBtn.setOnAction(e -> openPdf(cours.getPdfFile()));
            sidebarContent.getChildren().add(pdfBtn);
        }

        // Feature checklist
        VBox features = new VBox(10);
        String[] perks = {"✓ Accès illimité à vie", "✓ Certificat de fin de cours", "✓ Accès sur mobile et TV"};
        for(String perk : perks) {
            Label p = new Label(perk);
            p.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
            features.getChildren().add(p);
        }
        sidebarContent.getChildren().add(features);

        sidebarCard.getChildren().add(sidebarContent);

        mainLayout.getChildren().addAll(heroInfo, sidebarCard);

        contentArea.getChildren().addAll(backBtn, mainLayout);
    }

    private void openPdf(String pdfFile) {
        try {
            File file = new File(UPLOAD_DIR + pdfFile);
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Fichier introuvable");
                alert.setContentText("Le fichier PDF n'a pas été trouvé.");
                alert.showAndWait();
            }
        } catch (IOException e) {
            System.err.println("Erreur ouverture PDF: " + e.getMessage());
        }
    }
}

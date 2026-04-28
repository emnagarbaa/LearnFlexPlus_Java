package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Services.ServiceMatiere;
import org.example.entities.Matiere;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MatiereController {

    @FXML private FlowPane matiereGrid;
    @FXML private Label headerTitle;
    @FXML private Label headerSubtitle;
    @FXML private TextField searchField;

    @FXML private VBox formContainer;
    @FXML private Label formTitle;
    @FXML private TextField tfNom;
    @FXML private TextArea taDesc;
    @FXML private TextField tfSection;
    @FXML private TextField tfCode;
    @FXML private ComboBox<String> cbNiveau;
    @FXML private Label lblImageName;
    @FXML private Button btnSave;

    @FXML private Label errNom;
    @FXML private Label errDesc;
    @FXML private Label errSection;
    @FXML private Label errCode;
    @FXML private Label errNiveau;

    private Matiere editingMatiere = null;
    private String selectedImagePath = null;
    private List<Matiere> allMatieres; // Store for filtering

    private final ServiceMatiere serviceMatiere = new ServiceMatiere();
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/images/";

    // Initialisation du contrôleur : crée le dossier d'images et configure le sélecteur de niveau
    @FXML
    public void initialize() {
        // Ensure upload directory exists
        new File(UPLOAD_DIR).mkdirs();
        if (cbNiveau != null) {
            cbNiveau.getItems().addAll("Bac 1ère année", "Bac 2ème année", "Bac 3ème année", "Licence", "Master");
        }
        loadMatieres();
    }

    // Charge toutes les matières depuis le service et demande l'affichage
    private void loadMatieres() {
        if (matiereGrid == null) return;
        try {
            allMatieres = serviceMatiere.recuperer();
            displayMatieres(allMatieres);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des matières: " + e.getMessage());
        }
    }

    // Nettoie la grille et affiche les cartes graphiques pour chaque matière fournie
    private void displayMatieres(List<Matiere> list) {
        matiereGrid.getChildren().clear();
        if (list.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            emptyBox.setPrefWidth(800);
            Label empty = new Label("📂 Aucune matière correspondante.");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
            emptyBox.getChildren().add(empty);
            matiereGrid.getChildren().add(emptyBox);
        } else {
            for (Matiere m : list) {
                matiereGrid.getChildren().add(createMatiereCard(m));
            }
        }
    }

    // Filtre la liste des matières en fonction du texte saisi dans la barre de recherche
    @FXML
    public void handleSearch() {
        if (allMatieres == null) return;
        String query = searchField.getText().toLowerCase().trim();
        List<Matiere> filtered = allMatieres.stream()
            .filter(m -> m.getNomMatiere().toLowerCase().contains(query) || 
                         (m.getCodeMatiere() != null && m.getCodeMatiere().toLowerCase().contains(query)))
            .toList();
        displayMatieres(filtered);
    }

    // Efface le champ de texte et réaffiche toutes les matières
    @FXML
    public void resetSearch() {
        searchField.clear();
        handleSearch();
    }

    // Construit visuellement une carte (VBox) pour une matière, incluant son image et ses actions
    private VBox createMatiereCard(Matiere matiere) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 5); -fx-cursor: hand;");
        card.setPrefWidth(280);
        card.setMaxWidth(280);

        // Image area
        StackPane imageBox = new StackPane();
        imageBox.setPrefHeight(160);
        imageBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 16 16 0 0; -fx-overflow: hidden;");

        if (matiere.getImage() != null && !matiere.getImage().isEmpty()) {
            try {
                File imgFile = new File(UPLOAD_DIR + matiere.getImage());
                if (imgFile.exists()) {
                    ImageView iv = new ImageView(new Image(imgFile.toURI().toString()));
                    iv.setFitWidth(280);
                    iv.setFitHeight(160);
                    iv.setPreserveRatio(false);
                    imageBox.getChildren().add(iv);
                }
            } catch (Exception ignored) {}
        } else {
            Label placeholder = new Label("📚");
            placeholder.setStyle("-fx-font-size: 40px; -fx-text-fill: #cbd5e1;");
            imageBox.getChildren().add(placeholder);
        }

        // Content
        VBox content = new VBox(8);
        content.setPadding(new Insets(15));

        Label title = new Label(matiere.getNomMatiere());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        title.setWrapText(true);

        Label details = new Label(matiere.getSection() + " • " + matiere.getNiveau());
        details.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        content.getChildren().addAll(title, details);

        // Actions
        HBox actions = new HBox(10);
        actions.setPadding(new Insets(0, 15, 15, 15));
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnCours = new Button("Cours");
        btnCours.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnCours.setOnAction(e -> openCoursManagement(matiere));

        Button btnEdit = new Button("✎");
        btnEdit.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> editMatiere(matiere));

        Button btnDelete = new Button("🗑");
        btnDelete.setStyle("-fx-background-color: #fff1f2; -fx-text-fill: #e11d48; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> deleteMatiere(matiere));

        actions.getChildren().addAll(btnCours, btnEdit, btnDelete);

        card.getChildren().addAll(imageBox, content, actions);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8); -fx-translate-y: -5;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 5); -fx-translate-y: 0;"));

        return card;
    }

    // Déclenche l'ouverture du formulaire d'ajout (fenêtre popup)
    @FXML
    public void toggleAddForm() {
        openFormPage(null);
    }

    // Déclenche l'ouverture du formulaire de modification pour une matière donnée
    private void editMatiere(Matiere matiere) {
        openFormPage(matiere);
    }

    // Prépare et affiche la fenêtre popup pour ajouter ou modifier une matière (MatiereForm.fxml)
    private void openFormPage(Matiere m) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/MatiereForm.fxml"));
            Parent root = loader.load();
            MatiereFormController controller = loader.getController();
            controller.setMatiere(m);

            Stage stage = new Stage();
            stage.setTitle(m == null ? "Ajouter une matière" : "Modifier la matière");
            stage.setScene(new javafx.scene.Scene(root));
            
            // Recharger la grille des matières lors de la fermeture du popup
            stage.setOnHidden(e -> loadMatieres());
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Supprime une matière de la base de données après validation par l'utilisateur
    private void deleteMatiere(Matiere matiere) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la matière ?");
        confirm.setContentText("Voulez-vous vraiment supprimer \"" + matiere.getNomMatiere() + "\" ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceMatiere.supprimer(matiere);
                loadMatieres();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + ex.getMessage());
            }
        }
    }

    // Bascule l'affichage du dashboard vers la gestion des cours pour une matière spécifique
    private void openCoursManagement(Matiere matiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/CoursManage.fxml"));
            Parent root = loader.load();
            CoursController controller = loader.getController();
            controller.setMatiere(matiere);

            Stage stage = (Stage) matiereGrid.getScene().getWindow();
            StackPane mainContent = findMainContent(stage.getScene().getRoot());
            if (mainContent != null) {
                mainContent.getChildren().clear();
                mainContent.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Fonction utilitaire pour trouver le StackPane "mainContent" dans la structure de l'application Dashboard
    private StackPane findMainContent(javafx.scene.Node node) {
        if (node instanceof StackPane && "mainContent".equals(node.getId())) {
            return (StackPane) node;
        }
        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                StackPane found = findMainContent(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    // Extrait l'extension d'un nom de fichier (utilisé pour les images)
    private String getExtension(String path) {
        int idx = path.lastIndexOf('.');
        return idx >= 0 ? path.substring(idx) : ".png";
    }

    // Affiche une boîte d'alerte standardisée
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}

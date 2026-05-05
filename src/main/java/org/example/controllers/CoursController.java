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
import org.example.Services.ServiceCours;
import org.example.Services.ServiceMatiere;
import org.example.entities.Cours;
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
//un controller JavaFX qui affiche et gère les cours (CRUD + UI + filtres + navigation)
public class CoursController {

    @FXML private FlowPane coursGrid;
    @FXML private Label headerTitle;
    @FXML private Label headerSubtitle;
    @FXML private Button btnBack;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterLanguage;

    @FXML private VBox formContainer;
    @FXML private Label formTitle;
    @FXML private TextField tfTitre;
    @FXML private TextArea taDesc;
    @FXML private TextField tfSection;
    @FXML private TextField tfDuree;
    @FXML private TextField tfPrix;
    @FXML private ComboBox<String> cbLangue;
    @FXML private ComboBox<Matiere> cbMatiere;
    @FXML private Label lblImageName;
    @FXML private Label lblPdfName;

    @FXML private Label errTitre;
    @FXML private Label errDesc;
    @FXML private Label errSection;
    @FXML private Label errDuree;
    @FXML private Label errPrix;
    @FXML private Label errMatiere;

    private Cours editingCours = null;
    private String selectedImagePath = null;
    private String selectedPdfPath = null;

    private final ServiceCours serviceCours = new ServiceCours();
    private final ServiceMatiere serviceMatiere = new ServiceMatiere();
    private Matiere currentMatiere;
    private List<Cours> allCoursList; // Store all loaded cours for filtering

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/images/";

    // Définit la matière actuelle et recharge les cours associés
    public void setMatiere(Matiere matiere) {
        this.currentMatiere = matiere;
        if (headerTitle != null) headerTitle.setText("Cours: " + matiere.getNomMatiere());
        loadCours();
    }

    // Initialise le contrôleur, crée le dossier d'upload et configure les filtres de langue
    @FXML
    public void initialize() {
        new File(UPLOAD_DIR).mkdirs();
        if (cbLangue != null) {
            cbLangue.getItems().addAll("Français", "Anglais", "Arabe", "Espagnol");
        }
        if (filterLanguage != null) {
            filterLanguage.getItems().addAll("Tous", "Français", "Anglais", "Arabe", "Espagnol");
        }
        try {
            if (cbMatiere != null) {
                cbMatiere.getItems().addAll(serviceMatiere.recuperer());
            }
        } catch (SQLException ignored) {}
        loadCours();
    }

    // Récupère la liste des cours depuis la base de données (filtrée par matière si applicable)
    private void loadCours() {
        if (coursGrid == null) return;
        try {
            //filre de pmatire
            if (currentMatiere != null) {
                allCoursList = serviceCours.recupererParMatiere(currentMatiere.getId());
            } else {
                allCoursList = serviceCours.recuperer();
            }
            displayCours(allCoursList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
        }
    }

    // Affiche la liste des cours dans la grille FlowPane root
    private void displayCours(List<Cours> list) {
        coursGrid.getChildren().clear();
        if (list.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            emptyBox.setPrefWidth(800);
            Label msg = new Label("📚 Aucun cours ne correspond à vos critères.");
            msg.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
            emptyBox.getChildren().add(msg);
            coursGrid.getChildren().add(emptyBox);
        } else {
            for (Cours c : list) {
                coursGrid.getChildren().add(createCoursCard(c));
            }
        }
    }

    // Gère la recherche textuelle et le filtrage par langue en temps réel
    @FXML
    public void handleSearch() {
        if (allCoursList == null) return;
        
        String query = (searchField != null) ? searchField.getText().toLowerCase().trim() : "";
        String lang = (filterLanguage != null && filterLanguage.getValue() != null) ? filterLanguage.getValue() : "Tous";

        List<Cours> filtered = allCoursList.stream()
            .filter(c -> c.getTitre().toLowerCase().contains(query))
            .filter(c -> lang.equals("Tous") || (c.getLangue() != null && c.getLangue().equalsIgnoreCase(lang)))
            .toList();

        displayCours(filtered);
    }

    // Réinitialise tous les filtres de recherche et recharge la liste complète
    @FXML
    public void resetFilters() {
        if (searchField != null) searchField.clear();
        if (filterLanguage != null) filterLanguage.getSelectionModel().select("Tous");
        handleSearch();
    }

    // Crée une carte graphique (VBox) pour représenter visuellement un cours
    private VBox createCoursCard(Cours cours) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        card.setPrefWidth(320);
        card.setPadding(new Insets(15));
        card.setSpacing(10);

        Label title = new Label(cours.getTitre());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        title.setWrapText(true);

        Label desc = new Label(cours.getDescription());
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        desc.setWrapText(true);
        desc.setMaxHeight(60);

        HBox meta = new HBox(15);
        Label duree = new Label("⏱ " + cours.getDureeTotale());
        Label prix = new Label("💰 " + cours.getPrix() + " USD");
        duree.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        prix.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        meta.getChildren().addAll(duree, prix);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("✎");
        btnEdit.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 6;");
        btnEdit.setOnAction(e -> editCours(cours));

        Button btnDelete = new Button("🗑");
        btnDelete.setStyle("-fx-background-color: #fff1f2; -fx-text-fill: #e11d48; -fx-background-radius: 6;");
        btnDelete.setOnAction(e -> deleteCours(cours));

        actions.getChildren().addAll(btnEdit, btnDelete);

        card.getChildren().addAll(title, desc, meta, spacer, actions);
        return card;
    }

    // Ouvre la fenêtre popup pour ajouter un nouveau cours
    @FXML
    public void toggleAddForm() {
        openFormPage(null);
    }

    // Ouvre la fenêtre popup pour modifier un cours existant
    private void editCours(Cours cours) {
        openFormPage(cours);
    }

    // Logique centrale pour charger l'interface du formulaire (CoursForm.fxml) dans une nouvelle fenêtre (Stage)
    private void openFormPage(Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/CoursForm.fxml"));
            Parent root = loader.load();
            CoursFormController controller = loader.getController();
            controller.setCours(c, currentMatiere);

            Stage stage = new Stage();
            stage.setTitle(c == null ? "Ajouter un cours" : "Modifier le cours");
            stage.setScene(new javafx.scene.Scene(root));
            
            // Recharger la liste quand la fenêtre est fermée pour voir les changements
            stage.setOnHidden(e -> loadCours());
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Supprime un cours après confirmation de l'utilisateur
    private void deleteCours(Cours c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer \"" + c.getTitre() + "\" ?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try { serviceCours.supprimer(c); loadCours(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Retourne à la vue des matières en rechargeant le composant FXML correspondant
    @FXML
    public void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/MatiereView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) coursGrid.getScene().getWindow();
            StackPane mainContent = findMainContent(stage.getScene().getRoot());
            if (mainContent != null) {
                mainContent.getChildren().clear();
                mainContent.getChildren().add(root);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Recherche récursivement le conteneur principal (mainContent) dans la hiérarchie des nœuds de la scène
    private StackPane findMainContent(javafx.scene.Node node) {
        if (node instanceof StackPane && "mainContent".equals(node.getId())) return (StackPane) node;
        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                StackPane found = findMainContent(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    // Affiche une boîte de dialogue d'alerte personnalisée
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}

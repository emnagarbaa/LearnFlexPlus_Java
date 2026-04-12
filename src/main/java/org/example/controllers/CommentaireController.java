package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Services.ServiceCommentaire;
import org.example.entities.Commentaire;
import org.example.entities.Examen;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class CommentaireController implements Initializable {

    @FXML private Label                          lblTitreExamen;
    @FXML private TableView<Commentaire>         commentaireTable;
    @FXML private TableColumn<Commentaire, String>  colContenu;
    @FXML private TableColumn<Commentaire, String>  colAuteur;
    @FXML private TableColumn<Commentaire, String>  colDate;
    @FXML private TableColumn<Commentaire, Integer> colLikes;
    @FXML private TableColumn<Commentaire, Void>    colActions;

    private final ServiceCommentaire serviceCommentaire = new ServiceCommentaire();
    private Examen examen;

    public void setExamen(Examen examen) {
        this.examen = examen;
        lblTitreExamen.setText("Commentaires : " + examen.getTitre());
        loadCommentaires();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colAuteur.setCellValueFactory(new PropertyValueFactory<>("auteur"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("datecre"));
        colLikes.setCellValueFactory(new PropertyValueFactory<>("likes"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnModifier  = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox   box          = new HBox(6, btnModifier, btnSupprimer);
            {
                btnModifier.setStyle("-fx-background-color:#007bff; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:5;");
                btnSupprimer.setStyle("-fx-background-color:#dc3545; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:5;");
                btnModifier.setOnAction(e -> ouvrirFormulaire(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(e -> supprimer(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadCommentaires() {
        try {
            List<Commentaire> list = serviceCommentaire.recupererParExamenId(examen.getId());
            commentaireTable.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        ouvrirFormulaire(null);
    }

    private void ouvrirFormulaire(Commentaire commentaire) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fxml/AjouterCommentaire.fxml"));
            Parent root = loader.load();
            AjouterCommentaireController ctrl = loader.getController();
            ctrl.setExamenId(examen.getId());
            if (commentaire != null) ctrl.setCommentaire(commentaire);
            ctrl.setOnSuccessCallback(c -> loadCommentaires());
            Stage stage = new Stage();
            stage.setTitle(commentaire == null ? "Ajouter un commentaire" : "Modifier le commentaire");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException ex) {
            showAlert("Erreur", ex.getMessage());
        }
    }

    private void supprimer(Commentaire c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer ce commentaire ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    serviceCommentaire.supprimer(c);
                    loadCommentaires();
                } catch (SQLException ex) {
                    showAlert("Erreur", ex.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML
    private void handleFermer() {
        ((Stage) commentaireTable.getScene().getWindow()).close();
    }
}
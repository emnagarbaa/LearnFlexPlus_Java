package org.example.controllers;

import javafx.event.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import org.example.Services.ServiceUsers;
import org.example.entities.Users;

public class    SignupController {

    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private PasswordField passwordField;
    @FXML private TextField ageField;
    @FXML private TextField addressField;
    @FXML private Label imageLabel;
    @FXML private ImageView logoImage;


    private File selectedImage;
    private final ServiceUsers serviceUsers = new ServiceUsers();

    @FXML
    public void initialize() {
        roleCombo.getItems().addAll("STUDENT", "TEACHER", "ADMIN");
        loadImage(logoImage, "/org/example/images/logo1.png");
    }

    private void loadImage(ImageView imageView, String path) {
        try (var stream = getClass().getResourceAsStream(path)) {

            if (stream == null) {
                System.err.println("Image introuvable: " + path);
                return;
            }

            imageView.setImage(new javafx.scene.image.Image(stream));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // IMAGE PICKER
    @FXML
    public void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImage = file;
            imageLabel.setText(file.getName());
        }
    }

    // REGISTER ACTION
    @FXML
    public void register(ActionEvent event) {

        String prenom = prenomField.getText().trim();
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String phone = phoneField.getText().trim();
        String role = roleCombo.getValue();
        String password = passwordField.getText();
        String age = ageField.getText().trim();
        String address = addressField.getText().trim();

        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Please fill required fields");
            return;
        }

        try {
            Users user = new Users();
            user.setPrenom(prenom);
            user.setNom(nom);
            user.setEmail(email);
            user.setTelephone(phone);
            user.setRole(role);
            user.setPassword(password);
            user.setAdresseResidence(address);
            user.setVerified(false);
            user.setProfileImage(selectedImage != null ? selectedImage.getAbsolutePath() : null);

            if (!age.isEmpty()) {
                user.setAge(Integer.parseInt(age));
            }

            serviceUsers.ajouter(user);

            showAlert("User registered successfully!");

            goToLogin(event);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error: " + e.getMessage());
        }
    }

    // NAVIGATION
    @FXML
    public void goToLogin(Event event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/org/example/fxml/login.fxml")
            );

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Login");
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openFaceModal() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim().toLowerCase();
        if (email.isEmpty()) {
            showAlert("Enter your email before saving Face ID.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/face_register.fxml"));
            Parent root = loader.load();
            FaceRegisterController controller = loader.getController();
            controller.setEmail(email);

            Stage stage = new Stage();
            stage.setTitle("Face ID");
            stage.setScene(new Scene(root, 720, 560));
            stage.setOnCloseRequest(closeEvent -> controller.stop());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Face ID error: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Signup");
        alert.setContentText(msg);
        alert.show();
    }
}

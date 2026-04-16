package Controllers;

import javafx.event.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

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

    private File selectedImage;

    @FXML
    public void initialize() {
        roleCombo.getItems().addAll("STUDENT", "TEACHER", "ADMIN");
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

        String prenom = prenomField.getText();
        String nom = nomField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String role = roleCombo.getValue();
        String password = passwordField.getText();
        String age = ageField.getText();
        String address = addressField.getText();

        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Please fill required fields");
            return;
        }

        try {
            // TODO: insert into DB
            System.out.println("REGISTER USER:");
            System.out.println(prenom + " " + nom);
            System.out.println(email);
            System.out.println(role);

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
                    getClass().getResource("/fxml/login.fxml")
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
        System.out.println("Face ID modal placeholder");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Signup");
        alert.setContentText(msg);
        alert.show();
    }
}
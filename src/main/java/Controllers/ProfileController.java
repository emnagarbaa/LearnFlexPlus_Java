/* package Controllers;

import Main.java.Models.User;
import Main.java.Utils.NotificationUtil;
import Main.java.Utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfileController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private Label roleLabel;
    @FXML private ImageView profileImageView;

    private DashboardController dashboardController;
    private File selectedImageFile;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        loadUserData();
    }

    private void loadUserData() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            usernameField.setText(currentUser.getNom());
            emailField.setText(currentUser.getEmail());
            roleLabel.setText(currentUser.getRole());

            if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
                File imageFile = new File(currentUser.getImage());
                if (imageFile.exists()) profileImageView.setImage(new Image(imageFile.toURI().toString()));
            } else {
                profileImageView.setImage(new Image(getClass().getResourceAsStream("/Resources/Images/default_profile.png")));
            }
        }
    }

    @FXML
    private void handleChangePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            Image newImage = new Image(file.toURI().toString());
            profileImageView.setImage(newImage);
        }
    }

    @FXML
    private void handleSaveChanges() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newImagePath = (selectedImageFile != null) ? selectedImageFile.getAbsolutePath() : currentUser.getImage();

        // Validate input
        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            showNotification("Username and email cannot be empty.", false);
            return;
        }

        try {
            // If a new image was selected, save it persistently
            if (selectedImageFile != null) {
                // Ensure output directory exists
                File outputDir = new File("user_images");
                if (!outputDir.exists()) outputDir.mkdirs();

                // Save the image as PNG using a unique name based on user ID
                String outputFilePath = outputDir.getAbsolutePath() + File.separator + "user_" + currentUser.getId() + ".png";
                File outputFile = new File(outputFilePath);

                // Read original image and write as PNG
                BufferedImage bufferedImage = ImageIO.read(selectedImageFile);
                ImageIO.write(bufferedImage, "png", outputFile);

                newImagePath = outputFile.getAbsolutePath();
            }

            // Update database
            String sql = "UPDATE user SET username = ?, email = ?, image = ? WHERE id = ?";
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, newUsername);
                stmt.setString(2, newEmail);
                stmt.setString(3, newImagePath); // Save image path to DB
                stmt.setInt(4, currentUser.getId());
                stmt.executeUpdate();

                // Update currentUser object in memory
                currentUser.setNom(newUsername);
                currentUser.setEmail(newEmail);
                currentUser.setImage(newImagePath);

                showNotification("Profile updated successfully!", true);

                // Reload dashboard image
                if (dashboardController != null) {
                    dashboardController.loadProfileImage(newImagePath);
                    dashboardController.showDashboardHome();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showNotification("Database error: Could not update profile.", false);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showNotification("Error while saving profile image.", false);
        }
    }

        @FXML
    private void handleCancel() {
        if (dashboardController != null) {
            dashboardController.showDashboardHome();
        }
    }

    private void showNotification(String message, boolean success) {
        if (usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            NotificationUtil.showPopup(stage, message, success);
        }
    }
}
*/
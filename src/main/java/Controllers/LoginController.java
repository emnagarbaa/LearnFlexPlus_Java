package Controllers;

import Main.java.Models.User;
import Main.java.Utils.NotificationUtil;
import Main.java.Utils.SessionManager;
import Utils.MyDatabase;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.mindrot.jbcrypt.BCrypt;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Label togglePassword;
    @FXML private ImageView logoImage;
    private boolean passwordVisible = false;

    // ---------------- INIT ----------------
    @FXML
    public void initialize() {
        if (togglePassword != null) {
            togglePassword.setText("👁");
            togglePassword.setOnMouseClicked(e -> togglePasswordVisibility());
            loadImage(logoImage, "/images/logo1.png");
        }

        if (visiblePasswordField != null && passwordField != null) {
            visiblePasswordField.textProperty()
                    .bindBidirectional(passwordField.textProperty());
        }
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

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        visiblePasswordField.setVisible(passwordVisible);
        visiblePasswordField.setManaged(passwordVisible);

        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);

        togglePassword.setText(passwordVisible ? "🙈" : "👁");
    }

    // ---------------- LOGIN ----------------
    @FXML
    private void login(ActionEvent event) {

        String email = emailField.getText().trim();
        String password = passwordVisible
                ? visiblePasswordField.getText().trim()
                : passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showNotification("Email and password required", false);
            return;
        }

        String sql = "SELECT * FROM users WHERE email=?";

        try {
            Connection conn = MyDatabase.getInstance().getConnection();

            if (conn == null) {
                showNotification("Database connection failed", false);
                return;
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String hash = rs.getString("password");

                if (BCrypt.checkpw(password, hash)) {

                    int userId = rs.getInt("id");

                    // ── BAN CHECK ──────────────────────────────────────────
                    Services.ServiceBan serviceBan = new Services.ServiceBan();
                    entities.Ban activeBan = serviceBan.getActiveBan(userId);

                    if (activeBan != null) {
                        showNotification("Compte suspendu : " + activeBan.getReason(), false);

                        // Show detailed ban dialog
                        javafx.scene.control.Alert banAlert =
                                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                        banAlert.setTitle("Compte suspendu");
                        banAlert.setHeaderText("Vous ne pouvez pas vous connecter.");
                        banAlert.setContentText(
                                "Raison  : " + activeBan.getReason() + "\n" +
                                        "Type    : " + activeBan.getBanType()  + "\n" +
                                        "Jusqu'à : " + activeBan.getFormattedExpiry()
                        );
                        banAlert.showAndWait();
                        return; // ← stop here, do NOT navigate
                    }
                    // ── END BAN CHECK ──────────────────────────────────────

                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("nom") + " " + rs.getString("prenom"),
                            rs.getString("email"),
                            hash,
                            rs.getString("role"),
                            null
                    );

                    SessionManager.login(user);

                    Parent root = FXMLLoader.load(
                            getClass().getResource("/fxml/Dashboard.fxml")
                    );

                    Stage stage = (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

                    Scene scene = new Scene(root, 1200, 800);

                    stage.setScene(scene);
                    stage.setTitle("LearnFlex+ Dashboard");
                    stage.centerOnScreen();
                    stage.show();

                } else {
                    showNotification("Wrong password", false);
                }

            } else {
                showNotification("User not found", false);
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
            showNotification("DB error: " + e.getMessage(), false);
        }
    }

    // ---------------- NAVIGATION (OLD STYLE) ----------------

    @FXML
    public void goToSignup(MouseEvent event) {
        switchScene(event, "/fxml/Signup.fxml", "Signup");
    }

    @FXML
    private void handleSignupButton(ActionEvent event) {
        switchScene(event, "/fxml/Signup.fxml", "Signup");
    }

    @FXML
    private void goToForgotPassword(MouseEvent event) {
        switchScene(event, "/fxml/forgot_password.fxml", "Forgot Password");
    }

    // ---------------- HELPER ----------------
    private void switchScene(javafx.event.Event event, String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            Scene scene = new Scene(root, 1200, 800);

            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- NOTIFICATION ----------------
    private void showNotification(String msg, boolean success) {
        Stage stage = (Stage) emailField.getScene().getWindow();
        NotificationUtil.showPopup(stage, msg, success);
    }
}
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
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    // ---------------- FXML FIELDS ----------------
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Label togglePassword;
    @FXML private ImageView logoImage;
    @FXML private TextField captchaAnswerField;
    @FXML private Label captchaQuestionLabel;

    private boolean passwordVisible = false;
    private int captchaAnswer;

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

        loadCaptcha();
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

    // ---------------- CAPTCHA ----------------
    private void loadCaptcha() {
        java.util.Random rand = new java.util.Random();
        int a = rand.nextInt(9) + 1;
        int b = rand.nextInt(9) + 1;
        captchaAnswer = a + b;
        captchaQuestionLabel.setText("Combien font " + a + " + " + b + " ?");
        if (captchaAnswerField != null) captchaAnswerField.clear();
    }

    private boolean isCaptchaValid() {
        try {
            int entered = Integer.parseInt(captchaAnswerField.getText().trim());
            return entered == captchaAnswer;
        } catch (NumberFormatException e) {
            return false;
        }
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

        // ── CAPTCHA CHECK ─────────────────────────────────
        if (!isCaptchaValid()) {
            showNotification("Réponse au captcha incorrecte.", false);
            loadCaptcha();
            return;
        }
        // ── END CAPTCHA CHECK ─────────────────────────────

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
                        javafx.scene.control.Alert banAlert =
                                new javafx.scene.control.Alert(
                                        javafx.scene.control.Alert.AlertType.ERROR);
                        banAlert.setTitle("Compte suspendu");
                        banAlert.setHeaderText("Vous ne pouvez pas vous connecter.");
                        banAlert.setContentText(
                                "Raison  : " + activeBan.getReason() + "\n" +
                                        "Type    : " + activeBan.getBanType()  + "\n" +
                                        "Jusqu'à : " + activeBan.getFormattedExpiry()
                        );
                        banAlert.showAndWait();
                        loadCaptcha(); // reset captcha after ban block
                        return;
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

                    // ── ROLE-BASED REDIRECT ────────────────────────────────
                    String role = rs.getString("role");
                    String targetFxml;
                    String targetTitle;

                    if (role.equalsIgnoreCase("Admin") ||
                            role.equalsIgnoreCase("Enseignant")) {
                        targetFxml  = "/fxml/dashboard.fxml";
                        targetTitle = "LearnFlex+ Dashboard";
                    } else {
                        targetFxml  = "/fxml/front.fxml";
                        targetTitle = "LearnFlex+";
                    }
                    // ── END ROLE-BASED REDIRECT ────────────────────────────

                    Parent root = FXMLLoader.load(getClass().getResource(targetFxml));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, 1200, 800));
                    stage.setTitle(targetTitle);
                    stage.centerOnScreen();
                    stage.show();

                } else {
                    showNotification("Wrong password", false);
                    loadCaptcha(); // reset captcha on wrong password too
                }

            } else {
                showNotification("User not found", false);
                loadCaptcha();
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
            showNotification("DB error: " + e.getMessage(), false);
        }
    }

    // ---------------- GOOGLE LOGIN ----------------
    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        new Thread(() -> {
            try {
                com.google.api.services.oauth2.model.Userinfo userInfo =
                        Services.GoogleAuthService.authenticate();

                if (userInfo == null) return;

                final String email         = userInfo.getEmail();
                final String googleName    = userInfo.getName();
                final String googlePicture = userInfo.getPicture();

                javafx.application.Platform.runLater(() -> {
                    try {
                        Services.ServiceUsers serviceUsers = new Services.ServiceUsers();
                        entities.Users found = serviceUsers.findByEmail(email);

                        if (found == null) {
                            entities.Users newUser = new entities.Users();
                            String fullName = googleName != null ? googleName : email;
                            String[] parts  = fullName.split(" ", 2);
                            newUser.setNom(parts.length > 0 ? parts[0] : fullName);
                            newUser.setPrenom(parts.length > 1 ? parts[1] : "");
                            newUser.setEmail(email);
                            newUser.setRole("Etudiant");
                            newUser.setPassword(BCrypt.hashpw(
                                    java.util.UUID.randomUUID().toString(),
                                    BCrypt.gensalt()));
                            newUser.setVerified(true);
                            newUser.setProfileImage(googlePicture);
                            serviceUsers.ajouter(newUser);
                            found = serviceUsers.findByEmail(email);
                            showNotification("Compte créé automatiquement via Google.", true);
                        }

                        final entities.Users finalUser = found;

                        if (finalUser == null) {
                            showNotification("Erreur: impossible de récupérer l'utilisateur.", false);
                            return;
                        }

                        // Ban check
                        Services.ServiceBan serviceBan = new Services.ServiceBan();
                        entities.Ban activeBan = serviceBan.getActiveBan(finalUser.getId());
                        if (activeBan != null) {
                            showNotification("Compte suspendu : " + activeBan.getReason(), false);
                            return;
                        }

                        // Build session
                        User sessionUser = new User(
                                finalUser.getId(),
                                finalUser.getNom() + " " + finalUser.getPrenom(),
                                finalUser.getEmail(),
                                finalUser.getPassword(),
                                finalUser.getRole(),
                                finalUser.getProfileImage()
                        );
                        SessionManager.login(sessionUser);

                        // Role-based redirect
                        String role = finalUser.getRole();
                        String targetFxml = (role.equalsIgnoreCase("Admin") ||
                                role.equalsIgnoreCase("Enseignant"))
                                ? "/fxml/dashboard.fxml" : "/fxml/front.fxml";
                        String targetTitle = targetFxml.contains("dashboard")
                                ? "LearnFlex+ Dashboard" : "LearnFlex+";

                        Parent root = FXMLLoader.load(getClass().getResource(targetFxml));
                        Stage stage  = (Stage) emailField.getScene().getWindow();
                        stage.setScene(new Scene(root, 1200, 800));
                        stage.setTitle(targetTitle);
                        stage.centerOnScreen();
                        stage.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        showNotification("Erreur: " + e.getMessage(), false);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() ->
                        showNotification("Google auth annulée ou échouée.", false)
                );
            }
        }).start();
    }

    // ---------------- NAVIGATION ----------------
    @FXML
    public void goToSignup(MouseEvent event) {
        switchScene(event, "/fxml/Signup.fxml", "Signup");
    }

    @FXML
    private void handleSignupButton(ActionEvent event) {
        switchScene(event, "/fxml/Signup.fxml", "Signup");
    }

    @FXML
    private void openFaceAuth(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_auth.fxml"));
            Parent root = loader.load();
            FaceAuthController controller = loader.getController();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 720, 560));
            stage.setTitle("Face ID Login");
            stage.setOnCloseRequest(closeEvent -> controller.stop());
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Face ID error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void goToForgotPassword(MouseEvent event) {
        switchScene(event, "/fxml/forgot_password.fxml", "Forgot Password");
    }

    @FXML
    private void goToFront(MouseEvent event) {
        switchScene(event, "/fxml/front.fxml", "LearnFlex+");
    }

    // ---------------- HELPER ----------------
    private void switchScene(javafx.event.Event event, String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
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
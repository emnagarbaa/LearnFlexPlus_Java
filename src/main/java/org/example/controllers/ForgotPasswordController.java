package org.example.controllers;

import org.example.Services.ServiceUsers;
import org.example.utils.MyDatabase;
import org.example.utils.OtpStore;
import org.example.utils.SmsUtil;
import entities.Users;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ForgotPasswordController {

    // ── FXML fields (same ids as your original) ───────────────────────────────
    @FXML private ImageView   logoImage;
    @FXML private Label       lblSubtitle;

    @FXML private TextField   emailField;

    @FXML private VBox        otpPane;          // hidden in FXML, shown after SMS
    @FXML private TextField   otpField;

    @FXML private VBox        passwordPane;     // hidden in FXML, shown after OTP ok
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Button      mainButton;
    @FXML private Label       messageLabel;

    // ── State ─────────────────────────────────────────────────────────────────
    /**
     * STEP 1 → user enters email, clicks "Envoyer le code SMS"
     * STEP 2 → OTP row appears, user enters code, clicks "Vérifier le code"
     * STEP 3 → password fields appear, user sets new password, clicks "Réinitialiser"
     */
    private enum Step { SEND_OTP, VERIFY_OTP, RESET_PASSWORD }
    private Step currentStep = Step.SEND_OTP;

    private final ServiceUsers serviceUsers = new ServiceUsers();

    private Users  foundUser;       // resolved in step 1
    private String resolvedPhone;   // E.164 phone for OTP store key

    // ── Init ──────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        loadImage(logoImage, "/images/logo1.png");
    }

    private void loadImage(ImageView iv, String path) {
        try (var stream = getClass().getResourceAsStream(path)) {
            if (stream == null) { System.err.println("Image introuvable: " + path); return; }
            iv.setImage(new javafx.scene.image.Image(stream));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Single button handler — delegates to correct step ────────────────────
    @FXML
    private void handleMainButton(ActionEvent event) {
        clearMessage();
        switch (currentStep) {
            case SEND_OTP      -> sendOtp();
            case VERIFY_OTP    -> verifyOtp();
            case RESET_PASSWORD -> resetPassword(event);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 1 — look up user by email, send OTP to their phone
    // ─────────────────────────────────────────────────────────────────────────
    private void sendOtp() {

        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Veuillez entrer votre email."); return;
        }

        // Find user
        try {
            foundUser = serviceUsers.findByEmail(email);
        } catch (SQLException e) {
            showError("Erreur base de données : " + e.getMessage()); return;
        }

        if (foundUser == null) {
            showError("Aucun compte associé à cet email."); return;
        }

        if (foundUser.getTelephone() == null || foundUser.getTelephone().isBlank()) {
            showError("Aucun numéro de téléphone enregistré pour ce compte.\nContactez un administrateur.");
            return;
        }

        resolvedPhone = SmsUtil.toE164Tunisia(foundUser.getTelephone());
        String otp    = OtpStore.generateAndStore(resolvedPhone);

        // Send SMS in background — never block the JavaFX thread
        mainButton.setDisable(true);
        showInfo("Envoi du code en cours...");

        new Thread(() -> {
            try {
                SmsUtil.send(
                        resolvedPhone,
                        "LearnFlex+ — Votre code : " + otp + " (valide 5 min)"
                );
                Platform.runLater(this::advanceToOtpStep);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mainButton.setDisable(false);
                    showError("Envoi SMS échoué : " + e.getMessage());
                });
            }
        }).start();
    }

    private void advanceToOtpStep() {
        // Mask phone for display: +21628*****48
        String masked = resolvedPhone.replaceAll(
                "(\\+\\d{5})(\\d+)(\\d{2})$", "$1*****$3"
        );

        lblSubtitle.setText("Code envoyé au " + masked
                + ".\nEntrez-le ci-dessous. Valable 5 minutes.");

        emailField.setDisable(true);  // lock email field

        otpPane.setVisible(true);
        otpPane.setManaged(true);

        mainButton.setText("Vérifier le code");
        mainButton.setDisable(false);

        currentStep = Step.VERIFY_OTP;
        clearMessage();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 2 — verify OTP
    // ─────────────────────────────────────────────────────────────────────────
    private void verifyOtp() {

        String code = otpField.getText().trim();

        if (code.isEmpty()) {
            showError("Entrez le code reçu par SMS."); return;
        }

        if (!OtpStore.verify(resolvedPhone, code)) {
            showError("Code invalide ou expiré. Cliquez sur « Renvoyer le code »."); return;
        }

        // OTP correct → show password fields
        otpPane.setDisable(true);   // keep visible but grayed out (proof it was used)

        passwordPane.setVisible(true);
        passwordPane.setManaged(true);

        lblSubtitle.setText("Code vérifié ✔  Choisissez votre nouveau mot de passe.");

        mainButton.setText("Réinitialiser");
        currentStep = Step.RESET_PASSWORD;
        clearMessage();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 3 — save new password
    // ─────────────────────────────────────────────────────────────────────────
    private void resetPassword(ActionEvent event) {

        String newPass  = newPasswordField.getText();
        String confirm  = confirmPasswordField.getText();

        if (newPass.isEmpty() || confirm.isEmpty()) {
            showError("Veuillez remplir les deux champs."); return;
        }
        if (!newPass.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas."); return;
        }
        if (newPass.length() < 8) {
            showError("Le mot de passe doit contenir au moins 8 caractères."); return;
        }
        if (!newPass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$")) {
            showError("Mot de passe trop faible (majuscule, chiffre et caractère spécial requis).");
            return;
        }

        // Update DB — BCrypt hash (replaces the plain-text save in your original)
        String hashed = BCrypt.hashpw(newPass, BCrypt.gensalt());

        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET password = ? WHERE id = ?"
            );
            stmt.setString(1, hashed);
            stmt.setInt(2, foundUser.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            showError("Erreur serveur : " + e.getMessage()); return;
        }

        // Success feedback then redirect
        mainButton.setDisable(true);
        showInfo("Mot de passe mis à jour ! Redirection...");

        new Thread(() -> {
            try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> navigateToLogin(event));
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESEND
    // ─────────────────────────────────────────────────────────────────────────
    @FXML
    private void resendOtp(MouseEvent event) {
        if (resolvedPhone == null) return;

        String otp = OtpStore.generateAndStore(resolvedPhone);

        new Thread(() -> {
            try {
                SmsUtil.send(resolvedPhone,
                        "LearnFlex+ — Nouveau code : " + otp + " (valide 5 min)");
                Platform.runLater(() -> showInfo("Nouveau code envoyé."));
            } catch (Exception e) {
                Platform.runLater(() -> showError("Envoi échoué : " + e.getMessage()));
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────────────────
    @FXML
    public void goToLogin(ActionEvent event) {
        navigateToLogin(event);
    }

    private void navigateToLogin(javafx.event.Event event) {
        try {
            Parent root  = FXMLLoader.load(getClass().getResource("/org/example/fxml/login.fxml"));
            Stage  stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Connexion");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private void clearMessage() {
        messageLabel.setText("");
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
        messageLabel.setText(msg);
    }

    private void showInfo(String msg) {
        messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 12px;");
        messageLabel.setText(msg);
    }
}

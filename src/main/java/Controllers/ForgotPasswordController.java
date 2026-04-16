package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    private void resetPassword(ActionEvent event) {

        String email = emailField.getText().trim();
        String newPass = newPasswordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (email.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs");
            return;
        }

        if (!newPass.equals(confirm)) {
            messageLabel.setText("Les mots de passe ne correspondent pas");
            return;
        }

        try (Connection conn = Database.getConnection()) {

            String sql = "UPDATE users SET password=? WHERE email=?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            // NOTE: replace with BCrypt like your login
            stmt.setString(1, newPass);
            stmt.setString(2, email);

            int updated = stmt.executeUpdate();

            if (updated > 0) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Mot de passe mis à jour");

                goToLogin(event);
            } else {
                messageLabel.setText("Email introuvable");
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur serveur");
        }
    }

    private void goToLogin(ActionEvent event) {
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
}
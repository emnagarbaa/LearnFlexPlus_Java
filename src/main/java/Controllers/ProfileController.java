package Controllers;

import Main.java.Models.User;
import Main.java.Utils.SessionManager;
import Services.ServiceUsers;
import Utils.MyDatabase;
import entities.Users;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProfileController {

    // ── Left panel ────────────────────────────────────────────────────────────
    @FXML private ImageView profileImageView;
    @FXML private Label     lblName;
    @FXML private Label     lblRole;

    // ── Form fields ───────────────────────────────────────────────────────────
    @FXML private TextField     txtNom;
    @FXML private TextField     txtPrenom;
    @FXML private TextField     txtAge;
    @FXML private TextField     txtTelephone;
    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    // ── Error labels ─────────────────────────────────────────────────────────
    @FXML private Label errNom;
    @FXML private Label errPrenom;
    @FXML private Label errAge;
    @FXML private Label errTelephone;
    @FXML private Label errPassword;
    @FXML private Label lblStatus;

    // ── State ─────────────────────────────────────────────────────────────────
    private Users     currentUser;     // full Users entity from DB
    private String    newImagePath;    // set if user picks a new picture

    private static final String PROFILE_IMAGE_DIR = "src/main/resources/images/profiles/";

    private final ServiceUsers serviceUsers = new ServiceUsers();

    // ── INIT ──────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        User sessionUser = SessionManager.getCurrentUser();
        if (sessionUser == null) { goBack(); return; }

        try {
            currentUser = serviceUsers.findByEmail(sessionUser.getEmail());
        } catch (Exception e) {
            showError("Erreur chargement profil : " + e.getMessage());
            return;
        }

        if (currentUser == null) { goBack(); return; }

        // ── Populate form ────────────────────────────────────────────────────
        txtNom.setText(nullSafe(currentUser.getNom()));
        txtPrenom.setText(nullSafe(currentUser.getPrenom()));
        txtAge.setText(currentUser.getAge() != null ? String.valueOf(currentUser.getAge()) : "");
        txtTelephone.setText(nullSafe(currentUser.getTelephone()));
        txtEmail.setText(nullSafe(currentUser.getEmail()));

        // ── Left panel ──────────────────────────────────────────────────────
        lblName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
        lblRole.setText(currentUser.getRole());

        // ── Profile image ────────────────────────────────────────────────────
        loadProfileImage(currentUser.getProfileImage());
    }

    private void loadProfileImage(String filename) {
        if (filename != null && !filename.isBlank()) {
            try {
                File f = new File(PROFILE_IMAGE_DIR + filename);
                if (f.exists()) {
                    profileImageView.setImage(new Image(f.toURI().toString()));
                    return;
                }
            } catch (Exception ignored) {}
        }
        // Fallback to default avatar
        var stream = getClass().getResourceAsStream("/images/logo1.png");
        if (stream != null) profileImageView.setImage(new Image(stream));
    }

    // ── PICK PROFILE IMAGE ────────────────────────────────────────────────────
    @FXML
    private void chooseProfileImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo de profil");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) txtNom.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        // Preview immediately
        try {
            profileImageView.setImage(new Image(file.toURI().toString()));
        } catch (Exception e) {
            showError("Impossible de charger l'image.");
            return;
        }

        // Save path for when user clicks "Enregistrer"
        newImagePath = file.getAbsolutePath();
    }

    // ── SAVE PROFILE ──────────────────────────────────────────────────────────
    @FXML
    private void saveProfile() {
        clearErrors();

        if (!validateForm()) return;

        try {
            // ── Copy new image to resources if one was picked ────────────────
            String imageFilename = currentUser.getProfileImage();
            if (newImagePath != null) {
                imageFilename = copyImageToResources(newImagePath);
            }

            // ── Build UPDATE query ───────────────────────────────────────────
            String sql = """
                UPDATE users SET
                  nom = ?, prenom = ?, age = ?, telephone = ?,
                  profile_image = ?, updated_at = NOW()
                  %s
                WHERE id = ?
            """;

            boolean changingPassword = !txtNewPassword.getText().isBlank();
            String passwordClause = changingPassword ? ", password = ?" : "";
            sql = String.format(sql, passwordClause);

            Connection conn = MyDatabase.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            int i = 1;
            ps.setString(i++, txtNom.getText().trim());
            ps.setString(i++, txtPrenom.getText().trim());

            String ageStr = txtAge.getText().trim();
            if (ageStr.isEmpty()) ps.setNull(i++, java.sql.Types.INTEGER);
            else                  ps.setInt(i++, Integer.parseInt(ageStr));

            ps.setString(i++, txtTelephone.getText().trim());
            ps.setString(i++, imageFilename);

            if (changingPassword) {
                ps.setString(i++, BCrypt.hashpw(txtNewPassword.getText(), BCrypt.gensalt()));
            }

            ps.setInt(i, currentUser.getId());
            ps.executeUpdate();

            // ── Update session name ─────────────────────────────────────────
            SessionManager.getCurrentUser().setNom(
                    txtNom.getText().trim() + " " + txtPrenom.getText().trim()
            );

            // ── Refresh left panel ──────────────────────────────────────────
            lblName.setText(txtNom.getText().trim() + " " + txtPrenom.getText().trim());
            currentUser.setProfileImage(imageFilename);
            newImagePath = null;

            showSuccess("Profil mis à jour avec succès ✔");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur serveur : " + e.getMessage());
        }
    }

    // ── VALIDATION ────────────────────────────────────────────────────────────
    private boolean validateForm() {
        boolean ok = true;

        if (txtNom.getText().isBlank() || !txtNom.getText().matches("^[a-zA-Z\\s]{2,}$")) {
            errNom.setText("Nom invalide (lettres uniquement, min 2 caractères)"); ok = false;
        }
        if (txtPrenom.getText().isBlank() || !txtPrenom.getText().matches("^[a-zA-Z\\s]{2,}$")) {
            errPrenom.setText("Prénom invalide"); ok = false;
        }
        String ageStr = txtAge.getText().trim();
        if (!ageStr.isEmpty()) {
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 10 || age > 120) { errAge.setText("Âge invalide"); ok = false; }
            } catch (NumberFormatException e) {
                errAge.setText("Âge doit être un nombre"); ok = false;
            }
        }
        String tel = txtTelephone.getText().trim();
        if (!tel.isEmpty() && !tel.matches("^\\+?[0-9\\s\\-]{6,15}$")) {
            errTelephone.setText("Numéro invalide"); ok = false;
        }

        // Password (optional — only validate if filled)
        String newPwd  = txtNewPassword.getText();
        String confirm = txtConfirmPassword.getText();
        if (!newPwd.isBlank()) {
            if (newPwd.length() < 8) {
                errPassword.setText("Min 8 caractères"); ok = false;
            } else if (!newPwd.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$")) {
                errPassword.setText("Mot de passe trop faible (majuscule, chiffre, caractère spécial)");
                ok = false;
            } else if (!newPwd.equals(confirm)) {
                errPassword.setText("Les mots de passe ne correspondent pas"); ok = false;
            }
        }

        return ok;
    }

    // ── IMAGE COPY ────────────────────────────────────────────────────────────
    private String copyImageToResources(String sourcePath) throws IOException {
        Files.createDirectories(Paths.get(PROFILE_IMAGE_DIR));
        String ext      = sourcePath.substring(sourcePath.lastIndexOf('.'));
        String filename = System.currentTimeMillis() + "_" + currentUser.getId() + ext;
        Path   dest     = Paths.get(PROFILE_IMAGE_DIR + filename);
        Files.copy(Paths.get(sourcePath), dest, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    // ── NAVIGATION ────────────────────────────────────────────────────────────
    @FXML
    private void goBack() {
        try {
            Parent root  = FXMLLoader.load(getClass().getResource("/fxml/front.fxml"));
            Stage  stage = (Stage) txtNom.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 900));
            stage.setTitle("LearnFlex+");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    private void clearErrors() {
        errNom.setText(""); errPrenom.setText("");
        errAge.setText(""); errTelephone.setText("");
        errPassword.setText(""); lblStatus.setText("");
    }

    private void showError(String msg) {
        lblStatus.setStyle("-fx-text-fill: #c62828; -fx-font-size: 13px;");
        lblStatus.setText(msg);
    }

    private void showSuccess(String msg) {
        lblStatus.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 13px;");
        lblStatus.setText(msg);
    }

    private String nullSafe(String s) { return s != null ? s : ""; }
}

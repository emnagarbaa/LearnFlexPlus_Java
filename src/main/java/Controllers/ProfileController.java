package Controllers;

import Main.java.Models.User;
import Main.java.Utils.SessionManager;
import Services.ServiceUsers;
import Services.EmailService;
import Utils.MyDatabase;
import entities.Users;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;
import java.util.Random;

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

    // ── Error / status labels ─────────────────────────────────────────────────
    @FXML private Label errNom;
    @FXML private Label errPrenom;
    @FXML private Label errAge;
    @FXML private Label errTelephone;
    @FXML private Label errEmail;
    @FXML private Label errPassword;
    @FXML private Label lblStatus;

    // ── Verification UI ───────────────────────────────────────────────────────
    @FXML private Label  lblVerifBadge;   // "✔ Vérifié" / "✘ Non vérifié"
    @FXML private Button btnVerify;       // "Vérifier mon compte"

    // ── State ─────────────────────────────────────────────────────────────────
    private Users  currentUser;
    private String newImagePath;

    /** In-memory pending verification code + its expiry timestamp */
    private String pendingCode;
    private long   codeExpiry;

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

        // ── Left panel ───────────────────────────────────────────────────────
        lblName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
        lblRole.setText(currentUser.getRole());

        // ── Profile image ────────────────────────────────────────────────────
        loadProfileImage(currentUser.getProfileImage());

        // ── Verification state ───────────────────────────────────────────────
        applyVerificationState(currentUser.isVerified());
    }

    /**
     * Enables/disables email editing and shows the correct badge + button
     * depending on the current verification state.
     */
    private void applyVerificationState(boolean verified) {
        if (verified) {
            lblVerifBadge.setText("✔  Compte vérifié");
            lblVerifBadge.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 12px; -fx-font-weight: bold;");
            btnVerify.setVisible(false);
            btnVerify.setManaged(false);
            txtEmail.setEditable(true);
            txtEmail.setStyle("-fx-background-radius: 8; -fx-padding: 9;");
        } else {
            lblVerifBadge.setText("✘  Compte non vérifié – l'e-mail ne peut pas être modifié");
            lblVerifBadge.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
            btnVerify.setVisible(true);
            btnVerify.setManaged(true);
            txtEmail.setEditable(false);
            txtEmail.setStyle(
                    "-fx-background-radius: 8; -fx-padding: 9; " +
                            "-fx-background-color: #f0f0f0; -fx-text-fill: #999;");
        }
    }

    // ── PICK PROFILE IMAGE ────────────────────────────────────────────────────
    @FXML
    private void chooseProfileImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo de profil");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        Stage stage = (Stage) txtNom.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        try {
            profileImageView.setImage(new Image(file.toURI().toString()));
        } catch (Exception e) {
            showError("Impossible de charger l'image.");
            return;
        }
        newImagePath = file.getAbsolutePath();
    }

    // ── SEND VERIFICATION EMAIL ───────────────────────────────────────────────
    @FXML
    private void sendVerification() {
        // Generate a 6-digit code
        pendingCode  = String.format("%06d", new Random().nextInt(1_000_000));
        codeExpiry   = System.currentTimeMillis() + 10 * 60 * 1_000L; // 10 min

        String target = currentUser.getEmail();

        btnVerify.setDisable(true);
        showInfo("Envoi du code en cours…");

        // Send on a background thread so the UI stays responsive
        new Thread(() -> {
            try {
                EmailService.sendVerificationEmail(target, pendingCode);
                Platform.runLater(() -> {
                    showInfo("Code envoyé à " + target + "  — valable 10 min.");
                    btnVerify.setDisable(false);
                    showCodeDialog();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showError("Échec d'envoi : " + ex.getMessage());
                    btnVerify.setDisable(false);
                });
            }
        }, "verif-mail-thread").start();
    }

    /** Pops a small dialog asking the user to type the 6-digit code. */
    private void showCodeDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Vérification du compte");
        dialog.setHeaderText("Entrez le code à 6 chiffres reçu par e-mail.");

        ButtonType confirmBtn = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);

        TextField codeField = new TextField();
        codeField.setPromptText("ex : 482910");
        codeField.setStyle("-fx-font-size: 20px; -fx-letter-spacing: 6px; " +
                "-fx-background-radius: 8; -fx-padding: 10;");
        codeField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) codeField.setText(o);
            if (n.length() > 6)     codeField.setText(o);
        });

        Label hint = new Label();
        hint.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11px;");

        VBox box = new VBox(10, codeField, hint);
        box.setPadding(new Insets(16, 24, 0, 24));
        dialog.getDialogPane().setContent(box);

        // Intercept the OK button to validate before closing
        Button okButton = (Button) dialog.getDialogPane().lookupButton(confirmBtn);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String entered = codeField.getText().trim();

            if (System.currentTimeMillis() > codeExpiry) {
                hint.setText("Code expiré. Veuillez en demander un nouveau.");
                event.consume();
                return;
            }
            if (!entered.equals(pendingCode)) {
                hint.setText("Code incorrect. Réessayez.");
                event.consume();
            }
        });

        dialog.setResultConverter(btn ->
                btn == confirmBtn ? codeField.getText().trim() : null);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(code -> markAccountVerified());
    }

    /** Sets is_verified = 1 in the DB and refreshes the UI. */
    private void markAccountVerified() {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET is_verified = 1 WHERE id = ?");
            ps.setInt(1, currentUser.getId());
            ps.executeUpdate();

            currentUser.setVerified(true);
            applyVerificationState(true);
            showSuccess("Compte vérifié avec succès ✔  Vous pouvez maintenant modifier votre e-mail.");

        } catch (Exception e) {
            showError("Erreur lors de la vérification : " + e.getMessage());
        }
    }

    // ── SAVE PROFILE ──────────────────────────────────────────────────────────
    @FXML
    private void saveProfile() {
        clearErrors();
        if (!validateForm()) return;

        try {
            // ── Copy new image if one was picked ─────────────────────────────
            String imageFilename = currentUser.getProfileImage();
            if (newImagePath != null) {
                imageFilename = copyImageToResources(newImagePath);
            }

            // ── Detect e-mail change (only possible when verified) ────────────
            String oldEmail  = currentUser.getEmail();
            String newEmail  = txtEmail.getText().trim();
            boolean emailChanged = currentUser.isVerified() && !newEmail.equals(oldEmail);

            // ── Build SQL dynamically ─────────────────────────────────────────
            boolean changingPassword = !txtNewPassword.getText().isBlank();

            StringBuilder sql = new StringBuilder("""
                UPDATE users SET
                  nom = ?, prenom = ?, age = ?, telephone = ?,
                  profile_image = ?, updated_at = NOW()
                """);

            if (emailChanged)       sql.append(", email = ?, is_verified = 0");
            if (changingPassword)   sql.append(", password = ?");
            sql.append(" WHERE id = ?");

            Connection conn = MyDatabase.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());

            int i = 1;
            ps.setString(i++, txtNom.getText().trim());
            ps.setString(i++, txtPrenom.getText().trim());

            String ageStr = txtAge.getText().trim();
            if (ageStr.isEmpty()) ps.setNull(i++, java.sql.Types.INTEGER);
            else                  ps.setInt (i++, Integer.parseInt(ageStr));

            ps.setString(i++, txtTelephone.getText().trim());
            ps.setString(i++, imageFilename);

            if (emailChanged)     ps.setString(i++, newEmail);
            if (changingPassword) ps.setString(i++,
                    BCrypt.hashpw(txtNewPassword.getText(), BCrypt.gensalt()));

            ps.setInt(i, currentUser.getId());
            ps.executeUpdate();

            // ── Update in-memory state ────────────────────────────────────────
            SessionManager.getCurrentUser().setNom(
                    txtNom.getText().trim() + " " + txtPrenom.getText().trim());
            lblName.setText(txtNom.getText().trim() + " " + txtPrenom.getText().trim());
            currentUser.setProfileImage(imageFilename);
            newImagePath = null;

            // If email changed the account is now unverified again
            if (emailChanged) {
                currentUser.setEmail(newEmail);
                currentUser.setVerified(false);
                SessionManager.getCurrentUser().setEmail(newEmail);
                applyVerificationState(false);
                showSuccess("Profil mis à jour. Votre nouvel e-mail doit être vérifié.");
            } else {
                showSuccess("Profil mis à jour avec succès ✔");
            }

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
                if (age < 10 || age > 120) { errAge.setText("Âge invalide (10-120)"); ok = false; }
            } catch (NumberFormatException e) {
                errAge.setText("Âge doit être un nombre"); ok = false;
            }
        }

        String tel = txtTelephone.getText().trim();
        if (!tel.isEmpty() && !tel.matches("^\\+?[0-9\\s\\-]{6,15}$")) {
            errTelephone.setText("Numéro invalide"); ok = false;
        }

        // Validate new email only when field is editable (verified user)
        if (currentUser.isVerified()) {
            String email = txtEmail.getText().trim();
            if (email.isBlank() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                errEmail.setText("Adresse e-mail invalide"); ok = false;
            }
        }

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
        Files.copy(Paths.get(sourcePath),
                Paths.get(PROFILE_IMAGE_DIR + filename),
                StandardCopyOption.REPLACE_EXISTING);
        return filename;
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
        var stream = getClass().getResourceAsStream("/images/logo1.png");
        if (stream != null) profileImageView.setImage(new Image(stream));
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
        errNom.setText(""); errPrenom.setText(""); errAge.setText("");
        errTelephone.setText(""); errEmail.setText("");
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

    private void showInfo(String msg) {
        lblStatus.setStyle("-fx-text-fill: #1f4f65; -fx-font-size: 13px;");
        lblStatus.setText(msg);
    }

    private String nullSafe(String s) { return s != null ? s : ""; }
}
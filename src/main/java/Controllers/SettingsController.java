/* package Controllers;

import Main.java.Utils.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class SettingsController {

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // This variable will hold the reference to the main DashboardController
    private DashboardController dashboardController;

    /**
     * This method is called by the DashboardController to pass its own instance here.
     * @param dashboardController The instance of the parent controller.
     */
   /* public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    private void handleSaveChanges() {
        // --- Add your logic here to change the password in the database ---
        // 1. Get text from all password fields.
        // 2. Check if the new password and confirm password fields match.
        // 3. Get the current user from SessionManager.
        // 4. Verify that the old password entered matches the one in the database for the current user.
        // 5. If it matches, hash the new password and update it in the database.

        showNotification("Settings saved successfully!", true);

        // Use the stored reference to call the public method in DashboardController
        if (dashboardController != null) {
            dashboardController.showDashboardHome();
        }
    }

    private void showNotification(String message, boolean success) {
        // A null check is good practice in case the scene isn't fully loaded
        if (newPasswordField.getScene() != null) {
            Stage stage = (Stage) newPasswordField.getScene().getWindow();
            NotificationUtil.showPopup(stage, message, success);
        }
    }
}
*/

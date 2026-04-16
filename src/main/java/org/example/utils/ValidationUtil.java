package org.example.utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utilitaire de validation des champs de formulaire JavaFX.
 * Chaque méthode colore le champ en rouge si invalide, le remet en normal si valide.
 * Retourne true si valide, false sinon.
 */
public class ValidationUtil {

    private static final String STYLE_ERROR  =
            "-fx-border-color:#e74c3c; -fx-border-width:1.5; " +
                    "-fx-border-radius:8; -fx-background-radius:8; -fx-padding:10 14; -fx-font-size:13px;";
    private static final String STYLE_OK     =
            "-fx-border-color:#dce3ea; -fx-border-width:1; " +
                    "-fx-border-radius:8; -fx-background-radius:8; -fx-padding:10 14; -fx-font-size:13px;";
    private static final String STYLE_ERROR_SMALL =
            "-fx-border-color:#e74c3c; -fx-border-width:1.5; " +
                    "-fx-border-radius:8; -fx-background-radius:8; -fx-padding:9 12; -fx-font-size:13px;";
    private static final String STYLE_OK_SMALL =
            "-fx-border-color:#dce3ea; -fx-border-width:1; " +
                    "-fx-border-radius:8; -fx-background-radius:8; -fx-padding:9 12; -fx-font-size:13px;";

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9\\s\\-().]{7,20}$");
    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?://)[^\\s/$.?#].[^\\s]*$");

    // ─────────────────────────────────────────────────────────────────────────
    // Champ obligatoire
    // ─────────────────────────────────────────────────────────────────────────

    public static boolean required(TextField field, Label errorLabel, String message) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            setFieldError(field, false);
            showFieldError(errorLabel, message);
            return false;
        }
        setFieldOk(field, false);
        hideFieldError(errorLabel);
        return true;
    }

    public static boolean required(TextArea field, Label errorLabel, String message) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            field.setStyle("-fx-border-color:#e74c3c; -fx-border-width:1.5; " +
                    "-fx-border-radius:8; -fx-background-radius:8;");
            showFieldError(errorLabel, message);
            return false;
        }
        field.setStyle("-fx-border-color:#dce3ea; -fx-border-width:1; " +
                "-fx-border-radius:8; -fx-background-radius:8;");
        hideFieldError(errorLabel);
        return true;
    }

    public static <T> boolean required(ComboBox<T> field, Label errorLabel, String message) {
        if (field.getValue() == null) {
            field.setStyle("-fx-border-color:#e74c3c; -fx-border-width:1.5; " +
                    "-fx-border-radius:8; -fx-background-radius:8; -fx-padding:4 8; -fx-font-size:13px;");
            showFieldError(errorLabel, message);
            return false;
        }
        field.setStyle("-fx-border-color:#dce3ea; -fx-border-width:1; " +
                "-fx-border-radius:8; -fx-background-radius:8; -fx-padding:4 8; -fx-font-size:13px;");
        hideFieldError(errorLabel);
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Longueur min / max
    // ─────────────────────────────────────────────────────────────────────────

    public static boolean minLength(TextField field, Label errorLabel, int min, String message) {
        String val = field.getText() == null ? "" : field.getText().trim();
        if (!val.isEmpty() && val.length() < min) {
            setFieldError(field, false);
            showFieldError(errorLabel, message);
            return false;
        }
        setFieldOk(field, false);
        hideFieldError(errorLabel);
        return true;
    }

    public static boolean maxLength(TextField field, Label errorLabel, int max, String message) {
        String val = field.getText() == null ? "" : field.getText().trim();
        if (val.length() > max) {
            setFieldError(field, false);
            showFieldError(errorLabel, message);
            return false;
        }
        setFieldOk(field, false);
        hideFieldError(errorLabel);
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Valeurs numériques
    // ─────────────────────────────────────────────────────────────────────────

    public static boolean isPositiveInteger(TextField field, Label errorLabel, String message) {
        String val = field.getText() == null ? "" : field.getText().trim();
        if (val.isEmpty()) { setFieldOk(field, true); hideFieldError(errorLabel); return true; }
        try {
            int n = Integer.parseInt(val);
            if (n < 0) throw new NumberFormatException();
            setFieldOk(field, true);
            hideFieldError(errorLabel);
            return true;
        } catch (NumberFormatException e) {
            setFieldError(field, true);
            showFieldError(errorLabel, message);
            return false;
        }
    }

    public static boolean isPositiveDouble(TextField field, Label errorLabel, String message) {
        String val = field.getText() == null ? "" : field.getText().trim();
        if (val.isEmpty()) { setFieldOk(field, true); hideFieldError(errorLabel); return true; }
        try {
            double d = Double.parseDouble(val.replace(",", "."));
            if (d < 0) throw new NumberFormatException();
            setFieldOk(field, true);
            hideFieldError(errorLabel);
            return true;
        } catch (NumberFormatException e) {
            setFieldError(field, true);
            showFieldError(errorLabel, message);
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Format date/heure
    // ─────────────────────────────────────────────────────────────────────────

    public static boolean isDateTime(TextField field, Label errorLabel,
                                     String pattern, String message) {
        String val = field.getText() == null ? "" : field.getText().trim();
        if (val.isEmpty()) { setFieldOk(field, false); hideFieldError(errorLabel); return true; }
        try {
            LocalDateTime.parse(val, DateTimeFormatter.ofPattern(pattern));
            setFieldOk(field, false);
            hideFieldError(errorLabel);
            return true;
        } catch (DateTimeParseException e) {
            setFieldError(field, false);
            showFieldError(errorLabel, message);
            return false;
        }
    }

    public static boolean isDateBeforeOrEqual(TextField fieldDebut, TextField fieldFin,
                                              Label errorLabel, String pattern, String message) {
        String v1 = fieldDebut.getText() == null ? "" : fieldDebut.getText().trim();
        String v2 = fieldFin.getText()   == null ? "" : fieldFin.getText().trim();
        if (v1.isEmpty() || v2.isEmpty()) return true;
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime debut = LocalDateTime.parse(v1, fmt);
            LocalDateTime fin   = LocalDateTime.parse(v2, fmt);
            if (debut.isAfter(fin)) {
                setFieldError(fieldFin, false);
                showFieldError(errorLabel, message);
                return false;
            }
            setFieldOk(fieldFin, false);
            hideFieldError(errorLabel);
            return true;
        } catch (DateTimeParseException e) {
            return true; // déjà géré par isDateTime
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Format email / téléphone / URL
    // ─────────────────────────────────────────────────────────────────────────

    public static boolean isEmail(TextField field, Label errorLabel, String message) {
        String val = field.getText() == null ? "" : field.getText().trim();
        if (val.isEmpty()) { setFieldOk(field, true); hideFieldError(errorLabel); return true; }
        if (!EMAIL_PATTERN.matcher(val).matches()) {
            setFieldError(field, true);
            showFieldError(errorLabel, message);
            return false;
        }
        setFieldOk(field, true);
        hideFieldError(errorLabel);
        return true;
    }

    public static boolean isPhone(TextField field, Label errorLabel, String message) {
        String val = field.getText() == null ? "" : field.getText().trim();
        if (val.isEmpty()) { setFieldOk(field, true); hideFieldError(errorLabel); return true; }
        if (!PHONE_PATTERN.matcher(val).matches()) {
            setFieldError(field, true);
            showFieldError(errorLabel, message);
            return false;
        }
        setFieldOk(field, true);
        hideFieldError(errorLabel);
        return true;
    }

    public static boolean isUrl(TextField field, Label errorLabel, String message) {
        String val = field.getText() == null ? "" : field.getText().trim();
        if (val.isEmpty()) { setFieldOk(field, true); hideFieldError(errorLabel); return true; }
        if (!URL_PATTERN.matcher(val).matches()) {
            setFieldError(field, true);
            showFieldError(errorLabel, message);
            return false;
        }
        setFieldOk(field, true);
        hideFieldError(errorLabel);
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers internes
    // ─────────────────────────────────────────────────────────────────────────

    private static void setFieldError(TextField f, boolean small) {
        f.setStyle(small ? STYLE_ERROR_SMALL : STYLE_ERROR);
    }

    private static void setFieldOk(TextField f, boolean small) {
        f.setStyle(small ? STYLE_OK_SMALL : STYLE_OK);
    }

    public static void showFieldError(Label label, String message) {
        if (label == null) return;
        label.setText("⚠  " + message);
        label.setVisible(true);
        label.setManaged(true);
    }

    public static void hideFieldError(Label label) {
        if (label == null) return;
        label.setVisible(false);
        label.setManaged(false);
    }
}
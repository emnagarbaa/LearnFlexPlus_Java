package org.example.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.config.ApiConfig;
import org.example.entities.Evenement;
import org.example.entities.Organisme;
import org.example.Services.EvenementService;
import org.example.Services.OrganismeService;
import org.example.utils.ValidationUtil;
import org.example.Services.EmailService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddEvenementController {

    // ── Champs FXML ───────────────────────────────────────────────
    @FXML private TextField        fTitre;
    @FXML private TextField        fLieu;
    @FXML private ComboBox<String> fMode;
    @FXML private TextField        fCapaciteMax;
    @FXML private TextField        fDateDebut;
    @FXML private TextField        fDateFin;
    @FXML private TextField        fPublicCible;
    @FXML private ComboBox<String> fOrganisme;
    @FXML private TextField        fContactEmail;
    @FXML private TextField        fContactTelephone;
    @FXML private TextField        fLienInscription;
    @FXML private TextArea         fDescription;
    @FXML private CheckBox         fActif;
    @FXML private CheckBox         fInscriptionRequise;
    @FXML private CheckBox         fGratuit;
    @FXML private Label            errorLabel;

    // ── Labels d'erreur ───────────────────────────────────────────
    @FXML private Label errTitre;
    @FXML private Label errMode;
    @FXML private Label errCapaciteMax;
    @FXML private Label errDateDebut;
    @FXML private Label errDateFin;
    @FXML private Label errContactEmail;
    @FXML private Label errContactTelephone;
    @FXML private Label errLienInscription;
    @FXML private Label errDescription;

    // ── Bouton IA ─────────────────────────────────────────────────
    @FXML private Button btnGenerateAI;
    @FXML private Label  aiLoadingLabel;

    // ── Services ──────────────────────────────────────────────────
    private final EvenementService service    = new EvenementService();
    private final OrganismeService orgService = new OrganismeService();
    private DashboardController    dashboardController;

    private static final DateTimeFormatter FMT      = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String            DATE_PAT = "yyyy-MM-dd HH:mm";

    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    // ── Initialisation ────────────────────────────────────────────
    @FXML
    public void initialize() {
        fMode.setItems(FXCollections.observableArrayList(
                "Présentiel", "En ligne", "Hybride"));

        try {
            ObservableList<String> orgNames = FXCollections.observableArrayList();
            orgNames.add("-- Aucun --");
            orgService.findAll().forEach(o -> orgNames.add(o.getId() + " - " + o.getNom()));
            fOrganisme.setItems(orgNames);
            fOrganisme.setValue("-- Aucun --");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Validation en temps réel à la perte du focus
        fTitre.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ValidationUtil.required(fTitre, errTitre, "Le titre est obligatoire.");
        });
        fCapaciteMax.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ValidationUtil.isPositiveInteger(fCapaciteMax, errCapaciteMax,
                    "La capacité doit être un nombre entier positif.");
        });
        fDateDebut.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ValidationUtil.isDateTime(fDateDebut, errDateDebut,
                    DATE_PAT, "Format attendu : yyyy-MM-dd HH:mm");
        });
        fDateFin.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                ValidationUtil.isDateTime(fDateFin, errDateFin,
                        DATE_PAT, "Format attendu : yyyy-MM-dd HH:mm");
                ValidationUtil.isDateBeforeOrEqual(fDateDebut, fDateFin, errDateFin,
                        DATE_PAT, "La date de fin doit être après la date de début.");
            }
        });
        fContactEmail.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ValidationUtil.isEmail(fContactEmail, errContactEmail,
                    "Format email invalide. Ex: nom@domaine.com");
        });
        fContactTelephone.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ValidationUtil.isPhone(fContactTelephone, errContactTelephone,
                    "Format téléphone invalide. Ex: +216 22 345 678");
        });
        fLienInscription.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) ValidationUtil.isUrl(fLienInscription, errLienInscription,
                    "Format URL invalide. Ex: https://exemple.com");
        });
    }

    // ── Navigation ────────────────────────────────────────────────
    @FXML
    private void goBack() {
        if (dashboardController != null) dashboardController.showEvenement();
    }

    // ══════════════════════════════════════════════════════════════
    //  BOUTON IA — Générer/Compléter description avec Groq
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void handleGenerateAI(ActionEvent event) {
        String topic = (fTitre != null) ? fTitre.getText().trim() : "";

        if (topic.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "Veuillez saisir un titre avant de générer la description.");
            return;
        }

        // ✅ Récupère le texte déjà écrit dans la description
        String existingText = (fDescription.getText() != null) ? fDescription.getText().trim() : "";

        btnGenerateAI.setDisable(true);
        if (aiLoadingLabel != null) {
            aiLoadingLabel.setVisible(true);
            aiLoadingLabel.setManaged(true);
        }

        final String finalTopic    = topic;
        final String finalExisting = existingText;

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                // ✅ Passe le texte existant à l'API
                return callGroqAPI(finalTopic, finalExisting);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            // ✅ Concatène la suite générée au texte existant au lieu de remplacer
            String current = fDescription.getText() != null ? fDescription.getText() : "";
            if (current.trim().isEmpty()) {
                fDescription.setText(task.getValue());
            } else {
                // Ajoute un espace si le texte existant ne se termine pas par un espace
                String separator = current.endsWith(" ") ? "" : " ";
                fDescription.setText(current + separator + task.getValue());
            }
            btnGenerateAI.setDisable(false);
            if (aiLoadingLabel != null) {
                aiLoadingLabel.setVisible(false);
                aiLoadingLabel.setManaged(false);
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur IA : " + task.getException().getMessage());
            btnGenerateAI.setDisable(false);
            if (aiLoadingLabel != null) {
                aiLoadingLabel.setVisible(false);
                aiLoadingLabel.setManaged(false);
            }
        }));

        new Thread(task).start();
    }

    // ══════════════════════════════════════════════════════════════
    //  Appel API Groq — avec texte existant pour complétion
    // ══════════════════════════════════════════════════════════════
    private String callGroqAPI(String topic, String existingText) throws Exception {
        String apiKey = ApiConfig.getGroqKey();
        String apiUrl = ApiConfig.getGroqUrl();

        // Échapper le topic
        String safeTopic = topic
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        // ✅ Échapper le texte existant
        String safeExisting = existingText
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        // ✅ Prompt adapté : compléter si texte existant, générer sinon
        String userContent;
        if (safeExisting.isEmpty()) {
            userContent = "Génère une description professionnelle en français pour un événement intitulé : \\\""
                    + safeTopic
                    + "\\\". Rédige 3 à 4 phrases, ton professionnel et engageant.";
        } else {
            userContent = "L'utilisateur rédige une description pour un événement intitulé : \\\""
                    + safeTopic
                    + "\\\". Voici ce qu'il a déjà écrit : \\\""
                    + safeExisting
                    + "\\\". Continue ce texte naturellement à partir de là où il s'est arrêté. "
                    + "Ne répète pas ce qui est déjà écrit. "
                    + "Rédige 2 à 3 phrases supplémentaires, ton professionnel et engageant.";
        }

        String body = "{"
                + "\"model\":\"llama-3.3-70b-versatile\","
                + "\"max_tokens\":500,"
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"Tu es un assistant spécialisé dans la rédaction de descriptions d'événements professionnels en français.\"},"
                + "{\"role\":\"user\",\"content\":\"" + userContent + "\"}"
                + "]"
                + "}";

        HttpURLConnection conn =
                (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 400)
                ? conn.getErrorStream()
                : conn.getInputStream();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        if (status >= 400)
            throw new Exception("Erreur API Groq " + status + " → " + sb);

        return parseGroqResponse(sb.toString());
    }

    // ══════════════════════════════════════════════════════════════
    //  Parser réponse Groq — {"choices":[{"message":{"content":"..."}}]}
    // ══════════════════════════════════════════════════════════════
    private String parseGroqResponse(String json) {
        int start = json.indexOf("\"content\":\"");
        if (start == -1) return "Réponse inattendue de l'IA.";
        start += 11;
        int end = json.indexOf("\"", start);
        if (end == -1) return "Erreur de parsing.";
        return json.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\/", "/");
    }

    // ── Sauvegarde ────────────────────────────────────────────────
    @FXML
    private void saveEvenement() {
        if (!validateAll()) return;

        Evenement e = new Evenement();
        e.setTitre(fTitre.getText().trim());
        e.setLieu(fLieu.getText().trim());
        e.setMode(fMode.getValue());
        e.setPublicCible(fPublicCible.getText().trim());
        e.setContactEmail(fContactEmail.getText().trim());
        e.setContactTelephone(fContactTelephone.getText().trim());
        e.setLienInscription(fLienInscription.getText().trim());
        e.setDescription(fDescription.getText().trim());

        try   { e.setCapaciteMax(Integer.parseInt(fCapaciteMax.getText().trim())); }
        catch (NumberFormatException ex) { e.setCapaciteMax(0); }

        try   { e.setDateDebut(LocalDateTime.parse(fDateDebut.getText().trim(), FMT)); }
        catch (DateTimeParseException ex) { e.setDateDebut(null); }

        try   { e.setDateFin(LocalDateTime.parse(fDateFin.getText().trim(), FMT)); }
        catch (DateTimeParseException ex) { e.setDateFin(null); }

        String orgVal = fOrganisme.getValue();
        if (orgVal != null && !orgVal.startsWith("--")) {
            try {
                int orgId = Integer.parseInt(orgVal.split(" - ")[0].trim());
                Organisme org = new Organisme();
                org.setId(orgId);
                e.setOrganisme(org);
            } catch (Exception ex) { e.setOrganisme(null); }
        } else {
            e.setOrganisme(null);
        }

        try {
            service.create(e);
            EmailService.sendNewEvenementEmail(
                    e.getTitre(),
                    fDateDebut.getText().trim(),
                    fLieu.getText().trim()
            );
            if (dashboardController != null) dashboardController.showEvenement();
        } catch (SQLException ex) {
            ValidationUtil.showFieldError(errorLabel,
                    "Erreur lors de la sauvegarde : " + ex.getMessage());
        }
    }

    // ── Validation ────────────────────────────────────────────────
    private boolean validateAll() {
        boolean ok = true;

        ok &= ValidationUtil.required(fTitre, errTitre, "Le titre est obligatoire.");
        ok &= ValidationUtil.required(fMode, errMode, "Veuillez sélectionner un mode.");
        ok &= ValidationUtil.minLength(fTitre, errTitre, 1,
                "Le titre doit contenir au moins 1 caractères.");
        ok &= ValidationUtil.maxLength(fTitre, errTitre, 100,
                "Le titre ne peut pas dépasser 100 caractères.");
        ok &= ValidationUtil.minLength(
                new TextField(fDescription.getText() != null ? fDescription.getText() : ""),
                errDescription, 10, "La description doit contenir au moins 10 caractères.");
        ok &= ValidationUtil.isPositiveInteger(fCapaciteMax, errCapaciteMax,
                "La capacité doit être un nombre entier positif.");
        ok &= ValidationUtil.isDateTime(fDateDebut, errDateDebut,
                DATE_PAT, "Format attendu : yyyy-MM-dd HH:mm");
        ok &= ValidationUtil.isDateTime(fDateFin, errDateFin,
                DATE_PAT, "Format attendu : yyyy-MM-dd HH:mm");
        ok &= ValidationUtil.isDateBeforeOrEqual(fDateDebut, fDateFin, errDateFin,
                DATE_PAT, "La date de fin doit être après la date de début.");
        ok &= ValidationUtil.isEmail(fContactEmail, errContactEmail,
                "Format email invalide. Ex: nom@domaine.com");
        ok &= ValidationUtil.isPhone(fContactTelephone, errContactTelephone,
                "Format téléphone invalide. Ex: +216 22 345 678");
        ok &= ValidationUtil.isUrl(fLienInscription, errLienInscription,
                "Format URL invalide. Ex: https://exemple.com");

        if (!ok)
            ValidationUtil.showFieldError(errorLabel, "Veuillez corriger les erreurs avant d'enregistrer.");
        else
            ValidationUtil.hideFieldError(errorLabel);

        return ok;
    }

    // ── Helper Alert ──────────────────────────────────────────────
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
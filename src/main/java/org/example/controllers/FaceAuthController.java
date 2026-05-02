package org.example.controllers;
import org.example.Services.ServiceBan;

import org.example.Models.User;
import org.example.utils.SessionManager;
import org.example.Services.ServiceUsers;
import org.example.utils.FaceIdUtil;
import org.example.utils.ImageUtil;
import org.example.entities.Ban;
import org.example.entities.Users;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FaceAuthController {

    @FXML private ImageView cameraView;

    private VideoCapture capture;
    private CascadeClassifier faceDetector;
    private final List<StoredFace> storedFaces = new ArrayList<>();
    private volatile boolean running;
    private long lastAuthAttempt;
    private String lastMatchedEmail;
    private int consecutiveMatches;

    @FXML
    public void initialize() {
        try {
            FaceIdUtil.loadOpenCv();
            capture = new VideoCapture(0);
            capture.set(Videoio.CAP_PROP_FRAME_WIDTH, FaceIdUtil.CAMERA_WIDTH);
            capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, FaceIdUtil.CAMERA_HEIGHT);
            faceDetector = new CascadeClassifier(FaceIdUtil.cascadePath());
            running = true;

            if (faceDetector.empty()) {
                showAlert(Alert.AlertType.ERROR, "Face detector could not be loaded.");
                return;
            }

            if (capture == null || !capture.isOpened()) {
                showAlert(Alert.AlertType.ERROR, "Camera is not available. Close other camera apps and try again.");
                return;
            }

            loadStoredFaces();
            if (storedFaces.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No saved Face ID found in " + FaceIdUtil.FACE_DIRECTORY);
            }
            startCameraLoop();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Face ID error: " + e.getMessage());
        }
    }

    private void startCameraLoop() {
        Thread cameraThread = new Thread(() -> {
            Mat frame = new Mat();
            try {
                while (running) {
                    if (capture != null && capture.isOpened()) {
                        capture.read(frame);
                        if (!frame.empty()) {
                            Image image = ImageUtil.matToImage(frame);
                            Platform.runLater(() -> cameraView.setImage(image));

                            long now = System.currentTimeMillis();
                            if (now - lastAuthAttempt >= FaceIdUtil.AUTH_INTERVAL_MS) {
                                lastAuthAttempt = now;
                                tryAuthenticate(frame);
                            }
                        }
                    }

                    Thread.sleep(FaceIdUtil.PREVIEW_DELAY_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                frame.release();
            }
        }, "face-auth-camera");
        cameraThread.setDaemon(true);
        cameraThread.start();
    }

    private void loadStoredFaces() {
        releaseStoredFaces();

        File facesDir = FaceIdUtil.ensureFaceDirectory();
        File[] faceFiles = facesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (faceFiles == null) {
            return;
        }

        for (File file : faceFiles) {
            Mat image = Imgcodecs.imread(file.getAbsolutePath());
            try {
                if (!image.empty()) {
                    Mat template = FaceIdUtil.prepareStoredFace(image, faceDetector);
                    if (template != null && !template.empty()) {
                        storedFaces.add(new StoredFace(FaceIdUtil.emailFromFaceFile(file), template));
                    }
                }
            } finally {
                image.release();
            }
        }
    }

    private void tryAuthenticate(Mat frame) {
        if (storedFaces.isEmpty()) {
            loadStoredFaces();
            if (storedFaces.isEmpty()) {
                return;
            }
        }

        Mat liveTemplate = FaceIdUtil.normalizeFace(frame, faceDetector);
        if (liveTemplate == null || liveTemplate.empty()) {
            resetMatch();
            return;
        }

        try {
            StoredFace bestMatch = null;
            double bestDistance = Double.MAX_VALUE;

            for (StoredFace storedFace : storedFaces) {
                double distance = faceDistance(storedFace.template, liveTemplate);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestMatch = storedFace;
                }
            }

            if (bestMatch != null && bestDistance < FaceIdUtil.FACE_MATCH_THRESHOLD) {
                acceptMatch(bestMatch.email);
            } else {
                resetMatch();
            }
        } catch (Exception e) {
            e.printStackTrace();
            resetMatch();
        } finally {
            liveTemplate.release();
        }
    }

    private double faceDistance(Mat storedTemplate, Mat liveTemplate) {
        double l2Distance = Core.norm(storedTemplate, liveTemplate);
        double averageDiff = Core.norm(storedTemplate, liveTemplate, Core.NORM_L1)
                / (FaceIdUtil.TEMPLATE_SIZE * FaceIdUtil.TEMPLATE_SIZE);

        if (l2Distance < FaceIdUtil.FACE_MATCH_THRESHOLD
                || averageDiff < FaceIdUtil.FACE_AVERAGE_DIFF_THRESHOLD) {
            return Math.min(l2Distance, averageDiff * 100.0);
        }

        return l2Distance;
    }

    private void acceptMatch(String email) throws Exception {
        if (email.equals(lastMatchedEmail)) {
            consecutiveMatches++;
        } else {
            lastMatchedEmail = email;
            consecutiveMatches = 1;
        }

        if (consecutiveMatches >= FaceIdUtil.REQUIRED_CONSECUTIVE_MATCHES) {
            handleLogin(email);
        }
    }

    private void resetMatch() {
        lastMatchedEmail = null;
        consecutiveMatches = 0;
    }

    private void handleLogin(String email) throws Exception {
        running = false;

        ServiceUsers userService = new ServiceUsers();
        Users found = userService.findByEmail(email);

        if (found == null) {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "No user found for Face ID: " + email);
                returnToLogin();
            });
            return;
        }

        ServiceBan banService = new ServiceBan();
        Ban activeBan = banService.getActiveBan(found.getId());
        if (activeBan != null) {
            Platform.runLater(() -> {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Compte suspendu: " + activeBan.getReason() + "\nJusqu'a: " + activeBan.getFormattedExpiry()
                );
                returnToLogin();
            });
            return;
        }

        User sessionUser = new User(
                found.getId(),
                found.getNom() + " " + found.getPrenom(),
                found.getEmail(),
                found.getPassword(),
                found.getRole(),
                found.getProfileImage()
        );
        SessionManager.login(sessionUser);

        Platform.runLater(() -> {
            try {
                stop();
                Stage stage = (Stage) cameraView.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("/org/example/fxml/dashboard.fxml"));
                stage.setScene(new Scene(root, 1200, 800));
                stage.setTitle("LearnFlex+ Dashboard");
                stage.centerOnScreen();
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Dashboard loading error: " + e.getMessage());
            }
        });
    }

    @FXML
    private void cancel() {
        stop();
        returnToLogin();
    }

    private void returnToLogin() {
        try {
            Stage stage = (Stage) cameraView.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/fxml/login.fxml"));
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Login");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Login loading error: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
        releaseStoredFaces();
    }

    private void releaseStoredFaces() {
        for (StoredFace storedFace : storedFaces) {
            storedFace.template.release();
        }
        storedFaces.clear();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Face ID");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class StoredFace {
        private final String email;
        private final Mat template;

        private StoredFace(String email, Mat template) {
            this.email = email;
            this.template = template;
        }
    }
}


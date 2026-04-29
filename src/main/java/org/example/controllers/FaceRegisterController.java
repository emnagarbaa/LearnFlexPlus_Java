package org.example.controllers;

import org.example.utils.FaceIdUtil;
import org.example.utils.ImageUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class FaceRegisterController {

    @FXML private ImageView cameraView;

    private String email;
    private VideoCapture capture;
    private CascadeClassifier faceDetector;
    private Mat latestFrame;
    private volatile boolean running;

    public void setEmail(String email) {
        this.email = email == null ? "" : email.trim().toLowerCase();
    }

    @FXML
    public void initialize() {
        try {
            FaceIdUtil.loadOpenCv();
            capture = new VideoCapture(0);
            capture.set(Videoio.CAP_PROP_FRAME_WIDTH, FaceIdUtil.CAMERA_WIDTH);
            capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, FaceIdUtil.CAMERA_HEIGHT);
            faceDetector = new CascadeClassifier(FaceIdUtil.cascadePath());
            latestFrame = new Mat();
            running = true;

            if (faceDetector.empty()) {
                showAlert(Alert.AlertType.ERROR, "Face detector could not be loaded.");
                return;
            }

            if (capture == null || !capture.isOpened()) {
                showAlert(Alert.AlertType.ERROR, "Camera is not available. Close other camera apps and try again.");
                return;
            }

            startCamera();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Face ID error: " + e.getMessage());
        }
    }

    private void startCamera() {
        Thread cameraThread = new Thread(() -> {
            Mat frame = new Mat();
            try {
                while (running) {
                    if (capture != null && capture.isOpened()) {
                        capture.read(frame);
                        if (!frame.empty()) {
                            synchronized (this) {
                                frame.copyTo(latestFrame);
                            }
                            Image image = ImageUtil.matToImage(frame);
                            Platform.runLater(() -> cameraView.setImage(image));
                        }
                    }

                    Thread.sleep(FaceIdUtil.PREVIEW_DELAY_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                frame.release();
            }
        }, "face-register-camera");
        cameraThread.setDaemon(true);
        cameraThread.start();
    }

    @FXML
    private void registerFace() {
        if (email == null || email.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Email required before saving Face ID.");
            return;
        }

        if (capture == null || !capture.isOpened()) {
            showAlert(Alert.AlertType.ERROR, "Camera is not available.");
            return;
        }

        Mat frameToSave = new Mat();
        synchronized (this) {
            if (latestFrame != null && !latestFrame.empty()) {
                latestFrame.copyTo(frameToSave);
            }
        }

        if (frameToSave.empty()) {
            frameToSave.release();
            showAlert(Alert.AlertType.ERROR, "Could not capture a face image.");
            return;
        }

        Mat normalizedFace = FaceIdUtil.normalizeFace(frameToSave, faceDetector);
        frameToSave.release();

        if (normalizedFace == null || normalizedFace.empty()) {
            if (normalizedFace != null) {
                normalizedFace.release();
            }
            showAlert(Alert.AlertType.ERROR, "No face detected. Look at the camera and try again.");
            return;
        }

        try {
            FaceIdUtil.ensureFaceDirectory();
            String outputPath = FaceIdUtil.facePathForEmail(email);
            Imgcodecs.imwrite(outputPath, normalizedFace);
            showAlert(Alert.AlertType.INFORMATION, "Face ID saved successfully.");
            close();
        } finally {
            normalizedFace.release();
        }
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        stop();
        Stage stage = (Stage) cameraView.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    public void stop() {
        running = false;
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
        if (latestFrame != null) {
            latestFrame.release();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Face ID");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

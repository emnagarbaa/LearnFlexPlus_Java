package Utils;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FaceIdUtil {

    public static final String FACE_DIRECTORY = "C:\\xampp\\htdocs\\faces\\";
    public static final double FACE_MATCH_THRESHOLD = 4200.0;
    public static final double FACE_AVERAGE_DIFF_THRESHOLD = 42.0;
    public static final int TEMPLATE_SIZE = 120;
    public static final int CAMERA_WIDTH = 640;
    public static final int CAMERA_HEIGHT = 480;
    public static final long PREVIEW_DELAY_MS = 33;
    public static final long AUTH_INTERVAL_MS = 900;
    public static final int REQUIRED_CONSECUTIVE_MATCHES = 2;
    private static final String CASCADE_RESOURCE = "/haarcascade_frontalface_alt.xml";
    private static boolean loaded;

    private FaceIdUtil() {
    }

    public static synchronized void loadOpenCv() {
        if (!loaded) {
            OpenCV.loadLocally();
            loaded = true;
        }
    }

    public static File ensureFaceDirectory() {
        File dir = new File(FACE_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String facePathForEmail(String email) {
        return FACE_DIRECTORY + sanitizeEmail(email) + ".png";
    }

    public static String emailFromFaceFile(File file) {
        return file.getName().replaceFirst("\\.png$", "").trim();
    }

    public static String cascadePath() throws IOException {
        try (InputStream input = FaceIdUtil.class.getResourceAsStream(CASCADE_RESOURCE)) {
            if (input == null) {
                throw new IOException("Missing resource " + CASCADE_RESOURCE);
            }

            Path temp = Files.createTempFile("haarcascade_frontalface_alt", ".xml");
            Files.copy(input, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            temp.toFile().deleteOnExit();
            return temp.toAbsolutePath().toString();
        }
    }

    public static Mat normalizeFace(Mat source, CascadeClassifier faceDetector) {
        Mat gray = new Mat();
        Mat roi = null;
        Mat resized = new Mat();
        Mat normalized = new Mat();

        try {
            if (source.channels() == 1) {
                gray = source.clone();
            } else {
                Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY);
            }

            Rect face = largestFace(gray, faceDetector);
            if (face == null) {
                return null;
            }

            roi = new Mat(gray, face);
            Imgproc.resize(roi, resized, new Size(TEMPLATE_SIZE, TEMPLATE_SIZE));
            Imgproc.equalizeHist(resized, normalized);
            return normalized.clone();
        } finally {
            gray.release();
            resized.release();
            normalized.release();
            if (roi != null) {
                roi.release();
            }
        }
    }

    public static Mat prepareStoredFace(Mat storedImage, CascadeClassifier faceDetector) {
        if (storedImage.empty()) {
            return null;
        }

        if (storedImage.rows() == TEMPLATE_SIZE && storedImage.cols() == TEMPLATE_SIZE) {
            return normalizeTemplateImage(storedImage);
        }

        return normalizeFace(storedImage, faceDetector);
    }

    private static Mat normalizeTemplateImage(Mat source) {
        Mat gray = new Mat();
        Mat resized = new Mat();
        Mat normalized = new Mat();

        try {
            if (source.channels() == 1) {
                gray = source.clone();
            } else {
                Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY);
            }

            Imgproc.resize(gray, resized, new Size(TEMPLATE_SIZE, TEMPLATE_SIZE));
            Imgproc.equalizeHist(resized, normalized);
            return normalized.clone();
        } finally {
            gray.release();
            resized.release();
            normalized.release();
        }
    }

    private static Rect largestFace(Mat grayImage, CascadeClassifier faceDetector) {
        MatOfRect faces = new MatOfRect();
        try {
            faceDetector.detectMultiScale(
                    grayImage,
                    faces,
                    1.1,
                    4,
                    0,
                    new Size(80, 80),
                    new Size()
            );

            Rect largest = null;
            for (Rect face : faces.toArray()) {
                if (largest == null || face.area() > largest.area()) {
                    largest = face;
                }
            }
            return largest;
        } finally {
            faces.release();
        }
    }

    private static String sanitizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}

import Services.ServiceUsers;
import Utils.FaceIdUtil;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;

public class FaceIdSmokeTest {
    public static void main(String[] args) throws Exception {
        FaceIdUtil.loadOpenCv();
        CascadeClassifier detector = new CascadeClassifier(FaceIdUtil.cascadePath());
        if (detector.empty()) {
            throw new IllegalStateException("Detector failed to load");
        }

        File[] files = FaceIdUtil.ensureFaceDirectory().listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) {
            throw new IllegalStateException("No saved Face ID files found");
        }

        Mat image = Imgcodecs.imread(files[0].getAbsolutePath());
        Mat stored = FaceIdUtil.prepareStoredFace(image, detector);
        if (stored == null || stored.empty()) {
            throw new IllegalStateException("Saved Face ID could not be prepared: " + files[0].getName());
        }

        System.out.println("file=" + files[0].getName());
        System.out.println("image=" + image.cols() + "x" + image.rows() + " channels=" + image.channels());
        System.out.println("stored=" + stored.cols() + "x" + stored.rows() + " channels=" + stored.channels());
        System.out.println("selfDistance=" + Core.norm(stored, stored));
        System.out.println("threshold=" + FaceIdUtil.FACE_MATCH_THRESHOLD);
        System.out.println("dbUserFound=" + (new ServiceUsers().findByEmail(FaceIdUtil.emailFromFaceFile(files[0])) != null));

        VideoCapture capture = new VideoCapture(0);
        Mat frame = new Mat();
        boolean cameraOpened = capture.isOpened();
        boolean frameRead = cameraOpened && capture.read(frame) && !frame.empty();
        System.out.println("cameraOpened=" + cameraOpened);
        System.out.println("frameRead=" + frameRead + (frameRead ? " frame=" + frame.cols() + "x" + frame.rows() : ""));
        capture.release();
        frame.release();

        image.release();
        stored.release();
    }
}

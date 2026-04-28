package Utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

public class ImageUtil {

    public static Image matToImage(Mat source) {
        Mat rgb = new Mat();
        Imgproc.cvtColor(source, rgb, Imgproc.COLOR_BGR2RGB);

        WritableImage image = new WritableImage(rgb.cols(), rgb.rows());
        PixelWriter writer = image.getPixelWriter();
        ByteBuffer buffer = ByteBuffer.allocate(rgb.channels() * rgb.cols() * rgb.rows());
        rgb.get(0, 0, buffer.array());

        for (int y = 0; y < rgb.rows(); y++) {
            for (int x = 0; x < rgb.cols(); x++) {
                int index = (y * rgb.cols() + x) * 3;
                int r = buffer.get(index) & 0xFF;
                int g = buffer.get(index + 1) & 0xFF;
                int b = buffer.get(index + 2) & 0xFF;
                writer.setColor(x, y, Color.rgb(r, g, b));
            }
        }

        rgb.release();
        return image;
    }
}

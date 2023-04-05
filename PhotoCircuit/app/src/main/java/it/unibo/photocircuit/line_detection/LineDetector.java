package it.unibo.photocircuit.line_detection;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import it.unibo.photocircuit.Couple;

public class LineDetector {
    public static Mat detectLines(Mat image) {
        Mat lines = new Mat();
        Imgproc.HoughLinesP(image, lines, 1, Math.PI / 180, 20, 10, 50);
        return lines;
    }

    public static Mat detectLinesLSD(Mat image) {
        Mat lines = new Mat();
        Imgproc.createLineSegmentDetector(Imgproc.LSD_REFINE_NONE, 1, 0.6, 2.0, 70).detect(image, lines);
        return lines;
    }

    public static Mat removeDuplicateLines(Mat inLines) {
        final int threshold = 10;
        Mat outLines = new Mat(inLines.rows(), inLines.cols(), inLines.type());
        int outLinesLen = 0;
        boolean found;

        for (int i = 0; i < inLines.rows(); i++) {
            double[] inLine = inLines.get(i, 0);
            found = false;
            for (int j = 0; j < outLinesLen; j++) {
                double[] outLine = outLines.get(j, 0);

                double deltaX1 = Math.abs(inLine[0] - outLine[0]);
                double deltaY1 = Math.abs(inLine[1] - outLine[1]);

                double deltaX1T = Math.abs(inLine[0] - outLine[2]);
                double deltaY1T = Math.abs(inLine[1] - outLine[3]);

                if ((deltaX1 < threshold && deltaY1 < threshold) || (deltaX1T < threshold && deltaY1T < threshold)) {
                    double deltaX2 = Math.abs(inLine[2] - outLine[2]);
                    double deltaY2 = Math.abs(inLine[3] - outLine[3]);

                    double deltaX2T = Math.abs(inLine[2] - outLine[0]);
                    double deltaY2T = Math.abs(inLine[3] - outLine[1]);

                    if ((deltaX2 < threshold && deltaY2 < threshold) || (deltaX2T < threshold && deltaY2T < threshold)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                outLines.put(outLinesLen, 0, inLine);
                outLinesLen++;
            }
        }

        return outLines.rowRange(0, outLinesLen);
    }

    public static void normalizeLines(Mat lines) {
        for (int i = 0; i < lines.rows(); i++) {
            double[] coords = lines.get(i, 0);
            int[] iCoords = new int[coords.length];
            for (int j = 0; j < coords.length; j++) {
                iCoords[j] = ((int)coords[j] / 20 * 20);
            }
            lines.put(i, 0, new double[]{iCoords[0], iCoords[1], iCoords[2], iCoords[3]});
        }
    }

    public static void drawLines(Mat image, Mat lines) {
        for (int y = 0; y < lines.rows(); y++) {
            double[] line = lines.get(y, 0);
            Random r = new Random();
            int red = r.nextInt(255);
            int green = r.nextInt(255);
            int blue = r.nextInt(255);
            Imgproc.line(image, new Point(line[0], line[1]), new Point(line[2], line[3]), new Scalar(red, green, blue), 2);
        }
    }
}

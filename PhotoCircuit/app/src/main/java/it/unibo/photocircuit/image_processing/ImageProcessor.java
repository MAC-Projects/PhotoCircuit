package it.unibo.photocircuit.image_processing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unibo.photocircuit.ml_handling.CircuitElement;

public class ImageProcessor {
    private static Mat[] kernelsThinning;
    private static Mat[] kernelsPruning;
    private static Mat[] kernelsStraight;

    static {
        kernelsThinning = new Mat[]{ Mat.zeros(3, 3, CvType.CV_8S), Mat.zeros(3, 3, CvType.CV_8S) };
        kernelsPruning = new Mat[]{ Mat.zeros(3, 3, CvType.CV_8S), Mat.zeros(3, 3, CvType.CV_8S) };
        kernelsStraight = new Mat[]{ Mat.zeros(3, 3, CvType.CV_8S), Mat.zeros(3, 3, CvType.CV_8S) };

        kernelsThinning[0].put(0, 0, new byte[]{ 0, -1, -1 });
        kernelsThinning[0].put(1, 0, new byte[]{ 1, 1, -1 });
        kernelsThinning[0].put(2, 0, new byte[]{ 0, 1, 0 });

        kernelsThinning[1].put(0, 0, new byte[]{ -1, -1, -1 });
        kernelsThinning[1].put(1, 0, new byte[]{ 0, 1, 0 });
        kernelsThinning[1].put(2, 0, new byte[]{ 1, 1, 1 });

        kernelsPruning[0].put(0, 0, new byte[]{ -1, -1, -1 });
        kernelsPruning[0].put(1, 0, new byte[]{ -1, 1, -1 });
        kernelsPruning[0].put(2, 0, new byte[]{ -1, 0, 0 });

        kernelsPruning[1].put(0, 0, new byte[]{ -1, -1, -1 });
        kernelsPruning[1].put(1, 0, new byte[]{ -1, 1, -1 });
        kernelsPruning[1].put(2, 0, new byte[]{ 0, 0, -1 });

        kernelsStraight[0].put(0, 0, new byte[]{ 0, 0, 0 });
        kernelsStraight[0].put(1, 0, new byte[]{ 1, 1, 1 });
        kernelsStraight[0].put(2, 0, new byte[]{ 0, 0, 0 });

        kernelsStraight[1].put(0, 0, new byte[]{ 0, 1, 0 });
        kernelsStraight[1].put(1, 0, new byte[]{ 0, 1, 0 });
        kernelsStraight[1].put(2, 0, new byte[]{ 0, 1, 0 });

        kernelsThinning = allRotations(kernelsThinning);
        kernelsPruning = allRotations(kernelsPruning);
        kernelsStraight = allRotations(kernelsStraight);
    }

    public static Mat[] allRotations(Mat[] matrices) {
        int newLength = matrices.length * 4;
        Mat[] result = new Mat[newLength];
        for (int i = 0; i < matrices.length; i++) {
            for (int rot = 0; rot < 4; rot++) {
                if (rot == 0) {
                    result[i * 4 + rot] = matrices[i];
                } else {
                    result[i * 4 + rot] = Mat.zeros(matrices[i].size(), CvType.CV_8S);
                    int rotFlag  =-1;
                    switch (rot) {
                        case 1: rotFlag = Core.ROTATE_90_COUNTERCLOCKWISE; break;
                        case 2: rotFlag = Core.ROTATE_180; break;
                        case 3: rotFlag = Core.ROTATE_90_CLOCKWISE; break;
                    }
                    Core.rotate(matrices[i], result[i * 4 + rot], rotFlag);
                }
            }
        }

        return result;
    }

    /*public static Bitmap processImage(@NonNull ImageProxy image) {
        Mat mat = convertImageProxytoMat(image);

        mat = processImage(mat);

        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        return bitmap;
    }*/

    public static Mat resizeImageByWidth(Mat imgIn, int newWidth) {
        Mat imgOut = new Mat();
        int newHeight = (int) (newWidth * ((float)imgIn.height() / imgIn.width()));
        Imgproc.resize(imgIn, imgOut, new Size(newWidth, newHeight));
        return imgOut;
    }

    public static Mat resizeImageByHeight(Mat imgIn, int newHeight) {
        Mat imgOut = new Mat();
        int newWidth = (int) (newHeight * ((float)imgIn.width() / imgIn.height()));
        Imgproc.resize(imgIn, imgOut, new Size(newWidth, newHeight));
        return imgOut;
    }

    public static Mat resizeImageToRectangle(Mat imgIn, int newWidth, int newHeight) {
        float originalRatio = (float) imgIn.width() / imgIn.height();
        float targetRatio = (float) newWidth / newHeight;
        if (originalRatio > targetRatio) {
            return resizeImageByWidth(imgIn, newWidth);
        } else {
            return resizeImageByHeight(imgIn, newHeight);
        }
    }

    public static Mat resizeImageFillBlack(Mat imgIn, int newWidth, int newHeight, int newType) {
        Mat resizedMat = resizeImageToRectangle(imgIn, newWidth, newHeight);
        Mat result = new Mat(newWidth, newHeight, newType, Scalar.all(0));
        int startX, endX, startY, endY;
        if (resizedMat.width() == newWidth) {
            startX = 0;
            endX = newWidth;
            startY = newHeight / 2 - resizedMat.height() / 2;
            endY = startY + resizedMat.height();
        } else {
            startX = newWidth / 2 - resizedMat.width() / 2;
            endX = startX + resizedMat.width();
            startY = 0;
            endY = newHeight;
        }
        Mat submat = result.submat(startY, endY, startX, endX);
        resizedMat.copyTo(submat);
        return result;
    }

    public static ImageProcessingResult processImage(Mat imgIn) {
        Mat img = Mat.zeros(imgIn.size(), CvType.CV_8U);

        Imgproc.cvtColor(imgIn, img, Imgproc.COLOR_RGB2GRAY);

        Mat imgFiltered = new Mat();
        Mat imgThresh = new Mat();
        Mat imgClosed = new Mat();
        Mat imgThinned = null;
        Mat imgPruned = null;
        Mat outImgPruned = null;
        Mat preCurvedLines = null;
        Mat endpoints = new Mat();
        Mat elemPatches = new Mat();

        Imgproc.bilateralFilter(img, imgFiltered, 35, 10, 10);

        int avgColor = (int) (Core.mean(imgFiltered).val[0]);
        if (avgColor > 100) {
            Core.bitwise_not(imgFiltered, imgFiltered);
            avgColor = 255 - avgColor;
        }

        //Imgproc.threshold(imgFiltered, imgThresh, avgColor + 40, 255, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(imgFiltered, imgThresh, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 51, -20);

        // Closing
        Imgproc.morphologyEx(imgThresh, imgClosed, Imgproc.MORPH_CLOSE, Mat.ones(8, 8, CvType.CV_8U));

        // Thinning
        imgThinned = iterativeReverseHitmiss(imgClosed, kernelsThinning);

        // Light pruning (for output)
        outImgPruned = iterativeReverseHitmiss(imgThinned, kernelsPruning, 5);

        // Heavy pruning (for endpoints)
        imgPruned = iterativeReverseHitmiss(imgThinned, kernelsPruning, 22);

        // Get curved lines
        preCurvedLines = iterativeReverseHitmiss(imgPruned, kernelsStraight);
        Mat curvedLines = new Mat();
        Imgproc.GaussianBlur(preCurvedLines, curvedLines, new Size(101, 101), 0);
        Core.multiply(curvedLines, new Scalar(2), curvedLines);
        Imgproc.threshold(curvedLines, curvedLines, 6, 255, Imgproc.THRESH_BINARY);
        curvedLines = dilation(curvedLines, 12);

        // Get endpoints
        Core.subtract(imgThinned, imgPruned, endpoints);
        Imgproc.GaussianBlur(endpoints, endpoints, new Size(101, 101), 0);
        Core.multiply(endpoints, new Scalar(2), endpoints);
        Imgproc.threshold(endpoints, endpoints, 4, 255, Imgproc.THRESH_BINARY);
        curvedLines = dilation(curvedLines, 2);

        // Get patches
        Core.add(endpoints, curvedLines, elemPatches);
        elemPatches = dilation(elemPatches, 2);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(elemPatches, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Rect> rects = new ArrayList<>();
        List<Mat> mats = new ArrayList<>();

        int minArea = (int) (0.005 * imgIn.width() * imgIn.height());
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            if (rect.area() >= minArea) {
                rects.add(rect);
            }
        }

        return new ImageProcessingResult(rects, imgThresh, outImgPruned);
    }

    public static Mat detectLines(Mat image) {
        Mat lines = new Mat();
        Imgproc.createLineSegmentDetector(Imgproc.LSD_REFINE_NONE, 1, 0.6, 2.0, 70).detect(image, lines);
        return lines;
    }


    public static void drawRectangles(Mat img, List<Rect> rects, Scalar color) {
        for (Rect rect : rects) {
            //Rect rect = Imgproc.boundingRect(contour);
            //Imgproc.rectangle(elemRects, rect, new Scalar(0.01, 1, 1), 3);
            Imgproc.rectangle(img, rect, color, 3);
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

    public static void deleteElements(Mat img, List<CircuitElement> elements) {
        for (CircuitElement elem : elements) {
            Rect rect = elem.getRect();
            Mat blackRect = new Mat(rect.height, rect.width, CvType.CV_8U, new Scalar(0));
            Mat submat = img.submat(rect);
            blackRect.copyTo(submat);
        }
    }

    private static Mat iterativeReverseHitmiss(Mat src, Mat[] kernels) {
        return iterativeReverseHitmiss(src, kernels, -1);
    }

    private static Mat iterativeReverseHitmiss(Mat src, Mat[] kernels, int numIterations) {
        Mat dst = src.clone();
        //Mat tmp1 = new Mat(dst.size(), dst.type());
        Mat tmp1 = dst.clone();
        Mat tmp2 = new Mat();

        int i = 0;
        while (numIterations == -1 || i < numIterations) {
            //dst.copyTo(tmp1);

            // Apply all kernels
            for (Mat kernel : kernels) {
                Imgproc.morphologyEx(tmp1, tmp2, Imgproc.MORPH_HITMISS, kernel);
                Core.subtract(tmp1, tmp2, tmp1);
            }

            if (numIterations == -1) {
                Core.compare(dst, tmp1, tmp2, Core.CMP_NE);
                if (Core.countNonZero(tmp2) == 0)
                    break;
            }
            tmp1.copyTo(dst);
            i++;
        }
        return dst;
    }

    private static Mat dilation(Mat src) {
        return dilation(src, 1);
    }

    private static Mat dilation(Mat src, int numIterations) {
        Mat kernel = Mat.ones(3, 3, CvType.CV_8U);
        Mat result = src.clone();
        for (int i = 0; i < numIterations; i++) {
            Imgproc.dilate(result, result, kernel);
        }
        return result;
    }

    public static Mat convertYUVImageProxyToMat(@NonNull ImageProxy img) {
        byte[] nv21;

        ByteBuffer yBuffer = img.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = img.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = img.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        Mat yuv = new Mat(img.getHeight() + img.getHeight()/2, img.getWidth(), CvType.CV_8UC1);
        yuv.put(0, 0, nv21);
        Mat rgb = new Mat();
        Imgproc.cvtColor(yuv, rgb, Imgproc.COLOR_YUV2RGB_NV21, 3);
        Core.rotate(rgb, rgb, Core.ROTATE_90_CLOCKWISE);
        return rgb;
    }

    public static Mat convertRGBImageProxyToMat(@NonNull ImageProxy img) {
        byte[] rgbByteArray;

        ByteBuffer rBuffer = img.getPlanes()[0].getBuffer();
        ByteBuffer gBuffer = img.getPlanes()[1].getBuffer();
        ByteBuffer bBuffer = img.getPlanes()[2].getBuffer();

        int rSize = rBuffer.remaining();
        int gSize = gBuffer.remaining();
        int bSize = bBuffer.remaining();

        rgbByteArray = new byte[rSize + gSize + bSize];

        rBuffer.get(rgbByteArray, 0, rSize);
        gBuffer.get(rgbByteArray, rSize, gSize);
        bBuffer.get(rgbByteArray, rSize + gSize, bSize);

        Mat rgb = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
        rgb.put(0, 0, rgbByteArray);
        //Core.rotate(rgb, rgb, Core.ROTATE_90_CLOCKWISE);
        return rgb;
    }

    public static Mat convertJPEGImageProxyToMat(@NonNull ImageProxy img) {
        ByteBuffer dataBuf = img.getPlanes()[0].getBuffer();
        int size = dataBuf.remaining();
        byte[] buf = new byte[size];
        dataBuf.get(buf);
        Bitmap bmp = BitmapFactory.decodeByteArray(buf, 0, buf.length);
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat);
        Core.rotate(mat, mat, Core.ROTATE_90_CLOCKWISE);
        return mat;
    }

    public static Bitmap convertMatToBitmap(Mat mat) {
        Bitmap bmp = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
    }
}

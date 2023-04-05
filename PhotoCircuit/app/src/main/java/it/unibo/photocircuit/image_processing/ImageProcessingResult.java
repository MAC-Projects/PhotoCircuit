package it.unibo.photocircuit.image_processing;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.io.Serializable;
import java.util.List;

public class ImageProcessingResult implements Serializable {
    private List<Rect> circuitElementRects;
    private Mat thresholdedImage;
    private Mat thinnedImage;

    public ImageProcessingResult(List<Rect> circuitElementRects, Mat thresholdedImage, Mat thinnedImage) {
        this.circuitElementRects = circuitElementRects;
        this.thresholdedImage = thresholdedImage;
        this.thinnedImage = thinnedImage;
    }

    public List<Rect> getCircuitElementRects() {
        return circuitElementRects;
    }

    public void setCircuitElementRects(List<Rect> circuitElementRects) {
        this.circuitElementRects = circuitElementRects;
    }

    public Mat getThresholdedImage() {
        return thresholdedImage;
    }

    public void setClosedImage(Mat thresholdedImage) {
        this.thresholdedImage = thresholdedImage;
    }

    public Mat getThinnedImage() {
        return thinnedImage;
    }

    public void setThinnedImage(Mat thinnedImage) {
        this.thinnedImage = thinnedImage;
    }
}

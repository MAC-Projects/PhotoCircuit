package it.unibo.photocircuit;

import org.opencv.core.Mat;

import java.util.concurrent.Semaphore;

import it.unibo.photocircuit.image_processing.ImageProcessingResult;
import it.unibo.photocircuit.image_processing.ImageProcessor;

public class PreviewProcessingThread extends Thread {
    private Mat inputImage = null;
    private boolean newInputImageAvailable = false;
    private ImageProcessingResult processingResult = null;
    private final Object inputLock = new Object();
    private final Object outputLock = new Object();
    private final Semaphore sleepSemaphore = new Semaphore(0);

    public void setInputImage(Mat img) {
        synchronized (inputLock) {
            inputImage = img.clone();
            if (!newInputImageAvailable) {
                sleepSemaphore.release();
                newInputImageAvailable = true;
            }
        }
    }

    private Mat getInputImage() {
        Mat img;
        synchronized (inputLock) {
            img = inputImage;
            newInputImageAvailable = false;
        }
        return img;
    }

    private void setProcessingResult(ImageProcessingResult result) {
        synchronized (outputLock) {
            processingResult = result;
        }
    }

    public ImageProcessingResult getProcessingResult() {
        ImageProcessingResult result;
        synchronized (outputLock) {
            result = processingResult;
        }
        return result;
    }

    /*public void stopRunning() {
        synchronized (inputLock) {
            running = false;
        }
    }*/

    public void run() {
        while (true) {
            try {
                sleepSemaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Mat img = getInputImage();
            ImageProcessingResult result = ImageProcessor.processImage(img);
            setProcessingResult(result);
        }
    }
}

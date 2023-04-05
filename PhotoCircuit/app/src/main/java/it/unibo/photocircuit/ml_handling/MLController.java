package it.unibo.photocircuit.ml_handling;

import android.content.Context;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unibo.photocircuit.image_processing.ImageProcessor;
import it.unibo.photocircuit.ml.ClassificationModelFinalCProb;
import it.unibo.photocircuit.ml.ClassificationModelFinalProb;

public class MLController {
    public static Mat preprocessImage(Mat img) {
        Mat resizedBlack = ImageProcessor.resizeImageFillBlack(img, 120, 120, CvType.CV_8U);
        Mat converted = new Mat();
        resizedBlack.convertTo(converted, CvType.CV_32F);
        return converted;
    }

    public static float[] processImage(Mat img, Context context) {
        Mat preprocessed = preprocessImage(img);
        float[] imgBuf = new float[(int) preprocessed.total() * preprocessed.channels()];
        preprocessed.get(0, 0, imgBuf);

        float[] results = null;
        try {
            ClassificationModelFinalCProb model = ClassificationModelFinalCProb.newInstance(context);

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 120, 120, 1}, DataType.FLOAT32);
            inputFeature0.loadArray(imgBuf);

            ClassificationModelFinalCProb.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            results = outputFeature0.getFloatArray();

            model.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    public static List<CircuitElement> processImages(Mat image, List<Rect> rects, Context context) {
        List<CircuitElement> out = new ArrayList<>(rects.size());
        for (Rect rect : rects) {
            Mat cropped = new Mat(image, rect);
            float[] probabilities = processImage(cropped, context);
            int bestGuessIndex = -1;
            for (int i = 0; i < probabilities.length; i++) {
                if (bestGuessIndex == -1 || probabilities[i] > probabilities[bestGuessIndex]) {
                    bestGuessIndex = i;
                }
            }
            String className = getClassNameFromIndex(bestGuessIndex);

            out.add(new CircuitElement(probabilities, bestGuessIndex, className, rect));
        }
        return out;
    }

    public static String getClassNameFromIndex(int index) {
        String[] strings = new String[]{
                "ac_src_r0",
                "ac_src_r1",
                "battery_r0",
                "battery_r1",
                "battery_r2",
                "battery_r3",
                "cap_r0",
                "cap_r1",
                "curr_src_r0",
                "curr_src_r1",
                "curr_src_r2",
                "curr_src_r3",
                "dc_volt_src_1_r0",
                "dc_volt_src_1_r1",
                "dc_volt_src_1_r2",
                "dc_volt_src_1_r3",
                "dc_volt_src_2_r0",
                "dc_volt_src_2_r1",
                "dc_volt_src_2_r2",
                "dc_volt_src_2_r3",
                "dep_curr_src_r0",
                "dep_curr_src_r1",
                "dep_curr_src_r2",
                "dep_curr_src_r3",
                "dep_volt_r0",
                "dep_volt_r1",
                "dep_volt_r2",
                "dep_volt_r3",
                "diode_r0",
                "diode_r1",
                "diode_r2",
                "diode_r3",
                "gnd_1",
                "inductor_r0",
                "inductor_r1",
                "resistor_r0",
                "resistor_r1",
        };
        return strings[index];
    }
}

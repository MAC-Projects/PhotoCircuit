package it.unibo.photocircuit.ui;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

import it.unibo.photocircuit.circuit_model.Circuit;
import it.unibo.photocircuit.ml_handling.MLController;
import it.unibo.photocircuit.ml_handling.CircuitElement;
import it.unibo.photocircuit.R;
import it.unibo.photocircuit.image_processing.ImageProcessingResult;
import it.unibo.photocircuit.image_processing.ImageProcessor;

public class SecondFragment extends Fragment {
    private View root;
    private ImageView imageView;
    private Button showCodeButton;
    private Mat image;
    private List<Mat> images;
    private int currentImageIndex;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        root = inflater.inflate(R.layout.fragment_second, container, false);

        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new RuntimeException("arguments is null");
        }
        ImageProcessingResult result = (ImageProcessingResult) arguments.get("imageProcessingResult");

        imageView = root.findViewById(R.id.imageView2);
        showCodeButton = root.findViewById(R.id.showCodeButton);

        image = result.getThresholdedImage();

        List<Rect> rects = result.getCircuitElementRects();

        List<CircuitElement> circuitElements = MLController.processImages(image, rects, getContext());
        for (CircuitElement output : circuitElements) {
            System.out.println(output.getBestGuess() + " (" + output.getBestGuessIndex() + "), position: (" + output.getRect().x + ", " + output.getRect().y + ")");
        }

        Mat thinnedImage = result.getThinnedImage();

        Mat imageWithoutElements = thinnedImage.clone();
        ImageProcessor.deleteElements(imageWithoutElements, circuitElements);

        Mat lines = ImageProcessor.detectLines(imageWithoutElements);

        Mat linesPreviewImage = new Mat(imageWithoutElements.rows(), imageWithoutElements.cols(), CvType.CV_8UC3, new Scalar(0));
        ImageProcessor.drawLines(linesPreviewImage, lines);

        Circuit circuit = Circuit.from(lines, circuitElements);
        circuit.invertY(image.height());
        circuit.straighten();

        String circuitikzCode = circuit.toCircuiTikZ();
        System.out.println(circuitikzCode);

        showCodeButton.setOnClickListener((view) -> {
            Bundle bundle = new Bundle();
            bundle.putString("code", circuitikzCode);
            NavHostFragment.findNavController(this).navigate(R.id.action_secondFragment_to_thirdFragment, bundle);
        });

        showCodeButton.setEnabled(true);

        currentImageIndex = -1;
        images = new ArrayList<>(rects.size());
        images.add(image.clone());
        images.add(linesPreviewImage);
        for (Rect r : rects) {
            images.add(new Mat(image, r));
        }

        imageView.setOnClickListener((View v) -> {
            showNextImage();
        });

        showNextImage();

        return root;
    }

    private void showNextImage() {
        currentImageIndex = (currentImageIndex + 1) % images.size();
        Bitmap bmp = ImageProcessor.convertMatToBitmap(images.get(currentImageIndex));
        imageView.setImageBitmap(bmp);
    }
}
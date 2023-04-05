package it.unibo.photocircuit.ui;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

import it.unibo.photocircuit.circuit_model.Circuit;
import it.unibo.photocircuit.line_detection.LineDetector;
import it.unibo.photocircuit.ml_handling.MLController;
import it.unibo.photocircuit.ml_handling.CircuitElement;
import it.unibo.photocircuit.R;
import it.unibo.photocircuit.image_processing.ImageProcessingResult;
import it.unibo.photocircuit.image_processing.ImageProcessor;
import it.unibo.photocircuit.termbin_handling.TermbinHandler;

public class ThirdFragment extends Fragment {
    private View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        root = inflater.inflate(R.layout.fragment_third, container, false);

        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new RuntimeException("arguments is null");
        }
        String code = arguments.getString("code");

        EditText codeEditText = root.findViewById(R.id.codeEditText);
        TextView urlTextView = root.findViewById(R.id.urlTextView);

        codeEditText.setText(code);
        //codeTextView.setMovementMethod(new ScrollingMovementMethod());

        new Thread(() -> {
            try {
                String url = TermbinHandler.send(code);
                requireActivity().runOnUiThread(() -> {
                    urlTextView.setText(url.trim());
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return root;
    }
}
package it.unibo.photocircuit.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.concurrent.TimeUnit;

import it.unibo.photocircuit.PreviewProcessingThread;
import it.unibo.photocircuit.R;
import it.unibo.photocircuit.image_processing.ImageProcessingResult;
import it.unibo.photocircuit.image_processing.ImageProcessor;

public class FirstFragment extends Fragment implements ImageAnalysis.Analyzer {
    public FirstFragment() {
        super(R.layout.fragment_first);
    }

    private View root;
    private Button takePictureButton;
    private Button focusButton;
    private Button rotateCameraButton;
    private PreviewView previewView;
    private ImageView imageView;
    private ListenableFuture<ProcessCameraProvider> cameraProvider;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;
    private PreviewProcessingThread previewProcessingThread;
    private boolean cameraIsLandscape = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_first, container, false);

        super.onCreate(savedInstanceState);

        if (OpenCVLoader.initDebug())
            Log.d("SUCCESS", "OpenCV loaded");
        else
            Log.d("ERROR", "Unable to load OpenCV");

        if (root == null) {
            throw new RuntimeException("View is null");
        }

        takePictureButton = root.findViewById(R.id.takePictureButton);
        focusButton = root.findViewById(R.id.focusButton);
        rotateCameraButton = root.findViewById(R.id.rotateCameraButton);

        previewView = root.findViewById(R.id.previewView);
        imageView = root.findViewById(R.id.imageView);

        cameraProvider = ProcessCameraProvider.getInstance(getContext());
        cameraProvider.addListener(() -> {
            try {
                ProcessCameraProvider provider = cameraProvider.get();
                startCamera(provider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getContext()));

        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void startCamera(ProcessCameraProvider provider) {
        provider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        //Preview preview = new Preview.Builder().build();
        //preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();

        previewProcessingThread = new PreviewProcessingThread();
        previewProcessingThread.start();

        //imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build();

        imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(getContext()), this);

        Camera camera = provider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageCapture, imageAnalysis);

        takePictureButton.setOnClickListener((View v) -> {
            imageCapture.takePicture(ContextCompat.getMainExecutor(getContext()), new ImageCapture.OnImageCapturedCallback() {
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy image) {
                    super.onCaptureSuccess(image);

                    Mat mat = ImageProcessor.convertJPEGImageProxyToMat(image);

                    if (cameraIsLandscape) {
                        Core.rotate(mat, mat, Core.ROTATE_90_COUNTERCLOCKWISE);
                        mat = ImageProcessor.resizeImageByWidth(mat, 800);
                    } else {
                        mat = ImageProcessor.resizeImageByHeight(mat, 800);
                    }

                    ImageProcessingResult result = ImageProcessor.processImage(mat);

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("imageProcessingResult", result);

                    NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_firstFragment_to_secondFragment, bundle);
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    super.onError(exception);
                    Toast.makeText(getContext(), "Error taking picture", Toast.LENGTH_SHORT).show();
                }
            });
        });

        rotateCameraButton.setOnClickListener((view) -> {
            if (cameraIsLandscape) {
                cameraIsLandscape = false;
                rotateCameraButton.setText(R.string.switch_to_landscape);
            } else {
                cameraIsLandscape = true;
                rotateCameraButton.setText(R.string.switch_to_portrait);
            }
        });

        focusButton.setOnClickListener((View v) -> {
            MeteringPoint focusPoint = new SurfaceOrientedMeteringPointFactory(1, 1).createPoint(0.5f, 0.5f);
            FocusMeteringAction autoFocusAction = new FocusMeteringAction.Builder(focusPoint, FocusMeteringAction.FLAG_AF).setAutoCancelDuration(2, TimeUnit.SECONDS).build();
            camera.getCameraControl().startFocusAndMetering(autoFocusAction);
        });
    }

    static int counter = 0;
    @Override
    public void analyze(@NonNull ImageProxy image) {
        Mat mat = ImageProcessor.convertYUVImageProxyToMat(image);

        mat = ImageProcessor.resizeImageByHeight(mat, 800);

        previewProcessingThread.setInputImage(mat);
        ImageProcessingResult result = previewProcessingThread.getProcessingResult();
        if (result != null) {
            ImageProcessor.drawRectangles(mat, result.getCircuitElementRects(), new Scalar(1, 255, 255));
        }

        this.imageView.setImageBitmap(ImageProcessor.convertMatToBitmap(mat));

        image.close();
    }
}
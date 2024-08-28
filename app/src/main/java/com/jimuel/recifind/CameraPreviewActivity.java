package com.jimuel.recifind;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraPreviewActivity extends AppCompatActivity implements ObjectDetectorHelper.DetectorListener {

    private final String TAG = "ObjectDetection";

    private ObjectDetectorHelper objectDetectorHelper;
    private Bitmap bitmapBuffer;
    private Preview preview;
    private ImageAnalysis imageAnalysis;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;

    private ExecutorService cameraExecutor;
    private Overlay overlayView;
    private Button capturebutton;
    List<String> detectedLabels = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerapreview);
        capturebutton = findViewById(R.id.capturebutton);
        capturebutton.setOnClickListener(v -> {
            if (resultsList != null && !resultsList.isEmpty()) {
                // You can use the lastResultsList to identify ingredients on the screen
                // For example, iterate through the results and process them
                for (Detection result : resultsList) {
                    for (Category category : result.getCategories()) {
                        String label = category.getLabel();
                        detectedLabels.add(label);
                    }
                }
                handleBackPress(detectedLabels);
            } else {
                Toast.makeText(this, "No results available", Toast.LENGTH_SHORT).show();
            }
        });

        objectDetectorHelper = new ObjectDetectorHelper(
                this,
                this);

        cameraExecutor = Executors.newSingleThreadExecutor();

        findViewById(R.id.previewView).post(this::setUpCamera);
        overlayView = findViewById(R.id.OverlayView);

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void onBackPressed() {
        // Call the method to handle the back press and set the result
        handleBackPress(detectedLabels);
    }
    private void setUpCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    public void handleBackPress(List<String> detectedLabels) {
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra("detectedLabels", new ArrayList<>(detectedLabels));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }



    private void bindCameraUseCases() {
        CameraSelector cameraSelector =
                new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(findViewById(R.id.previewView).getDisplay().getRotation())
                .build();

        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(findViewById(R.id.previewView).getDisplay().getRotation())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, image -> {
            if (bitmapBuffer == null) {
                // The image rotation and RGB image buffer are initialized only once
                // the analyzer has started running
                bitmapBuffer = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
            }

            detectObjects(image);
        });

        cameraProvider.unbindAll();

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            preview.setSurfaceProvider(((PreviewView) findViewById(R.id.previewView)).getSurfaceProvider());
        } catch (Exception exc) {
            Log.e(TAG, "Use case binding failed", exc);
        }

        imageAnalysis.setAnalyzer(cameraExecutor, this::detectObjects);
    }

    private void detectObjects(ImageProxy image) {
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        int width = image.getWidth();
        int height = image.getHeight();
        int pixelStride = planeProxy.getPixelStride();
        int rowStride = planeProxy.getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        if (bitmapBuffer == null) {
            bitmapBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        int offset = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = 0;
                pixel |= (buffer.get(offset) & 0xff) << 16;     // R
                pixel |= (buffer.get(offset + 1) & 0xff) << 8;  // G
                pixel |= (buffer.get(offset + 2) & 0xff);       // B
                pixel |= (buffer.get(offset + 3) & 0xff) << 24; // A
                bitmapBuffer.setPixel(x, y, pixel);
                offset += pixelStride;
            }
            offset += rowPadding;
        }

        int imageRotation = image.getImageInfo().getRotationDegrees();
        objectDetectorHelper.detect(bitmapBuffer, imageRotation);

        image.close();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (imageAnalysis != null) {
            imageAnalysis.setTargetRotation(findViewById(R.id.previewView).getDisplay().getRotation());
        }
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }
    private LinkedList<Detection> resultsList;
    @Override
    public void onResults(List<Detection> results, Long inferenceTime, int imageHeight, int imageWidth) {
        resultsList = new LinkedList<>(results);
        runOnUiThread(() -> {
            overlayView.setResults(resultsList, imageHeight, imageWidth);
            overlayView.invalidate(); // Force a redraw
        });
    }

}

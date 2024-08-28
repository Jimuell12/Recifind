package com.jimuel.recifind;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.IOException;
import java.util.List;

public class ObjectDetectorHelper {

    private float threshold = 0.5f;
    private int numThreads = 2;
    private int maxResults = 3;
    private int currentDelegate = 0;
    private int currentModel = 0;
    private Context context;
    private DetectorListener objectDetectorListener;
    private ObjectDetector objectDetector;

    public ObjectDetectorHelper(
            Context context,
            DetectorListener objectDetectorListener) {
        this.threshold = threshold;
        this.numThreads = numThreads;
        this.maxResults = maxResults;
        this.currentDelegate = currentDelegate;
        this.currentModel = currentModel;
        this.context = context;
        this.objectDetectorListener = objectDetectorListener;
        setupObjectDetector();
    }

    public void setupObjectDetector() {
        ObjectDetector.ObjectDetectorOptions.Builder optionsBuilder =
                ObjectDetector.ObjectDetectorOptions.builder()
                        .setScoreThreshold(threshold)
                        .setMaxResults(maxResults);

        BaseOptions.Builder baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads);

        switch (currentDelegate) {
            case DELEGATE_CPU:
                break;
            case DELEGATE_GPU:
                if (new CompatibilityList().isDelegateSupportedOnThisDevice()) {
                    baseOptionsBuilder.useGpu();
                } else {
                    objectDetectorListener.onError("GPU is not supported on this device");
                }
                break;
            case DELEGATE_NNAPI:
                baseOptionsBuilder.useNnapi();
                break;
        }

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build());

        String modelName = "mobilenetv1.tflite";

        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(context, modelName, optionsBuilder.build());
            Toast.makeText(context, "Object detector initialized with the model: " + modelName, Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException | IOException e) {
            objectDetectorListener.onError("Object detector failed to initialize. See error logs for details");
            Log.e("Test", "TFLite failed to load model with error: " + e.getMessage());
        }
    }


    public void detect(Bitmap image, int imageRotation) {
        if (objectDetector == null) {
            setupObjectDetector();
        }

        long inferenceTime = SystemClock.uptimeMillis();

        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new Rot90Op(-imageRotation / 90))
                        .build();

        TensorImage tensorImage = TensorImage.fromBitmap(image);
        tensorImage = imageProcessor.process(tensorImage);

        List<Detection> results = objectDetector.detect(tensorImage);
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime;
        objectDetectorListener.onResults(results, inferenceTime, tensorImage.getHeight(), tensorImage.getWidth());
    }

    public interface DetectorListener {
        void onError(String error);
        void onResults(List<Detection> results, Long inferenceTime, int imageHeight, int imageWidth);
    }

    public static final int DELEGATE_CPU = 0;
    public static final int DELEGATE_GPU = 1;
    public static final int DELEGATE_NNAPI = 2;
}

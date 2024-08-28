package com.jimuel.recifind;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import java.util.LinkedList;
import java.util.List;
import org.tensorflow.lite.task.vision.detector.Detection;

public class Overlay extends View {

    private List<Detection> results = new LinkedList<>();
    private Paint boxPaint = new Paint();
    private Paint textBackgroundPaint = new Paint();
    private Paint textPaint = new Paint();

    private float scaleFactor = 1f;

    private Rect bounds = new Rect();

    public Overlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    public void clear() {
        textPaint.reset();
        textBackgroundPaint.reset();
        boxPaint.reset();
        invalidate();
        initPaints();
    }

    private void initPaints() {
        textBackgroundPaint.setColor(Color.BLACK);
        textBackgroundPaint.setStyle(Paint.Style.FILL);
        textBackgroundPaint.setTextSize(50f);

        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(50f);

        boxPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray));
        boxPaint.setStrokeWidth(8F);
        boxPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Detection result : results) {
            RectF boundingBox = result.getBoundingBox();

            float top = boundingBox.top * scaleFactor;
            float bottom = boundingBox.bottom * scaleFactor;
            float left = boundingBox.left * scaleFactor;
            float right = boundingBox.right * scaleFactor;

            // Draw bounding box around detected objects
            RectF drawableRect = new RectF(left, top, right, bottom);
            canvas.drawRect(drawableRect, boxPaint);

            // Create text to display alongside detected objects
            String drawableText =
                    result.getCategories().get(0).getLabel() + " " +
                            String.format("%.2f", result.getCategories().get(0).getScore());

            // Draw rect behind display text
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length(), bounds);
            float textWidth = bounds.width();
            float textHeight = bounds.height();
            canvas.drawRect(
                    left,
                    top,
                    left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                    top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                    textBackgroundPaint
            );

            // Draw text for detected object
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint);
        }
    }

    public void setResults(List<Detection> detectionResults, int imageHeight, int imageWidth) {
        results = detectionResults;

        // PreviewView is in FILL_START mode. So we need to scale up the bounding box to match with
        // the size that the captured images will be displayed.
        scaleFactor = Math.max(getWidth() * 1f / imageWidth, getHeight() * 1f / imageHeight);
    }

    private static final int BOUNDING_RECT_TEXT_PADDING = 5;
}

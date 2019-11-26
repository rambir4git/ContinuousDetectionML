package com.example.continuousdetectionml;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.continuousdetectionml.Helper.GraphicOverlay;
import com.example.continuousdetectionml.Helper.RectOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private String TAG = "ContinuousDetection";
    private GraphicOverlay graphicOverlay;
    private CameraView cameraView;
    private Button button;
    private FirebaseVisionObjectDetectorOptions options;
    private FirebaseVisionObjectDetector detector;
    private boolean start_click = true;
    private boolean latch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        graphicOverlay = findViewById(R.id.graphicOverlay);
        cameraView = findViewById(R.id.cameraView);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraView.isOpened()) {
                    stopCamera();
                } else {
                    startCamera();
                }
            }
        });
    }

    private void stopCamera() {
        cameraView.clearFrameProcessors();
        cameraView.close();
        cameraView.setVisibility(View.GONE);
        button.setText(R.string.start);
        graphicOverlay.clear();
        graphicOverlay.setVisibility(View.INVISIBLE);
    }

    private void startCamera() {
        button.setText(R.string.stop);
        graphicOverlay.setVisibility(View.VISIBLE);
        cameraView.setVisibility(View.VISIBLE);
        cameraView.open();
        cameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                Log.d(TAG, "frame: " + frame.getSize() + " rotation: " + frame.getRotation());
                if (!latch) {
                    int rotation;
                    switch (frame.getRotation()) {
                        case 0:
                            rotation = FirebaseVisionImageMetadata.ROTATION_0;
                            break;
                        case 90:
                            rotation = FirebaseVisionImageMetadata.ROTATION_90;
                            break;
                        case 180:
                            rotation = FirebaseVisionImageMetadata.ROTATION_180;
                            break;
                        case 270:
                            rotation = FirebaseVisionImageMetadata.ROTATION_270;
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + frame.getRotation());
                    }
                    FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata
                            .Builder()
                            .setRotation(rotation)
                            .setWidth(frame.getSize().getWidth())
                            .setHeight(frame.getSize().getHeight())
                            .setFormat(frame.getFormat())
                            .build();
                    FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(frame.getData(), metadata);
                    continuousDetection(image);
                    Log.d(TAG, "ImageSent for recoginition: ");
                } else {
                    frame.release();
                    Log.d(TAG, "Frame dropped: ");
                }
            }
        });
        options = new FirebaseVisionObjectDetectorOptions.Builder()
                .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                .enableMultipleObjects()
                .build();
        detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options);
    }

    private void continuousDetection(FirebaseVisionImage image) {
        latch = true;
        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionObject>>() {
            @Override
            public void onSuccess(List<FirebaseVisionObject> firebaseVisionObjects) {
                Log.d(TAG, "onSuccess: success objects:" + firebaseVisionObjects.size());
                graphicOverlay.clear();
                for (FirebaseVisionObject object : firebaseVisionObjects) {
                    RectOverlay rectOverlay = new RectOverlay(graphicOverlay, object.getBoundingBox(), object.getTrackingId().toString());
                    Log.d(TAG, "onSuccess: rect " + " left: " + object.getBoundingBox().left + " right: " + object.getBoundingBox().right
                            + " top: " + object.getBoundingBox().top + " bottom: " + object.getBoundingBox().top);
                    graphicOverlay.add(rectOverlay);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: failed to detect any object in the frame");
            }
        });
        latch = false;
    }
}

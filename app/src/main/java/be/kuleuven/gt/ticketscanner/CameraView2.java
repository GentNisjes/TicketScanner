package be.kuleuven.gt.ticketscanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class CameraView2 extends AppCompatActivity implements View.OnClickListener {

    private PreviewView previewView;
    private ImageButton captureButton;
    private CameraHelper cameraHelper;
    private String activityIdentifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_view2);

        activityIdentifier = getIntent().getStringExtra("activityIdentifier");

        captureButton = findViewById(R.id.captureButton);
        previewView = findViewById(R.id.previewView);

        captureButton.setOnClickListener(this);

        // Initialize the ProcessCameraProvider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Pass context and camera provider to CameraHelper
                cameraHelper = new CameraHelper(this, cameraProvider);

                // Start the camera with the provided preview view
                cameraHelper.startCamera(previewView);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.captureButton) {
            // Provide file path and handle success or failure in callback
            String filePath = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg").getAbsolutePath();
            cameraHelper.capturePhoto(filePath, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Toast.makeText(CameraView2.this, "Photo saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CameraView2.this, PhotoReviewActivity.class);
                    intent.putExtra("photoPath", filePath);
                    intent.putExtra("activityIdentifier", activityIdentifier);
                    Log.d("activityID", "onImageSaved: " + activityIdentifier);
                    startActivity(intent);
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Toast.makeText(CameraView2.this, "Error capturing photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

package be.kuleuven.gt.ticketscanner;

import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraHelper {
    private final Context context;
    private final Executor mainExecutor;
    private final ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;

    // Constructor to initialize CameraHelper with context and ProcessCameraProvider
    public CameraHelper(@NonNull Context context, @NonNull ProcessCameraProvider cameraProvider) {
        this.context = context;
        this.cameraProvider = cameraProvider;
        //fix for main executor being the cameraview object in the cameraView class (=this)
        this.mainExecutor = ContextCompat.getMainExecutor(context);
    }

    // Method to set up the camera
    public void startCamera(PreviewView previewView) {
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .build();

        // Bind the camera to the lifecycle of the context's lifecycle owner (Activity)
        cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview, imageCapture);
    }

    // Method to capture a photo
    public void capturePhoto(String filePath, ImageCapture.OnImageSavedCallback callback) {
        long timeStamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        // Storage location using the provided filePath
        File tempFile = new File(filePath);

        // Create OutputFileOptions for saving the image
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(tempFile).build();

        // Capture the photo with the provided OutputFileOptions and callback
        imageCapture.takePicture(
                outputFileOptions,
                mainExecutor,
                callback
        );
    }
}


package be.kuleuven.gt.ticketscanner;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraView extends AppCompatActivity implements View.OnClickListener {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    public PreviewView previewView;

    private ImageButton flashButton;
    private ImageButton captureButton;
    private TextView cameraInfo;

    private ImageCapture imageCapture;
    @SuppressLint("RestrictedApi")
    private VideoCapture videoCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_view);

        /*flashButton = findViewById(R.id.flashButton);*/
        captureButton = findViewById(R.id.captureButton);
        /*cameraInfo = findViewById(R.id.cameraInfo);*/
        previewView = findViewById(R.id.previewView);


        captureButton.setOnClickListener(this);
        /*flashButton.setOnClickListener(this);*/

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, getExecutor());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {

        //previous lifecycle bonded settings need to be reset

        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //flash mode auto will decide based on light intensity if the flash light is needed or not
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .build();

        //set up for possible video capture implementation
        // TODO: 24/09/2024 implementation to record video (not that important)
        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        //bindToLifecycle is used to sort of remember the settings provided in the previous lines
        //as long as the activity is alive

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture);
    }
    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View view) {
        /*switch (view.getId()) {
            case R.id.recordButton: {
                if (cameraInfo.getText() == "RECORD") {
                    cameraInfo.setText("STOP");
                    recordVideo();
                } else {
                    cameraInfo.setText("RECORD");
                    videoCapture.stopRecording();
                }
                break;
            }
            case captureButton: {
                capturePhoto();
                break;
            }

        }*/
        if (view.getId() == R.id.captureButton) {
            capturePhoto();
        }

    }

    public void recordVideo(){

    }

    /*
        CAPTURE PHOTO

        Generate a unique filename using a timestamp.
        Create metadata for the image file.
        Configure where and how to save the image using OutputFileOptions.
        Capture the photo and handle success or failure with a callback.
    */

    private void capturePhoto() {

        long timeStamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        // storage location inaccessible by the gallery...
        File tempFile = new File(getExternalCacheDir(), timeStamp + ".jpg");


        imageCapture.takePicture(
            //1. WE CAN SAVE THE PICTURE IN A STORAGE SECTION INACCESSIBLE FOR THE GALLERY
            //   AND THEN MOVE THE PICTURE IF ITS APPROVED

                new ImageCapture.OutputFileOptions.Builder(tempFile).build(),

            //2. WE CAN SAVE THE PICTURE DIRECTLY IN THE GALLERY
                /*new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),*/

                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(CameraView.this,"Review photo...", Toast.LENGTH_SHORT).show();
                        // After image is saved, go to review screen
                        String tempUri = outputFileResults.getSavedUri().toString();

                        String tempFilePath = tempFile.getAbsolutePath();
                        Intent intent = new Intent(CameraView.this, PhotoReviewActivity.class);
                        intent.putExtra("photoPath", tempFilePath);
                        startActivity(intent);
                    }


                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CameraView.this,"Error: "+exception.getMessage(),Toast.LENGTH_SHORT).show();


                    }
                });

    }
}
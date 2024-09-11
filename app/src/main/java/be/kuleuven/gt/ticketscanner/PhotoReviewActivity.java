package be.kuleuven.gt.ticketscanner;

import android.content.ContentValues;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PhotoReviewActivity extends AppCompatActivity {

    private ImageView photoPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_review);

        photoPreview = findViewById(R.id.photoPreview);

        // Get photo URI from intent
        String photoUri = getIntent().getStringExtra("photoPath");
        photoPreview.setImageURI(Uri.parse(photoUri));

        findViewById(R.id.confirmButton).setOnClickListener(view -> savePhoto(photoUri));

        findViewById(R.id.retakeButton).setOnClickListener(view -> retakePhoto());
    }


    private void savePhoto(String tempFilePath) {

        Log.d("TempPhotoPath", "Path: " + tempFilePath);
        File tempFile = new File(tempFilePath);
            if (!tempFile.exists()) {
            Log.e("FileNotFound", "The temp file doesn't exist at " + tempFilePath);
            return; // Or handle the error appropriately
        }

        if (tempFile.exists()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "approved_photo_" + System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

            Uri externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri savedUri = getContentResolver().insert(externalUri, values);

            try (OutputStream outStream = getContentResolver().openOutputStream(savedUri)) {
                // Copy the file content to gallery
                FileInputStream inStream = new FileInputStream(tempFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }
                inStream.close();

                // Show confirmation to the user
                Toast.makeText(this, "Photo saved to gallery!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Delete old temporary file
        new File(tempFilePath).delete();
    }



    private void retakePhoto() {
        // Simply go back to the camera activity
        Intent intent = new Intent(this, CameraView.class);
        startActivity(intent);
        finish(); // Finish the review activity
    }
}

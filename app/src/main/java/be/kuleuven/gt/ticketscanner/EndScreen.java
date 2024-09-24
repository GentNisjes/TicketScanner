package be.kuleuven.gt.ticketscanner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EndScreen extends AppCompatActivity {

    private TextView displayField;
    private Button copyBtn;
    private Button saveImgBtn;
    private String photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_end_screen);

        displayField = findViewById(R.id.displayField);
        copyBtn = findViewById(R.id.copyBtn);
        copyBtn.setOnClickListener(view -> copyTextToClipboard());
        saveImgBtn = findViewById(R.id.saveImgBtn);
        saveImgBtn.setOnClickListener(view -> saveImage());

        String capturedText = getIntent().getStringExtra("EXTRACTED_TEXT");
        photoURI = getIntent().getStringExtra("IMAGE_URI");
        displayField.setText(capturedText);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void saveImage() {
        // Get the temporary file path passed from the previous activity
        File tempFile = new File(photoURI);

        // Check if the file exists
        if (!tempFile.exists()) {
            Log.e("FileNotFound", "The temp file doesn't exist at " + photoURI);
            Toast.makeText(this, "Temporary file not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare ContentValues for the MediaStore
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "approved_photo_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        // Insert the new image into the MediaStore
        Uri externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri savedUri = getContentResolver().insert(externalUri, values);

        if (savedUri != null) {
            try (OutputStream outStream = getContentResolver().openOutputStream(savedUri);
                 FileInputStream inStream = new FileInputStream(tempFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }

                // Show confirmation to the user
                Toast.makeText(this, "Photo saved to gallery!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to save photo to gallery!", Toast.LENGTH_SHORT).show();
        }
    }


    // TODO: 24/09/2024 fix the copy to clipboard implementation 
    private void copyTextToClipboard() {
        // Get the text from the TextView
        String textToCopy = displayField.getText().toString();

        // Check if the ClipboardManager is available
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            // Create a ClipData object with the label and text to copy
            ClipData clip = ClipData.newPlainText("Copied Text", textToCopy);

            // Set the clip data to the clipboard
            clipboard.setPrimaryClip(clip);

            // Optional: Notify the user that the text has been copied
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }


}
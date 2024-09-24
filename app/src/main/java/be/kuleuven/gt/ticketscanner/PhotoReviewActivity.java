package be.kuleuven.gt.ticketscanner;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PhotoReviewActivity extends AppCompatActivity {

    private ImageView photoPreview;
    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_review);

        photoPreview = findViewById(R.id.photoPreview);

        // Get photo URI from intent
        String photoUri = getIntent().getStringExtra("photoPath");
        photoPreview.setImageURI(Uri.parse(photoUri));

        findViewById(R.id.confirmButton).setOnClickListener(view -> {
            /*savePhoto(photoUri);*/
            processImage(Uri.parse(photoUri));
        });

        findViewById(R.id.retakeButton).setOnClickListener(view -> retakePhoto());
    }

    // 2. Process the image for text recognition
    private void processImage(Uri imageUri) {
        try {
            // Convert file path to Uri if necessary
            if (imageUri.getScheme() == null) {
                imageUri = Uri.fromFile(new File(imageUri.getPath()));
            }

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // 3. Convert Bitmap to InputImage for ML Kit
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            // 4. Initialize the TextRecognizer from ML Kit
            textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            // 5. Process the image
            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> handleTextRecognitionResult(visionText))
                    .addOnFailureListener(e -> {
                        // Handle the error
                        Toast.makeText(this, "Text recognition failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("IMG_ERR", "processImage: "+ e.getMessage());
        }
    }

    // 6. Handle the result of the text recognition
    private void handleTextRecognitionResult(Text visionText) {
        StringBuilder extractedText = new StringBuilder();

        // Extract text from recognized blocks
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            extractedText.append(block.getText()).append("\n");
        }

        if (extractedText.length() > 0) {
            // For simplicity, we display it in a Toast (you could display in a TextView, save it, etc.)
            /*Toast.makeText(this, "Recognized Text:\n" + extractedText.toString(), Toast.LENGTH_LONG).show();
            System.out.println("Recognized Text:\n" + extractedText.toString());*/

            //Display the resulting text in a new frame
            Intent intent = new Intent(this, EndScreen.class);
            intent.putExtra("EXTRACTED_TEXT", extractedText.toString());
            intent.putExtra("IMAGE_URI", getIntent().getStringExtra("photoPath"));
            startActivity(intent);

        } else {
            Toast.makeText(this, "No text found in the image", Toast.LENGTH_SHORT).show();
        }
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
        /*new File(tempFilePath).delete();*/
    }



    private void retakePhoto() {
        // Simply go back to the camera activity
        Intent intent = new Intent(this, CameraView.class);
        startActivity(intent);
        finish(); // Finish the review activity
    }
}

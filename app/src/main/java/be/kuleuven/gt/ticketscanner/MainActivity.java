package be.kuleuven.gt.ticketscanner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    //test change
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private ImageButton CameraButton;
    private ImageButton ticketButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        CameraButton = findViewById(R.id.TakeImageBtn);
        ticketButton = findViewById(R.id.ticketButton);

        ticketButton.setOnClickListener(view -> {
            onTicketClicked();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void onTicketClicked() {
        Intent intent = new Intent(this, CameraView2.class);
        intent.putExtra("activityIdentifier", "ticketOCR");
        startActivity(intent);
    }

    //check permissions for camera access
    //and redirect if access granted
    //otherwise ask for access
    public void onCameraClicked (View Caller){
        if (hasCameraPermission()){
            Intent intent = new Intent(this, CameraView2.class);
            intent.putExtra("activityIdentifier", "cameraOCR");
            startActivity(intent);
        } else {
            requestPermission();
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }
}
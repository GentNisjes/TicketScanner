package be.kuleuven.gt.ticketscanner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EndScreen extends AppCompatActivity {

    private TextView displayField;
    private Button copyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_end_screen);

        displayField = findViewById(R.id.displayField);
        copyBtn = findViewById(R.id.copyBtn);
        copyBtn.setOnClickListener(view -> copyTextToClipboard());

        String capturedText = getIntent().getStringExtra("EXTRACTED_TEXT");
        displayField.setText(capturedText);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void copyTextToClipboard() {
    }
}
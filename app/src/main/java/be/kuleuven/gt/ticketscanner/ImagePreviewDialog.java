package be.kuleuven.gt.ticketscanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class ImagePreviewDialog extends DialogFragment {

    private String imagePath;

    public static ImagePreviewDialog newInstance(String imagePath) {
        ImagePreviewDialog fragment = new ImagePreviewDialog();
        Bundle args = new Bundle();
        args.putString("imagePath", imagePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imagePath = getArguments().getString("imagePath");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate the custom layout for the preview dialog
        View view = inflater.inflate(R.layout.dialog_image_preview, null);

        // Get the ImageView and load the captured image
        ImageView previewImageView = view.findViewById(R.id.previewImageView);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        previewImageView.setImageBitmap(bitmap);

        // Set up the dialog view
        builder.setView(view)
                .setPositiveButton("Close", (dialog, id) -> {
                    dialog.dismiss();
                });

        return builder.create();
    }
}


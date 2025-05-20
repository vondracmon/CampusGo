
package com.example.campusgo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.campusgo.databinding.UploadImageActivityBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class uploadImage extends AppCompatActivity {

    UploadImageActivityBinding binding;
    Uri imageUri;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        FirebaseApp.initializeApp(this);

        binding = UploadImageActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.uploadImage.setVisibility(View.GONE);

        binding.selectImageBtn.setOnClickListener(v -> selectImage());

        binding.uploadImage.setOnClickListener(v -> uploadImage());
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            binding.imageView.setImageURI(imageUri);
            binding.uploadImage.setVisibility(View.VISIBLE);
        }
    }

    private void uploadImage() {
        if (imageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Encoding and Uploading...");
        progressDialog.show();

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // Resize image to avoid Realtime Database size limits
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos); // compress to reduce size
            byte[] imageBytes = baos.toByteArray();

            // Convert to Base64
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Get timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

            // Upload to Realtime Database
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("images");
            dbRef.child(timeStamp).setValue(base64Image)
                    .addOnSuccessListener(unused -> {
                        progressDialog.dismiss();
                        binding.imageView.setImageURI(null);
                        binding.uploadImage.setVisibility(View.GONE);
                        Toast.makeText(uploadImage.this, "Upload successful!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(uploadImage.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("Firebase", "Upload failed", e);
                    });

        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Firebase", "Encoding error", e);
        }
    }
}
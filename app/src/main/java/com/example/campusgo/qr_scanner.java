package com.example.campusgo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.Executor;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class qr_scanner extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private PreviewView previewView;
    private TextView qrTextView;
    private Executor executor;


    private boolean isScanHandled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_scanner);

        previewView = findViewById(R.id.previewView);
        qrTextView = findViewById(R.id.qrTextView);
        executor = ContextCompat.getMainExecutor(this);

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview use case
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Image analysis use case for QR detection
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(executor, imageProxy -> scanQRCode(imageProxy));

                // Back camera selector
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Bind use cases to lifecycle
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Log.e("qr_scanner", "Camera error: ", e);
            }
        }, executor);
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void scanQRCode(ImageProxy imageProxy) {
        if (isScanHandled) {
            imageProxy.close();
            return;
        }

        if (imageProxy.getImage() != null) {
            InputImage inputImage = InputImage.fromMediaImage(
                    imageProxy.getImage(),
                    imageProxy.getImageInfo().getRotationDegrees());

            BarcodeScanner scanner = BarcodeScanning.getClient();

            scanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            runOnUiThread(() -> qrTextView.setText("QR Code: " + rawValue));

                            try {
                                JSONObject qrData = new JSONObject(rawValue);
                                String username = qrData.optString("username");
                                String studentNumber = qrData.optString("student_number");

                                // Lock scanning to prevent multiple triggers
                                isScanHandled = true;
                                saveToFirebase(username, studentNumber);
                                // Delay 3 seconds, then allow scanning again
                                new android.os.Handler().postDelayed(() -> {
                                    isScanHandled = false;
                                }, 3000);

                                break;
                            } catch (JSONException e) {
                                runOnUiThread(() -> Toast.makeText(this, "Invalid QR format", Toast.LENGTH_SHORT).show());
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("qr_scanner", "QR scan failed", e))
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }


    private void saveToFirebase(String username, String studNumber) {
        // Format: yyyy-MM-dd
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("attendance")
                .child(currentDate);

        // First, check if the student number is already recorded today
        databaseRef.orderByChild("stud_number").equalTo(studNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Already scanned
                            Toast.makeText(qr_scanner.this, "Already scanned!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Not yet scanned — proceed to save
                            String entryId = databaseRef.push().getKey();

                            if (entryId != null) {
                                HashMap<String, Object> data = new HashMap<>();
                                data.put("username", username);
                                data.put("stud_number", studNumber);
                                String formattedTimestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(new Date());
                                data.put("timestamp", formattedTimestamp);

                                databaseRef.child(entryId).setValue(data)
                                        .addOnSuccessListener(unused ->
                                                Toast.makeText(qr_scanner.this, "Data saved to Firebase", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e ->
                                                Toast.makeText(qr_scanner.this, "Failed to save data", Toast.LENGTH_SHORT).show());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(qr_scanner.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showQRDialog(String username, String studNumber) {
        @SuppressLint("InflateParams")
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_qr_data, null);

        TextView tvUsername = dialogView.findViewById(R.id.tvUsername);
        TextView tvStudNumber = dialogView.findViewById(R.id.tvStudNumber);

        tvUsername.setText("Username: " + username);
        tvStudNumber.setText("Student Number: " + studNumber);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Scanned Data")
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show();
    }



}

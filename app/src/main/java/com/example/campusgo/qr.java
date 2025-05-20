package com.example.campusgo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class qr extends AppCompatActivity {

    ImageView qrImage, profileImage;
    TextView profileName, profileId, profileEmail;
    Button downloadBtn, shareBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr);

        qrImage = findViewById(R.id.qr_image);
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileId = findViewById(R.id.profile_id);
        profileEmail = findViewById(R.id.profile_email);
        downloadBtn = findViewById(R.id.download_button);
        shareBtn = findViewById(R.id.share_button);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        displayProfileInfo();

        downloadBtn.setOnClickListener(v -> {
            qrImage.setDrawingCacheEnabled(true);
            Bitmap bitmap = qrImage.getDrawingCache();
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "QR_Code", null);
            Toast.makeText(this, "QR Code saved to gallery", Toast.LENGTH_SHORT).show();
        });

        shareBtn.setOnClickListener(v -> {
            qrImage.setDrawingCacheEnabled(true);
            Bitmap bitmap = qrImage.getDrawingCache();
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "QR_Code", null);
            Uri uri = Uri.parse(path);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, "Share QR Code"));
        });
    }

    private void displayProfileInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String studentNumber = snapshot.child("studentNumber").getValue(String.class);
                        String base64Image = snapshot.child("image").getValue(String.class);

                        profileName.setText(name != null ? name : "Unknown User");
                        profileId.setText(studentNumber != null ? studentNumber : "No Student Number");
                        profileEmail.setText(email != null ? email : "No Email");

                        if (base64Image != null && !base64Image.isEmpty()) {
                            try {
                                byte[] decodedBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                                Bitmap decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                Glide.with(qr.this)
                                        .load(decodedBitmap)
                                        .circleCrop()
                                        .into(profileImage);
                            } catch (IllegalArgumentException e) {
                                profileImage.setImageResource(R.drawable.human_icon);
                            }
                        } else {
                            profileImage.setImageResource(R.drawable.human_icon);
                        }

                        String qrData = "Name: " + (name != null ? name : "N/A") +
                                "\nStudent Number: " + (studentNumber != null ? studentNumber : "N/A") +
                                "\nEmail: " + (email != null ? email : "N/A");
                        generateQRCode(qrData);

                    } else {
                        Toast.makeText(qr.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(qr.this, "Failed to load profile info.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void generateQRCode(String data) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 600, 600);
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}

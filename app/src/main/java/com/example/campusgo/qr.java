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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class qr extends AppCompatActivity {

    ImageView qrImage;
    TextView profileName, profileId;
    Button downloadBtn, shareBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr);

        qrImage = findViewById(R.id.qr_image);
        profileName = findViewById(R.id.profile_name);
        profileId = findViewById(R.id.profile_id);
        downloadBtn = findViewById(R.id.download_button);
        shareBtn = findViewById(R.id.share_button);

        // Handle back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        generateQRCode();
        displayProfileInfo();

        // Download QR
        downloadBtn.setOnClickListener(v -> {
            qrImage.setDrawingCacheEnabled(true);
            Bitmap bitmap = qrImage.getDrawingCache();
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "QR_Code", null);
            Toast.makeText(this, "QR Code saved to gallery", Toast.LENGTH_SHORT).show();
        });

        // Share QR
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

    private void generateQRCode() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String data = "Email: " + user.getEmail() + "\nUID: " + user.getUid();
            try {
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 600, 600);
                qrImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayProfileInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String userName = email != null ? email.split("@")[0] : "Guest";
            String uid = user.getUid();

            profileName.setText(userName);
            profileId.setText(uid);
        }
    }
}

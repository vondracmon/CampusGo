package com.example.campusgo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class register_activity extends AppCompatActivity {

    Button registerBtn, selectImageBtn;
    EditText username, studNum, emailAdd, pass;
    ImageView imagePreview;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    private Uri imageUri;
    private String base64Image = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        registerBtn = findViewById(R.id.registerBtn);
        selectImageBtn = findViewById(R.id.uploadImageBtn);
        imagePreview = findViewById(R.id.imagePreview);

        username = findViewById(R.id.username);
        studNum = findViewById(R.id.studNum);
        emailAdd = findViewById(R.id.emailAdd);
        pass = findViewById(R.id.pass);

        selectImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userN = username.getText().toString().trim();
                String idNum = studNum.getText().toString().trim();
                String emailAddress = emailAdd.getText().toString().trim();
                String password = pass.getText().toString().trim();

                if (userN.isEmpty() || idNum.isEmpty() || emailAddress.isEmpty() || password.isEmpty() || base64Image.isEmpty()) {
                    Toast.makeText(register_activity.this, "Please fill all fields and upload a photo", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(emailAddress, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                String userId = auth.getCurrentUser().getUid();

                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("name", userN);
                                userMap.put("email", emailAddress);
                                userMap.put("studentNumber", idNum);
                                userMap.put("image", base64Image);

                                databaseReference.child(userId).setValue(userMap)
                                        .addOnSuccessListener(unused -> {
                                            showUserDetailsDialog(userN, idNum, emailAddress, password);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(register_activity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(register_activity.this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                byte[] imageBytes = baos.toByteArray();
                base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Image processing error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showUserDetailsDialog(String username, String studNum, String email, String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registration Successful");
        builder.setMessage("Username: " + username + "\nStudent Number: " + studNum + "\nEmail: " + email + "\nPassword: " + password);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(register_activity.this, login_activity.class));
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

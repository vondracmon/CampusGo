package com.example.campusgo;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    private EditText currentPasswordInput, newPasswordInput, confirmNewPasswordInput;
    private Button changePasswordBtn;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        currentPasswordInput = findViewById(R.id.currentPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmNewPasswordInput = findViewById(R.id.confirmNewPasswordInput);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        changePasswordBtn.setOnClickListener(v -> {
            String currentPass = currentPasswordInput.getText().toString().trim();
            String newPass = newPasswordInput.getText().toString().trim();
            String confirmPass = confirmNewPasswordInput.getText().toString().trim();

            if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "New password and confirmation do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Re-authenticate user with current password first before changing password
            // You need user's email to re-authenticate
            String email = user.getEmail();

            auth.signInWithEmailAndPassword(email, currentPass)
                    .addOnSuccessListener(authResult -> {
                        // Now update password
                        user.updatePassword(newPass)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                    finish(); // close activity
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to change password: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }
}

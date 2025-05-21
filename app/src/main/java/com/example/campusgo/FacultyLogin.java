package com.example.campusgo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class FacultyLogin extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginBtn;
    private TextView forgotPasswordText;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_login); // Your XML layout file

        // Initialize views
        emailInput = findViewById(R.id.facultyEmail);
        passwordInput = findViewById(R.id.facultyPassword);
        loginBtn = findViewById(R.id.facultyLoginBtn);
        forgotPasswordText = findViewById(R.id.facultyForgotPassword); // Make sure this ID matches in XML

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();

        // Handle login
        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty()) {
                emailInput.setError("Email is required");
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Password is required");
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(user.getUid())
                                    .child("role");

                            userRef.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String role = task.getResult().getValue(String.class);
                                    if ("faculty".equals(role)) {
                                        Toast.makeText(FacultyLogin.this, "Welcome, faculty!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(FacultyLogin.this, FacultyDashboard.class));
                                        finish();
                                    } else {
                                        auth.signOut();
                                        Toast.makeText(FacultyLogin.this, "Access denied: Not a faculty account.", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(FacultyLogin.this, "Failed to verify role.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FacultyLogin.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Handle "Forgot Password?"
        forgotPasswordText.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                emailInput.setError("Enter your email to reset password");
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(FacultyLogin.this, "Reset link sent to your email", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(FacultyLogin.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}

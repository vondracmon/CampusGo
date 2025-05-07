package com.example.campusgo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class login_activity extends AppCompatActivity {
    Button loginBtn, googleLoginBtn;
    EditText IdOrEmail, pass;
    TextView forgotPassText;
    GoogleSignInClient gsc;
    FirebaseDatabase database;
    FirebaseAuth auth;

    private final String CHANNEL_ID = "My_notification";// notification channel ID


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        loginBtn = findViewById(R.id.loginBtn);
        googleLoginBtn = findViewById(R.id.googleLoginBtn);
        IdOrEmail = findViewById(R.id.IdOrEmail);
        pass = findViewById(R.id.pass);
        forgotPassText = findViewById(R.id.forgotPassword);

        // Set up notification channel

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "My Notification Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        loginBtn.setOnClickListener(v -> {
            String email = IdOrEmail.getText().toString().trim();
            String password = pass.getText().toString().trim();

            if (email.isEmpty()) {
                IdOrEmail.setError("Email cannot be empty");
            } else if (password.isEmpty()) {
                pass.setError("Password cannot be empty");
            } else if (email.equals("Admin") || password.equals("Admin")) {
                Toast.makeText(login_activity.this, "Logged in Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(login_activity.this, AdminView.class));
                finish();
            } else {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            Toast.makeText(login_activity.this, "Logged in Successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(login_activity.this, home_activity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(login_activity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        forgotPassText.setOnClickListener(v -> {
            String email = IdOrEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
            } else {
                auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                            showResetNotification(email);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);

        googleLoginBtn.setOnClickListener(view -> gSignIn());
    }

    private void showResetNotification(String email) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Password Reset")
                .setContentText("Reset link sent to: " + email)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(1, builder.build());
    }

    int RC_SIGN_IN = 40;

    private void gSignIn() {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                auth(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(login_activity.this, "Google Sign-In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void auth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();

                        Users users = new Users();
                        users.setUsername(user.getDisplayName() != null ? user.getDisplayName() : "Default Username");
                        users.setStudNum("000000");
                        users.setEmail(user.getEmail());

                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                        userRef.child(user.getUid()).setValue(users);

                        startActivity(new Intent(login_activity.this, home_activity.class));
                        finish();
                    } else {
                        Toast.makeText(login_activity.this, "Authentication Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

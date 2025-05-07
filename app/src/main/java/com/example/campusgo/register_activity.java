package com.example.campusgo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class register_activity extends AppCompatActivity {
    Button registerBtn;
    EditText username, studNum, emailAdd, pass;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        registerBtn = findViewById(R.id.registerBtn);
        username = findViewById(R.id.username);
        studNum = findViewById(R.id.studNum);
        emailAdd = findViewById(R.id.emailAdd);
        pass = findViewById(R.id.pass);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userN = username.getText().toString().trim();
                String idNum = studNum.getText().toString().trim();
                String emailAddress = emailAdd.getText().toString().trim();
                String password = pass.getText().toString().trim();

                if (userN.isEmpty() || idNum.isEmpty() || emailAddress.isEmpty() || password.isEmpty()) {
                    Toast.makeText(register_activity.this, "Do not leave anything blank", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(emailAddress, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                String userId = auth.getCurrentUser().getUid();
                                Users user = new Users(userN, idNum, emailAddress);
                                databaseReference.child(userId).setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                showUserDetailsDialog(userN, idNum, emailAddress, password);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(register_activity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(register_activity.this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
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

package com.example.campusgo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class FacultyDashboard extends AppCompatActivity {

    private TextView facultyWelcomeText;
    private ImageView facultyProfileImage;
    private Button btnSchedule, btnEditCalendar, btnEditRoom, btnViewAttendance, logoutButton;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_dashboard);

        facultyWelcomeText = findViewById(R.id.facultyWelcomeText);
        facultyProfileImage = findViewById(R.id.facultyProfileImage);
        btnSchedule = findViewById(R.id.btnSchedule);
        btnEditCalendar = findViewById(R.id.btnEditCalendar);
        btnEditRoom = findViewById(R.id.btnEditRoom);
        btnViewAttendance = findViewById(R.id.btnViewAttendance);
        logoutButton = findViewById(R.id.logoutButton);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, login_activity.class));
            finish();
            return;
        }

        String userId = currentUser.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Load faculty name and image
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String imageBase64 = snapshot.child("profileImage").getValue(String.class);

                    if (name != null) {
                        facultyWelcomeText.setText("👋 Welcome, " + name);
                    }

                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        facultyProfileImage.setImageBitmap(bitmap);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        btnSchedule.setOnClickListener(v -> {
            startActivity(new Intent(this, FacultySchedule.class));
        });
        btnViewAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(FacultyDashboard.this, faculty_attendance_scan.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(FacultyDashboard.this, FacultyLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        btnEditRoom.setOnClickListener(v -> {
            startActivity(new Intent(this, FacultyRoomAvailable.class));
        });
        btnEditCalendar.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminCalendar.class));
        });
    }
}

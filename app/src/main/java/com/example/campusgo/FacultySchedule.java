package com.example.campusgo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FacultySchedule extends AppCompatActivity {

    private LinearLayout scheduleContainer;
    private Button backButton;
    private TextView facultyWelcomeText;
    private FirebaseAuth auth;
    private DatabaseReference scheduleRef;
    private String facultyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faculty_schedule_activity);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        facultyWelcomeText = findViewById(R.id.facultyWelcomeText);
        scheduleContainer = findViewById(R.id.scheduleContainer);
        backButton = findViewById(R.id.backButton);

        if (currentUser == null) {
            Toast.makeText(this, "No logged in user found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        facultyId = currentUser.getUid();
        facultyWelcomeText.setText("👋 Welcome, " + (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Faculty"));

        scheduleRef = FirebaseDatabase.getInstance().getReference("FacultySchedule").child(facultyId);
        Log.d("DEBUG_UID", "Current UID: " + facultyId);
        Toast.makeText(this, "UID: " + facultyId, Toast.LENGTH_LONG).show();

        loadFacultySchedule();

        backButton.setOnClickListener(v -> {
            // Return to home or login activity
            Intent intent = new Intent(FacultySchedule.this, FacultyDashboard.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadFacultySchedule() {
        scheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<FacultyClass> classList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot classSnap : snapshot.getChildren()) {
                        FacultyClass fc = classSnap.getValue(FacultyClass.class);
                        if (fc != null) {
                            classList.add(fc);
                        }
                    }
                    displaySchedule(classList);
                } else {
                    Toast.makeText(FacultySchedule.this, "No schedule found for this faculty.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FacultySchedule.this, "Failed to load: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void displaySchedule(List<FacultyClass> scheduleList) {
        scheduleContainer.removeAllViews();

        for (FacultyClass fc : scheduleList) {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 12, 0, 12);
            card.setLayoutParams(cardParams);
            card.setRadius(16);
            card.setCardElevation(8);
            card.setUseCompatPadding(true);
            card.setCardBackgroundColor(Color.parseColor("#33000000")); // semi-transparent black bg

            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(32, 24, 32, 24);

            TextView dayText = new TextView(this);
            dayText.setText("📅 Day: " + fc.getDay());
            dayText.setTextSize(18);
            dayText.setTextColor(Color.WHITE);
            dayText.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView roomText = new TextView(this);
            roomText.setText("🏫 Room: " + fc.getRoom());
            roomText.setTextSize(16);
            roomText.setTextColor(Color.LTGRAY);
            roomText.setPadding(0, 8, 0, 0);

            TextView timeText = new TextView(this);
            timeText.setText("⏰ Time: " + fc.getTime());
            timeText.setTextSize(16);
            timeText.setTextColor(Color.LTGRAY);
            timeText.setPadding(0, 4, 0, 0);

            cardContent.addView(dayText);
            cardContent.addView(roomText);
            cardContent.addView(timeText);

            card.addView(cardContent);

            scheduleContainer.addView(card);
        }
    }
}

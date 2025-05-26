package com.example.campusgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class faculty_attendance_scan extends AppCompatActivity {

    private LinearLayout containerLayout;
    ImageView scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_attendance_scan);

        containerLayout = findViewById(R.id.containerLayout);
        scanButton = findViewById(R.id.qr_scanner);

        // Get today's date as node
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Reference to the "attendance/yyyy-MM-dd" node
        DatabaseReference attendanceRef = FirebaseDatabase.getInstance()
                .getReference("attendance").child(currentDate);

        // Log for debugging
        Log.d("DEBUG_DATE", "Querying date node: " + currentDate);

        // Listen for today's attendance
        attendanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("DEBUG_DATE", "Snapshot exists: " + snapshot.exists());
                Log.d("DEBUG_DATE", "Children count: " + snapshot.getChildrenCount());

                if (!snapshot.exists()) {
                    Toast.makeText(faculty_attendance_scan.this, "No data for today.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    Log.d("DEBUG_DATE", "Child key: " + child.getKey());

                    String username = child.child("username").getValue(String.class);
                    String studNum = child.child("stud_number").getValue(String.class);

                    Log.d("DEBUG_DATE", "Parsed username: " + username + ", stud_number: " + studNum);

                    addEntryToLayout(username, studNum);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FIREBASE_ERROR", "Database error: " + error.getMessage());
                Toast.makeText(faculty_attendance_scan.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });

        scanButton.setOnClickListener(v -> {
            startActivity(new Intent(this, qr_scanner.class));
        });
    }


    private void addEntryToLayout(String username, String studNumber) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.item_qr_data, containerLayout, false);

        TextView usernameText = itemView.findViewById(R.id.usernameText);
        TextView studNumberText = itemView.findViewById(R.id.studNumberText);

        usernameText.setText("Username: " + username);
        studNumberText.setText("Student #: " + studNumber);

        containerLayout.addView(itemView);
    }
}

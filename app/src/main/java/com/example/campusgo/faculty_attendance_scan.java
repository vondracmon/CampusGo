package com.example.campusgo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class faculty_attendance_scan extends AppCompatActivity {

    private LinearLayout containerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_attendance_scan);

        containerLayout = findViewById(R.id.containerLayout);

        // Get today's date as node
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Reference to the "Attendance/yyyy-MM-dd" node
        DatabaseReference attendanceRef = FirebaseDatabase.getInstance()
                .getReference("Attendance").child(currentDate);

        // Listen to changes
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String username = child.child("username").getValue(String.class);
                    String studNum = child.child("student_number").getValue(String.class);
                    addEntryToLayout(username, studNum);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }

    private void addEntryToLayout(String username, String studentNumber) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.item_qr_data, containerLayout, false);

        TextView usernameText = itemView.findViewById(R.id.usernameText);
        TextView studNumberText = itemView.findViewById(R.id.studNumberText);

        usernameText.setText("Username: " + username);
        studNumberText.setText("Student #: " + studentNumber);

        containerLayout.addView(itemView);
    }
}

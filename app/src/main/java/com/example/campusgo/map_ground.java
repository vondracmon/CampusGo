package com.example.campusgo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.HashMap;

public class map_ground extends AppCompatActivity {

    private DatabaseReference roomRef;
    private HashMap<String, Button> roomButtons;
    private HashMap<String, View> roomIndicators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_ground);

        roomRef = FirebaseDatabase.getInstance().getReference("rooms");
        roomButtons = new HashMap<>();
        roomIndicators = new HashMap<>();

        // Buttons
        Button btnRoom1 = findViewById(R.id.btnRoom1);
        Button btnRoom2 = findViewById(R.id.btnRoom2);
        Button btnRoom3 = findViewById(R.id.btnRoom3);
        Button btnRoom4 = findViewById(R.id.btnRoom4);
        Button btnRoom6 = findViewById(R.id.btnRoom6);
        Button btnAlvarado102 = findViewById(R.id.btnAlvarado102);
        Button btnNB1A = findViewById(R.id.btnNB1A);
        Button btnNB1B = findViewById(R.id.btnNB1B);
        Button btnScienceLab = findViewById(R.id.btnScienceLab);



        // Map buttons
        roomButtons.put("Room 1", btnRoom1);
        roomButtons.put("Room 2", btnRoom2);
        roomButtons.put("Room 3", btnRoom3);
        roomButtons.put("Room 4", btnRoom4);
        roomButtons.put("Room 6", btnRoom6);
        roomButtons.put("Alvarado 102", btnAlvarado102);
        roomButtons.put("NB1A", btnNB1A);
        roomButtons.put("NB1B", btnNB1B);
        roomButtons.put("Science Laboratory", btnScienceLab);


        // Set click listeners
        for (String roomName : roomButtons.keySet()) {
            Button btn = roomButtons.get(roomName);
            if (btn != null) {
                btn.setOnClickListener(v -> showRoomStatus(roomName));
            }
        }

        loadRoomStatuses();
    }

    private void loadRoomStatuses() {
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot roomSnap : snapshot.getChildren()) {
                    String roomName = roomSnap.getKey();
                    if (roomName == null) continue;

                    String availability = roomSnap.child("availability").getValue(String.class);

                    // Update indicator
                    View indicator = roomIndicators.get(roomName);
                    if (indicator != null && availability != null) {
                        int drawableResId = availability.equalsIgnoreCase("Available") ?
                                R.drawable.green_indicator : R.drawable.red_indicator;
                        indicator.setBackgroundResource(drawableResId);
                    }

                    // Optional: also change button color
                    Button btn = roomButtons.get(roomName);
                    if (btn != null && availability != null) {
                        int color = availability.equals("Available") ?
                                android.R.color.holo_green_light : android.R.color.holo_red_light;
                        btn.setBackgroundTintList(getResources().getColorStateList(color));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void showRoomStatus(String roomName) {
        roomRef.child(roomName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String availability = snapshot.child("availability").getValue(String.class);
                String markedBy = snapshot.child("markedBy").getValue(String.class);

                if (availability == null) availability = "Unknown";
                if (markedBy == null || markedBy.isEmpty()) markedBy = "N/A";

                new AlertDialog.Builder(map_ground.this)
                        .setTitle(roomName)
                        .setMessage("Availability: " + availability + "\nMarked by: " + markedBy)
                        .setPositiveButton("OK", null)
                        .show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
}

package com.example.campusgo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.HashMap;

public class map_second extends AppCompatActivity {

    private DatabaseReference roomRef;
    private HashMap<String, Button> roomButtons;
    private HashMap<String, View> roomIndicators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_second);

        roomRef = FirebaseDatabase.getInstance().getReference("rooms");
        roomButtons = new HashMap<>();
        roomIndicators = new HashMap<>();

        // Buttons
        Button btnFoodLab = findViewById(R.id.btnFoodLab);
        Button btnRoom205A = findViewById(R.id.btnRoom205A);
        Button btnDrawingRoom = findViewById(R.id.btnDrawingRoom);
        Button btnCPELab = findViewById(R.id.btnCPELab);
        Button btnCompLab2 = findViewById(R.id.btnCompLab2);
        Button btnCompLab1 = findViewById(R.id.btnCompLab1);
        Button btnNB2B = findViewById(R.id.btnNB2B);
        Button btnNB2A = findViewById(R.id.btnNB2A);


        roomButtons.put("Food Laboratory", btnFoodLab);
        roomButtons.put("Room 205A", btnRoom205A);
        roomButtons.put("Drawing Room", btnDrawingRoom);
        roomButtons.put("CPE Laboratory", btnCPELab);
        roomButtons.put("Computer Laboratory 2", btnCompLab2);
        roomButtons.put("Computer Laboratory 1", btnCompLab1);
        roomButtons.put("NB2B", btnNB2B);
        roomButtons.put("NB2A", btnNB2A);


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
                    View indicator = roomIndicators.get(roomName);

                    if (indicator != null && availability != null) {
                        int drawableResId = availability.equalsIgnoreCase("Available") ?
                                R.drawable.green_indicator : R.drawable.red_indicator;
                        indicator.setBackgroundResource(drawableResId);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
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

                new AlertDialog.Builder(map_second.this)
                        .setTitle(roomName)
                        .setMessage("Availability: " + availability + "\nMarked by: " + markedBy)
                        .setPositiveButton("OK", null)
                        .show();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}

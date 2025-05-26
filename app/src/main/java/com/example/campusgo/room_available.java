package com.example.campusgo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class room_available extends AppCompatActivity {

    private boolean isAdmin = false;
    private LinearLayout mainLayout;
    private DatabaseReference roomRef;
    private FirebaseUser currentUser;
    private ValueEventListener roomsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_available);

        mainLayout = findViewById(R.id.main);
        roomRef = FirebaseDatabase.getInstance().getReference("rooms");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        if (currentUser != null && isAdmin) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("username");

            userRef.get().addOnSuccessListener(dataSnapshot -> {
                String username = dataSnapshot.getValue(String.class);
                fetchRooms(username);
            }).addOnFailureListener(e -> {
                Toast.makeText(room_available.this, "Failed to fetch username", Toast.LENGTH_SHORT).show();
                fetchRooms("Unknown");
            });
        } else {
            fetchRooms(null); // for non-admin users
        }
    }

    private void fetchRooms(String username) {
        LayoutInflater inflater = LayoutInflater.from(this);

        // Remove previous listener if exists to avoid duplicates
        if (roomsListener != null) {
            roomRef.removeEventListener(roomsListener);
        }

        roomsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mainLayout.removeAllViews(); // Clear views before adding updated list

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    getRoom rooms = dataSnapshot.getValue(getRoom.class);
                    if (rooms == null) continue;

                    View roomView = inflater.inflate(R.layout.room_item, mainLayout, false);

                    TextView tvRoom = roomView.findViewById(R.id.roomNumber);
                    TextView tvAvailability = roomView.findViewById(R.id.availablility);
                    TextView tvMarkedBy = roomView.findViewById(R.id.markedByText);
                    Switch switchStatus = roomView.findViewById(R.id.statusSwitch);

                    tvRoom.setText(rooms.getRoom());
                    tvAvailability.setText(rooms.getAvailability());

                    if (isAdmin) {
                        switchStatus.setVisibility(View.VISIBLE);

                        // Prevent listener triggers when setting checked programmatically
                        switchStatus.setOnCheckedChangeListener(null);
                        switchStatus.setChecked(rooms.getAvailability().equals("Available"));
                        switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            String newStatus = isChecked ? "Available" : "In Class";
                            tvAvailability.setText(newStatus);

                            if (rooms.getRoom() == null || rooms.getRoom().isEmpty()) {
                                Log.e("Firebase", "Room name is null or empty — skipping update");
                                return;
                            }

                            // Update room availability in Firebase
                            roomRef.child(rooms.getRoom()).child("availability").setValue(newStatus)
                                    .addOnSuccessListener(unused -> Toast.makeText(room_available.this, "Updated!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(room_available.this, "Failed to update", Toast.LENGTH_SHORT).show());

                            // Track admin who marked the room busy
                            if (!isChecked && username != null) {
                                roomRef.child(rooms.getRoom()).child("markedBy").setValue(username)
                                        .addOnSuccessListener(unused -> Log.d("Firebase", "MarkedBy updated"))
                                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to update markedBy", e));
                            } else if (isChecked) {
                                roomRef.child(rooms.getRoom()).child("markedBy").removeValue()
                                        .addOnSuccessListener(unused -> Log.d("Firebase", "MarkedBy removed"))
                                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to remove markedBy", e));
                            }
                        });

                        tvMarkedBy.setVisibility(View.GONE);
                    } else {
                        switchStatus.setVisibility(View.GONE);

                        // Show who marked the room if not available
                        if (!rooms.getAvailability().equals("Available") && rooms.getMarkedBy() != null) {
                            tvMarkedBy.setText("Marked by: " + rooms.getMarkedBy());
                            tvMarkedBy.setVisibility(View.VISIBLE);
                        } else {
                            tvMarkedBy.setVisibility(View.GONE);
                        }
                    }

                    mainLayout.addView(roomView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ROOM_FETCH", "Failed to read rooms: ", error.toException());
                Toast.makeText(room_available.this, "Failed to load rooms", Toast.LENGTH_SHORT).show();
            }
        };

        // Attach the real-time listener
        roomRef.addValueEventListener(roomsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener to avoid memory leaks
        if (roomsListener != null) {
            roomRef.removeEventListener(roomsListener);
        }
    }
}

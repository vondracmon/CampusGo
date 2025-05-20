package com.example.campusgo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class room_available extends AppCompatActivity {

    private boolean isAdmin = false; // 🔧 default false
    private LinearLayout mainLayout;
    private DatabaseReference roomRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_available);

        mainLayout = findViewById(R.id.main);
        roomRef = FirebaseDatabase.getInstance().getReference("rooms");

        // 🔧 get admin flag from intent
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        fetchRooms();

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void fetchRooms() {
        mainLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        roomRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    getRoom room = snapshot.getValue(getRoom.class);
                    if (room == null) continue;

                    View roomView = inflater.inflate(R.layout.room_item, mainLayout, false);

                    TextView tvRoom = roomView.findViewById(R.id.roomNumber);
                    TextView tvAvailability = roomView.findViewById(R.id.availablility);
                    Switch switchStatus = roomView.findViewById(R.id.statusSwitch);

                    tvRoom.setText(room.getRoom());
                    tvAvailability.setText(room.getAvailability());

                    if (isAdmin) {
                        switchStatus.setVisibility(View.VISIBLE);
                        switchStatus.setChecked(room.getAvailability().equals("Available"));

                        switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            String newStatus = isChecked ? "Available" : "In Class";
                            tvAvailability.setText(newStatus);
                            roomRef.child(room.getRoom()).child("availability").setValue(newStatus)
                                    .addOnSuccessListener(unused -> Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show());
                        });
                    } else {
                        switchStatus.setVisibility(View.GONE); // hide switch for non-admin
                    }

                    mainLayout.addView(roomView);
                }
            } else {
                Toast.makeText(this, "Failed to load rooms", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.example.campusgo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusgo.databinding.ActivityGroundMapBinding;

import java.util.HashMap;
import java.util.Map;

public class ground_map extends AppCompatActivity {

    private ActivityGroundMapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Directly set the content view to the interactive_map layout file
        setContentView(R.layout.interactive_map);

        // Room status map using view IDs as keys
        Map<Integer, String> roomStatus = new HashMap<>();
        roomStatus.put(R.id.scienceLab, "Available");
        roomStatus.put(R.id.room6, "Occupied");
        roomStatus.put(R.id.alvarado102, "Available");
        roomStatus.put(R.id.room1, "Occupied");
        roomStatus.put(R.id.room2, "Available");
        roomStatus.put(R.id.room3, "Available");
        roomStatus.put(R.id.room4, "Occupied");
        roomStatus.put(R.id.nb1a, "Available");
        roomStatus.put(R.id.nb1b, "Occupied");

        // Array of all room view IDs
        int[] roomIds = {
                R.id.scienceLab, R.id.room6,
                R.id.alvarado102, R.id.room1, R.id.room2, R.id.room3,
                R.id.room4, R.id.nb1a, R.id.nb1b
        };

        // Set click listener for all room buttons
        for (int roomId : roomIds) {
            Button room = findViewById(roomId);
            if (room != null) {
                room.setOnClickListener(v -> {
                    int id = v.getId();
                    String status = roomStatus.get(id);
                    String roomName = getResources().getResourceEntryName(id);
                    if (status != null) {
                        Toast.makeText(this, roomName + ": " + status, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, roomName + ": Status Unknown", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}

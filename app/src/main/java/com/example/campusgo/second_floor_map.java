package com.example.campusgo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusgo.databinding.ActivityGroundMapBinding;

import java.util.HashMap;
import java.util.Map;

public class second_floor_map extends AppCompatActivity {

    private ActivityGroundMapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Directly set the content view to the interactive_map layout file
        setContentView(R.layout.second_floor_map);

        // Room status map using view IDs as keys
        Map<Integer, String> roomStatus = new HashMap<>();
        roomStatus.put(R.id.foodLab, "Available");
        roomStatus.put(R.id.room205a, "Available");
        roomStatus.put(R.id.drawRoom, "Available");
        roomStatus.put(R.id.avr, "Available");
        roomStatus.put(R.id.cpeLab, "Available");
        roomStatus.put(R.id.compLab1, "Available");
        roomStatus.put(R.id.compLab2, "Available");
        roomStatus.put(R.id.nb1a, "Available");
        roomStatus.put(R.id.nb1b, "Available");


        // Array of all room view IDs
        int[] roomIds = {
                R.id.foodLab, R.id.room205a, R.id.drawRoom, R.id.avr, R.id.cpeLab,
                R.id.compLab1, R.id.compLab2, R.id.nb1a, R.id.nb1b
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

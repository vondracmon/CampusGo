package com.example.campusgo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class room_available extends AppCompatActivity {
    // List to store the room data
    ArrayList<getRoom> getroom = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_room_available); // Main layout containing the 'main' LinearLayout

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadData();

        LinearLayout mainLayout = findViewById(R.id.main);
        LayoutInflater inflater = getLayoutInflater();

        for (int i = 0; i < getroom.size(); i++) {

            View roomView = inflater.inflate(R.layout.room_item, mainLayout, false);

            TextView tvRoomNumber = roomView.findViewById(R.id.roomNumber);
            tvRoomNumber.setText(getroom.get(i).getRoom());

            TextView tvAvailability = roomView.findViewById(R.id.availablility);
            tvAvailability.setText(getroom.get(i).getAvailablity());

            mainLayout.addView(roomView);

            Button backButton = findViewById(R.id.backButton);
            backButton.setOnClickListener(v -> {
                onBackPressed();
            });

        }
    }

    private void loadData() {
        getroom.add(new getRoom("Room 1", "Available"));
        getroom.add(new getRoom("Room 2", "Available"));
        getroom.add(new getRoom("Room 3", "In Class"));
        getroom.add(new getRoom("Room 4", "Available"));
        getroom.add(new getRoom("Alvarado 102", "Available"));
        getroom.add(new getRoom("Room 6", "Available"));
        getroom.add(new getRoom("Science Laboratory", "In Class"));
        getroom.add(new getRoom("NB1A", "Available"));
        getroom.add(new getRoom("NB1B", "In Class"));
        getroom.add(new getRoom("Computer Laboratory 1", "In Class"));
        getroom.add(new getRoom("Computer Laboratory 2", "In Class"));
        getroom.add(new getRoom("CPE Laboratory", "In Class"));
        getroom.add(new getRoom("Audio Visual Room", "Available"));
        getroom.add(new getRoom("Drawing Room", "In Class"));
        getroom.add(new getRoom("Room 205A", "Available"));
        getroom.add(new getRoom("Food Laboratory", "Available"));
        getroom.add(new getRoom("NB2A", "In Class"));
        getroom.add(new getRoom("NB2B", "Available"));
        getroom.add(new getRoom("Campus Library", "Available"));

        for (getRoom room : getroom) {
            Log.d("RoomAvailable", "Room: " + room.getRoom() + ", Availability: " + room.getAvailablity());
        }
    }
}

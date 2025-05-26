package com.example.campusgo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class floor extends AppCompatActivity {
    Button groundFloorButton, secondFloorButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor);

        groundFloorButton = findViewById(R.id.GroundFloor);
        secondFloorButton = findViewById(R.id.SecondFloor);
        backButton =  findViewById(R.id.backBtn);

        groundFloorButton.setOnClickListener(v -> {
            Intent intent = new Intent(floor.this, map_ground.class);
            startActivity(intent);
        });

        secondFloorButton.setOnClickListener(v -> {
            Intent intent = new Intent(floor.this, map_second.class);
            startActivity(intent);
        });


        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(floor.this, home_activity.class);
            startActivity(intent);
        });
    }
}

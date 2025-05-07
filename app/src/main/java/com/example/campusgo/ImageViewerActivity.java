package com.example.campusgo;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ImageViewerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        ImageView scrollableImageView = findViewById(R.id.scrollableImageView);

        int imageResId = getIntent().getIntExtra("imageResId", 0);

        if (imageResId != 0) {
            scrollableImageView.setImageResource(imageResId);
        }
    }
}

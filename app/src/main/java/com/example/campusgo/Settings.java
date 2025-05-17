package com.example.campusgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // Adjust for system bars
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.changePasswordBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePassword.class));
        });

        findViewById(R.id.languageBtn).setOnClickListener(v -> {
            new LanguageDialog().show(getSupportFragmentManager(), "languageDialog");
        });

        findViewById(R.id.notificationsBtn).setOnClickListener(v -> {
            new NotificationSettingsDialogFragment().show(getSupportFragmentManager(), "notifDialog");
        });

        findViewById(R.id.privacyPolicyBtn).setOnClickListener(v -> {
            showPrivacyPolicyDialog();
        });
    }

    private void showPrivacyPolicyDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("Sample privacy policy text")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

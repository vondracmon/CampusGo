package com.example.campusgo;

import android.content.Context;
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
    protected void attachBaseContext(Context newBase) {
        Context context = LocaleHelper.wrap(newBase); // wrap with locale
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this);
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
                .setMessage("Effective Date: May 27, 2025\n" +
                        "\n" +
                        "Welcome to CampusGo of Bulacan State University - Meneses Campus. By accessing or using this portal, you agree to be bound by the following Terms and Conditions. Please read them carefully.\n" +
                        "\n" +
                        "1. Acceptance of Terms\n" +
                        "By logging in and using the Student Portal, you agree to comply with and be bound by these Terms and Conditions. If you do not agree, please do not use the portal.\n" +
                        "\n" +
                        "2. Eligibility\n" +
                        "Only officially enrolled students, authorized faculty, and staff of Bulacan State University - Meneses Campus are permitted to access this portal.\n" +
                        "\n" +
                        "3. User Account and Responsibility\n" +
                        "You are responsible for maintaining the confidentiality of your login credentials. You agree to notify the administration immediately of any unauthorized use of your account. Misuse of your account (e.g., impersonating others or accessing restricted areas) may result in disciplinary action.\n" +
                        "\n" +
                        "4. Permitted Use\n" +
                        "The portal is intended solely for educational and academic purposes, including:\n" +
                        "\n" +
                        "Viewing class schedules, announcements, and academic records.\n" +
                        "\n" +
                        "Submitting assignments and communicating with faculty.\n" +
                        "\n" +
                        "You agree not to use the portal for any illegal or unauthorized purpose.\n" +
                        "\n" +
                        "5. Data Privacy\n" +
                        "Your personal information will be handled in accordance with our Privacy Policy and applicable data protection laws. The school reserves the right to monitor portal activity for security and academic integrity.\n" +
                        "\n" +
                        "6. Content Ownership\n" +
                        "All materials available on the portal, including documents, videos, and software, are the property of [School Name] or its licensors and are protected by intellectual property laws.\n" +
                        "\n" +
                        "7. Prohibited Activities\n" +
                        "You agree not to:\n" +
                        "\n" +
                        "Attempt to breach the security of the system.\n" +
                        "\n" +
                        "Upload viruses or harmful software.\n" +
                        "\n" +
                        "Harass, threaten, or harm other users.\n" +
                        "\n" +
                        "Share or distribute copyrighted material without permission.\n" +
                        "\n" +
                        "8. System Availability\n" +
                        "We strive to ensure the portal is always accessible but do not guarantee uninterrupted service. Maintenance or technical issues may result in temporary unavailability.\n" +
                        "\n" +
                        "9. Modifications\n" +
                        "We reserve the right to modify these Terms at any time. Continued use of the portal after changes means you accept the new Terms.\n" +
                        "\n" +
                        "10. Termination of Access\n" +
                        "We reserve the right to suspend or terminate your access to the portal for violations of these Terms, academic misconduct, or other applicable policies.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

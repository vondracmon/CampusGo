package com.example.campusgo;

import static com.example.campusgo.R.id.goodDay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class home_activity extends AppCompatActivity {
    TextClock timeTeller;
    TextView dateToday, goodDayText;
    ImageView qrIcon, mapIcon, searchIcon;
    ImageButton logoutButton, calendarBtn, settingsBtn;
    GoogleSignInClient gsc;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        // Firebase & Google sign-in setup
        auth = FirebaseAuth.getInstance();
        gsc = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());

        // UI element bindings
        logoutButton = findViewById(R.id.logoutBtn);
        calendarBtn = findViewById(R.id.calendarBtn);
        settingsBtn = findViewById(R.id.settingsBtn);

        timeTeller = findViewById(R.id.timeTeller);
        dateToday = findViewById(R.id.dateToday);
        qrIcon = findViewById(R.id.qr_icon);
        mapIcon = findViewById(R.id.map_icon);
        searchIcon = findViewById(R.id.search_icon);
        goodDayText = findViewById(goodDay);

        // Set date
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        dateToday.setText(sdf.format(new Date()));

        // Set time zone
        timeTeller.setTimeZone("Asia/Singapore");

        // Greet user
        FirebaseUser user = auth.getCurrentUser();
        String userId = "guest_user";

        if (user != null && user.getEmail() != null) {
            String email = user.getEmail();
            String userName = email.split("@")[0];
            userId = userName;
            goodDayText.setText("Good day, " + userName + "!");
        } else {
            goodDayText.setText("Good day, Guest!");
        }

        String finalUserId = userId; // for lambda usage

        // Logout
        logoutButton.setOnClickListener(view -> {
            auth.signOut();
            gsc.signOut().addOnCompleteListener(task -> redirectToLogin());
        });

        // QR Code Icon
        qrIcon.setOnClickListener(v -> {
            Intent intent = new Intent(home_activity.this, qr.class);
            intent.putExtra("USER_DATA", finalUserId);
            startActivity(intent);
        });

        // Map
        mapIcon.setOnClickListener(v -> startActivity(new Intent(home_activity.this, floor.class)));

        // Room Availability
        searchIcon.setOnClickListener(v -> startActivity(new Intent(home_activity.this, room_available.class)));

        // Calendar
        calendarBtn.setOnClickListener(v -> startActivity(new Intent(home_activity.this, AdminCalendar.class)));

        // Settings
        settingsBtn.setOnClickListener(v -> startActivity(new Intent(home_activity.this, Settings.class)));
    }

    private void redirectToLogin() {
        Intent intent = new Intent(getApplicationContext(), login_activity.class);
        startActivity(intent);
        finish();
    }
}

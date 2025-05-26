package com.example.campusgo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    Button signUpBtn, loginBtn;

    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = LocaleHelper.wrap(newBase); // wrap with locale
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signUpBtn = findViewById(R.id.signUpBtn);
        loginBtn = findViewById(R.id.loginBtn);


        signUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, register_activity.class);
            startActivity(intent);
        });

        loginBtn.setOnClickListener(v -> showRoleSelectionDialog());



    }

    // 👇 Correctly defined outside onCreate
    private void showRoleSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_role_selector, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);

        Button studentBtn = dialogView.findViewById(R.id.studentBtn);
        Button facultyBtn = dialogView.findViewById(R.id.facultyBtn);

        studentBtn.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MainActivity.this, login_activity.class);
            intent.putExtra("role", "student");
            startActivity(intent);
        });

        facultyBtn.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MainActivity.this, FacultyLogin.class);
            intent.putExtra("role", "faculty");
            startActivity(intent);
        });

        dialog.show();
    }
}

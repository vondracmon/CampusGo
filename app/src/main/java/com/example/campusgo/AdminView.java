package com.example.campusgo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import java.util.ArrayList;
import java.util.List;

public class AdminView extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button registerBtn, loginBtn, updateEmailBtn, deleteUserBtn, logoutBtn;
    RecyclerView userRecyclerView;

    DrawerLayout drawerLayout;
    NavigationView navView;

    FirebaseAuth auth;
    DatabaseReference databaseReference;
    List<Users> userList;
    UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerBtn = findViewById(R.id.registerBtn);
        loginBtn = findViewById(R.id.loginBtn);
        updateEmailBtn = findViewById(R.id.updateEmailBtn);
        deleteUserBtn = findViewById(R.id.deleteUserBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        userRecyclerView = findViewById(R.id.userRecyclerView);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userRecyclerView.setAdapter(userAdapter);

        fetchUsers();

        registerBtn.setOnClickListener(v -> registerUser());
        loginBtn.setOnClickListener(v -> loginUser());
        updateEmailBtn.setOnClickListener(v -> updateEmail());
        deleteUserBtn.setOnClickListener(v -> deleteUser());
        logoutBtn.setOnClickListener(v -> logoutUser());

        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();

            if (id == R.id.nav_users) {
                Toast.makeText(this, "Manage Users", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_rooms) {
                Intent intent = new Intent(this, room_available.class);
                intent.putExtra("isAdmin", true); // ✅ tell room screen admin is true
                startActivity(intent);
            } else if (id == R.id.nav_calendar) {
                startActivity(new Intent(this, AdminCalendar.class));
            } else if (id == R.id.settings) {
                startActivity(new Intent(this, Settings.class));
            } else if (id == R.id.nav_logout) {
                logoutUser();
            }
            return true;
        });
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Do not leave anything blank", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = auth.getCurrentUser().getUid();
                    Users user = new Users(email);
                    databaseReference.child(userId).setValue(user)
                            .addOnSuccessListener(unused -> {
                                fetchUsers();
                                showAlert("Registration Successful", "Email: " + email + "\nPassword: " + password);
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Logged in Successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, room_available.class);
                    intent.putExtra("isAdmin", true); // ✅ hardcoded admin
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String newEmail = emailInput.getText().toString().trim();
        if (TextUtils.isEmpty(newEmail)) {
            emailInput.setError("Enter new email");
            return;
        }

        user.updateEmail(newEmail)
                .addOnSuccessListener(unused -> {
                    databaseReference.child(user.getUid()).child("email").setValue(newEmail);
                    Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show();
                    fetchUsers();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        user.delete()
                .addOnSuccessListener(unused -> {
                    databaseReference.child(uid).removeValue();
                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                    fetchUsers();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void logoutUser() {
        auth.signOut();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, login_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchUsers() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Users user = child.getValue(Users.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read users", error.toException());
            }
        });
    }

    private void showAlert(String title, String msg) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

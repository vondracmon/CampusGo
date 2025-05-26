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

    EditText emailInput, stud_email_change, passwordInput, usernameInput;
    Button registerBtn, updateEmailBtn, deleteUserBtn, logoutBtn;
    RecyclerView userRecyclerView;

    DrawerLayout drawerLayout;
    NavigationView navView;

    FirebaseAuth auth;
    DatabaseReference databaseReference;
    List<Users> userList;
    UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view);

        //admin checker
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("role");
            ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String role = task.getResult().getValue(String.class);
                    if (!"admin".equalsIgnoreCase(role)) {
                        Toast.makeText(AdminView.this, "Access denied", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminView.this, home_activity.class));
                        finish();
                    }
                } else {
                    Toast.makeText(AdminView.this, "Could not verify role", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }


        // Setup UI components
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Init inputs & buttons
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerBtn = findViewById(R.id.registerBtn);
        updateEmailBtn = findViewById(R.id.updateEmailBtn);
        deleteUserBtn = findViewById(R.id.deleteUserBtn);
        userRecyclerView = findViewById(R.id.userRecyclerView);
        stud_email_change = findViewById(R.id.stud_email_change);
        usernameInput = findViewById(R.id.usernameInput);

        // Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Setup RecyclerView
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userRecyclerView.setAdapter(userAdapter);

        // Load existing users
        fetchUsers();

        // Button listeners
        registerBtn.setOnClickListener(v -> registerUser());
        updateEmailBtn.setOnClickListener(v -> updateEmail());
        deleteUserBtn.setOnClickListener(v -> deleteUser());

        // Navigation drawer logic
        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();

            if (id == R.id.nav_users) {
                Toast.makeText(this, "Manage Users", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_rooms) {
                Intent intent = new Intent(this, room_available.class);
                intent.putExtra("isAdmin", true);
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
        String facultyUsername = usernameInput.getText().toString().trim();

        String role = "faculty";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(facultyUsername)) {
            Toast.makeText(this, "Do not leave anything blank", Toast.LENGTH_SHORT).show();
            emailInput.setError("Enter email");
            passwordInput.setError("Enter password");
            usernameInput.setError("Enter username");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = auth.getCurrentUser().getUid();

                    Users user = new Users(facultyUsername, email, role);

                    databaseReference.child(userId).setValue(user)
                            .addOnSuccessListener(unused -> {
                                fetchUsers();
                                showAlert("Registration Successful", "Email: " + email + "\nUsername: " + facultyUsername + "\nPassword: " + password);

                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        emailInput.setText("");
        passwordInput.setText("");
        usernameInput.setText("");
    }


    private void updateEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String newEmail = stud_email_change.getText().toString().trim();
        if (TextUtils.isEmpty(newEmail)) {
            stud_email_change.setError("Enter new email");
            return;
        }

        user.updateEmail(newEmail)
                .addOnSuccessListener(unused -> {
                    databaseReference.child(user.getUid()).child("email").setValue(newEmail);
                    Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show();
                    fetchUsers();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        stud_email_change.setText("");
    }

    private void deleteUser() {
        String targetEmail = stud_email_change.getText().toString().trim();

        if (TextUtils.isEmpty(targetEmail)) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
            stud_email_change.setError("Enter an email");
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Users user = userSnapshot.getValue(Users.class);

                    if (user != null && targetEmail.equalsIgnoreCase(user.getEmail())) {
                        userSnapshot.getRef().removeValue()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getApplicationContext(), "User deleted from database", Toast.LENGTH_SHORT).show();
                                    fetchUsers(); // Refresh UI or list
                                })
                                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        stud_email_change.setText("");
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
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Users user = child.getValue(Users.class);
                    if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
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

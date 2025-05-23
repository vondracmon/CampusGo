
package com.example.campusgo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class FacultyRoomAvailable extends AppCompatActivity {
    private Spinner filterSpinner;
    private LinearLayout roomContainer;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private String facultyUID;
    private String facultyName;
    private List<String> facultyRooms = new ArrayList<>();
    private Map<String, String> endTimeMap = new HashMap<>();
    private String currentDay;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_room_available);

        roomContainer = findViewById(R.id.roomContainer);
        filterSpinner = findViewById(R.id.filterSpinner);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        facultyUID = currentUser.getUid();
        database = FirebaseDatabase.getInstance().getReference();

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(FacultyRoomAvailable.this, FacultyDashboard.class);
            startActivity(intent);
            finish();
        });

        database.child("Users").child(facultyUID).child("username").get().addOnSuccessListener(snapshot -> {
            facultyName = snapshot.getValue(String.class);
            fetchFacultySchedule();
        });

        currentDay = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.room_filters, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadRooms();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void fetchFacultySchedule() {
        DatabaseReference scheduleRef = database.child("FacultySchedule").child(facultyUID);
        scheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                facultyRooms.clear();
                endTimeMap.clear();
                for (DataSnapshot classSnap : snapshot.getChildren()) {
                    String day = classSnap.child("day").getValue(String.class);
                    String room = classSnap.child("room").getValue(String.class);
                    String time = classSnap.child("time").getValue(String.class);
                    if (room != null && day != null && day.equalsIgnoreCase(currentDay)) {
                        facultyRooms.add(room);
                        if (time != null && time.contains("-")) {
                            String[] times = time.split("-");
                            if (times.length == 2) endTimeMap.put(room, times[1].trim());
                        }
                    }
                }
                loadRooms();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadRooms() {
        roomContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        String selectedFilter = filterSpinner.getSelectedItem().toString();

        database.child("rooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot roomSnap : snapshot.getChildren()) {
                    String roomName = roomSnap.getKey();
                    String status = roomSnap.child("availability").getValue(String.class);
                    String markedBy = roomSnap.child("markedBy").getValue(String.class);

                    boolean isMyRoom = facultyRooms.contains(roomName);
                    if (("My Rooms".equals(selectedFilter) && !isMyRoom) ||
                            ("Other Rooms".equals(selectedFilter) && isMyRoom)) {
                        continue;
                    }

                    View roomView = inflater.inflate(R.layout.room_item, roomContainer, false);

                    TextView tvRoom = roomView.findViewById(R.id.roomNumber);
                    TextView tvAvailability = roomView.findViewById(R.id.availablility);
                    TextView tvMarkedBy = roomView.findViewById(R.id.markedByText);
                    Switch statusSwitch = roomView.findViewById(R.id.statusSwitch);

                    tvRoom.setText(roomName);
                    tvAvailability.setText("Status: " + status);
                    statusSwitch.setChecked("Available".equalsIgnoreCase(status));
                    statusSwitch.setVisibility(isMyRoom ? View.VISIBLE : View.GONE);

                    if (!"Available".equalsIgnoreCase(status) && markedBy != null && !markedBy.isEmpty()) {
                        tvMarkedBy.setVisibility(View.VISIBLE);
                        tvMarkedBy.setText("Marked by: " + markedBy);
                    } else {
                        tvMarkedBy.setVisibility(View.GONE);
                    }

                    if (isMyRoom) {
                        statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (!isWithinSchedule(roomName)) {
                                Toast.makeText(FacultyRoomAvailable.this, "Not within scheduled time", Toast.LENGTH_SHORT).show();
                                buttonView.setChecked(!isChecked);
                                return;
                            }
                            String newStatus = isChecked ? "Available" : "In Class";
                            String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                            String endTime = endTimeMap.getOrDefault(roomName, "?");
                            String marked = facultyName + "\n(" + currentTime + " - " + endTime + ")";

                            database.child("rooms").child(roomName).child("availability").setValue(newStatus);
                            database.child("rooms").child(roomName).child("markedBy").setValue(isChecked ? "" : marked);
                            tvAvailability.setText("Status: " + newStatus);
                            tvMarkedBy.setText(isChecked ? "" : ("Marked by: " + marked));
                            tvMarkedBy.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                        });
                    }

                    roomContainer.addView(roomView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FacultyRoomAvailable.this, "Failed to load rooms", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isWithinSchedule(String room) {
        Calendar now = Calendar.getInstance();
        String[] scheduleTimes = endTimeMap.getOrDefault(room, "").split(":");
        return scheduleTimes.length > 0; // future improvement: validate against real time
    }
}

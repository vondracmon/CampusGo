package com.example.campusgo;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FacultyRoomAvailable extends AppCompatActivity {

    private LinearLayout roomContainer;
    private Spinner filterSpinner;
    private DatabaseReference roomsRef, facultyScheduleRef, usersRef;
    private String facultyUid;
    private LayoutInflater inflater;
    private static final String CHANNEL_ID = "OvertimeNotificationChannel";
    private Map<String, String> todayScheduledRooms = new HashMap<>();
    private String selectedFilter = "All Rooms";
    private Set<String> allScheduledRooms = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_room_available);

        roomContainer = findViewById(R.id.roomContainer);
        filterSpinner = findViewById(R.id.filterSpinner);

        facultyUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        roomsRef = FirebaseDatabase.getInstance().getReference("rooms");
        facultyScheduleRef = FirebaseDatabase.getInstance().getReference("FacultySchedule").child(facultyUid);
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        inflater = LayoutInflater.from(this);

        createNotificationChannel();
        setupSpinner();
        loadTodaySchedule();

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                Arrays.asList("All Rooms", "My Rooms", "Other Rooms"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setSelection(0);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = parent.getItemAtPosition(position).toString();
                loadRooms();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadTodaySchedule() {
        String today = getTodayDayName();
        todayScheduledRooms.clear();
        allScheduledRooms.clear();

        facultyScheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot classSnap : snapshot.getChildren()) {
                    String day = classSnap.child("day").getValue(String.class);
                    String room = classSnap.child("room").getValue(String.class);
                    String time = classSnap.child("time").getValue(String.class);

                    if (room != null) {
                        allScheduledRooms.add(room);
                        if (day != null && time != null && day.equalsIgnoreCase(today)) {
                            todayScheduledRooms.put(room, time);
                        }
                    }
                }
                loadRooms();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FacultyRoomAvailable.this, "Failed to load schedule.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRooms() {
        roomContainer.removeAllViews();

        roomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot roomSnap : snapshot.getChildren()) {
                    String roomName = roomSnap.getKey();
                    String availability = roomSnap.child("availability").getValue(String.class);
                    String markedByUid = roomSnap.child("markedBy").getValue(String.class);
                    String startTime = roomSnap.child("startTime").getValue(String.class);
                    String endTime = roomSnap.child("endTime").getValue(String.class);
                    String markedAt = roomSnap.child("markedAt").getValue(String.class);

                    boolean isMyRoom = allScheduledRooms.contains(roomName);
                    boolean canToggle = todayScheduledRooms.containsKey(roomName);
                    if (selectedFilter.equals("My Rooms") && !isMyRoom) continue;
                    if (selectedFilter.equals("Other Rooms") && isMyRoom) continue;

                    View roomView = inflater.inflate(R.layout.room_item, roomContainer, false);

                    TextView tvRoomNumber = roomView.findViewById(R.id.roomNumber);
                    TextView tvAvailability = roomView.findViewById(R.id.availablility);
                    TextView tvMarkedBy = roomView.findViewById(R.id.markedByText);
                    Switch statusSwitch = roomView.findViewById(R.id.statusSwitch);

                    tvRoomNumber.setText(roomName);
                    tvAvailability.setText("Status: " + availability);

                    // Fetch username for markedBy UID asynchronously
                    if (markedByUid == null || markedByUid.isEmpty()) {
                        tvMarkedBy.setText("Marked by: N/A");
                    } else {
                        usersRef.child(markedByUid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                String username = userSnapshot.getValue(String.class);
                                if (username != null && !username.isEmpty()) {
                                    tvMarkedBy.setText("Marked by: " + username);
                                } else {
                                    tvMarkedBy.setText("Marked by: " + markedByUid); // fallback to UID
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                tvMarkedBy.setText("Marked by: " + markedByUid); // fallback
                            }
                        });
                    }

                    statusSwitch.setChecked("In Class".equalsIgnoreCase(availability));
                    statusSwitch.setEnabled(canToggle);

                    String classTime = todayScheduledRooms.get(roomName);

                    statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            if (!isMyRoom) {
                                Toast.makeText(FacultyRoomAvailable.this, "You are not scheduled for this room today.", Toast.LENGTH_SHORT).show();
                                statusSwitch.setChecked(false);
                                return;
                            }

                            String currentTimeStr = getCurrentTimeString();
                            String endTimeStr = getEndTimeFromSchedule(classTime);

                            roomsRef.child(roomName).child("availability").setValue("In Class");
                            roomsRef.child(roomName).child("markedBy").setValue(facultyUid);
                            roomsRef.child(roomName).child("startTime").setValue(currentTimeStr);
                            roomsRef.child(roomName).child("markedAt").setValue(String.valueOf(System.currentTimeMillis()));
                            roomsRef.child(roomName).child("endTime").setValue(endTimeStr);

                            tvAvailability.setText("Status: In Class");
                            // Fetch username for current facultyUid to display immediately
                            usersRef.child(facultyUid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    String username = userSnapshot.getValue(String.class);
                                    if (username != null && !username.isEmpty()) {
                                        tvMarkedBy.setText("Marked by: " + username);
                                    } else {
                                        tvMarkedBy.setText("Marked by: " + facultyUid);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    tvMarkedBy.setText("Marked by: " + facultyUid);
                                }
                            });

                            scheduleOvertimeCheck(roomName, endTimeStr);

                            Toast.makeText(FacultyRoomAvailable.this, roomName + " marked as In Class", Toast.LENGTH_SHORT).show();
                        } else {
                            roomsRef.child(roomName).child("availability").setValue("Available");
                            roomsRef.child(roomName).child("markedBy").setValue("");
                            roomsRef.child(roomName).child("startTime").setValue("");
                            roomsRef.child(roomName).child("endTime").setValue("");
                            roomsRef.child(roomName).child("markedAt").setValue("");

                            tvAvailability.setText("Status: Available");
                            tvMarkedBy.setText("Marked by: N/A");

                            cancelOvertimeAlarm(roomName);

                            Toast.makeText(FacultyRoomAvailable.this, roomName + " marked as Available", Toast.LENGTH_SHORT).show();
                        }
                    });

                    roomContainer.addView(roomView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FacultyRoomAvailable.this, "Failed to load rooms.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getTodayDayName() {
        return new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());
    }

    private String getCurrentTimeString() {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
    }

    private String getEndTimeFromSchedule(String schedule) {
        if (schedule == null) return "";
        String[] parts = schedule.split("-");
        return parts.length == 2 ? parts[1].trim() : "";
    }

    private void scheduleOvertimeCheck(String roomName, String endTimeStr) {
        long endMillis = parseTimeToMillis(endTimeStr);
        if (endMillis == -1) return;

        long now = System.currentTimeMillis();
        long timeUntilEnd = Math.max(endMillis - now, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent notifyIntent = new Intent(this, OvertimeNotificationReceiver.class);
        notifyIntent.putExtra("room", roomName);
        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(this, roomName.hashCode(), notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + timeUntilEnd, notifyPendingIntent);

        Intent resetIntent = new Intent(this, RoomResetReceiver.class);
        resetIntent.putExtra("room", roomName);
        PendingIntent resetPendingIntent = PendingIntent.getBroadcast(this, roomName.hashCode() + 1000, resetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + timeUntilEnd + 10 * 60 * 1000, resetPendingIntent);
    }

    private void cancelOvertimeAlarm(String roomName) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent notifyIntent = new Intent(this, OvertimeNotificationReceiver.class);
        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(this, roomName.hashCode(), notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(notifyPendingIntent);

        Intent resetIntent = new Intent(this, RoomResetReceiver.class);
        PendingIntent resetPendingIntent = PendingIntent.getBroadcast(this, roomName.hashCode() + 1000, resetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(resetPendingIntent);
    }

    private long parseTimeToMillis(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return -1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date date = sdf.parse(timeStr);
            if (date == null) return -1;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            Calendar now = Calendar.getInstance();
            calendar.set(Calendar.YEAR, now.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Overtime Notification";
            String description = "Notifications for room overtime usage";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

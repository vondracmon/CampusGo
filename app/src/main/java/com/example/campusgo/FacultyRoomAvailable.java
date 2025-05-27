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
import android.widget.*;
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

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                Arrays.asList("All Rooms", "My Rooms", "Other Rooms"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setSelection(0);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = parent.getItemAtPosition(position).toString();
                loadRooms();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadTodaySchedule() {
        String today = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());
        todayScheduledRooms.clear();
        allScheduledRooms.clear();

        facultyScheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
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
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FacultyRoomAvailable.this, "Failed to load schedule.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRooms() {
        roomContainer.removeAllViews();
        roomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot roomSnap : snapshot.getChildren()) {
                    String roomName = roomSnap.getKey();
                    String availability = roomSnap.child("availability").getValue(String.class);
                    String markedByUid = roomSnap.child("markedBy").getValue(String.class);
                    String startTime = roomSnap.child("startTime").getValue(String.class);
                    String endTime = roomSnap.child("endTime").getValue(String.class);

                    boolean isMyRoom = allScheduledRooms.contains(roomName);
                    boolean canToggle = todayScheduledRooms.containsKey(roomName);

                    if (selectedFilter.equals("My Rooms") && !isMyRoom) continue;
                    if (selectedFilter.equals("Other Rooms") && isMyRoom) continue;

                    View roomView = inflater.inflate(R.layout.room_item, roomContainer, false);
                    TextView tvRoomNumber = roomView.findViewById(R.id.roomNumber);
                    TextView tvAvailability = roomView.findViewById(R.id.availablility);
                    TextView tvMarkedBy = roomView.findViewById(R.id.markedByText);
                    TextView tvStartTime = roomView.findViewById(R.id.startTimeText);
                    TextView tvEndTime = roomView.findViewById(R.id.endTimeText);
                    Switch statusSwitch = roomView.findViewById(R.id.statusSwitch);

                    tvRoomNumber.setText(roomName);
                    tvAvailability.setText("Status: " + (availability != null ? availability : "Unknown"));
                    tvMarkedBy.setText("Marked by: N/A");
                    tvStartTime.setText("Start Time: " + (startTime != null ? startTime : "-"));
                    tvEndTime.setText("End Time: " + (endTime != null ? endTime : "-"));

                    if (markedByUid != null && !markedByUid.isEmpty()) {
                        usersRef.child(markedByUid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot userSnap) {
                                String username = userSnap.getValue(String.class);
                                tvMarkedBy.setText("Marked by: " + (username != null ? username : markedByUid));
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) {
                                tvMarkedBy.setText("Marked by: " + markedByUid);
                            }
                        });
                    }

                    statusSwitch.setOnCheckedChangeListener(null);
                    statusSwitch.setChecked("In Class".equalsIgnoreCase(availability));
                    statusSwitch.setEnabled(canToggle);

                    String classTime = todayScheduledRooms.get(roomName);

                    statusSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                        DatabaseReference roomRef = roomsRef.child(roomName);
                        if (isChecked) {
                            if (!isMyRoom) {
                                Toast.makeText(FacultyRoomAvailable.this, "You are not scheduled for this room today.", Toast.LENGTH_SHORT).show();
                                statusSwitch.setChecked(false);
                                return;
                            }

                            String currentTime = getCurrentTime();
                            String endClassTime = extractEndTime(classTime);
                            long now = System.currentTimeMillis();

                            roomRef.child("availability").setValue("In Class");
                            roomRef.child("markedBy").setValue(facultyUid);
                            roomRef.child("startTime").setValue(currentTime);
                            roomRef.child("endTime").setValue(endClassTime);
                            roomRef.child("markedAt").setValue(String.valueOf(now));

                            tvAvailability.setText("Status: In Class");
                            tvStartTime.setText("Start Time: " + currentTime);
                            tvEndTime.setText("End Time: " + endClassTime);

                            usersRef.child(facultyUid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String username = snapshot.getValue(String.class);
                                    tvMarkedBy.setText("Marked by: " + (username != null ? username : "You"));
                                }
                                @Override public void onCancelled(@NonNull DatabaseError error) {}
                            });

                            scheduleOvertimeCheck(roomName, endClassTime);
                        } else {
                            roomRef.child("availability").setValue("Available");
                            roomRef.child("markedBy").setValue("");
                            roomRef.child("startTime").setValue("");
                            roomRef.child("endTime").setValue("");
                            roomRef.child("markedAt").setValue("");

                            tvAvailability.setText("Status: Available");
                            tvMarkedBy.setText("Marked by: N/A");
                            tvStartTime.setText("Start Time: -");
                            tvEndTime.setText("End Time: -");

                            cancelOvertimeAlarm(roomName);
                        }
                    });

                    roomContainer.addView(roomView);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FacultyRoomAvailable.this, "Failed to load rooms.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scheduleOvertimeCheck(String roomName, String endTimeStr) {
        long endMillis = parseTimeToMillis(endTimeStr);
        if (endMillis == -1) return;

        long now = System.currentTimeMillis();
        if (endMillis < now) endMillis += 24 * 60 * 60 * 1000;

        long delay = endMillis - now;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent notifyIntent = new Intent(this, OvertimeNotificationReceiver.class);
        notifyIntent.putExtra("room", roomName);
        PendingIntent notifyPending = PendingIntent.getBroadcast(this, roomName.hashCode(), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + delay, notifyPending);

        Intent resetIntent = new Intent(this, RoomResetReceiver.class);
        resetIntent.putExtra("room", roomName);
        PendingIntent resetPending = PendingIntent.getBroadcast(this, roomName.hashCode() + 1000, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + delay + 10 * 60 * 1000, resetPending);
    }

    private void cancelOvertimeAlarm(String roomName) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        PendingIntent notifyPending = PendingIntent.getBroadcast(this, roomName.hashCode(), new Intent(this, OvertimeNotificationReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(notifyPending);

        PendingIntent resetPending = PendingIntent.getBroadcast(this, roomName.hashCode() + 1000, new Intent(this, RoomResetReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(resetPending);
    }

    private long parseTimeToMillis(String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date date = sdf.parse(timeStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            Calendar now = Calendar.getInstance();
            calendar.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            return -1;
        }
    }

    private String extractEndTime(String schedule) {
        if (schedule == null || !schedule.contains("-")) return "";
        return schedule.split("-")[1].trim();
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Overtime Notifications";
            String description = "Notifies faculty about overtime usage";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

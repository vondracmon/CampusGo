package com.example.campusgo;

import android.os.Bundle;
import android.text.InputType;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.campusgo.EventItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class AdminCalendar extends AppCompatActivity {

    private TextView textMonthYear, eventTitle;
    private CalendarView calendarView;
    private RecyclerView eventsRecyclerView;
    private FloatingActionButton fabAddEvent;

    private EventAdapter adapter;
    private List<EventItem> eventList = new ArrayList<>();
    private String selectedDate = "";

    private final DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_calendar);

        // Initialize views
        textMonthYear = findViewById(R.id.textMonthYear);
        calendarView = findViewById(R.id.calendarView);
        eventTitle = findViewById(R.id.eventTitle);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        fabAddEvent = findViewById(R.id.fabAddEvent);
//testing
        // Setup RecyclerView
        adapter = new EventAdapter(eventList);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(adapter);

        // Calendar selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = (month + 1) + "/" + dayOfMonth + "/" + year;
            eventTitle.setText("Events on: " + selectedDate);
            loadEventsForDate(selectedDate);
        });

        // FAB to open add-event dialog
        fabAddEvent.setOnClickListener(v -> {
            showAddEventDialog();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set today’s date and load
        Calendar calendar = Calendar.getInstance();
        long todayMillis = calendar.getTimeInMillis();
        String todayFormatted = formatDate(todayMillis);

        if (!selectedDate.equals(todayFormatted)) {
            calendarView.setDate(todayMillis, true, true);
            selectedDate = todayFormatted;
            textMonthYear.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.getTime()));
            eventTitle.setText("Events on: " + selectedDate);
            loadEventsForDate(selectedDate);
        }
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy", Locale.getDefault());
        return sdf.format(millis);
    }

    private void loadEventsForDate(String date) {
        eventsRef.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        EventItem item = data.getValue(EventItem.class);
                        if (item != null) {
                            item.setId(data.getKey());
                            eventList.add(item);
                        }
                    }
                }

                adapter.updateList(eventList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminCalendar.this, "Error loading events: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Event");

        final EditText input = new EditText(this);
        input.setHint("Enter event title");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String eventTitle = input.getText().toString().trim();
            if (!eventTitle.isEmpty()) {
                String eventId = eventsRef.push().getKey();
                EventItem newItem = new EventItem(eventId, eventTitle, selectedDate, "General", "");
                if (eventId != null) {
                    eventsRef.child(eventId).setValue(newItem)
                            .addOnSuccessListener(aVoid -> {
                                loadEventsForDate(selectedDate);
                                Toast.makeText(this, "Event added", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to add event", Toast.LENGTH_SHORT).show()
                            );
                }
            } else {
                Toast.makeText(this, "Event title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}

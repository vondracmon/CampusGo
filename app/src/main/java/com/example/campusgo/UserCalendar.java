package com.example.campusgo;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.campusgo.EventItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class UserCalendar extends AppCompatActivity {

    private TextView textMonthYear, eventTitle;
    private CalendarView calendarView;
    private RecyclerView eventsRecyclerView;

    private EventAdapter adapter;
    private List<EventItem> eventList = new ArrayList<>();
    private String selectedDate = "";

    private final DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_calendar);  // A layout similar to admin but no FAB

        textMonthYear = findViewById(R.id.textMonthYear);
        calendarView = findViewById(R.id.calendarView);
        eventTitle = findViewById(R.id.eventTitle);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);

        adapter = new EventAdapter(eventList);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(adapter);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = (month + 1) + "/" + dayOfMonth + "/" + year;
            eventTitle.setText("Events on: " + selectedDate);
            loadEventsForDate(selectedDate);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

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
                Toast.makeText(UserCalendar.this, "Error loading events: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.example.campusgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventItem> eventList;

    public EventAdapter(List<EventItem> eventList) {
        this.eventList = eventList;
    }

    public void updateList(List<EventItem> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, categoryTextView, noteTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.eventTitleTextView);
            categoryTextView = itemView.findViewById(R.id.eventCategoryTextView);
            noteTextView = itemView.findViewById(R.id.eventNoteTextView);
        }
    }


    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventItem item = eventList.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.categoryTextView.setText(item.getCategory());
        holder.noteTextView.setText(item.getNote());
    }

    @Override
    public int getItemCount() {
        return (eventList != null) ? eventList.size() : 0;
    }
}

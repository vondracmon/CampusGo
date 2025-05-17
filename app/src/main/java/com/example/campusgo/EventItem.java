package com.example.campusgo;

public class EventItem {
    private String id;
    private String title;
    private String date;
    private String category; // e.g. "Holiday", "Exam", "Seminar"
    private String note;

    public EventItem() {} // Required by Firebase

    public EventItem(String id, String title, String date, String category, String note) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.category = category;
        this.note = note;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

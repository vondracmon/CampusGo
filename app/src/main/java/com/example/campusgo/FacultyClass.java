package com.example.campusgo;

public class FacultyClass {
    private String day;
    private String room;
    private String time;

    public FacultyClass() {}

    public FacultyClass(String day, String room, String time) {
        this.day = day;
        this.room = room;
        this.time = time;
    }

    public String getDay() {
        return day;
    }

    public String getRoom() {
        return room;
    }

    public String getTime() {
        return time;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setTime(String time) {
        this.time = time;
    }
}


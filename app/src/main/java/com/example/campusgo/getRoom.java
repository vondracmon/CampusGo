package com.example.campusgo;

public class getRoom {
    private String room;
    private String availability;
    private String markedBy;

    public getRoom() {} // Needed for Firebase

    public getRoom(String room, String availability, String markedBy) {
        this.room = room;
        this.availability = availability;
        this.markedBy = markedBy;
    }

    public String getMarkedBy() {
        return markedBy;
    }

    public String getRoom() {
        return room;
    }

    public String getAvailability() {
        return availability;
    }

    public void setMarkedBy(String markedBy) {
        this.markedBy = markedBy;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }
}

package com.example.campusgo;

public class getRoom {
    private String room;
    private String availability;

    public getRoom() {} // Needed for Firebase

    public getRoom(String room, String availability) {
        this.room = room;
        this.availability = availability;
    }

    public String getRoom() {
        return room;
    }

    public String getAvailability() {
        return availability;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }
}

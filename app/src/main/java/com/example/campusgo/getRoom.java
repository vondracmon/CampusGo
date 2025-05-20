package com.example.campusgo;

public class getRoom {
    private String room;
    private String availability;
    private String username;

    public getRoom() {} // Needed for Firebase

    public getRoom(String room, String availability, String username) {
        this.room = room;
        this.availability = availability;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    public String getRoom() {
        return room;
    }
    public String getAvailability() {
        return availability;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setRoom(String room) {
        this.room = room;
    }
    public void setAvailability(String availability) {
        this.availability = availability;
    }
}

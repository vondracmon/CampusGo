package com.example.campusgo;

public class getRoom {
    private String room;
    private String availablity;

    public getRoom(String room, String availablity) {
        this.room = room;
        this.availablity = availablity;
    }

    public String getRoom() {
        return room;
    }

    public String getAvailablity() {
        return availablity;
    }

    public void setAvailablity(String availablity) {
        this.availablity = availablity;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}

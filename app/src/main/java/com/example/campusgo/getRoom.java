package com.example.campusgo;

public class getRoom {
    private String room;
    private String availability;
    private String markedBy;
    private String startTime;
    private String endTime;
    private String markedAt;

    public getRoom() {
    }

    public getRoom(String room, String availability, String markedBy, String startTime, String endTime, String markedAt) {
        this.room = room;
        this.availability = availability;
        this.markedBy = markedBy;
        this.startTime = startTime;
        this.endTime = endTime;
        this.markedAt = markedAt;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getMarkedBy() {
        return markedBy;
    }

    public void setMarkedBy(String markedBy) {
        this.markedBy = markedBy;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(String markedAt) {
        this.markedAt = markedAt;
    }
}

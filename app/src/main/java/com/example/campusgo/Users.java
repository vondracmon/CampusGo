package com.example.campusgo;

public class Users {
    private String username;
    private String studNum;
    private String email;

    public Users() {}

    public Users(String username, String studNum, String email) {
        this.username = username;
        this.studNum = studNum;
        this.email = email;
    }
    public Users(String email) {
        this.username = "Default Username";
        this.studNum = "000000";
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStudNum() {
        return studNum;
    }

    public void setStudNum(String studNum) {
        this.studNum = studNum;
    }

    public String getEmail() {  // Updated method name for consistency
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
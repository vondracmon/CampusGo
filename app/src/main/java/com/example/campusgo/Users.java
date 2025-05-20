package com.example.campusgo;

public class Users {
    private String username;
    private String studNum;
    private String email;
    private String image; // Base64 image string

    public Users() {}

    public Users(String username, String studNum, String email, String image) {
        this.username = username;
        this.studNum = studNum;
        this.email = email;
        this.image = image;
    }

    public Users(String email) {
        this.username = "Default Username";
        this.studNum = "000000";
        this.email = email;
        this.image = ""; // Initialize as empty
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

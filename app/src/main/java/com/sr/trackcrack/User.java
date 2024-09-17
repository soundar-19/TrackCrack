package com.sr.trackcrack;

public class User {
    private String name;
    private String email;

    // Default constructor required for Firestore
    public User() {
    }

    // Constructor with parameters
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

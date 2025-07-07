package com.scheduler.userservice.controller;

// A simple DTO (Data Transfer Object) to handle login credentials
public class LoginRequest {
    private String email;
    private String password;

    // Getters and setters are needed for JSON deserialization
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

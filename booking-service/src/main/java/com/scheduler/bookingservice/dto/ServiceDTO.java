package com.scheduler.bookingservice.dto;

// A simple class to represent a Service object received from the Schedule-Service
public class ServiceDTO {
    private Long id;
    private String name;
    private int durationMinutes;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
}

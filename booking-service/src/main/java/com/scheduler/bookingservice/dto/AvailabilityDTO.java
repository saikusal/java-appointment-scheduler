package com.scheduler.bookingservice.dto;

import java.time.LocalTime;

// A simple class to represent an Availability object received from the Schedule-Service
public class AvailabilityDTO {
    private int dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    // Getters and Setters
    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}

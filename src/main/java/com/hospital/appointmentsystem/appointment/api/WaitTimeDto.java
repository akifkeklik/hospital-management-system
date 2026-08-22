package com.hospital.appointmentsystem.appointment.api;

public class WaitTimeDto {
    private int estimatedMinutes;
    private String busyLevel; // LOW, MEDIUM, HIGH
    private int queuePosition; // Sıra numarası

    public WaitTimeDto() {}

    public WaitTimeDto(int estimatedMinutes, String busyLevel, int queuePosition) {
        this.estimatedMinutes = estimatedMinutes;
        this.busyLevel = busyLevel;
        this.queuePosition = queuePosition;
    }

    // Getters & Setters
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public String getBusyLevel() { return busyLevel; }
    public void setBusyLevel(String busyLevel) { this.busyLevel = busyLevel; }
    public int getQueuePosition() { return queuePosition; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }
}

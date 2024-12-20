package com.healthyoda.web;

public class SessionInfo {
    private final String sessionId;
    private final String patientName;
    private final String doctorName;
    private final String appointmentDateTime;

    public SessionInfo(String sessionId, String patientName, String doctorName, String appointmentDateTime) {
        this.sessionId = sessionId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDateTime = appointmentDateTime;
    }

    // Add getters
} 
package com.healthyoda.web;

public record Appointment(String id, String patientName, String doctorName, String dt, int questionSetId) {
}

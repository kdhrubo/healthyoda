package com.healthyoda.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AppointmentService {

    @Autowired
    private ObjectMapper objectMapper;

    List<Appointment> appointments = new ArrayList<>();

    @PostConstruct
    public void load() {
        try {
            appointments =
            this.objectMapper.readValue(new ClassPathResource("/data/session.json").getInputStream()
                    , new TypeReference<List<Appointment>>() {});

            System.out.println("Loaded {} appointments - " +  appointments);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Appointment findById(String appointmentId) {
        return
                this.appointments.stream()
                        .filter(appointment -> appointment.id().equals(appointmentId))
                        .findFirst()
                        .orElse(null);

    }
}

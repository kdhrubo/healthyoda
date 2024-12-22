package com.healthyoda.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HealthyodaApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthyodaApplication.class, args);
    }

}

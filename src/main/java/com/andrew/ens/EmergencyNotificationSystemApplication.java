package com.andrew.ens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class EmergencyNotificationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmergencyNotificationSystemApplication.class, args);
    }
}

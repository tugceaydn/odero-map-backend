package com.example.oderomapbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OderoMapBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OderoMapBackendApplication.class, args);
    }

}

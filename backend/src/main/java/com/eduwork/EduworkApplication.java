package com.eduwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Eduwork Platform.
 * 
 * Architecture: Modular Monolith
 * - Identity Module: User registration, auth, KYC
 * - Catalog Module: Schedules, subjects
 * - Booking Module: Bookings management
 * - Discussion Module: Pre-booking and in-session chat
 * - Payment Module: Manual payment verification
 * - Notification Module: Email and in-app alerts
 * - Audit Module: Activity logging
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class EduworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduworkApplication.class, args);
    }
}

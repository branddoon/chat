package com.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Entry point for the Chat Spring Boot application.
 */
@SpringBootApplication(exclude = {
UserDetailsServiceAutoConfiguration.class
})
public class ChatApplication {

    /**
     * Bootstraps the Spring application context.
     *
     * @param args command-line arguments forwarded to the Spring context
     */
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}

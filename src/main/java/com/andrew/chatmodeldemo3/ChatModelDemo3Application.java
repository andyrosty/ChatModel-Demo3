package com.andrew.chatmodeldemo3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the ChatModel Demo application.
 * 
 * The @SpringBootApplication annotation is a convenience annotation that adds:
 * - @Configuration: Tags the class as a source of bean definitions
 * - @EnableAutoConfiguration: Tells Spring Boot to add beans based on classpath settings
 * - @ComponentScan: Tells Spring to look for components in the current package and below
 * 
 * This is the entry point for the Spring Boot application.
 */
@SpringBootApplication
public class ChatModelDemo3Application {

    /**
     * The main method that starts the Spring Boot application.
     * 
     * This method uses SpringApplication.run() to bootstrap the application,
     * creating the application context and starting the embedded web server.
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(ChatModelDemo3Application.class, args);
    }

}

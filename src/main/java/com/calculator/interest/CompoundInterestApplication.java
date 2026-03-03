package com.calculator.interest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main entry point for the Compound Interest Calculator application.
 * <p>
 * Enables caching to improve performance on repeated simulation requests.
 * </p>
 */
@SpringBootApplication
@EnableCaching
public class CompoundInterestApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompoundInterestApplication.class, args);
    }
}

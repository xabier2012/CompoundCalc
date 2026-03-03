package com.calculator.interest.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Central application configuration.
 * <p>
 * Registers shared beans such as {@link ModelMapper} and the
 * Caffeine-backed {@link CacheManager} used by the simulation services.
 * </p>
 */
@Configuration
public class AppConfig {

    /**
     * Configures a strict {@link ModelMapper} instance for DTO conversions.
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper;
    }

    /**
     * Caffeine cache spec: max 500 entries, expire 10 min after write.
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats();
    }

    /**
     * Registers a {@link CacheManager} backed by Caffeine for the "simulations" cache.
     */
    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("simulations");
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }
}

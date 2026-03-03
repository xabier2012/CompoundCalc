package com.calculator.interest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for a simple compound interest simulation.
 * <p>
 * Contains only the essential parameters: principal, annual rate,
 * duration in years, and compounding frequency.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleSimulationRequest {

    @NotNull(message = "Initial principal is required")
    @DecimalMin(value = "0.01", message = "Principal must be greater than 0")
    @DecimalMax(value = "999999999.99", message = "Principal exceeds maximum allowed value")
    private Double principal;

    @NotNull(message = "Annual interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be greater than 0")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%")
    private Double annualRate;

    @NotNull(message = "Number of years is required")
    @Min(value = 1, message = "Duration must be at least 1 year")
    @Max(value = 100, message = "Duration cannot exceed 100 years")
    private Integer years;

    @NotNull(message = "Compounding frequency is required")
    @Min(value = 1, message = "Compounding frequency must be at least 1")
    @Max(value = 365, message = "Compounding frequency cannot exceed 365")
    private Integer compoundingFrequency;

    @DecimalMin(value = "0.0", message = "Periodic contribution cannot be negative")
    @DecimalMax(value = "999999.99", message = "Periodic contribution exceeds maximum")
    private Double periodicContribution;
}

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
 * Request DTO for a complex compound interest simulation.
 * <p>
 * Extends the simple parameters with monthly contributions,
 * annual contribution increases, inflation rate, tax rate,
 * and optional Monte Carlo analysis with configurable volatility.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplexSimulationRequest {

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

    @DecimalMin(value = "0.0", message = "Monthly contribution cannot be negative")
    @DecimalMax(value = "999999.99", message = "Monthly contribution exceeds maximum")
    private Double monthlyContribution;

    @DecimalMin(value = "0.0", message = "Annual contribution increase cannot be negative")
    @DecimalMax(value = "50.0", message = "Annual contribution increase cannot exceed 50%")
    private Double annualContributionIncrease;

    @DecimalMin(value = "0.0", message = "Inflation rate cannot be negative")
    @DecimalMax(value = "50.0", message = "Inflation rate cannot exceed 50%")
    private Double inflationRate;

    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
    private Double taxRate;

    @DecimalMin(value = "0.0", message = "Volatility cannot be negative")
    @DecimalMax(value = "100.0", message = "Volatility cannot exceed 100%")
    private Double volatility;

    @Min(value = 0, message = "Number of simulations cannot be negative")
    @Max(value = 10000, message = "Maximum 10,000 Monte Carlo simulations")
    private Integer monteCarloSimulations;
}

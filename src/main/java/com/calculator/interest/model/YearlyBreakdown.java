package com.calculator.interest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single year's data within a compound interest simulation.
 * <p>
 * Stores the starting balance, interest earned, contributions made,
 * ending balance, and cumulative totals. The inflation-adjusted balance
 * is only meaningful when an inflation rate is provided.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearlyBreakdown {

    private int year;
    private double startingBalance;
    private double interestEarned;
    private double contributions;
    private double endingBalance;
    private double cumulativeInterest;
    private double cumulativeContributions;
    private double inflationAdjustedBalance;
}

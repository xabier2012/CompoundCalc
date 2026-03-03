package com.calculator.interest.dto;

import com.calculator.interest.model.YearlyBreakdown;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO containing the full simulation output.
 * <p>
 * Includes summary metrics, year-by-year breakdowns, and
 * optional Monte Carlo analysis results for complex simulations.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResponse {

    private double initialPrincipal;
    private double finalAmount;
    private double totalInterestEarned;
    private double totalContributions;
    private double effectiveAnnualRate;
    private double realReturnRate;
    private double afterTaxFinalAmount;

    private List<YearlyBreakdown> yearlyBreakdowns;

    /* Monte Carlo results — populated only for complex simulations */
    private Double monteCarloBestCase;
    private Double monteCarloWorstCase;
    private Double monteCarloMedian;
    private Double monteCarloStdDev;
    private List<Double> monteCarloPercentiles;

    private String simulationType;
}

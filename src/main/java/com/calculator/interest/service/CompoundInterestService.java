package com.calculator.interest.service;

import com.calculator.interest.dto.ComplexSimulationRequest;
import com.calculator.interest.dto.SimpleSimulationRequest;
import com.calculator.interest.dto.SimulationResponse;
import com.calculator.interest.model.YearlyBreakdown;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Core service responsible for compound interest calculations.
 * <p>
 * Supports two modes:
 * <ul>
 *   <li><b>Simple</b> – classic formula A = P(1 + r/n)^(nt)</li>
 *   <li><b>Complex</b> – adds periodic contributions, contribution growth,
 *       inflation adjustment, tax impact, and optional Monte Carlo analysis</li>
 * </ul>
 * All monetary results are rounded to two decimal places using
 * {@link RoundingMode#HALF_UP}.
 * </p>
 */
@Service
public class CompoundInterestService {

    private static final Logger log = LoggerFactory.getLogger(CompoundInterestService.class);
    private static final int SCALE = 2;

    /**
     * Calculates simple compound interest year by year.
     *
     * @param request validated simple simulation parameters
     * @return full simulation response with yearly breakdowns
     */
    public SimulationResponse calculateSimple(SimpleSimulationRequest request) {
        Preconditions.checkNotNull(request, "Simulation request must not be null");
        Preconditions.checkArgument(request.getPrincipal() > 0, "Principal must be positive");

        log.info("Simple simulation: principal={}, rate={}%, years={}, freq={}, contrib={}",
                request.getPrincipal(), request.getAnnualRate(),
                request.getYears(), request.getCompoundingFrequency(),
                request.getPeriodicContribution());

        double principal       = request.getPrincipal();
        double rate            = request.getAnnualRate() / 100.0;
        int    years           = request.getYears();
        int    n               = request.getCompoundingFrequency();
        double periodicContrib = defaultZero(request.getPeriodicContribution());

        List<YearlyBreakdown> breakdowns = new ArrayList<>();
        double cumulativeInterest      = 0.0;
        double cumulativeContributions = 0.0;
        double currentBalance          = principal;

        for (int year = 1; year <= years; year++) {
            double startBalance      = currentBalance;
            double yearInterest      = 0.0;
            double yearContributions = 0.0;

            // Apply n compounding periods per year
            for (int period = 0; period < n; period++) {
                // Add periodic contribution at the start of each period
                currentBalance   += periodicContrib;
                yearContributions += periodicContrib;

                // Compound interest for this period
                double periodInterest = currentBalance * (rate / n);
                yearInterest   += periodInterest;
                currentBalance += periodInterest;
            }

            cumulativeInterest      += yearInterest;
            cumulativeContributions += yearContributions;

            breakdowns.add(YearlyBreakdown.builder()
                    .year(year)
                    .startingBalance(round(startBalance))
                    .interestEarned(round(yearInterest))
                    .contributions(round(yearContributions))
                    .endingBalance(round(currentBalance))
                    .cumulativeInterest(round(cumulativeInterest))
                    .cumulativeContributions(round(cumulativeContributions))
                    .inflationAdjustedBalance(round(currentBalance))
                    .build());
        }

        double effectiveRate = Math.pow(1.0 + rate / n, n) - 1.0;

        return SimulationResponse.builder()
                .initialPrincipal(principal)
                .finalAmount(round(currentBalance))
                .totalInterestEarned(round(cumulativeInterest))
                .totalContributions(round(cumulativeContributions))
                .effectiveAnnualRate(round(effectiveRate * 100))
                .realReturnRate(round(rate * 100))
                .afterTaxFinalAmount(round(currentBalance))
                .yearlyBreakdowns(ImmutableList.copyOf(breakdowns))
                .simulationType("SIMPLE")
                .build();
    }

    /**
     * Calculates compound interest with periodic contributions,
     * inflation adjustment, tax, and optional Monte Carlo analysis.
     *
     * @param request validated complex simulation parameters
     * @return full simulation response with yearly breakdowns and Monte Carlo data
     */
    public SimulationResponse calculateComplex(ComplexSimulationRequest request) {
        Preconditions.checkNotNull(request, "Simulation request must not be null");

        log.info("Complex simulation: principal={}, rate={}%, years={}, monthly={}",
                request.getPrincipal(), request.getAnnualRate(),
                request.getYears(), request.getMonthlyContribution());

        double principal        = request.getPrincipal();
        double rate             = request.getAnnualRate() / 100.0;
        int    years            = request.getYears();
        int    n                = request.getCompoundingFrequency();
        double monthlyContrib   = defaultZero(request.getMonthlyContribution());
        double annualIncrease   = defaultZero(request.getAnnualContributionIncrease()) / 100.0;
        double inflation        = defaultZero(request.getInflationRate()) / 100.0;
        double taxRate          = defaultZero(request.getTaxRate()) / 100.0;

        List<YearlyBreakdown> breakdowns = new ArrayList<>();
        double currentBalance         = principal;
        double cumulativeInterest     = 0.0;
        double cumulativeContributions = 0.0;
        double currentMonthlyContrib  = monthlyContrib;

        for (int year = 1; year <= years; year++) {
            double startBalance      = currentBalance;
            double yearInterest      = 0.0;
            double yearContributions = 0.0;

            for (int month = 1; month <= 12; month++) {
                // Periodic contribution
                currentBalance   += currentMonthlyContrib;
                yearContributions += currentMonthlyContrib;

                // Compound interest application
                if (n >= 12) {
                    double periodsThisMonth = (double) n / 12.0;
                    for (int p = 0; p < Math.max(1, Math.round(periodsThisMonth)); p++) {
                        double periodInterest = currentBalance * (rate / n);
                        yearInterest   += periodInterest;
                        currentBalance += periodInterest;
                    }
                } else {
                    double monthsPerPeriod = 12.0 / n;
                    if (month % Math.max(1, Math.round(monthsPerPeriod)) == 0) {
                        double periodInterest = currentBalance * (rate / n);
                        yearInterest   += periodInterest;
                        currentBalance += periodInterest;
                    }
                }
            }

            cumulativeInterest     += yearInterest;
            cumulativeContributions += yearContributions;

            double inflationFactor    = Math.pow(1.0 + inflation, year);
            double inflationAdjusted  = currentBalance / inflationFactor;

            breakdowns.add(YearlyBreakdown.builder()
                    .year(year)
                    .startingBalance(round(startBalance))
                    .interestEarned(round(yearInterest))
                    .contributions(round(yearContributions))
                    .endingBalance(round(currentBalance))
                    .cumulativeInterest(round(cumulativeInterest))
                    .cumulativeContributions(round(cumulativeContributions))
                    .inflationAdjustedBalance(round(inflationAdjusted))
                    .build());

            // Increase monthly contribution for the next year
            currentMonthlyContrib *= (1.0 + annualIncrease);
        }

        // After-tax calculation
        double totalGain       = currentBalance - principal - cumulativeContributions;
        double taxOnGain       = totalGain > 0 ? totalGain * taxRate : 0.0;
        double afterTaxAmount  = currentBalance - taxOnGain;

        // Effective annual rate & real (inflation-adjusted) rate
        double effectiveRate = Math.pow(1.0 + rate / n, n) - 1.0;
        double realRate      = ((1.0 + effectiveRate) / (1.0 + inflation)) - 1.0;

        SimulationResponse.SimulationResponseBuilder builder = SimulationResponse.builder()
                .initialPrincipal(principal)
                .finalAmount(round(currentBalance))
                .totalInterestEarned(round(cumulativeInterest))
                .totalContributions(round(cumulativeContributions))
                .effectiveAnnualRate(round(effectiveRate * 100))
                .realReturnRate(round(realRate * 100))
                .afterTaxFinalAmount(round(afterTaxAmount))
                .yearlyBreakdowns(ImmutableList.copyOf(breakdowns))
                .simulationType("COMPLEX");

        // Monte Carlo analysis
        if (request.getMonteCarloSimulations() != null && request.getMonteCarloSimulations() > 0) {
            double vol = request.getVolatility() != null
                    ? request.getVolatility() / 100.0
                    : rate * 0.30;
            runMonteCarloSimulation(builder, request, vol);
        }

        return builder.build();
    }

    /**
     * Runs a Monte Carlo simulation using normally distributed annual returns.
     * Results (5th, 10th, 25th, 50th, 75th, 90th, 95th percentiles) are
     * appended to the response builder.
     */
    private void runMonteCarloSimulation(
            SimulationResponse.SimulationResponseBuilder builder,
            ComplexSimulationRequest request,
            double volatility) {

        log.info("Monte Carlo: {} iterations, volatility={}", request.getMonteCarloSimulations(), volatility);

        NormalDistribution normalDist = new NormalDistribution(
                request.getAnnualRate() / 100.0, volatility);
        DescriptiveStatistics stats = new DescriptiveStatistics();

        int    simulations     = request.getMonteCarloSimulations();
        double principal       = request.getPrincipal();
        int    years           = request.getYears();
        int    n               = request.getCompoundingFrequency();
        double monthlyContrib  = defaultZero(request.getMonthlyContribution());
        double annualIncrease  = defaultZero(request.getAnnualContributionIncrease()) / 100.0;

        for (int sim = 0; sim < simulations; sim++) {
            double balance       = principal;
            double currentContrib = monthlyContrib;

            for (int year = 1; year <= years; year++) {
                double yearRate = Math.max(normalDist.sample(), -0.50);

                for (int month = 1; month <= 12; month++) {
                    balance += currentContrib;
                    balance *= Math.pow(1.0 + yearRate / n, (double) n / 12.0);
                }

                currentContrib *= (1.0 + annualIncrease);
            }

            stats.addValue(balance);
        }

        builder.monteCarloBestCase(round(stats.getPercentile(95)))
               .monteCarloWorstCase(round(stats.getPercentile(5)))
               .monteCarloMedian(round(stats.getPercentile(50)))
               .monteCarloStdDev(round(stats.getStandardDeviation()))
               .monteCarloPercentiles(ImmutableList.of(
                       round(stats.getPercentile(10)),
                       round(stats.getPercentile(25)),
                       round(stats.getPercentile(50)),
                       round(stats.getPercentile(75)),
                       round(stats.getPercentile(90))
               ));
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double defaultZero(Double value) {
        return value != null ? value : 0.0;
    }
}

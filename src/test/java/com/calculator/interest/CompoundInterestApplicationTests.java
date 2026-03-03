package com.calculator.interest;

import com.calculator.interest.dto.ComplexSimulationRequest;
import com.calculator.interest.dto.SimpleSimulationRequest;
import com.calculator.interest.dto.SimulationResponse;
import com.calculator.interest.service.CompoundInterestService;
import com.calculator.interest.service.ExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration and unit tests for the Compound Interest Calculator.
 */
@SpringBootTest
class CompoundInterestApplicationTests {

    @Autowired
    private CompoundInterestService calculatorService;

    @Autowired
    private ExportService exportService;

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
        assertNotNull(calculatorService);
        assertNotNull(exportService);
    }

    @Nested
    @DisplayName("Simple Simulation Tests")
    class SimpleSimulationTests {

        @Test
        @DisplayName("10000 at 5% for 10 years compounded monthly produces correct result")
        void testBasicSimpleCalculation() {
            SimpleSimulationRequest request = SimpleSimulationRequest.builder()
                    .principal(10000.0)
                    .annualRate(5.0)
                    .years(10)
                    .compoundingFrequency(12)
                    .build();

            SimulationResponse response = calculatorService.calculateSimple(request);

            assertNotNull(response);
            assertEquals("SIMPLE", response.getSimulationType());
            assertEquals(10000.0, response.getInitialPrincipal());
            assertTrue(response.getFinalAmount() > 16000.0, "Final amount should exceed $16,000");
            assertTrue(response.getFinalAmount() < 17000.0, "Final amount should be below $17,000");
            assertEquals(10, response.getYearlyBreakdowns().size());
            assertEquals(0.0, response.getTotalContributions());
        }

        @Test
        @DisplayName("Yearly breakdown is chronologically ordered")
        void testBreakdownOrder() {
            SimpleSimulationRequest request = SimpleSimulationRequest.builder()
                    .principal(5000.0)
                    .annualRate(8.0)
                    .years(5)
                    .compoundingFrequency(4)
                    .build();

            SimulationResponse response = calculatorService.calculateSimple(request);

            for (int i = 0; i < response.getYearlyBreakdowns().size(); i++) {
                assertEquals(i + 1, response.getYearlyBreakdowns().get(i).getYear());
            }
        }

        @Test
        @DisplayName("Annual compounding matches formula exactly")
        void testAnnualCompounding() {
            SimpleSimulationRequest request = SimpleSimulationRequest.builder()
                    .principal(1000.0)
                    .annualRate(10.0)
                    .years(3)
                    .compoundingFrequency(1)
                    .build();

            SimulationResponse response = calculatorService.calculateSimple(request);

            // A = 1000 * (1 + 0.10)^3 = 1331.00
            assertEquals(1331.0, response.getFinalAmount(), 0.01);
        }
    }

    @Nested
    @DisplayName("Complex Simulation Tests")
    class ComplexSimulationTests {

        @Test
        @DisplayName("Complex simulation with contributions produces higher final amount")
        void testWithContributions() {
            ComplexSimulationRequest request = ComplexSimulationRequest.builder()
                    .principal(10000.0)
                    .annualRate(7.0)
                    .years(20)
                    .compoundingFrequency(12)
                    .monthlyContribution(500.0)
                    .annualContributionIncrease(0.0)
                    .inflationRate(0.0)
                    .taxRate(0.0)
                    .monteCarloSimulations(0)
                    .build();

            SimulationResponse response = calculatorService.calculateComplex(request);

            assertNotNull(response);
            assertEquals("COMPLEX", response.getSimulationType());
            assertTrue(response.getFinalAmount() > 200000.0, "With $500/month over 20 years at 7%, should exceed $200k");
            assertTrue(response.getTotalContributions() > 0);
            assertEquals(20, response.getYearlyBreakdowns().size());
        }

        @Test
        @DisplayName("Inflation adjustment reduces real balance")
        void testInflationAdjustment() {
            ComplexSimulationRequest request = ComplexSimulationRequest.builder()
                    .principal(10000.0)
                    .annualRate(5.0)
                    .years(10)
                    .compoundingFrequency(12)
                    .monthlyContribution(0.0)
                    .annualContributionIncrease(0.0)
                    .inflationRate(3.0)
                    .taxRate(0.0)
                    .monteCarloSimulations(0)
                    .build();

            SimulationResponse response = calculatorService.calculateComplex(request);

            var lastBreakdown = response.getYearlyBreakdowns().get(response.getYearlyBreakdowns().size() - 1);
            assertTrue(lastBreakdown.getInflationAdjustedBalance() < lastBreakdown.getEndingBalance(),
                    "Inflation-adjusted balance should be lower than nominal");
        }

        @Test
        @DisplayName("Monte Carlo produces valid percentile data")
        void testMonteCarloSimulation() {
            ComplexSimulationRequest request = ComplexSimulationRequest.builder()
                    .principal(10000.0)
                    .annualRate(7.0)
                    .years(10)
                    .compoundingFrequency(12)
                    .monthlyContribution(200.0)
                    .annualContributionIncrease(0.0)
                    .inflationRate(0.0)
                    .taxRate(0.0)
                    .volatility(15.0)
                    .monteCarloSimulations(500)
                    .build();

            SimulationResponse response = calculatorService.calculateComplex(request);

            assertNotNull(response.getMonteCarloMedian());
            assertNotNull(response.getMonteCarloBestCase());
            assertNotNull(response.getMonteCarloWorstCase());
            assertNotNull(response.getMonteCarloPercentiles());
            assertEquals(5, response.getMonteCarloPercentiles().size());
            assertTrue(response.getMonteCarloWorstCase() < response.getMonteCarloBestCase(),
                    "Worst case must be less than best case");
        }

        @Test
        @DisplayName("Tax rate reduces after-tax final amount")
        void testTaxImpact() {
            ComplexSimulationRequest request = ComplexSimulationRequest.builder()
                    .principal(10000.0)
                    .annualRate(7.0)
                    .years(10)
                    .compoundingFrequency(12)
                    .monthlyContribution(0.0)
                    .annualContributionIncrease(0.0)
                    .inflationRate(0.0)
                    .taxRate(25.0)
                    .monteCarloSimulations(0)
                    .build();

            SimulationResponse response = calculatorService.calculateComplex(request);

            assertTrue(response.getAfterTaxFinalAmount() < response.getFinalAmount(),
                    "After-tax amount should be less than pre-tax amount");
            assertTrue(response.getAfterTaxFinalAmount() > response.getInitialPrincipal(),
                    "After-tax amount should still exceed principal");
        }
    }

    @Nested
    @DisplayName("Export Service Tests")
    class ExportTests {

        private SimulationResponse createSampleResponse() {
            return calculatorService.calculateSimple(SimpleSimulationRequest.builder()
                    .principal(5000.0)
                    .annualRate(6.0)
                    .years(3)
                    .compoundingFrequency(12)
                    .build());
        }

        @Test
        @DisplayName("CSV export produces non-empty bytes")
        void testCsvExport() throws Exception {
            byte[] csv = exportService.exportToCsv(createSampleResponse());
            assertNotNull(csv);
            assertTrue(csv.length > 0);
            String content = new String(csv);
            assertTrue(content.contains("Year"), "CSV should contain header row");
        }

        @Test
        @DisplayName("Excel export produces non-empty bytes")
        void testExcelExport() throws Exception {
            byte[] xlsx = exportService.exportToExcel(createSampleResponse());
            assertNotNull(xlsx);
            assertTrue(xlsx.length > 0);
        }

        @Test
        @DisplayName("PDF export produces non-empty bytes")
        void testPdfExport() throws Exception {
            byte[] pdf = exportService.exportToPdf(createSampleResponse());
            assertNotNull(pdf);
            assertTrue(pdf.length > 0);
        }

        @Test
        @DisplayName("JSON export produces valid JSON string")
        void testJsonExport() {
            byte[] json = exportService.exportToJson(createSampleResponse());
            assertNotNull(json);
            String content = new String(json);
            assertTrue(content.contains("initialPrincipal"));
            assertTrue(content.contains("yearlyBreakdowns"));
        }
    }
}

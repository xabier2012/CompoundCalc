package com.calculator.interest.controller;

import com.calculator.interest.dto.ComplexSimulationRequest;
import com.calculator.interest.dto.SimpleSimulationRequest;
import com.calculator.interest.dto.SimulationResponse;
import com.calculator.interest.service.CompoundInterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller for programmatic access to compound interest calculations.
 * <p>
 * All endpoints are documented via SpringDoc OpenAPI and accessible through
 * the Swagger UI at {@code /swagger-ui.html}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Compound Interest API", description = "REST API for compound interest calculations")
public class ApiController {

    private final CompoundInterestService calculatorService;

    public ApiController(CompoundInterestService calculatorService) {
        this.calculatorService = calculatorService;
    }

    /**
     * Calculates simple compound interest via JSON request.
     */
    @PostMapping("/simple")
    @Operation(summary = "Calculate simple compound interest",
               description = "Computes compound interest using the standard formula A = P(1+r/n)^(nt)")
    public ResponseEntity<SimulationResponse> calculateSimple(
            @Valid @RequestBody SimpleSimulationRequest request) {
        return ResponseEntity.ok(calculatorService.calculateSimple(request));
    }

    /**
     * Calculates complex compound interest with contributions, inflation,
     * taxes, and optional Monte Carlo analysis.
     */
    @PostMapping("/complex")
    @Operation(summary = "Calculate complex compound interest",
               description = "Includes contributions, inflation, tax, and optional Monte Carlo simulation")
    public ResponseEntity<SimulationResponse> calculateComplex(
            @Valid @RequestBody ComplexSimulationRequest request) {
        return ResponseEntity.ok(calculatorService.calculateComplex(request));
    }
}

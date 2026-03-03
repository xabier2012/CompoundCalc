package com.calculator.interest.controller;

import com.calculator.interest.dto.ComplexSimulationRequest;
import com.calculator.interest.dto.SimpleSimulationRequest;
import com.calculator.interest.dto.SimulationResponse;
import com.calculator.interest.service.CompoundInterestService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Main web controller for the Compound Interest Calculator.
 * <p>
 * Serves Thymeleaf views for the landing page, simple simulation,
 * and complex simulation. Form submissions are validated and the
 * results (including chart data) are returned on the same page.
 * </p>
 */
@Controller
public class CalculatorController {

    private static final Logger log = LoggerFactory.getLogger(CalculatorController.class);

    private final CompoundInterestService calculatorService;

    public CalculatorController(CompoundInterestService calculatorService) {
        this.calculatorService = calculatorService;
    }

    /**
     * Renders the landing page.
     *
     * @return the index view name
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Displays the simple simulation form with sensible default values.
     */
    @GetMapping("/simple")
    public String showSimpleForm(Model model) {
        model.addAttribute("request", SimpleSimulationRequest.builder()
                .principal(10000.0)
                .annualRate(5.0)
                .years(10)
                .compoundingFrequency(12)
                .periodicContribution(100.0)
                .build());
        return "simple";
    }

    /**
     * Processes the simple simulation form, validates inputs,
     * and returns the results on the same page.
     */
    @PostMapping("/simple")
    public String calculateSimple(
            @Valid @ModelAttribute("request") SimpleSimulationRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in simple simulation: {}", bindingResult.getAllErrors());
            return "simple";
        }

        SimulationResponse response = calculatorService.calculateSimple(request);
        model.addAttribute("result", response);
        model.addAttribute("request", request);
        return "simple";
    }

    /**
     * Displays the complex simulation form with sensible default values.
     */
    @GetMapping("/complex")
    public String showComplexForm(Model model) {
        model.addAttribute("request", ComplexSimulationRequest.builder()
                .principal(10000.0)
                .annualRate(7.0)
                .years(20)
                .compoundingFrequency(12)
                .monthlyContribution(500.0)
                .annualContributionIncrease(2.0)
                .inflationRate(2.5)
                .taxRate(20.0)
                .volatility(15.0)
                .monteCarloSimulations(1000)
                .build());
        return "complex";
    }

    /**
     * Processes the complex simulation form, validates inputs,
     * and returns the results (including Monte Carlo data) on the same page.
     */
    @PostMapping("/complex")
    public String calculateComplex(
            @Valid @ModelAttribute("request") ComplexSimulationRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in complex simulation: {}", bindingResult.getAllErrors());
            return "complex";
        }

        SimulationResponse response = calculatorService.calculateComplex(request);
        model.addAttribute("result", response);
        model.addAttribute("request", request);
        return "complex";
    }
}

package com.calculator.interest.controller;

import com.calculator.interest.dto.ComplexSimulationRequest;
import com.calculator.interest.dto.SimpleSimulationRequest;
import com.calculator.interest.dto.SimulationResponse;
import com.calculator.interest.service.CompoundInterestService;
import com.calculator.interest.service.ExportService;
import com.itextpdf.text.DocumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * REST controller responsible for exporting simulation results
 * to downloadable file formats (CSV, Excel, PDF, JSON).
 */
@RestController
@RequestMapping("/api/v1/export")
@Tag(name = "Export API", description = "Download simulation results in various formats")
public class ExportController {

    private final ExportService exportService;
    private final CompoundInterestService calculatorService;

    public ExportController(ExportService exportService, CompoundInterestService calculatorService) {
        this.exportService = exportService;
        this.calculatorService = calculatorService;
    }

    @PostMapping("/csv/simple")
    @Operation(summary = "Export simple simulation to CSV")
    public ResponseEntity<byte[]> exportSimpleCsv(
            @Valid @RequestBody SimpleSimulationRequest request) throws IOException {
        SimulationResponse response = calculatorService.calculateSimple(request);
        byte[] data = exportService.exportToCsv(response);
        return buildFileResponse(data, "simulation.csv", "text/csv");
    }

    @PostMapping("/excel/simple")
    @Operation(summary = "Export simple simulation to Excel")
    public ResponseEntity<byte[]> exportSimpleExcel(
            @Valid @RequestBody SimpleSimulationRequest request) throws IOException {
        SimulationResponse response = calculatorService.calculateSimple(request);
        byte[] data = exportService.exportToExcel(response);
        return buildFileResponse(data, "simulation.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @PostMapping("/pdf/simple")
    @Operation(summary = "Export simple simulation to PDF")
    public ResponseEntity<byte[]> exportSimplePdf(
            @Valid @RequestBody SimpleSimulationRequest request) throws IOException, DocumentException {
        SimulationResponse response = calculatorService.calculateSimple(request);
        byte[] data = exportService.exportToPdf(response);
        return buildFileResponse(data, "simulation.pdf", "application/pdf");
    }

    @PostMapping("/json/simple")
    @Operation(summary = "Export simple simulation to JSON")
    public ResponseEntity<byte[]> exportSimpleJson(
            @Valid @RequestBody SimpleSimulationRequest request) {
        SimulationResponse response = calculatorService.calculateSimple(request);
        byte[] data = exportService.exportToJson(response);
        return buildFileResponse(data, "simulation.json", "application/json");
    }

    @PostMapping("/csv/complex")
    @Operation(summary = "Export complex simulation to CSV")
    public ResponseEntity<byte[]> exportComplexCsv(
            @Valid @RequestBody ComplexSimulationRequest request) throws IOException {
        SimulationResponse response = calculatorService.calculateComplex(request);
        byte[] data = exportService.exportToCsv(response);
        return buildFileResponse(data, "simulation_complex.csv", "text/csv");
    }

    @PostMapping("/excel/complex")
    @Operation(summary = "Export complex simulation to Excel")
    public ResponseEntity<byte[]> exportComplexExcel(
            @Valid @RequestBody ComplexSimulationRequest request) throws IOException {
        SimulationResponse response = calculatorService.calculateComplex(request);
        byte[] data = exportService.exportToExcel(response);
        return buildFileResponse(data, "simulation_complex.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @PostMapping("/pdf/complex")
    @Operation(summary = "Export complex simulation to PDF")
    public ResponseEntity<byte[]> exportComplexPdf(
            @Valid @RequestBody ComplexSimulationRequest request) throws IOException, DocumentException {
        SimulationResponse response = calculatorService.calculateComplex(request);
        byte[] data = exportService.exportToPdf(response);
        return buildFileResponse(data, "simulation_complex.pdf", "application/pdf");
    }

    /**
     * Builds an HTTP response with the given binary payload and content-disposition header.
     */
    private ResponseEntity<byte[]> buildFileResponse(byte[] data, String filename, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(data.length);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}

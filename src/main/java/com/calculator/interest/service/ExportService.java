package com.calculator.interest.service;

import com.calculator.interest.dto.SimulationResponse;
import com.calculator.interest.model.YearlyBreakdown;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Service that exports {@link SimulationResponse} data to multiple file formats.
 * <p>
 * Supported formats:
 * <ul>
 *   <li><b>CSV</b>  – via Apache Commons CSV</li>
 *   <li><b>XLSX</b> – via Apache POI</li>
 *   <li><b>PDF</b>  – via iText 5</li>
 *   <li><b>JSON</b> – via Google Gson</li>
 * </ul>
 * </p>
 */
@Service
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private static final String[] BREAKDOWN_HEADERS = {
            "Year", "Starting Balance", "Interest Earned", "Contributions",
            "Ending Balance", "Cumulative Interest", "Cumulative Contributions",
            "Inflation Adjusted Balance"
    };

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Serialises the simulation result into RFC-4180 compliant CSV bytes.
     */
    public byte[] exportToCsv(SimulationResponse response) throws IOException {
        log.info("Exporting simulation results to CSV");

        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer,
                CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(BREAKDOWN_HEADERS).build())) {

            for (YearlyBreakdown b : response.getYearlyBreakdowns()) {
                printer.printRecord(
                        b.getYear(),
                        b.getStartingBalance(),
                        b.getInterestEarned(),
                        b.getContributions(),
                        b.getEndingBalance(),
                        b.getCumulativeInterest(),
                        b.getCumulativeContributions(),
                        b.getInflationAdjustedBalance()
                );
            }
        }

        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Produces an XLSX workbook with a summary section and a yearly-breakdown table.
     */
    public byte[] exportToExcel(SimulationResponse response) throws IOException {
        log.info("Exporting simulation results to Excel");

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Compound Interest Simulation");

            // --- header style ---
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // --- summary ---
            createTextCell(sheet.createRow(0), 0, "Simulation Summary", headerStyle);
            createSummaryRow(sheet, 1, "Initial Principal", response.getInitialPrincipal());
            createSummaryRow(sheet, 2, "Final Amount", response.getFinalAmount());
            createSummaryRow(sheet, 3, "Total Interest Earned", response.getTotalInterestEarned());
            createSummaryRow(sheet, 4, "Total Contributions", response.getTotalContributions());
            createSummaryRow(sheet, 5, "Effective Annual Rate (%)", response.getEffectiveAnnualRate());
            createSummaryRow(sheet, 6, "After-Tax Final Amount", response.getAfterTaxFinalAmount());

            // --- breakdown header ---
            int startRow = 8;
            Row breakdownHeader = sheet.createRow(startRow);
            for (int i = 0; i < BREAKDOWN_HEADERS.length; i++) {
                createTextCell(breakdownHeader, i, BREAKDOWN_HEADERS[i], headerStyle);
            }

            // --- breakdown data ---
            CellStyle numStyle = workbook.createCellStyle();
            DataFormat fmt = workbook.createDataFormat();
            numStyle.setDataFormat(fmt.getFormat("#,##0.00"));

            int rowNum = startRow + 1;
            for (YearlyBreakdown b : response.getYearlyBreakdowns()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(b.getYear());
                createNumericCell(row, 1, b.getStartingBalance(), numStyle);
                createNumericCell(row, 2, b.getInterestEarned(), numStyle);
                createNumericCell(row, 3, b.getContributions(), numStyle);
                createNumericCell(row, 4, b.getEndingBalance(), numStyle);
                createNumericCell(row, 5, b.getCumulativeInterest(), numStyle);
                createNumericCell(row, 6, b.getCumulativeContributions(), numStyle);
                createNumericCell(row, 7, b.getInflationAdjustedBalance(), numStyle);
            }

            for (int i = 0; i < BREAKDOWN_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Generates a landscape-oriented PDF report with summary and breakdown table.
     */
    public byte[] exportToPdf(SimulationResponse response) throws IOException, DocumentException {
        log.info("Exporting simulation results to PDF");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);
        document.open();
        try {
            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Compound Interest Simulation Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Summary
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            document.add(new Paragraph("Summary", sectionFont));
            document.add(new Paragraph(String.format("Initial Principal: $%,.2f", response.getInitialPrincipal())));
            document.add(new Paragraph(String.format("Final Amount: $%,.2f", response.getFinalAmount())));
            document.add(new Paragraph(String.format("Total Interest Earned: $%,.2f", response.getTotalInterestEarned())));
            document.add(new Paragraph(String.format("Total Contributions: $%,.2f", response.getTotalContributions())));
            document.add(new Paragraph(String.format("Effective Annual Rate: %.2f%%", response.getEffectiveAnnualRate())));
            document.add(new Paragraph(String.format("After-Tax Final Amount: $%,.2f", response.getAfterTaxFinalAmount())));
            document.add(new Paragraph(" "));

            // Table
            PdfPTable table = new PdfPTable(BREAKDOWN_HEADERS.length);
            table.setWidthPercentage(100);

            Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE);
            for (String header : BREAKDOWN_HEADERS) {
                PdfPCell cell = new PdfPCell(new Phrase(header, thFont));
                cell.setBackgroundColor(new BaseColor(52, 73, 94));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            Font tdFont = FontFactory.getFont(FontFactory.HELVETICA, 7);
            for (YearlyBreakdown b : response.getYearlyBreakdowns()) {
                table.addCell(new Phrase(String.valueOf(b.getYear()), tdFont));
                table.addCell(new Phrase(String.format("$%,.2f", b.getStartingBalance()), tdFont));
                table.addCell(new Phrase(String.format("$%,.2f", b.getInterestEarned()), tdFont));
                table.addCell(new Phrase(String.format("$%,.2f", b.getContributions()), tdFont));
                table.addCell(new Phrase(String.format("$%,.2f", b.getEndingBalance()), tdFont));
                table.addCell(new Phrase(String.format("$%,.2f", b.getCumulativeInterest()), tdFont));
                table.addCell(new Phrase(String.format("$%,.2f", b.getCumulativeContributions()), tdFont));
                table.addCell(new Phrase(String.format("$%,.2f", b.getInflationAdjustedBalance()), tdFont));
            }

            document.add(table);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return baos.toByteArray();
    }

    /**
     * Serialises the full simulation response as pretty-printed JSON.
     */
    public byte[] exportToJson(SimulationResponse response) {
        log.info("Exporting simulation results to JSON");
        return GSON.toJson(response).getBytes(StandardCharsets.UTF_8);
    }

    private void createTextCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createNumericCell(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createSummaryRow(Sheet sheet, int rowNum, String label, double value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }
}

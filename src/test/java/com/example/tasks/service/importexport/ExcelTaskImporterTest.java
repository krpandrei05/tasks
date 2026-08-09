package com.example.tasks.service.importexport;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelTaskImporterTest {

    private final ExcelTaskImporter importer = new ExcelTaskImporter();

    private static final String[] HEADERS = {
            "taskName", "statusName", "username", "dueDate", "createdBy", "creationDate"
    };

    private byte[] buildWorkbook(String taskName, String statusName, String username,
                                 LocalDateTime dueDate, String createdBy, LocalDateTime creationDate) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Tasks");

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                headerRow.createCell(col).setCellValue(HEADERS[col]);
            }

            Row dataRow = sheet.createRow(1);
            if (taskName != null) dataRow.createCell(0).setCellValue(taskName);
            if (statusName != null) dataRow.createCell(1).setCellValue(statusName);
            if (username != null) dataRow.createCell(2).setCellValue(username);
            if (dueDate != null) dataRow.createCell(3).setCellValue(dueDate);
            if (createdBy != null) dataRow.createCell(4).setCellValue(createdBy);
            if (creationDate != null) dataRow.createCell(5).setCellValue(creationDate);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Test
    void parse_returnsRowsWithAllFieldsParsed() throws IOException {
        byte[] fileContent = buildWorkbook(
                "Write handover", "Pending", "andrei",
                LocalDateTime.of(2026, 8, 10, 12, 0),
                "admin@example.com",
                LocalDateTime.of(2026, 8, 1, 9, 0)
        );

        List<TaskFileRow> rows = importer.parse(fileContent);

        assertEquals(1, rows.size());
        TaskFileRow row = rows.get(0);
        assertEquals("Write handover", row.getTaskName());
        assertEquals("andrei", row.getUsername());
        assertEquals(LocalDateTime.of(2026, 8, 10, 12, 0), row.getDueDate());
        assertEquals("admin@example.com", row.getCreatedBy());
    }

    @Test
    void parse_throwsRuntimeException_whenRequiredStringFieldIsMissing() throws IOException {
        byte[] fileContent = buildWorkbook(
                null, "Pending", "andrei",
                LocalDateTime.of(2026, 8, 10, 12, 0), "admin@example.com",
                LocalDateTime.of(2026, 8, 1, 9, 0)
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> importer.parse(fileContent));

        assertTrue(exception.getMessage().contains("taskName"));
    }

    @Test
    void parse_throwsRuntimeException_whenDueDateCellIsNotNumeric() throws IOException {
        byte[] fileContent = buildWorkbook(
                "Write handover", "Pending", "andrei",
                null, "admin@example.com",
                LocalDateTime.of(2026, 8, 1, 9, 0)
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> importer.parse(fileContent));

        assertTrue(exception.getMessage().contains("dueDate"));
    }

    @Test
    void parse_allowsMissingCreatedBy() throws IOException {
        byte[] fileContent = buildWorkbook(
                "Write handover", "Pending", "andrei",
                LocalDateTime.of(2026, 8, 10, 12, 0), null,
                LocalDateTime.of(2026, 8, 1, 9, 0)
        );

        List<TaskFileRow> rows = importer.parse(fileContent);

        assertEquals("", rows.get(0).getCreatedBy());
    }

    @Test
    void supports_returnsExcelFormat() {
        assertEquals(FileFormat.EXCEL, importer.supports());
    }
}
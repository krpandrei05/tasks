package com.example.tasks.service.importexport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelTaskExporterTest {

    private final ExcelTaskExporter exporter = new ExcelTaskExporter();

    @Test
    void export_writesHeaderAndRowValues() throws IOException {
        TaskFileRow row = TaskFileRow.builder()
                .taskName("Write handover")
                .statusName("Pending")
                .username("andrei")
                .dueDate(LocalDateTime.of(2026, 8, 10, 12, 0))
                .createdBy("admin@example.com")
                .creationDate(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();

        byte[] result = exporter.export(List.of(row));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            assertEquals("taskName", headerRow.getCell(0).getStringCellValue());
            assertEquals("dueDate", headerRow.getCell(3).getStringCellValue());

            Row dataRow = sheet.getRow(1);
            assertEquals("Write handover", dataRow.getCell(0).getStringCellValue());
            assertEquals("andrei", dataRow.getCell(2).getStringCellValue());
            assertEquals(LocalDateTime.of(2026, 8, 10, 12, 0), dataRow.getCell(3).getLocalDateTimeCellValue());
            assertEquals("admin@example.com", dataRow.getCell(4).getStringCellValue());
        }
    }

    @Test
    void export_leavesDueDateCellBlank_whenDueDateIsNull() {
        TaskFileRow row = TaskFileRow.builder()
                .taskName("Task without due date")
                .statusName("Pending")
                .username("andrei")
                .dueDate(null)
                .createdBy("admin@example.com")
                .creationDate(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();

        byte[] result = exporter.export(List.of(row));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Row dataRow = workbook.getSheetAt(0).getRow(1);
            Cell dueDateCell = dataRow.getCell(3);

            assertTrue(dueDateCell == null || dueDateCell.getCellType() == CellType.BLANK);
        } catch (IOException e) {
            fail("Failed to read exported Excel file: " + e.getMessage());
        }
    }

    @Test
    void supports_returnsExcelFormat() {
        assertEquals(FileFormat.EXCEL, exporter.supports());
    }
}
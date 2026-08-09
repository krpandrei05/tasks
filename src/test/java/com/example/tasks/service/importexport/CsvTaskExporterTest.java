package com.example.tasks.service.importexport;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvTaskExporterTest {
    private final CsvTaskExporter exporter = new CsvTaskExporter();

    @Test
    void export_producesCsvWithHeaderAndRowValues() {
        TaskFileRow row = TaskFileRow.builder()
                .taskName("Write handover")
                .statusName("Pending")
                .username("andrei")
                .dueDate(LocalDateTime.of(2026, 8, 10, 12, 0))
                .createdBy("admin@example.com")
                .creationDate(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();

        byte[] result = exporter.export(List.of(row));
        String csv = new String(result, StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("taskName,statusName,username,dueDate,createdBy,creationDate"));
        assertTrue(csv.contains("Write handover"));
        assertTrue(csv.contains("Pending"));
        assertTrue(csv.contains("andrei"));
        assertTrue(csv.contains("2026-08-10T12:00"));
        assertTrue(csv.contains("admin@example.com"));
    }

    @Test
    void export_writesEmptyStrings_whenOptionalFieldsAreNull() {
        TaskFileRow row = TaskFileRow.builder()
                .taskName("Task without dates")
                .statusName("Pending")
                .username("andrei")
                .dueDate(null)
                .createdBy(null)
                .creationDate(null)
                .build();

        byte[] result = exporter.export(List.of(row));
        String csv = new String(result, StandardCharsets.UTF_8);

        assertTrue(csv.contains("Task without dates,Pending,andrei,,,"));
    }

    @Test
    void export_returnsHeaderOnly_whenRowsListIsEmpty() {
        byte[] result = exporter.export(List.of());
        String csv = new String(result, StandardCharsets.UTF_8);

        assertEquals("taskName,statusName,username,dueDate,createdBy,creationDate\r\n", csv);
    }

    @Test
    void supports_returnsCsvFormat() {
        assertEquals(FileFormat.CSV, exporter.supports());
    }
}
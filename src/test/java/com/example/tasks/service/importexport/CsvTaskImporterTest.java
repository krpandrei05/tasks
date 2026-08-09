package com.example.tasks.service.importexport;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvTaskImporterTest {
    private final CsvTaskImporter importer = new CsvTaskImporter();

    private static final String HEADER = "taskName,statusName,username,dueDate,createdBy,creationDate";

    @Test
    void parse_returnsRowsWithAllFieldsParsed() {
        String csv = HEADER + "\n"
                + "Write handover,Pending,andrei,2026-08-10T12:00:00,admin@example.com,2026-08-01T09:00:00\n";

        List<TaskFileRow> rows = importer.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(1, rows.size());
        TaskFileRow row = rows.get(0);
        assertEquals("Write handover", row.getTaskName());
        assertEquals("Pending", row.getStatusName());
        assertEquals("andrei", row.getUsername());
        assertEquals(LocalDateTime.of(2026, 8, 10, 12, 0), row.getDueDate());
        assertEquals("admin@example.com", row.getCreatedBy());
        assertEquals(LocalDateTime.of(2026, 8, 1, 9, 0), row.getCreationDate());
    }

    @Test
    void parse_throwsRuntimeException_whenRequiredFieldIsBlank() {
        String csv = HEADER + "\n"
                + ",Pending,andrei,2026-08-10T12:00:00,admin@example.com,2026-08-01T09:00:00\n";

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> importer.parse(csv.getBytes(StandardCharsets.UTF_8)));

        assertTrue(exception.getMessage().contains("taskName"));
    }

    @Test
    void parse_throwsRuntimeException_whenDueDateHasInvalidFormat() {
        String csv = HEADER + "\n"
                + "Write handover,Pending,andrei,not-a-date,admin@example.com,2026-08-01T09:00:00\n";

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> importer.parse(csv.getBytes(StandardCharsets.UTF_8)));

        assertTrue(exception.getMessage().contains("dueDate"));
    }

    @Test
    void parse_allowsBlankCreatedBy() {
        String csv = HEADER + "\n"
                + "Write handover,Pending,andrei,2026-08-10T12:00:00,,2026-08-01T09:00:00\n";

        List<TaskFileRow> rows = importer.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertEquals("", rows.get(0).getCreatedBy());
    }

    @Test
    void supports_returnsCsvFormat() {
        assertEquals(FileFormat.CSV, importer.supports());
    }
}
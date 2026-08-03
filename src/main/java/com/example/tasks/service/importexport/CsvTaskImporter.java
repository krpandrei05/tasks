package com.example.tasks.service.importexport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvTaskImporter implements TaskImporter {
    @Override
    public List<TaskFileRow> parse(byte[] fileContent) {
        List<TaskFileRow> rows = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(fileContent), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build())) {
            for (CSVRecord record : parser) {
                rows.add(toTaskFileRow(record));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }

        return rows;
    }

    private TaskFileRow toTaskFileRow(CSVRecord record) {
        return TaskFileRow.builder()
                .taskName(requireNonBlank(record, "taskName"))
                .statusName(requireNonBlank(record, "statusName"))
                .username(requireNonBlank(record, "username"))
                .dueDate(parseDate(record, "dueDate"))
                .createdBy(record.get("createdBy"))
                .creationDate(parseDate(record, "creationDate"))
                .build();
    }

    private String requireNonBlank(CSVRecord record, String column) {
        String value = record.get(column);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Missing value for '" + column + "' at row " + record.getRecordNumber());
        }
        return value;
    }

    private LocalDateTime parseDate(CSVRecord record, String column) {
        String value = record.get(column);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Missing value for '" + column + "' at row " + record.getRecordNumber());
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid date format for '" + column + "' at row " + record.getRecordNumber(), e);
        }
    }

    @Override
    public FileFormat supports() {
        return FileFormat.CSV;
    }
}

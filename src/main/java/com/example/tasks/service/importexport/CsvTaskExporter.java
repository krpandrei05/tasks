package com.example.tasks.service.importexport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvTaskExporter implements TaskExporter {
    private static final String[] HEADERS = {
            "taskName", "statusName", "username", "dueDate", "createdBy", "creationDate"
    };

    @Override
    public byte[] export(List<TaskFileRow> rows) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(HEADERS).build())) {
                 for (TaskFileRow row : rows) {
                     printer.printRecord(
                             row.getTaskName(),
                             row.getStatusName(),
                             row.getUsername(),
                             row.getDueDate() != null ? row.getDueDate().toString() : "",
                             row.getCreatedBy() != null ? row.getCreatedBy().toString() : "",
                             row.getCreationDate() != null ? row.getCreationDate().toString() : ""
                     );
                 }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV export", e);
        }

        return outputStream.toByteArray();
    }

    @Override
    public FileFormat supports() {
        return FileFormat.CSV;
    }
}

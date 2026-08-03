package com.example.tasks.controller;

import com.example.tasks.service.TaskImportExportService;
import com.example.tasks.service.importexport.FileFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/tasks")
public class TaskImportExportController {

    private final TaskImportExportService taskImportExportService;

    public TaskImportExportController(TaskImportExportService taskImportExportService) {
        this.taskImportExportService = taskImportExportService;
    }

    @GetMapping("/export")
    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'READ')")
    public ResponseEntity<byte[]> exportTasks(@RequestParam FileFormat format) {
        byte[] content = taskImportExportService.export(format);
        String filename = "tasks." + format.name().toLowerCase();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(contentTypeFor(format))
                .body(content);
    }

    @PostMapping("/import")
    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'CREATE')")
    public ResponseEntity<Void> importTasks(@RequestParam("file") MultipartFile file) throws IOException {
        FileFormat format = detectFormat(file.getOriginalFilename());
        taskImportExportService.importTasks(file.getBytes(), format);
        return ResponseEntity.ok().build();
    }

    private MediaType contentTypeFor(FileFormat format) {
        return switch (format) {
            case CSV -> MediaType.parseMediaType("text/csv");
            case EXCEL -> MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        };
    }

    private FileFormat detectFormat(String filename) {
        if (filename == null) {
            throw new RuntimeException("Filename is missing");
        }
        if (filename.endsWith(".csv")) {
            return FileFormat.CSV;
        }
        if (filename.endsWith(".xlsx")) {
            return FileFormat.EXCEL;
        }
        throw new RuntimeException("Unsupported file extension: " + filename);
    }
}
package com.example.tasks.service;

import com.example.tasks.domain.StatusType;
import com.example.tasks.domain.User;
import com.example.tasks.dto.TaskDTO;
import com.example.tasks.repository.StatusTypeRepository;
import com.example.tasks.repository.UserRepository;
import com.example.tasks.service.importexport.FileFormat;
import com.example.tasks.service.importexport.TaskExporter;
import com.example.tasks.service.importexport.TaskFileRow;
import com.example.tasks.service.importexport.TaskImporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskImportExportService {
    private final TaskService taskService;
    private final StatusTypeRepository statusTypeRepository;
    private final UserRepository userRepository;
    private final List<TaskExporter> exporters;
    private final List<TaskImporter> importers;

    public byte[] export(FileFormat format) {
        log.info("Exporting tasks as {}", format);

        List<TaskDTO> tasks = taskService.getAllTasks();

        Map<String, String> statusNames = statusTypeRepository.findAll().stream()
                .collect(Collectors.toMap(StatusType::getStatusTypeId, StatusType::getStatusName));

        Map<Long, String> usernames = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));

        List<TaskFileRow> rows = tasks.stream()
                .map(task -> toFileRow(task, statusNames, usernames))
                .toList();

        TaskExporter exporter = findExporter(format);
        return exporter.export(rows);
    }

    public void importTasks(byte[] fileContent, FileFormat format) {
        log.info("Importing tasks from {}", format);

        TaskImporter importer = findImporter(format);
        List<TaskFileRow> rows = importer.parse(fileContent);

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Set<String> existingSignatures = taskService.getAllTasks().stream()
                .map(this::taskSignature)
                .collect(Collectors.toSet());

        for (TaskFileRow row : rows) {
            String signature = rowSignature(row);
            if (existingSignatures.contains(signature)) {
                log.warn("Skipping duplicate task on import: {}", signature);
                continue;
            }

            TaskDTO taskDTO = toTaskDTO(row, currentEmail);
            taskService.addTask(taskDTO);
            existingSignatures.add(signature);
        }
    }

    private String taskSignature(TaskDTO task) {
        return task.getTaskName() + "|" + task.getUserId() + "|" + task.getDueDate();
    }

    private String rowSignature(TaskFileRow row) {
        User user = userRepository.findByUsername(row.getUsername())
                .orElseThrow(() -> new RuntimeException("Unknown user: " + row.getUsername()));
        return row.getTaskName() + "|" + user.getUserId() + "|" + row.getDueDate();
    }

    private TaskFileRow toFileRow(TaskDTO task, Map<String, String> statusNames, Map<Long, String> usernames) {
        return TaskFileRow.builder()
                .taskName(task.getTaskName())
                .statusName(statusNames.get(task.getStatusTypeId()))
                .username(usernames.get(task.getUserId()))
                .dueDate(task.getDueDate())
                .createdBy(task.getCreatedBy())
                .creationDate(task.getCreationDate())
                .build();
    }

    private TaskDTO toTaskDTO(TaskFileRow row, String currentEmail) {
        StatusType statusType = statusTypeRepository.findByStatusName(row.getStatusName())
                .orElseThrow(() -> new RuntimeException("Unknown status: " + row.getStatusName()));

        User user = userRepository.findByUsername(row.getUsername())
                .orElseThrow(() -> new RuntimeException("Unknown user: " + row.getUsername()));

        return TaskDTO.builder()
                .taskName(row.getTaskName())
                .statusTypeId(statusType.getStatusTypeId())
                .userId(user.getUserId())
                .dueDate(row.getDueDate())
                .createdBy(currentEmail)
                .build();
    }

    private TaskExporter findExporter(FileFormat format) {
        return exporters.stream()
                .filter(e -> e.supports() == format)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unsupported export format: " + format));
    }

    private TaskImporter findImporter(FileFormat format) {
        return importers.stream()
                .filter(e -> e.supports() == format)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unsupported import format: " + format));
    }
}

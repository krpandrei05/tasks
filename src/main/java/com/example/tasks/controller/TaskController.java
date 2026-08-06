package com.example.tasks.controller;

import com.example.tasks.dto.TaskDTO;
import com.example.tasks.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'READ')")
    @GetMapping
    public Page<TaskDTO> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "taskId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        return taskService.getTasks(buildPageable(page, size, sortBy, sortDir));
    }

    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'READ')")
    @GetMapping("/{id}")
    public TaskDTO getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'CREATE')")
    @PostMapping
    public TaskDTO addTask(@Valid @RequestBody TaskDTO task) {
        return taskService.addTask(task);
    }

    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'DELETE')")
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'UPDATE')")
    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable Long id, @Valid @RequestBody TaskDTO task) {
        return taskService.updateTask(id, task);
    }

    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'CREATE')")
    @PostMapping("/batch")
    public List<TaskDTO> addTasks(@Valid @RequestBody List<TaskDTO> tasks) {
        return taskService.addTasksFromList(tasks);
    }

    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'UPDATE')")
    @PatchMapping("/{id}/status")
    public TaskDTO updateTaskStatus(@PathVariable Long id, @RequestBody String statusTypeId) {
        return taskService.updateTaskStatus(id, statusTypeId);
    }

    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'DELETE')")
    @DeleteMapping
    public void deleteAllTasks() {
        taskService.deleteAllTasks();
    }

    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'READ')")
    @GetMapping("/due-before")
    public List<TaskDTO> getTasksDueBefore(@RequestParam LocalDateTime date) {
        return taskService.getTasksDueBefore(date);
    }

    // [Homework 1] 1st endpoint
    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'READ')")
    @GetMapping("/status/{statusName}")
    public List<TaskDTO> getTasksByStatus(@PathVariable String statusName) {
        return taskService.getTasksByStatus(statusName);
    }

    // [Homework 1] 2nd endpoint
    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'READ')")
    @GetMapping("/count")
    public int getTaskCount() {
        return taskService.getTasksCount();
    }

    // [Homework 1] 3rd endpoint
    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'READ')")
    @GetMapping("/overdue")
    public List<TaskDTO> getOverdueTasks() {
        return taskService.getOverdueTasks();
    }

    // [Homework 1] 4th endpoint
    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'UPDATE')")
    @PatchMapping("/{id}/content")
    public TaskDTO updateTaskContent(@PathVariable Long id, @RequestBody String taskName) {
        return taskService.updateTaskContent(id, taskName);
    }

    @PreAuthorize("@permissionChecker.hasPermission('TASK', 'UPDATE')")
    @PatchMapping("/transfer")
    public void transferTasks(@RequestParam Long fromUserId, @RequestParam Long toUserId) {
        taskService.transferTasks(fromUserId, toUserId);
    }

    // [Homework 5] Search
    @GetMapping("/search")
    public Page<TaskDTO> searchTasks(
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String statusName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "taskId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        return taskService.searchTasks(taskName, statusName, username, dueDate, buildPageable(page, size, sortBy, sortDir));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Map<String, String> allowedSortFields = Map.of(
                "taskId", "taskId",
                "user", "user.username",
                "taskName", "taskName",
                "dueDate", "dueDate"
        );

        String property = allowedSortFields.get(sortBy);
        if (property == null) {
            throw new IllegalArgumentException("Invalid sortBy value: " + sortBy);
        }

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
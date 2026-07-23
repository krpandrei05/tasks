package com.example.tasks.controller;

import com.example.tasks.dto.TaskDTO;
import com.example.tasks.service.TaskService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskDTO> getTasks() {
        return taskService.getTasks();
    }

    @GetMapping("/{id}")
    public TaskDTO getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PostMapping
    public TaskDTO addTask(@Valid @RequestBody TaskDTO task) {
        return taskService.addTask(task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable Long id, @Valid @RequestBody TaskDTO task) {
        return taskService.updateTask(id, task);
    }

    @PostMapping("/batch")
    public List<TaskDTO> addTasks(@Valid @RequestBody List<TaskDTO> tasks) {
        return taskService.addTasksFromList(tasks);
    }

    @PatchMapping("/{id}/status")
    public TaskDTO updateTaskStatus(@PathVariable Long id, @RequestBody String statusTypeId) {
        return taskService.updateTaskStatus(id, statusTypeId);
    }

    @DeleteMapping
    public void deleteAllTasks() {
        taskService.deleteAllTasks();
    }

    @GetMapping("/due-before")
    public List<TaskDTO> getTasksDueBefore(@RequestParam LocalDateTime date) {
        return taskService.getTasksDueBefore(date);
    }

    // [Homework 1] 1st endpoint
    @GetMapping("/status/{statusName}")
    public List<TaskDTO> getTasksByStatus(@PathVariable String statusName) {
        return taskService.getTasksByStatus(statusName);
    }

    // [Homework 1] 2nd endpoint
    @GetMapping("/count")
    public int getTaskCount() {
        return taskService.getTasksCount();
    }

    // [Homework 1] 3rd endpoint
    @GetMapping("/overdue")
    public List<TaskDTO> getOverdueTasks() {
        return taskService.getOverdueTasks();
    }

    // [Homework 1] 4th endpoint
    @PatchMapping("/{id}/content")
    public TaskDTO updateTaskContent(@PathVariable Long id, @RequestBody String taskName) {
        return taskService.updateTaskContent(id, taskName);
    }

    @PatchMapping("/transfer")
    public void transferTasks(@RequestParam Long fromUserId, @RequestParam Long toUserId) {
        taskService.transferTasks(fromUserId, toUserId);
    }

    // [Homework 5] Search
    @GetMapping("/search")
    public List<TaskDTO> searchTasks(
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String statusName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        return taskService.searchTasks(taskName, statusName, username, dueDate);
    }
}
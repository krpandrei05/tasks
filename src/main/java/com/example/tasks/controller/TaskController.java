package com.example.tasks.controller;

import com.example.tasks.dto.TaskDTO;
import com.example.tasks.service.TaskService;
import org.springframework.web.bind.annotation.*;

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
    public List<TaskDTO> getTask() {
        return taskService.getTasks();
    }

    @GetMapping("/{id}")
    public TaskDTO getTaskById(@PathVariable Long id) {
        return taskService.getTaskByid(id);
    }

    @PostMapping
    public List<TaskDTO> addTask(@Valid @RequestBody TaskDTO task) {
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
    public TaskDTO updateTaskStatus(@PathVariable Long id, @RequestBody String status) {
        return taskService.updateTaskStatus(id, status);
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
    @GetMapping("/status/{status}")
    public List<TaskDTO> getTasksByStatus(@PathVariable String status) {
        return taskService.getTasksByStatus(status);
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
    public TaskDTO updateTaskContent(@PathVariable Long id, @RequestBody String content) {
        return taskService.updateTaskContent(id, content);
    }
}


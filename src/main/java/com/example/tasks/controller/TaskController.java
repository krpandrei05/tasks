package com.example.tasks.controller;

import com.example.tasks.dto.TaskDTO;
import com.example.tasks.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
    public List<TaskDTO> addTask(@RequestBody TaskDTO task) {
        return taskService.addTask(task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable Long id, @RequestBody TaskDTO task) {
        return taskService.updateTask(id, task);
    }

    @PostMapping("/batch")
    public List<TaskDTO> addTasks(@RequestBody List<TaskDTO> tasks) {
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
}

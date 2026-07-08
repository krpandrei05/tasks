package com.example.tasks.service;

import com.example.tasks.dto.TaskDTO;
import com.example.tasks.exception.TaskNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class TaskService {
    private List<TaskDTO> tasks = new ArrayList<>();

    public List<TaskDTO> getTasks() {
        log.info("Getting tasks: ");
        return tasks;
    }

    public List<TaskDTO> addTask(TaskDTO task) {
        TaskDTO builtTask = buildTask(task);

        tasks.add(builtTask);
        log.info("Added task: {}", builtTask);

        return tasks;
    }

    public List<TaskDTO> addTasksFromList(List<TaskDTO> tasksFromList) {
        tasks.addAll(tasksFromList);
        log.info("Added tasks from a list. Updated tasks: {} ", tasks);
        return tasks;
    }

    public TaskDTO getTaskByid(Long id) {
        log.info("Getting task by id: {}", id);
        for (TaskDTO task : tasks) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        throw new TaskNotFoundException(id);
    }

    public List<TaskDTO> deleteTask(Long id) {
        boolean removed = false;
        Iterator<TaskDTO> iterator = tasks.iterator();

        while (iterator.hasNext()) {
            TaskDTO task = iterator.next();
            if (task.getId().equals(id)) {
                iterator.remove();
                removed = true;
                break;
            }
        }

        if (removed) {
            log.info("Deleting task with id: {}", id);
        } else {
            log.warn("Task with id: {} not found", id);
        }
        return tasks;
    }

    public TaskDTO updateTask(Long id, TaskDTO task) {
        TaskDTO builtTask = buildTask(task);
        for (TaskDTO t : tasks) {
            if (t.getId().equals(id)) {
                t.setId(builtTask.getId());
                t.setContent(builtTask.getContent());
                t.setDueDate(builtTask.getDueDate());
                t.setStatus(builtTask.getStatus());
                return t;
            }
        }
        throw new TaskNotFoundException(id);
    }

    private TaskDTO buildTask(TaskDTO task) {
        return TaskDTO.builder()
                .id(task.getId())
                .content(task.getContent())
                .dueDate(task.getDueDate())
                .status(task.getStatus())
                .build();
    }

    public TaskDTO updateTaskStatus(Long id, String status) {
        for (TaskDTO t : tasks) {
            if (t.getId().equals(id)) {
                t.setStatus(status);
                log.info("Updated status of task with id: {} to {}", id, status);
                return t;
            }
        }
        throw new TaskNotFoundException(id);
    }

    public void deleteAllTasks() {
        tasks.clear();
        log.info("Deleted all tasks");
    }

    public List<TaskDTO> getTasksDueBefore(LocalDateTime date) {
        log.info("Getting tasks due before: {}", date);
        return tasks.stream()
                .filter(task -> task.getDueDate().isBefore(date))
                .toList();
    }

    // [Homework 1] 1st endpoint
    public List<TaskDTO> getTasksByStatus(String status) {
        log.info("Getting tasks with status: {}", status);
        return tasks.stream()
                .filter(task -> task.getStatus().equals(status))
                .toList();
    }

    // [Homework 1] 2nd endpoint
    public int getTasksCount() {
        log.info("Getting tasks count: {}", tasks.size());
        return tasks.size();
    }

    // [Homework 1] 3rd endpoint
    public List<TaskDTO> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Getting overdue tasks. Current date: {}", now);
        return tasks.stream()
                .filter(task -> task.getDueDate().isBefore(now))
                .toList();
    }

    // [Homework 1] 4th endpoint
    public TaskDTO updateTaskContent(Long id, String content) {
        for (TaskDTO t : tasks) {
            if (t.getId().equals(id)) {
                t.setContent(content);
                log.info("Updated content of task with id: {} to {}", id, content);
                return t;
            }
        }
        throw new TaskNotFoundException(id);
    }
}

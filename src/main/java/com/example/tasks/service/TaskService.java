package com.example.tasks.service;

import com.example.tasks.TasksApplication;
import com.example.tasks.dto.TaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        return tasks.get(Math.toIntExact(id));
    }

    public List<TaskDTO> deleteTask(Long id) {
        for (TaskDTO task : tasks) {
            if (task.getId() == id) {
                log.info("Deleting task with id: {}", id);
                tasks.remove(task);
                return tasks;
            }
        }
        log.warn("Task with id: {} not found", id);
        return tasks;
    }

    public TaskDTO updateTask(Long id, TaskDTO task) {
        TaskDTO builtTask = buildTask(task);
        for (TaskDTO t : tasks) {
            if (t.getId() == id) {
                t.setId(builtTask.getId());
                t.setContent(builtTask.getContent());
                t.setDueDate(builtTask.getDueDate());
                t.setStatus(builtTask.getStatus());
            }
        }
        return task;
    }

    private TaskDTO buildTask(TaskDTO task) {
        return TaskDTO.builder()
                .id(task.getId())
                .content(task.getContent())
                .dueDate(task.getDueDate())
                .status(task.getStatus())
                .build();
    }

}

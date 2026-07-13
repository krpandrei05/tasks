package com.example.tasks.service;

import com.example.tasks.domain.StatusType;
import com.example.tasks.domain.Task;
import com.example.tasks.domain.User;
import com.example.tasks.dto.TaskDTO;
import com.example.tasks.exception.TaskNotFoundException;
import com.example.tasks.mapper.TaskMapper;
import com.example.tasks.repository.StatusTypeRepository;
import com.example.tasks.repository.TaskRepository;
import com.example.tasks.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final StatusTypeRepository statusTypeRepository;
    private final UserRepository userRepository;

    public List<TaskDTO> getTasks() {
        log.info("Getting tasks");
        return taskRepository.findAll()
                .stream()
                .map(taskMapper::toDto)
                .toList();
    }

    public TaskDTO getTaskById(Long id) {
        log.info("Getting task by id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return taskMapper.toDto(task);
    }

    @Transactional
    public TaskDTO addTask(@Valid TaskDTO taskDTO) {
        log.info("Adding task: {}", taskDTO);
        StatusType statusType = findStatusType(taskDTO.getStatusTypeId());
        User user = findUser(taskDTO.getUserId());

        Task task = taskMapper.toEntity(taskDTO, statusType, user);
        Task savedTask = taskRepository.save(task);

        return taskMapper.toDto(savedTask);
    }

    @Transactional
    public List<TaskDTO> addTasksFromList(List<TaskDTO> taskDTOs) {
        log.info("Adding tasks from a list: {}", taskDTOs);
        List<Task> tasks = taskDTOs.stream()
                .map(dto -> taskMapper.toEntity(dto, findStatusType(dto.getStatusTypeId()), findUser(dto.getUserId())))
                .toList();
        taskRepository.saveAll(tasks);
        return getTasks();
    }

    @Transactional
    public TaskDTO updateTask(Long id, @Valid TaskDTO taskDTO) {
        log.info("Updating task with id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.setTaskName(taskDTO.getTaskName());
        task.setStatusType(findStatusType(taskDTO.getStatusTypeId()));
        task.setUser(findUser(taskDTO.getUserId()));
        task.setDueDate(taskDTO.getDueDate());

        return taskMapper.toDto(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional
    public void deleteAllTasks() {
        log.info("Deleting all tasks");
        taskRepository.deleteAll();
    }

    @Transactional
    public TaskDTO updateTaskStatus(Long id, String statusTypeId) {
        log.info("Updating status of task with id: {} to {}", id, statusTypeId);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        task.setStatusType(findStatusType(statusTypeId));
        return taskMapper.toDto(task);
    }

    @Transactional
    public TaskDTO updateTaskContent(Long id, String taskName) {
        log.info("Updating content of task with id: {} to {}", id, taskName);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        task.setTaskName(taskName);
        return taskMapper.toDto(task);
    }

    public List<TaskDTO> getTasksDueBefore(LocalDateTime date) {
        log.info("Getting tasks due before: {}", date);
        return taskRepository.findAll().stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(date))
                .map(taskMapper::toDto)
                .toList();
    }

    public List<TaskDTO> getTasksByStatus(String statusTypeId) {
        log.info("Getting tasks with status type id: {}", statusTypeId);
        return taskRepository.findAll().stream()
                .filter(task -> task.getStatusType() != null && task.getStatusType().getStatusTypeId().equals(statusTypeId))
                .map(taskMapper::toDto)
                .toList();
    }

    public int getTasksCount() {
        int count = (int) taskRepository.count();
        log.info("Getting tasks count: {}", count);
        return count;
    }

    public List<TaskDTO> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Getting overdue tasks. Current date: {}", now);
        return taskRepository.findAll().stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(now))
                .map(taskMapper::toDto)
                .toList();
    }

    private StatusType findStatusType(String statusTypeId) {
        return statusTypeRepository.findById(statusTypeId)
                .orElseThrow(() -> new RuntimeException("Status type not found: " + statusTypeId));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
}
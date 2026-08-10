package com.example.tasks.service;

import com.example.tasks.config.PermissionChecker;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private final PermissionChecker permissionChecker;

    public Page<TaskDTO> getTasks(Pageable pageable) {
        log.info("Getting tasks, page {}", pageable);
        return searchTasks(null, null, null, null, pageable);
    }

    // ADMIN
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream().map(taskMapper::toDto).toList();
    }

    public TaskDTO getTaskById(Long id) {
        log.info("Getting task by id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        if (!permissionChecker.canAccessTask(task)) {
            throw new AccessDeniedException("Not allowed to access this task");
        }
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
    public TaskDTO updateTask(Long id, @Valid TaskDTO taskDTO) {
        log.info("Updating task with id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        if (!permissionChecker.canAccessTask(task)) {
            throw new AccessDeniedException("Not allowed to modify this task");
        }

        task.setTaskName(taskDTO.getTaskName());
        task.setStatusType(findStatusType(taskDTO.getStatusTypeId()));
        task.setUser(findUser(taskDTO.getUserId()));
        task.setDueDate(taskDTO.getDueDate());

        return taskMapper.toDto(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        if (!permissionChecker.canAccessTask(task)) {
            throw new AccessDeniedException("Not allowed to delete this task");
        }
        taskRepository.deleteById(id);
    }

    private StatusType findStatusType(String statusTypeId) {
        return statusTypeRepository.findById(statusTypeId)
                .orElseThrow(() -> new RuntimeException("Status type not found: " + statusTypeId));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    @Transactional
    public void transferTasks(Long fromUserId, Long toUserId) {
        log.info("Transferring tasks from user {} to user {}", fromUserId, toUserId);
        User newUser = findUser(toUserId);
        List<Task> tasks = taskRepository.findAll().stream()
                .filter(task -> task.getUser() != null && task.getUser().getUserId().equals(fromUserId))
                .toList();

        for (Task task : tasks) {
            task.setUser(newUser);
        }
    }

    public Page<TaskDTO> searchTasks(String taskName, String statusName, String username, LocalDate dueDate, Pageable pageable) {
        log.info("Searching tasks with filters - name: {}, status: {}, user: {}, dueDate: {}", taskName, statusName, username, dueDate);

        LocalDateTime startOfDay = (dueDate != null) ? dueDate.atStartOfDay() : null;
        LocalDateTime endOfDay = (dueDate != null) ? dueDate.plusDays(1).atStartOfDay() : null;

        // ADMIN = NULL
        String ownerEmail = permissionChecker.isCurrentUserAdmin() ? null : permissionChecker.getCurrentUserEmail();

        return taskRepository.searchTasks(ownerEmail, taskName, statusName, username, startOfDay, endOfDay, pageable).map(taskMapper::toDto);
    }
}
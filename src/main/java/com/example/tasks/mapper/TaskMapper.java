package com.example.tasks.mapper;

import com.example.tasks.domain.StatusType;
import com.example.tasks.domain.Task;
import com.example.tasks.domain.User;
import com.example.tasks.dto.TaskDTO;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public TaskDTO toDto(Task task) {
        return TaskDTO.builder()
                .taskId(task.getTaskId())
                .taskName(task.getTaskName())
                .statusTypeId(task.getStatusType() != null ? task.getStatusType().getStatusTypeId() : null)
                .userId(task.getUser() != null ? task.getUser().getUserId() : null)
                .dueDate(task.getDueDate())
                .createdBy(task.getCreatedBy())
                .creationDate(task.getCreationDate())
                .build();
    }

    public Task toEntity(TaskDTO taskDTO, StatusType statusType, User user) {
        return Task.builder()
                .taskName(taskDTO.getTaskName())
                .statusType(statusType)
                .user(user)
                .dueDate(taskDTO.getDueDate())
                .createdBy(taskDTO.getCreatedBy())
                .build();
    }
}
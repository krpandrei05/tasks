package com.example.tasks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDTO {
    private Long taskId;

    @NotBlank(message = "Task name cannot be blank")
    private String taskName;

    @NotNull(message = "Status type id cannot be null")
    private String statusTypeId;

    @NotNull(message = "User id cannot be null")
    private Long userId;

    @NotNull(message = "Due date cannot be null")
    private LocalDateTime dueDate;

    private String createdBy;
    private LocalDateTime creationDate;
}
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
@ToString
public class TaskDTO {
    @NotNull(message = "Id cannot be null")
    private Long id;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    @NotNull(message = "Due date cannot be null")
    private LocalDateTime dueDate;

    @NotBlank(message = "Status cannot be blank")
    private String status;
}

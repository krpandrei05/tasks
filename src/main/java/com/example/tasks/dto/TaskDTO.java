package com.example.tasks.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TaskDTO {
    private Long id;
    private String content;
    private LocalDateTime dueDate;
    private String status;
}

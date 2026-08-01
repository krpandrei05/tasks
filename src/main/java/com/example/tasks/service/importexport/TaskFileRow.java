package com.example.tasks.service.importexport;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class TaskFileRow {
    private String taskName;
    private String statusName;
    private String username;
    private LocalDateTime dueDate;
    private String createdBy;
    private LocalDateTime creationDate;
}

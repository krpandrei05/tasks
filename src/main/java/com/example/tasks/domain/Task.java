package com.example.tasks.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @Column(name = "TASK_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(name = "TASK_NAME", nullable = false)
    private String taskName;

    @ManyToOne
    @JoinColumn(name = "STATUS_TYPE_ID")
    private StatusType statusType;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "DUE_DATE")
    private LocalDateTime dueDate;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATION_DATE")
    @Builder.Default
    private LocalDateTime creationDate = LocalDateTime.now();
}
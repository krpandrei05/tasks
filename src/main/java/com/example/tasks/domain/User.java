package com.example.tasks.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "USER_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Column(name = "IS_INTERNAL")
    private Integer isInternal;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATION_DATE")
    @Builder.Default
    private LocalDateTime creationDate = LocalDateTime.now();
}
package com.example.tasks.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @Id
    @Column(name = "PERMISSION_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long permissionId;

    @Column(name = "PERMISSION_ACTION", nullable = false)
    private String action;

    @Column(name = "RESOURCE_NAME", nullable = false)
    private String resourceName;
}
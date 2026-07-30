package com.example.tasks.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRoleDTO {
    @NotBlank(message = "Role name cannot be blank")
    private String roleName;
}
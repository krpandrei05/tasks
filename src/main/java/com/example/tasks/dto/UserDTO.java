package com.example.tasks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long userId;

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @NotNull(message = "Birth date cannot be null")
    private LocalDate birthDate;

    @NotNull(message = "Is internal cannot be null")
    private Integer isInternal;

    private String createdBy;
    private LocalDateTime creationDate;
}
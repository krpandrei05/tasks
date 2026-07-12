package com.example.tasks.mapper;

import com.example.tasks.domain.User;
import com.example.tasks.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDTO toDto(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .birthDate(user.getBirthDate())
                .isInternal(user.getIsInternal())
                .createdBy(user.getCreatedBy())
                .creationDate(user.getCreationDate())
                .build();
    }

    public User toEntity(UserDTO userDTO) {
        return User.builder()
                .username(userDTO.getUsername())
                .birthDate(userDTO.getBirthDate())
                .isInternal(userDTO.getIsInternal())
                .createdBy(userDTO.getCreatedBy())
                .build();
    }
}
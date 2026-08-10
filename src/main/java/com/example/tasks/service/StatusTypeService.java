package com.example.tasks.service;

import com.example.tasks.dto.StatusTypeDTO;
import com.example.tasks.mapper.StatusTypeMapper;
import com.example.tasks.repository.StatusTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusTypeService {
    private final StatusTypeRepository statusTypeRepository;
    private final StatusTypeMapper statusTypeMapper;

    public List<StatusTypeDTO> getAllStatuses() {
        log.info("Statuses retrieved!");
        return statusTypeRepository.findAll()
                .stream()
                .map(statusTypeMapper::toDto)
                .toList();
    }
}
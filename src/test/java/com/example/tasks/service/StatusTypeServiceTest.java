package com.example.tasks.service;

import com.example.tasks.domain.StatusType;
import com.example.tasks.dto.StatusTypeDTO;
import com.example.tasks.mapper.StatusTypeMapper;
import com.example.tasks.repository.StatusTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusTypeServiceTest {
    @Mock
    private StatusTypeRepository statusTypeRepository;

    @Mock
    private StatusTypeMapper statusTypeMapper;

    @InjectMocks
    private StatusTypeService statusTypeService;

    private StatusType pendingStatus;
    private StatusTypeDTO pendingStatusDto;

    @BeforeEach
    void setUp() {
        pendingStatus = StatusType.builder()
                .statusTypeId("status-1")
                .statusName("Pending")
                .createdBy("andrei")
                .build();

        pendingStatusDto = StatusTypeDTO.builder()
                .statusTypeId("status-1")
                .statusName("Pending")
                .createdBy("andrei")
                .build();
    }

    @Test
    void getAllStatuses_returnsEmptyList_whenNoStatusesExist() {
        when(statusTypeRepository.findAll()).thenReturn(List.of());

        List<StatusTypeDTO> result = statusTypeService.getAllStatuses();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllStatuses_mapsEachEntityToDto() {
        StatusType inProgressStatus = StatusType.builder()
                .statusTypeId("status-2")
                .statusName("In Progress")
                .build();
        StatusTypeDTO inProgressDto = StatusTypeDTO.builder()
                .statusTypeId("status-2")
                .statusName("In Progress")
                .build();

        when(statusTypeRepository.findAll()).thenReturn(List.of(pendingStatus, inProgressStatus));
        when(statusTypeMapper.toDto(pendingStatus)).thenReturn(pendingStatusDto);
        when(statusTypeMapper.toDto(inProgressStatus)).thenReturn(inProgressDto);

        List<StatusTypeDTO> result = statusTypeService.getAllStatuses();

        assertEquals(2, result.size());
        assertEquals(pendingStatusDto, result.get(0));
        assertEquals(inProgressDto, result.get(1));
    }
}
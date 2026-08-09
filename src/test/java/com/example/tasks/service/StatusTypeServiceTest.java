package com.example.tasks.service;

import com.example.tasks.domain.StatusType;
import com.example.tasks.domain.Task;
import com.example.tasks.dto.StatusTypeDTO;
import com.example.tasks.mapper.StatusTypeMapper;
import com.example.tasks.repository.StatusTypeRepository;
import com.example.tasks.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusTypeServiceTest {
    @Mock
    private StatusTypeRepository statusTypeRepository;

    @Mock
    private StatusTypeMapper statusTypeMapper;

    @Mock
    private TaskRepository taskRepository;

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
        assertEquals(pendingStatusDto, result.get(0))  ;
        assertEquals(inProgressDto, result.get(1));
    }

    @Test
    void createStatus_mapsSavesAndReturnsDto() {
        StatusType unsavedEntity = StatusType.builder()
                .statusName("Pending")
                .createdBy("andrei")
                .build();
        StatusType savedEntity = StatusType.builder()
                .statusTypeId("status-1")
                .statusName("Pending")
                .createdBy("andrei")
                .build();

        when(statusTypeMapper.toEntity(pendingStatusDto)).thenReturn(unsavedEntity);
        when(statusTypeRepository.save(unsavedEntity)).thenReturn(savedEntity);
        when(statusTypeMapper.toDto(savedEntity)).thenReturn(pendingStatusDto);

        StatusTypeDTO result = statusTypeService.createStatus(pendingStatusDto);

        assertEquals(pendingStatusDto, result);
        verify(statusTypeRepository, times(1)).save(unsavedEntity);
    }

    @Test
    void deleteStatusAndReassignTasks_throws_whenReplacementStatusNotFound() {
        when(statusTypeRepository.findById("missing-replacement")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                statusTypeService.deleteStatusAndReassignTasks("status-1", "missing-replacement"));

        verify(taskRepository, never()).findAll();
        verify(statusTypeRepository, never()).deleteById(any());
    }

    @Test
    void deleteStatusAndReassignTasks_reassignsOnlyMatchingTasksAndDeletesStatus() {
        StatusType replacementStatus = StatusType.builder()
                .statusTypeId("status-2")
                .statusName("Completed")
                .build();

        StatusType unrelatedStatus = StatusType.builder()
                .statusTypeId("status-3")
                .statusName("Blocked")
                .build();

        Task matchingTask = Task.builder()
                .taskId(1L)
                .statusType(pendingStatus)
                .build();
        Task unrelatedStatusTask = Task.builder()
                .taskId(2L)
                .statusType(unrelatedStatus)
                .build();
        Task nullStatusTask = Task.builder()
                .taskId(3L)
                .statusType(null)
                .build();

        when(statusTypeRepository.findById("status-2")).thenReturn(Optional.of(replacementStatus));
        when(taskRepository.findAll()).thenReturn(List.of(matchingTask, unrelatedStatusTask, nullStatusTask));

        statusTypeService.deleteStatusAndReassignTasks("status-1", "status-2");

        assertEquals(replacementStatus, matchingTask.getStatusType());
        assertEquals(unrelatedStatus, unrelatedStatusTask.getStatusType());
        assertEquals(null, nullStatusTask.getStatusType());

        ArgumentCaptor<String> deletedIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(statusTypeRepository, times(1)).deleteById(deletedIdCaptor.capture());
        assertEquals("status-1", deletedIdCaptor.getValue());
    }
}
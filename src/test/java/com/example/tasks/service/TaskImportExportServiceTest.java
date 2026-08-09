package com.example.tasks.service;

import com.example.tasks.config.PermissionChecker;
import com.example.tasks.domain.StatusType;
import com.example.tasks.domain.User;
import com.example.tasks.dto.TaskDTO;
import com.example.tasks.repository.StatusTypeRepository;
import com.example.tasks.repository.UserRepository;
import com.example.tasks.service.importexport.FileFormat;
import com.example.tasks.service.importexport.TaskExporter;
import com.example.tasks.service.importexport.TaskFileRow;
import com.example.tasks.service.importexport.TaskImporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskImportExportServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private StatusTypeRepository statusTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionChecker permissionChecker;

    @Mock
    private TaskExporter csvExporter;

    @Mock
    private TaskImporter csvImporter;

    private TaskImportExportService taskImportExportService;

    private StatusType pendingStatus;
    private User assignedUser;
    private TaskDTO existingTaskDto;

    @BeforeEach
    void setUp() {
        pendingStatus = StatusType.builder()
                .statusTypeId("status-1")
                .statusName("Pending")
                .build();

        assignedUser = User.builder()
                .userId(1L)
                .username("andrei")
                .email("andrei.krp@gmail.com")
                .build();

        existingTaskDto = TaskDTO.builder()
                .taskId(100L)
                .taskName("Write handover")
                .statusTypeId("status-1")
                .userId(1L)
                .dueDate(LocalDateTime.of(2026, 8, 10, 12, 0))
                .createdBy("admin@example.com")
                .creationDate(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();

        taskImportExportService = new TaskImportExportService(
                taskService,
                statusTypeRepository,
                userRepository,
                List.of(csvExporter),
                List.of(csvImporter),
                permissionChecker
        );
    }

    // Cazul 1 - Succes
    @Test
    void export_buildsFileRowsAndDelegatesToMatchingExporter() {
        when(taskService.getAllTasks()).thenReturn(List.of(existingTaskDto));
        when(statusTypeRepository.findAll()).thenReturn(List.of(pendingStatus));
        when(userRepository.findAll()).thenReturn(List.of(assignedUser));
        when(csvExporter.supports()).thenReturn(FileFormat.CSV);

        byte[] fakeBytes = "fake-csv-content".getBytes();
        when(csvExporter.export(anyList())).thenReturn(fakeBytes);

        byte[] result = taskImportExportService.export(FileFormat.CSV);

        assertArrayEquals(fakeBytes, result);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TaskFileRow>> captor = ArgumentCaptor.forClass(List.class);
        verify(csvExporter).export(captor.capture());
        TaskFileRow row = captor.getValue().get(0);

        assertEquals("Write handover", row.getTaskName());
        assertEquals("Pending", row.getStatusName());
        assertEquals("andrei", row.getUsername());
    }

    // Cazul 2 - Format nesuportat
    @Test
    void export_throwsRuntimeException_whenNoExporterSupportsFormat() {
        when(csvExporter.supports()).thenReturn(FileFormat.CSV);

        assertThrows(RuntimeException.class, () -> taskImportExportService.export(FileFormat.EXCEL));

        verify(csvExporter, never()).export(any());
    }

    // Cazul 1 - Format nesuportat
    @Test
    void importTasks_throwsRuntimeException_whenFormatNotSupported() {
        when(csvImporter.supports()).thenReturn(FileFormat.CSV);

        assertThrows(RuntimeException.class, () -> taskImportExportService.importTasks(new byte[0], FileFormat.EXCEL));

        verify(csvImporter, never()).parse(any());
    }

    // Cazul 2 - Rand nou
    @Test
    void importTasks_addsNewTask_whenRowIsNotDuplicate() {
        TaskFileRow newRow = TaskFileRow.builder()
                .taskName("Review PR")
                .statusName("Pending")
                .username("andrei")
                .dueDate(LocalDateTime.of(2026, 8, 20, 10, 0))
                .build();

        when(csvImporter.supports()).thenReturn(FileFormat.CSV);
        when(csvImporter.parse(any())).thenReturn(List.of(newRow));
        when(permissionChecker.getCurrentUserEmail()).thenReturn("admin@example.com");
        when(taskService.getAllTasks()).thenReturn(List.of());
        when(userRepository.findByUsername("andrei")).thenReturn(Optional.of(assignedUser));
        when(statusTypeRepository.findByStatusName("Pending")).thenReturn(Optional.of(pendingStatus));

        taskImportExportService.importTasks(new byte[0], FileFormat.CSV);

        ArgumentCaptor<TaskDTO> captor = ArgumentCaptor.forClass(TaskDTO.class);
        verify(taskService, times(1)).addTask(captor.capture());

        TaskDTO addedTask = captor.getValue();
        assertEquals("Review PR", addedTask.getTaskName());
        assertEquals("admin@example.com", addedTask.getCreatedBy());
        assertEquals(1L, addedTask.getUserId());
        assertEquals("status-1", addedTask.getStatusTypeId());
    }

    // Cazul 3 - Rand duplicat, sarit siletion
    @Test
    void importTasks_skipsDuplicateRow_withoutCallingAddTask() {
        TaskFileRow duplicateRow = TaskFileRow.builder()
                .taskName("Write handover")
                .statusName("Pending")
                .username("andrei")
                .dueDate(existingTaskDto.getDueDate())
                .build();

        when(csvImporter.supports()).thenReturn(FileFormat.CSV);
        when(csvImporter.parse(any())).thenReturn(List.of(duplicateRow));
        when(permissionChecker.getCurrentUserEmail()).thenReturn("admin@example.com");
        when(taskService.getAllTasks()).thenReturn(List.of(existingTaskDto));
        when(userRepository.findByUsername("andrei")).thenReturn(Optional.of(assignedUser));

        taskImportExportService.importTasks(new byte[0], FileFormat.CSV);

        verify(taskService, never()).addTask(any());
    }

    // Cazul 4 - User necunoscut
    @Test
    void importTasks_throwsRuntimeException_whenUserIsUnknown() {
        TaskFileRow rowWithUnknownUser = TaskFileRow.builder()
                .taskName("Review PR")
                .statusName("Pending")
                .username("ghost-user")
                .dueDate(existingTaskDto.getDueDate())
                .build();

        when(csvImporter.supports()).thenReturn(FileFormat.CSV);
        when(csvImporter.parse(any())).thenReturn(List.of(rowWithUnknownUser));
        when(permissionChecker.getCurrentUserEmail()).thenReturn("admin@example.com");
        when(taskService.getAllTasks()).thenReturn(List.of());
        when(userRepository.findByUsername("ghost-user")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskImportExportService.importTasks(new byte[0], FileFormat.CSV));

        verify(taskService, never()).addTask(any());
    }

    // Cazul 5 - Status necunoscut
    @Test
    void importTasks_throwsRuntimeException_whenStatusIsUnknown() {
        TaskFileRow rowWithUnknownStatus = TaskFileRow.builder()
                .taskName("Review PR")
                .statusName("Not Started")
                .username("andrei")
                .dueDate(existingTaskDto.getDueDate())
                .build();

        when(csvImporter.supports()).thenReturn(FileFormat.CSV);
        when(csvImporter.parse(any())).thenReturn(List.of(rowWithUnknownStatus));
        when(permissionChecker.getCurrentUserEmail()).thenReturn("admin@example.com");
        when(taskService.getAllTasks()).thenReturn(List.of());
        when(userRepository.findByUsername("andrei")).thenReturn(Optional.of(assignedUser));
        when(statusTypeRepository.findByStatusName("Not Started")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskImportExportService.importTasks(new byte[0], FileFormat.CSV));

        verify(taskService, never()).addTask(any());
    }
}
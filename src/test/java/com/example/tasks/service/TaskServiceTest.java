package com.example.tasks.service;

import com.example.tasks.config.PermissionChecker;
import com.example.tasks.domain.StatusType;
import com.example.tasks.domain.Task;
import com.example.tasks.domain.User;
import com.example.tasks.dto.TaskDTO;
import com.example.tasks.exception.TaskNotFoundException;
import com.example.tasks.mapper.TaskMapper;
import com.example.tasks.repository.StatusTypeRepository;
import com.example.tasks.repository.TaskRepository;
import com.example.tasks.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private StatusTypeRepository statusTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionChecker permissionChecker;

    @InjectMocks
    private TaskService taskService;

    private StatusType pendingStatus;
    private User assignedUser;
    private Task existingTask;
    private TaskDTO existingTaskDto;

    @BeforeEach
    void setUp() {
        pendingStatus = StatusType.builder()
                .statusTypeId("status-1")
                .statusName("Pending")
                .build();

        assignedUser = User.builder()
                .userId(1l)
                .username("andrei")
                .email("andrei.krp@gmail.com")
                .build();

        existingTask = Task.builder()
                .taskId(100L)
                .taskName("Write handover")
                .statusType(pendingStatus)
                .user(assignedUser)
                .dueDate(LocalDateTime.of(2026, 8, 10, 12, 0))
                .build();

        existingTaskDto = TaskDTO.builder()
                .taskId(100L)
                .taskName("Write handover")
                .statusTypeId("status-1")
                .userId(1L)
                .dueDate(LocalDateTime.of(2026, 8, 10, 12, 0))
                .build();
    }

    @Test
    void getTasks_delegatesToSearchTasksWithNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(existingTask), pageable, 1);

        when(permissionChecker.isCurrentUserAdmin()).thenReturn(true);
        when(taskRepository.searchTasks(null, null, null, null, null, null, pageable))
                .thenReturn(taskPage);
        when(taskMapper.toDto(existingTask)).thenReturn(existingTaskDto);

        Page<TaskDTO> result = taskService.getTasks(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(existingTaskDto, result.getContent().get(0));
    }

    @Test
    void getAllTasks_returnsAllTasksMappedToDto() {
        when(taskRepository.findAll()).thenReturn(List.of(existingTask));
        when(taskMapper.toDto(existingTask)).thenReturn(existingTaskDto);

        List<TaskDTO> result = taskService.getAllTasks();

        assertEquals(1, result.size());
        assertEquals(existingTaskDto, result.get(0));
    }

    @Test
    void getTaskById_returnsDto_whenTaskExistsAndUserCanAccess() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(existingTask));
        when(permissionChecker.canAccessTask(existingTask)).thenReturn(true);
        when(taskMapper.toDto(existingTask)).thenReturn(existingTaskDto);

        TaskDTO result = taskService.getTaskById(100L);

        assertEquals(existingTaskDto, result);
    }

    @Test
    void getTaskById_throwsTaskNotFoundException_whenTaskDoesNotExist() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(999L));

        verify(permissionChecker, never()).canAccessTask(any());
        verify(taskMapper, never()).toDto(any());
    }

    @Test
    void getTaskById_throwsAccessDeniedException_whenUserCannotAccessTask() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(existingTask));
        when(permissionChecker.canAccessTask(existingTask)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(100L));

        verify(taskMapper, never()).toDto(any());
    }

    @Test
    void addTask_savesAndReturnsDto_whenStatusAndUserExist() {
        Task unsavedTask = Task.builder()
                .taskName("Write handover")
                .statusType(pendingStatus)
                .user(assignedUser)
                .dueDate(existingTaskDto.getDueDate())
                .build();

        when(statusTypeRepository.findById("status-1")).thenReturn(Optional.of(pendingStatus));
        when(userRepository.findById(1L)).thenReturn(Optional.of(assignedUser));
        when(taskMapper.toEntity(existingTaskDto, pendingStatus, assignedUser)).thenReturn(unsavedTask);
        when(taskRepository.save(unsavedTask)).thenReturn(existingTask);
        when(taskMapper.toDto(existingTask)).thenReturn(existingTaskDto);

        TaskDTO result = taskService.addTask(existingTaskDto);

        assertEquals(existingTaskDto, result);
        verify(taskRepository, times(1)).save(unsavedTask);
    }

    @Test
    void addTask_throwsRuntimeException_whenStatusTypeDoesNotExist() {
        TaskDTO dtoWithBadStatus = TaskDTO.builder()
                .taskName("Write handover")
                .statusTypeId("missing-status")
                .userId(1L)
                .dueDate(existingTaskDto.getDueDate())
                .build();

        when(statusTypeRepository.findById("missing-status")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.addTask(dtoWithBadStatus));

        verify(userRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void addTask_throwsRuntimeException_whenUserDoesNotExist() {
        TaskDTO dtoWithBadUser = TaskDTO.builder()
                .taskName("Write handover")
                .statusTypeId("status-1")
                .userId(999L)
                .dueDate(existingTaskDto.getDueDate())
                .build();

        when(statusTypeRepository.findById("status-1")).thenReturn(Optional.of(pendingStatus));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.addTask(dtoWithBadUser));

        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_updatesFieldsAndReturnsDto_whenAllowed() {
        TaskDTO updateDto = TaskDTO.builder()
                .taskName("Write handover v2")
                .statusTypeId("status-1")
                .userId(1L)
                .dueDate(LocalDateTime.of(2026, 8, 15, 9, 0))
                .build();

        when(taskRepository.findById(100L)).thenReturn(Optional.of(existingTask));
        when(permissionChecker.canAccessTask(existingTask)).thenReturn(true);
        when(statusTypeRepository.findById("status-1")).thenReturn(Optional.of(pendingStatus));
        when(userRepository.findById(1L)).thenReturn(Optional.of(assignedUser));
        when(taskMapper.toDto(existingTask)).thenReturn(updateDto);

        TaskDTO result = taskService.updateTask(100L, updateDto);

        assertEquals(updateDto, result);
        assertEquals("Write handover v2", existingTask.getTaskName());
        assertEquals(LocalDateTime.of(2026, 8, 15, 9, 0), existingTask.getDueDate());
    }

    @Test
    void updateTask_throwsTaskNotFoundException_whenTaskDoesNotExist() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(999L, existingTaskDto));

        verify(permissionChecker, never()).canAccessTask(any());
    }

    @Test
    void updateTask_throwsAccessDeniedException_whenUserCannotModifyTask() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(existingTask));
        when(permissionChecker.canAccessTask(existingTask)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> taskService.updateTask(100L, existingTaskDto));

        assertEquals("Write handover", existingTask.getTaskName());
    }

    @Test
    void deleteTask_deletesTask_whenAllowed() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(existingTask));
        when(permissionChecker.canAccessTask(existingTask)).thenReturn(true);

        taskService.deleteTask(100L);

        verify(taskRepository, times(1)).deleteById(100L);
    }

    @Test
    void deleteTask_throwsTaskNotFoundException_whenTaskDoesNotExist() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(999L));

        verify(permissionChecker, never()).canAccessTask(any());
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void deleteTask_throwsAccessDeniedException_whenUserCannotDeleteTask() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(existingTask));
        when(permissionChecker.canAccessTask(existingTask)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> taskService.deleteTask(100L));

        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void transferTasks_reassignsOnlyMatchingTasksToNewUser() {
        User newOwner = User.builder().userId(3L).email("new-owner@example.com").build();

        Task matchingTask = Task.builder()
                .taskId(100L)
                .taskName("Write handover")
                .user(assignedUser)
                .build();
        Task unrelatedTask = Task.builder()
                .taskId(200L)
                .taskName("Unrelated task")
                .user(User.builder().userId(9L).email("other@example.com").build())
                .build();
        Task nullUserTask = Task.builder()
                .taskId(300L)
                .taskName("Unassigned task")
                .user(null)
                .build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(newOwner));
        when(taskRepository.findAll()).thenReturn(List.of(matchingTask, unrelatedTask, nullUserTask));

        taskService.transferTasks(1L, 3L);

        assertEquals(newOwner, matchingTask.getUser());
        assertEquals(9L, unrelatedTask.getUser().getUserId());
        assertNull(nullUserTask.getUser());
    }

    @Test
    void transferTasks_throwsRuntimeException_whenToUserDoesNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.transferTasks(1L, 999L));

        verify(taskRepository, never()).findAll();
    }

    // Cazul 1 - ADMIN
    @Test
    void searchTasks_passesNullOwnerEmail_whenUserIsAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(existingTask), pageable, 1);

        when(permissionChecker.isCurrentUserAdmin()).thenReturn(true);
        when(taskRepository.searchTasks(null, "handover", null, null, null, null, pageable))
                .thenReturn(taskPage);
        when(taskMapper.toDto(existingTask)).thenReturn(existingTaskDto);

        Page<TaskDTO> result = taskService.searchTasks("handover", null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(existingTaskDto, result.getContent().get(0));
        verify(permissionChecker, never()).getCurrentUserEmail();
    }

    // Cazul 2 - USER
    @Test
    void searchTasks_passesCurrentUserEmail_whenUserIsNotAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(existingTask), pageable, 1);

        when(permissionChecker.isCurrentUserAdmin()).thenReturn(false);
        when(permissionChecker.getCurrentUserEmail()).thenReturn("andrei.krp@gmail.com");
        when(taskRepository.searchTasks("andrei.krp@gmail.com", null, null, null, null, null, pageable))
                .thenReturn(taskPage);
        when(taskMapper.toDto(existingTask)).thenReturn(existingTaskDto);

        Page<TaskDTO> result = taskService.searchTasks(null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    // Cazul 3 - Cacul interval de zi
    @Test
    void searchTasks_computesStartAndEndOfDay_whenDueDateProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate searchDate = LocalDate.of(2026, 8, 10);
        LocalDateTime expectedStart = LocalDateTime.of(2026, 8, 10, 0, 0);
        LocalDateTime expectedEnd = LocalDateTime.of(2026, 8, 11, 0, 0);
        Page<Task> taskPage = new PageImpl<>(List.of(existingTask), pageable, 1);

        when(permissionChecker.isCurrentUserAdmin()).thenReturn(true);
        when(taskRepository.searchTasks(null, null, null, null, expectedStart, expectedEnd, pageable))
                .thenReturn(taskPage);
        when(taskMapper.toDto(existingTask)).thenReturn(existingTaskDto);

        Page<TaskDTO> result = taskService.searchTasks(null, null, null, searchDate, pageable);

        assertEquals(1, result.getTotalElements());
    }
}
package com.example.tasks.service;

import com.example.tasks.domain.StatusType;
import com.example.tasks.domain.Task;
import com.example.tasks.dto.StatusTypeDTO;
import com.example.tasks.mapper.StatusTypeMapper;
import com.example.tasks.repository.StatusTypeRepository;
import com.example.tasks.repository.TaskRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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
    private final TaskRepository taskRepository;

    public List<StatusTypeDTO> getAllStatuses() {
        log.info("Statuses retrieved!");
        return statusTypeRepository.findAll()
                .stream()
                .map(statusTypeMapper::toDto)
                .toList();
    }

    @Transactional
    public StatusTypeDTO createStatus(@Valid StatusTypeDTO statusTypeDTO) {
        log.info("Status created!");
        StatusType status = statusTypeMapper.toEntity(statusTypeDTO);
        StatusType savedStatus = statusTypeRepository.save(status);

        return statusTypeMapper.toDto(savedStatus);
    }

    @Transactional
    public void deleteStatusAndReassignTasks(String statusTypeIdToDelete, String replacementStatusTypeId) {
        log.info("Deleting status {} and reassigning its tasks to status {}", statusTypeIdToDelete, replacementStatusTypeId);
        StatusType replacementStatus = statusTypeRepository.findById(replacementStatusTypeId)
                .orElseThrow(() -> new RuntimeException("Replacement status not found: " + replacementStatusTypeId));

        List<Task> affectedTasks = taskRepository.findAll().stream()
                .filter(task -> task.getStatusType() != null && task.getStatusType().getStatusTypeId().equals(statusTypeIdToDelete))
                .toList();

        for (Task task : affectedTasks) {
            task.setStatusType(replacementStatus);
        }

        statusTypeRepository.deleteById(statusTypeIdToDelete);
    }
}

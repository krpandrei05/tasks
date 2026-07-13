package com.example.tasks.repository;

import com.example.tasks.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Query method
    List<Task> findByStatusType_StatusName(String statusName);

    List<Task> findByDueDateBefore(LocalDateTime date);

    // @Query explicit
    @Query("SELECT t FROM Task t WHERE t.dueDate < :date AND t.statusType.statusName != 'Completed'")
    List<Task> findOverdueTasksExcludingDone(@Param("date") LocalDateTime date);
}
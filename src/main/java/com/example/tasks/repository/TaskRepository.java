package com.example.tasks.repository;

import com.example.tasks.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // @Query explicit
    @Query("SELECT t FROM Task t WHERE t.dueDate < :date AND t.statusType.statusName != 'Completed'")
    List<Task> findOverdueTasksExcludingDone(@Param("date") LocalDateTime date);

    @Query("SELECT t FROM Task t " +
            "LEFT JOIN t.user u " +
            "LEFT JOIN t.statusType s " +
            "WHERE (:ownerEmail IS NULL OR u.email = :ownerEmail) " +
            "AND (:taskName IS NULL OR LOWER(t.taskName) LIKE LOWER(CONCAT('%', :taskName, '%'))) " +
            "AND (:statusName IS NULL OR s.statusName = :statusName) " +
            "AND (:username IS NULL OR u.username = :username) " +
            "AND (:startOfDay IS NULL OR t.dueDate >= :startOfDay) " +
            "AND (:endOfDay IS NULL OR t.dueDate < :endOfDay)")
    Page<Task> searchTasks(
            @Param("ownerEmail") String ownerEmail,
            @Param("taskName") String taskName,
            @Param("statusName") String statusName,
            @Param("username") String username,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            // OFFSET/FETCH la final de Query
            Pageable pageable
    );
}
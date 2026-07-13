package com.example.tasks.repository;

import com.example.tasks.domain.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusTypeRepository extends JpaRepository<StatusType, String> {
    Optional<StatusType> findByStatusName(String statusName);
}
package com.example.tasks.repository;

import com.example.tasks.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByIsInternal(Integer isInternal);
    Optional<User> findByEmail(String email);
}
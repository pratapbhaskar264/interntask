package com.primetrade.taskmanager.repository;

import com.primetrade.taskmanager.model.Task;
import com.primetrade.taskmanager.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByOwner(User owner, Pageable pageable);
    Optional<Task> findByIdAndOwner(Long id, User owner);
}

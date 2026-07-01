package com.primetrade.taskmanager.service;

import com.primetrade.taskmanager.dto.TaskRequest;
import com.primetrade.taskmanager.dto.TaskResponse;
import com.primetrade.taskmanager.exception.ResourceNotFoundException;
import com.primetrade.taskmanager.model.Role;
import com.primetrade.taskmanager.model.Task;
import com.primetrade.taskmanager.model.TaskStatus;
import com.primetrade.taskmanager.model.User;
import com.primetrade.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public TaskResponse createTask(TaskRequest request, User currentUser) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.PENDING)
                .owner(currentUser)
                .build();

        Task saved = taskRepository.save(task);
        return TaskResponse.fromEntity(saved);
    }

    /**
     * Users only see their own tasks; admins can see every task in the system.
     */
    public Page<TaskResponse> listTasks(User currentUser, Pageable pageable) {
        Page<Task> tasks = currentUser.getRole() == Role.ADMIN
                ? taskRepository.findAll(pageable)
                : taskRepository.findByOwner(currentUser, pageable);

        return tasks.map(TaskResponse::fromEntity);
    }

    public TaskResponse getTask(Long id, User currentUser) {
        Task task = findAccessibleTask(id, currentUser);
        return TaskResponse.fromEntity(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, User currentUser) {
        Task task = findAccessibleTask(id, currentUser);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        Task saved = taskRepository.save(task);
        return TaskResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteTask(Long id, User currentUser) {
        Task task = findAccessibleTask(id, currentUser);
        taskRepository.delete(task);
    }

    private Task findAccessibleTask(Long id, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        }
        return taskRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }
}

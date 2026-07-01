package com.primetrade.taskmanager.controller;

import com.primetrade.taskmanager.dto.TaskRequest;
import com.primetrade.taskmanager.dto.TaskResponse;
import com.primetrade.taskmanager.model.User;
import com.primetrade.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * CRUD endpoints for the Task entity. Every endpoint requires a valid JWT.
 * Regular users can only access their own tasks; admins can access all tasks
 * (enforced in TaskService, not just at the URL level).
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tasks", description = "CRUD operations for tasks (protected, requires JWT)")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task owned by the current user")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request,
                                                     @AuthenticationPrincipal User currentUser) {
        TaskResponse response = taskService.createTask(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List tasks (own tasks for USER, all tasks for ADMIN), paginated")
    public ResponseEntity<Page<TaskResponse>> listTasks(@AuthenticationPrincipal User currentUser,
                                                          @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(taskService.listTasks(currentUser, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single task by id")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id,
                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.getTask(id, currentUser));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing task")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                     @Valid @RequestBody TaskRequest request,
                                                     @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.updateTask(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,
                                            @AuthenticationPrincipal User currentUser) {
        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}

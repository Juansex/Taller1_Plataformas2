package com.example.clean.usecase;

import com.example.clean.domain.Task;
import com.example.clean.domain.TaskRepository;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso - Lógica de negocio
 * Esta capa orquesta las operaciones del dominio
 * No depende de frameworks, solo del dominio
 */
public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task createTask(String title, String description) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        Task task = new Task(title, description);
        return repository.save(task);
    }

    public List<Task> getAllTasks() {
        return repository.findAll();
    }

    public Optional<Task> getTaskById(String id) {
        return repository.findById(id);
    }

    public Task completeTask(String id) {
        Task task = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada: " + id));
        Task completedTask = task.markAsCompleted();
        return repository.save(completedTask);
    }

    public void deleteTask(String id) {
        repository.deleteById(id);
    }
}

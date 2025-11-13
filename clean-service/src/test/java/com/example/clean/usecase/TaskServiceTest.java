package com.example.clean.usecase;

import com.example.clean.domain.Task;
import com.example.clean.domain.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TaskService
 * Probamos la lógica de negocio de forma aislada usando mocks
 */
class TaskServiceTest {

    private TaskRepository mockRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        mockRepository = mock(TaskRepository.class);
        taskService = new TaskService(mockRepository);
    }

    @Test
    @DisplayName("Debe crear una tarea correctamente")
    void testCreateTask() {
        // Arrange
        String title = "Tarea de prueba";
        String description = "Descripción de prueba";
        Task expectedTask = new Task(title, description);
        when(mockRepository.save(any(Task.class))).thenReturn(expectedTask);

        // Act
        Task result = taskService.createTask(title, description);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        verify(mockRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el título está vacío")
    void testCreateTaskWithEmptyTitle() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask("", "descripción");
        });
        verify(mockRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Debe obtener todas las tareas")
    void testGetAllTasks() {
        // Arrange
        List<Task> expectedTasks = Arrays.asList(
            new Task("Tarea 1", "Desc 1"),
            new Task("Tarea 2", "Desc 2")
        );
        when(mockRepository.findAll()).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskService.getAllTasks();

        // Assert
        assertEquals(2, result.size());
        verify(mockRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe completar una tarea existente")
    void testCompleteTask() {
        // Arrange
        String taskId = "123";
        Task existingTask = new Task(taskId, "Tarea", "Desc", false);
        Task completedTask = existingTask.markAsCompleted();
        
        when(mockRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(mockRepository.save(any(Task.class))).thenReturn(completedTask);

        // Act
        Task result = taskService.completeTask(taskId);

        // Assert
        assertTrue(result.isCompleted());
        verify(mockRepository, times(1)).findById(taskId);
        verify(mockRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al completar tarea inexistente")
    void testCompleteNonExistentTask() {
        // Arrange
        String taskId = "999";
        when(mockRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.completeTask(taskId);
        });
        verify(mockRepository, never()).save(any(Task.class));
    }
}

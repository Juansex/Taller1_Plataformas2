package com.example.clean.adapter.inbound;

import com.example.clean.CleanServiceApplication;
import com.example.clean.domain.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para el TaskController
 * Probamos la API REST completa con Spring Boot
 */
@SpringBootTest(
    classes = CleanServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class TaskControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/tasks";
    }

    @Test
    @DisplayName("Debe crear y obtener una tarea")
    void testCreateAndGetTask() {
        // Crear tarea
        TaskController.CreateTaskRequest request = 
            new TaskController.CreateTaskRequest("Nueva tarea", "Descripción de prueba");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TaskController.CreateTaskRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Task> createResponse = restTemplate.postForEntity(
            baseUrl, 
            entity, 
            Task.class
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertEquals("Nueva tarea", createResponse.getBody().getTitle());

        // Obtener todas las tareas
        ResponseEntity<Task[]> getResponse = restTemplate.getForEntity(
            baseUrl, 
            Task[].class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertTrue(getResponse.getBody().length > 0);
    }

    @Test
    @DisplayName("Debe completar una tarea")
    void testCompleteTask() {
        // Crear tarea
        TaskController.CreateTaskRequest request = 
            new TaskController.CreateTaskRequest("Tarea a completar", "Descripción");
        
        ResponseEntity<Task> createResponse = restTemplate.postForEntity(
            baseUrl, 
            request, 
            Task.class
        );

        String taskId = createResponse.getBody().getId();

        // Completar tarea
        ResponseEntity<Task> completeResponse = restTemplate.exchange(
            baseUrl + "/" + taskId + "/complete",
            HttpMethod.PUT,
            null,
            Task.class
        );

        assertEquals(HttpStatus.OK, completeResponse.getStatusCode());
        assertTrue(completeResponse.getBody().isCompleted());
    }

    @Test
    @DisplayName("Debe eliminar una tarea")
    void testDeleteTask() {
        // Crear tarea
        TaskController.CreateTaskRequest request = 
            new TaskController.CreateTaskRequest("Tarea a eliminar", "Descripción");
        
        ResponseEntity<Task> createResponse = restTemplate.postForEntity(
            baseUrl, 
            request, 
            Task.class
        );

        String taskId = createResponse.getBody().getId();

        // Eliminar tarea
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            baseUrl + "/" + taskId,
            HttpMethod.DELETE,
            null,
            Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // Verificar que no existe
        ResponseEntity<Task> getResponse = restTemplate.getForEntity(
            baseUrl + "/" + taskId,
            Task.class
        );

        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
}

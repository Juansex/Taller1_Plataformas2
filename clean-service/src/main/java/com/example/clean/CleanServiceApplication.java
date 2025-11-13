package com.example.clean;

import com.example.clean.adapter.outbound.InMemoryTaskRepository;
import com.example.clean.domain.TaskRepository;
import com.example.clean.usecase.TaskService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Aplicación principal Spring Boot
 * Aquí configuramos la inyección de dependencias siguiendo Clean Architecture
 */
@SpringBootApplication
public class CleanServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CleanServiceApplication.class, args);
    }

    /**
     * Bean del caso de uso TaskService
     * Inyectamos el repositorio (puerto de salida)
     */
    @Bean
    public TaskService taskService(TaskRepository taskRepository) {
        return new TaskService(taskRepository);
    }
}

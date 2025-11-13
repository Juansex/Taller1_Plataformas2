package com.example.clean.domain;

import java.util.List;
import java.util.Optional;

/**
 * Interface del repositorio - Puerto de salida del dominio
 * Define el contrato sin implementación específica
 */
public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(String id);
    List<Task> findAll();
    void deleteById(String id);
}

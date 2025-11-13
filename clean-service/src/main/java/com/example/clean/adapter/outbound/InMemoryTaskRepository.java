package com.example.clean.adapter.outbound;

import com.example.clean.domain.Task;
import com.example.clean.domain.TaskRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador de persistencia - Implementación en memoria
 * Este adaptador implementa el puerto de salida definido en el dominio
 * Aquí sí usamos anotaciones de Spring (@Repository)
 */
@Repository
public class InMemoryTaskRepository implements TaskRepository {
    
    private final Map<String, Task> storage = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        storage.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(String id) {
        storage.remove(id);
    }
}

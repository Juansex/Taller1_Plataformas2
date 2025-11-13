package com.example.clean.domain;

import java.util.UUID;

/**
 * Entidad del dominio - Representa una tarea
 * Esta es la capa de dominio puro, sin dependencias de frameworks
 */
public class Task {
    private String id;
    private String title;
    private String description;
    private boolean completed;

    // Constructor vacío para Jackson
    public Task() {
    }

    public Task(String title, String description) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.completed = false;
    }

    public Task(String id, String title, String description, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Task markAsCompleted() {
        return new Task(this.id, this.title, this.description, true);
    }
}

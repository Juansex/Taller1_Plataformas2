# Guion de Presentación: Microservicio con Clean Architecture

**Duración estimada:** 30 minutos  
**Objetivo:** Demostrar la implementación de un microservicio siguiendo los principios de Clean Architecture

---

## Parte 1: Introducción y Contexto (5 minutos)

### Presentación del Proyecto

"Este proyecto implementa un microservicio de gestión de tareas (TODO) utilizando Java Spring Boot y siguiendo estrictamente los principios de Clean Architecture propuestos por Robert C. Martin."

**Objetivos del proyecto:**
- Demostrar la separación de responsabilidades en capas arquitectónicas
- Implementar un sistema desacoplado de frameworks específicos
- Garantizar alta testabilidad mediante inyección de dependencias
- Aplicar el principio de inversión de dependencias

### Fundamentos de Clean Architecture

**Explicación conceptual:**

"Clean Architecture propone organizar el código en capas concéntricas donde las dependencias siempre apuntan hacia el centro, hacia el dominio de negocio."

**Dirección de dependencias:** Siempre hacia adentro (hacia el dominio)

---

## Parte 2: Demostración del Código 

### Estructura del Proyecto

```bash
cd /workspaces/Taller1_Plataformas2/clean-service
tree src/main/java/com/example/clean
```

### 1. Capa de Dominio 

**Archivo:** `src/main/java/com/example/clean/domain/Task.java`

```java
public class Task {
    private String id;
    private String title;
    private String description;
    private boolean completed;
    // ...
}
```

**Explicar:**
- Entidad pura de dominio
- No tiene anotaciones de Spring ni Jackson (excepto mínimas para serialización)
- Contiene reglas de negocio: `markAsCompleted()`

**Archivo:** `src/main/java/com/example/clean/domain/TaskRepository.java`

```java
public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(String id);
    List<Task> findAll();
    void deleteById(String id);
}
```

**Explicar:**
- **Puerto de salida** (Outbound Port)
- Interface del dominio que define CÓMO guardar tareas
- No dice DÓNDE (memoria, BD, archivo...)

---

### 2. Capa de Casos de Uso (Use Case Layer)

**Archivo:** `src/main/java/com/example/clean/usecase/TaskService.java`

```java
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
    // ...
}
```

**Explicar:**
- Contiene la lógica de aplicación
- Valida reglas de negocio
- **Inyección de dependencias** por constructor
- No depende de Spring, solo del dominio

---

### 3. Adaptadores (Adapters Layer)

#### Adaptador de Salida (Outbound Adapter)

**Archivo:** `src/main/java/com/example/clean/adapter/outbound/InMemoryTaskRepository.java`

```java
@Repository
public class InMemoryTaskRepository implements TaskRepository {
    private final Map<String, Task> storage = new ConcurrentHashMap<>();
    
    @Override
    public Task save(Task task) {
        storage.put(task.getId(), task);
        return task;
    }
    // ...
}
```

**Explicar:**
- Implementa la interface del dominio (`TaskRepository`)
- **Aquí sí** usamos anotaciones de Spring (`@Repository`)
- Almacenamiento en memoria (podría ser JPA, MongoDB, etc.)
- **Polimorfismo:** cambiamos la implementación sin tocar el dominio

#### Adaptador de Entrada (Inbound Adapter)

**Archivo:** `src/main/java/com/example/clean/adapter/inbound/TaskController.java`

```java
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(request.getTitle(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }
    // ...
}
```

**Explicar:**
- Expone la API REST
- Adapta HTTP requests → Casos de uso
- Maneja DTOs y conversiones
- **Inyección de dependencias** de `TaskService`

---

### 4. Configuración de Spring Boot

**Archivo:** `src/main/java/com/example/clean/CleanServiceApplication.java`

```java
@SpringBootApplication
public class CleanServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CleanServiceApplication.class, args);
    }
    
    @Bean
    public TaskService taskService(TaskRepository taskRepository) {
        return new TaskService(taskRepository);
    }
}
```

**Explicar:**
- Configuración de dependencias manual
- `TaskService` NO tiene `@Service` - es un POJO puro
- Spring inyecta `TaskRepository` (que sí tiene `@Repository`)

---

## Parte 4: Tests (3 minutos)

### Test Unitario (Uso de Mocks)

**Archivo:** `src/test/java/com/example/clean/usecase/TaskServiceTest.java`

```bash
cd /workspaces/Taller1_Plataformas2/clean-service
./mvnw test -Dtest=TaskServiceTest
```

**Explicar:**
- Usa Mockito para simular el repositorio
- Prueba solo la lógica de negocio
- No necesita Spring ni base de datos
- Tests rápidos (~1 segundo)

### Test de Integración

**Archivo:** `src/test/java/com/example/clean/adapter/inbound/TaskControllerIntegrationTest.java`

```bash
./mvnw test -Dtest=TaskControllerIntegrationTest
```

**Explicar:**
- Levanta Spring Boot completo
- Prueba endpoints HTTP reales
- Verifica la integración entre capas

---

## Parte 5: Dockerización y Ejecución (4 minutos)

### Dockerfile Multi-Stage

**Archivo:** `Dockerfile`

**Etapa 1: Build**
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml mvnw ./
COPY src ./src
RUN ./mvnw clean package -DskipTests
```

**Etapa 2: Runtime**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY --from=builder /app/target/clean-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Explicar:**
- Multi-stage para reducir tamaño de imagen
- Solo el JAR final va a producción
- Healthcheck incluido

### Docker Compose

**Archivo:** `docker-compose.yml`

```bash
cd /workspaces/Taller1_Plataformas2
docker compose up --build
```

**Mostrar logs:**
```bash
docker compose logs -f clean-service
```

---

## Parte 6: Demostración en Vivo (3 minutos)

### Probar endpoints con curl

**1. Crear una tarea:**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Presentar taller","description":"Explicar Clean Architecture"}'
```

**2. Listar tareas:**
```bash
curl http://localhost:8080/api/tasks | jq '.'
```

**3. Completar tarea:**
```bash
# Usar el ID de la respuesta anterior
curl -X PUT http://localhost:8080/api/tasks/{ID}/complete
```

**4. Eliminar tarea:**
```bash
curl -X DELETE http://localhost:8080/api/tasks/{ID}
```

---

## Parte 7: Ventajas de Clean Architecture (2 minutos)

### Comparación con arquitectura tradicional

| Aspecto | Tradicional | Clean Architecture |
|---------|-------------|-------------------|
| **Acoplamiento** | Alto (todo depende del framework) | Bajo (dominio independiente) |
| **Testabilidad** | Difícil (necesita BD y Spring) | Fácil (mocks simples) |
| **Cambio de BD** | Refactor masivo | Solo cambiar adaptador |
| **Cambio de framework** | Reescribir todo | Solo cambiar adaptadores |
| **Velocidad de tests** | Lentos (segundos) | Rápidos (milisegundos) |

### Ejemplo práctico: Cambiar de In-Memory a PostgreSQL

**Solo tocar 2 archivos:**
1. Añadir dependencia en `pom.xml`
2. Crear `PostgresTaskRepository implements TaskRepository`

**NO tocar:**
- `Task.java` (dominio)
- `TaskService.java` (caso de uso)
- `TaskController.java` (adaptador HTTP)

---

## Parte 8: Relación con el Repo Original (2 minutos)

### Cómo aplicar Clean Architecture al repo microservice-app-example

**TODOs API (Node.js)** podría refactorizarse:

```
todos-api/
├── domain/
│   ├── todo.js          # Entidad
│   └── todoRepository.js # Interface
├── usecase/
│   └── todoService.js   # Lógica de negocio
├── adapters/
│   ├── inbound/
│   │   └── todoController.js  # Express routes
│   └── outbound/
│       ├── mongoTodoRepository.js
│       └── redisPublisher.js
└── app.js  # Configuración
```

**Beneficios:**
- Tests más fáciles (mockear Redis y MongoDB)
- Cambiar de Express a Fastify sin tocar lógica
- Reutilizar lógica en CLI o workers

---

## Conclusión (1 minuto)

### Resumen

✅ **Clean Architecture** separa el dominio de los detalles técnicos  
✅ **Facilita testing** con mocks simples  
✅ **Reduce acoplamiento** entre capas  
✅ **Permite cambios** de framework, BD o UI sin refactorizar todo  
✅ **Escalable** para proyectos grandes y equipos distribuidos  

### Recursos

- Código completo: `/workspaces/Taller1_Plataformas2/clean-service`
- Repo original: https://github.com/Barcino44/microservice-app-example
- Tests: `./mvnw test`
- Docker: `docker compose up`

---

## Preguntas Frecuentes del Profesor

**P: ¿Por qué no usar `@Service` en `TaskService`?**  
R: Para mantenerlo como POJO puro sin dependencias de Spring. La configuración manual en `@Bean` hace explícita la inyección.

**P: ¿Cuándo NO usar Clean Architecture?**  
R: En proyectos muy pequeños (< 5 clases) donde la simplicidad es más importante que la escalabilidad.

**P: ¿Cómo se compara con MVC?**  
R: MVC organiza por tipo técnico (Controller, Model, View). Clean Architecture organiza por flujo de negocio (dominio → usecase → adapters).

**P: ¿Funciona con otros frameworks (NestJS, Flask, etc.)?**  
R: Sí, el patrón es independiente del lenguaje y framework.

---

## Comandos para Demostración en Vivo

```bash
# Ejecutar tests
cd /workspaces/Taller1_Plataformas2/clean-service
./mvnw test

# Levantar servicio
cd /workspaces/Taller1_Plataformas2
docker compose up --build -d

# Ver logs
docker compose logs -f clean-service

# Verificar estado
docker compose ps

# Probar API
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","description":"Demo"}'

curl http://localhost:8080/api/tasks | jq

# Parar servicio
docker compose down
```

---

## Conclusión

Este proyecto demuestra la aplicación práctica de Clean Architecture en un microservicio empresarial, logrando un código mantenible, testeable y preparado para escalar según las necesidades del negocio.


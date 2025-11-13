# Microservicio de Gestión de Tareas con Clean Architecture

## Introducción

Este proyecto constituye una implementación original de un microservicio de gestión de tareas (TODO) utilizando los principios de **Clean Architecture**. El objetivo principal es demostrar cómo estructurar una aplicación empresarial siguiendo un diseño arquitectónico que separa las responsabilidades en capas bien definidas, facilitando el mantenimiento, las pruebas y la escalabilidad del sistema.

El desarrollo se centra en aplicar correctamente los conceptos fundamentales de Clean Architecture propuestos por Robert C. Martin, estableciendo una separación clara entre el dominio de negocio, los casos de uso y los adaptadores de infraestructura, logrando así un código desacoplado y altamente testeable.

---

## Evidencias de Implementación

### Estructura del Proyecto

![Estructura del Proyecto](docs/images/01-estructura-proyecto.png)

La arquitectura del proyecto sigue una organización en capas claramente definidas:

---

## Paso 1: Análisis de la Estructura del Proyecto

El proyecto `clean-service` ha sido desarrollado siguiendo estrictamente los principios de Clean Architecture, organizando el código en una estructura jerárquica que respeta las dependencias unidireccionales.

```bash
cd /workspaces/Taller1_Plataformas2/clean-service
tree src/main/java -L 3
```

La estructura resultante organiza el código en tres capas principales:

- **domain/**: Contiene las entidades de negocio y las interfaces (ports) que definen contratos sin depender de implementaciones concretas.
- **usecase/**: Implementa la lógica de aplicación, orquestando las operaciones del dominio.
- **adapter/**: Proporciona las implementaciones concretas tanto para la entrada (inbound) como para la salida (outbound) de datos.

Esta separación garantiza que el dominio permanezca independiente de frameworks y tecnologías específicas, permitiendo que la lógica de negocio sea reutilizable y fácilmente testeable.

### Capa de Dominio

![Entidad Task](docs/images/02-domain-task.png)

![Interfaz TaskRepository](docs/images/03-domain-repository.png)

La capa de dominio implementa entidades de negocio puras sin dependencias de frameworks.

### Capa de Casos de Uso

![TaskService](docs/images/04-usecase-service.png)

El servicio contiene la lógica de aplicación y las reglas de negocio.

### Capa de Adaptadores

![TaskController](docs/images/05-adapter-controller.png)

![InMemoryTaskRepository](docs/images/06-adapter-repository.png)

Los adaptadores conectan el núcleo de la aplicación con el mundo exterior.

---

## Paso 2: Exploración de la Capa de Dominio

La capa de dominio constituye el núcleo de la aplicación y define los conceptos fundamentales del negocio sin ninguna dependencia hacia infraestructura o frameworks externos.

```bash
cat src/main/java/com/example/clean/domain/Task.java
```

Esta clase representa la entidad `Task` con sus atributos esenciales (identificador, título, descripción y estado de completitud). Es importante notar que esta clase no contiene anotaciones de ningún framework (como `@Entity` de JPA o `@Service` de Spring), manteniéndose como un objeto Java puro (POJO).

```bash
cat src/main/java/com/example/clean/domain/TaskRepository.java
```

La interfaz `TaskRepository` define el contrato para las operaciones de persistencia sin especificar cómo se implementarán dichas operaciones. Este es un ejemplo clásico del patrón de inversión de dependencias: el dominio define qué necesita, pero no cómo se proporciona. La implementación concreta podría utilizar cualquier tecnología de persistencia (memoria, base de datos relacional, NoSQL, etc.) sin afectar al dominio.

---

## Paso 3: Análisis de la Capa de Casos de Uso

La capa de casos de uso contiene la lógica de aplicación que coordina las operaciones del dominio según las reglas de negocio establecidas.

```bash
cat src/main/java/com/example/clean/usecase/TaskService.java
```

La clase `TaskService` implementa las operaciones principales del sistema:
- **Creación de tareas**: Valida que el título no esté vacío antes de persistir la tarea.
- **Consulta de tareas**: Permite obtener todas las tareas o buscar una específica por su identificador.
- **Completar tareas**: Actualiza el estado de una tarea existente.
- **Eliminación de tareas**: Remueve una tarea del sistema.

Esta clase recibe el repositorio a través de su constructor (inyección de dependencias), pero no conoce la implementación concreta del mismo. Además, no contiene anotaciones de Spring, siendo configurada manualmente en la clase de aplicación principal para mantener la independencia del framework.

---

## Paso 4: Exploración de la Capa de Adaptadores

Los adaptadores son las implementaciones concretas que conectan el núcleo de la aplicación con el mundo exterior, ya sea para recibir peticiones o para persistir datos.

**Adaptador de Salida (Persistencia):**
```bash
cat src/main/java/com/example/clean/adapter/outbound/InMemoryTaskRepository.java
```

Esta clase implementa la interfaz `TaskRepository` utilizando un `ConcurrentHashMap` para almacenar las tareas en memoria. La anotación `@Repository` indica que es un componente de Spring, pero esta dependencia está contenida en la capa de adaptadores. Si en el futuro se requiere cambiar a una base de datos PostgreSQL o MongoDB, solo sería necesario crear un nuevo adaptador sin modificar el dominio ni los casos de uso.

**Adaptador de Entrada (Controlador REST):**
```bash
cat src/main/java/com/example/clean/adapter/inbound/TaskController.java
```

Este controlador expone los endpoints HTTP que permiten interactuar con el microservicio. Utiliza las anotaciones de Spring Web (`@RestController`, `@PostMapping`, etc.) para mapear las peticiones HTTP a los métodos correspondientes. El controlador transforma los datos de entrada (DTOs) y delega toda la lógica de negocio al `TaskService`, actuando únicamente como un adaptador entre el protocolo HTTP y la lógica de aplicación.

---

## Paso 5: Ejecución de las Pruebas

### Código de Tests

![Test Unitario](docs/images/07-test-unitario.png)

![Test de Integración](docs/images/08-test-integracion.png)

### Ejecución de Tests

![Ejecución de Tests](docs/images/09-tests-ejecutando.png)

![Resultados de Tests](docs/images/10-tests-resultados.png)

Las pruebas son un componente fundamental para garantizar la calidad y el correcto funcionamiento del sistema. El proyecto incluye tanto pruebas unitarias como pruebas de integración.

```bash
cd /workspaces/Taller1_Plataformas2/clean-service
./mvnw test
```

Este comando ejecutará el ciclo completo de pruebas:
1. Descarga de dependencias necesarias (en la primera ejecución)
2. Compilación del código fuente
3. Ejecución de todas las pruebas
4. Generación de reportes de resultados

**Verificación de resultados:**
```bash
cat target/surefire-reports/*.txt | grep "Tests run"
```

El resultado esperado debe mostrar que se ejecutaron 8 pruebas en total: 5 pruebas unitarias en `TaskServiceTest` y 3 pruebas de integración en `TaskControllerIntegrationTest`, todas sin errores ni fallos.

Las pruebas unitarias utilizan Mockito para simular el comportamiento del repositorio, permitiendo probar la lógica de negocio de forma aislada sin necesidad de levantar el contexto de Spring ni utilizar una base de datos real. Esto resulta en pruebas extremadamente rápidas y enfocadas exclusivamente en la lógica del caso de uso.

---

## Paso 6: Construcción de la Imagen Docker

### Archivos de Docker

![Dockerfile](docs/images/11-dockerfile.png)

![Docker Compose](docs/images/12-docker-compose.png)

### Proceso de Build

![Docker Build](docs/images/13-docker-build.png)

![Imagen Creada](docs/images/14-docker-image.png)

La contenedorización del microservicio mediante Docker permite garantizar que la aplicación se ejecute de manera consistente en cualquier entorno.

```bash
cd /workspaces/Taller1_Plataformas2
docker compose build
```

El proceso de construcción utiliza un Dockerfile multi-stage que optimiza el tamaño final de la imagen:

**Etapa 1 (Builder):** Utiliza una imagen con JDK completo y Maven para compilar el código fuente y generar el archivo JAR ejecutable.

**Etapa 2 (Runtime):** Copia únicamente el JAR compilado a una imagen con JRE (Java Runtime Environment), eliminando todas las herramientas de desarrollo y código fuente. Esto reduce significativamente el tamaño de la imagen final y mejora la seguridad al minimizar la superficie de ataque.

**Verificación:**
```bash
docker images | grep clean-service
```

Este comando debe mostrar la imagen creada con el nombre `taller1_plataformas2-clean-service` y su tamaño correspondiente.

---

## Paso 7: Despliegue del Microservicio

### Servicio en Ejecución

![Docker Compose Up](docs/images/15-docker-up.png)

![Contenedor Activo](docs/images/16-container-running.png)

![Logs del Servicio](docs/images/17-service-logs.png)

Una vez construida la imagen, se procede a iniciar el contenedor que ejecutará el microservicio.

```bash
docker compose up -d
```

El flag `-d` (detached) indica que el contenedor debe ejecutarse en segundo plano, liberando la terminal para continuar con otras operaciones.

**Verificación del estado:**
```bash
docker compose ps
```

El servicio debe aparecer con estado `Up` y la indicación `(healthy)`, lo que significa que el healthcheck configurado en Docker Compose está funcionando correctamente y el servicio está respondiendo a las peticiones.

**Inspección de logs:**
```bash
docker compose logs clean-service | tail -20
```

Los logs deben mostrar información similar a:
```
Started CleanServiceApplication in 4.611 seconds
Tomcat started on port(s): 8080 (http)
```

Esto confirma que Spring Boot ha inicializado correctamente y el servidor Tomcat está escuchando en el puerto 8080.

---

## Paso 8: Verificación de Funcionalidad - Operaciones CRUD

### Crear Tareas (POST)

![Crear Tarea 1](docs/images/18-api-post-1.png)

![Crear Tarea 2](docs/images/19-api-post-2.png)

![Crear Tarea 3](docs/images/20-api-post-3.png)

### Listar Tareas (GET)

![Listar Tareas](docs/images/21-api-get.png)

### Completar Tarea (PUT)

![Completar Tarea](docs/images/22-api-put.png)

![Verificar Completada](docs/images/23-api-get-completed.png)

### Eliminar Tarea (DELETE)

![Eliminar Tarea](docs/images/24-api-delete.png)

![Verificar Eliminación](docs/images/25-api-get-deleted.png)

Con el servicio en ejecución, se demuestran todas las operaciones CRUD sobre la API REST expuesta.

---

## Paso 9: Consulta de Todas las Tareas

Para obtener un listado completo de todas las tareas almacenadas en el sistema:

```bash
curl -s http://localhost:8080/api/tasks | jq '.'
```

El comando utiliza `jq` para formatear la respuesta JSON de manera legible. Si `jq` no está disponible en el sistema, se puede omitir esta parte y obtener la respuesta JSON sin formateo.

**Respuesta esperada:**
Un array JSON conteniendo todas las tareas creadas hasta el momento:
```json
[
  {
    "id": "402dbc48-5169-436a-beda-f79bc08fb7cb",
    "title": "Estudiar Clean Architecture",
    "description": "Repasar conceptos fundamentales para evaluación",
    "completed": false
  }
]
```

---

## Paso 10: Actualización del Estado de una Tarea

Para marcar una tarea como completada, se utiliza el endpoint correspondiente reemplazando `{ID}` con el identificador de la tarea:

```bash
curl -X PUT http://localhost:8080/api/tasks/{ID}/complete
```

**Ejemplo con identificador real:**
```bash
curl -X PUT http://localhost:8080/api/tasks/402dbc48-5169-436a-beda-f79bc08fb7cb/complete
```

**Verificación del cambio:**
```bash
curl -s http://localhost:8080/api/tasks | jq '.'
```

La respuesta debe mostrar la misma tarea pero con el campo `completed` ahora establecido en `true`, reflejando el cambio de estado realizado.

---

## Paso 11: Eliminación de Tareas

Para remover una tarea del sistema:

```bash
curl -X DELETE http://localhost:8080/api/tasks/{ID}
```

**Verificación de la eliminación:**
```bash
curl -s http://localhost:8080/api/tasks | jq '.'
```

Si la tarea eliminada era la única en el sistema, la respuesta será un array vacío: `[]`

---

## Paso 12: Monitoreo de Logs en Tiempo Real

Para observar el comportamiento interno del microservicio en tiempo real:

```bash
docker compose logs -f clean-service
```

Este comando muestra los logs del contenedor de forma continua (flag `-f` de follow). Se pueden observar todas las peticiones HTTP recibidas, las operaciones ejecutadas y cualquier mensaje de log generado por la aplicación.

Para detener la visualización de logs, presionar `Ctrl+C`.

---

## Paso 13: Detención del Servicio

Cuando se finaliza el trabajo con el microservicio:

```bash
docker compose down
```

Este comando detiene y elimina los contenedores en ejecución. Las imágenes Docker permanecen almacenadas localmente para facilitar futuros despliegues sin necesidad de reconstruirlas.

---

## Evidencias Adicionales

### Estadísticas del Proyecto

![Estadísticas del Código](docs/images/26-estadisticas.png)

![Historial de Commits](docs/images/27-git-log.png)

### Documentación

![README](docs/images/28-readme.png)

![Guión de Presentación](docs/images/29-guion.png)

### Monitoreo

![Logs en Tiempo Real](docs/images/30-logs.png)

### Detención del Servicio

![Docker Compose Down](docs/images/31-docker-down.png)

---

## Resultados Obtenidos

Este proyecto ha permitido demostrar los siguientes aspectos:

1. **Separación de responsabilidades**: El código está organizado en capas con dependencias unidireccionales (domain ← usecase ← adapters).

2. **Independencia del dominio**: Las entidades de negocio no dependen de frameworks externos, permitiendo cambios tecnológicos sin afectar la lógica central.

3. **Testabilidad**: Las pruebas unitarias se ejecutan sin necesidad de infraestructura, mientras que las pruebas de integración validan el funcionamiento completo del sistema.

4. **Contenedorización**: La aplicación está empaquetada en una imagen Docker optimizada, lista para despliegue en cualquier entorno compatible.

5. **API REST funcional**: El microservicio expone endpoints HTTP para todas las operaciones CRUD sobre tareas.

---

## Análisis Comparativo

**Enfoque Tradicional (Arquitectura en Capas):**
- La lógica de negocio está acoplada a frameworks específicos (Spring, JPA, etc.)
- Las pruebas requieren levantar el contexto completo de la aplicación
- Cambiar de tecnología implica modificaciones extensas en múltiples capas
- La estructura dificulta la comprensión de las reglas de negocio

**Enfoque Clean Architecture:**
- El dominio permanece independiente de frameworks y tecnologías
- Las pruebas son rápidas y focalizadas en la lógica de negocio
- Los cambios tecnológicos se aíslan en los adaptadores
- Cada capa tiene una responsabilidad claramente definida

---

## Estructura de Archivos del Proyecto

**Capa de Dominio:**
- `clean-service/src/main/java/com/example/clean/domain/Task.java`
- `clean-service/src/main/java/com/example/clean/domain/TaskRepository.java`

**Capa de Casos de Uso:**
- `clean-service/src/main/java/com/example/clean/usecase/TaskService.java`

**Capa de Adaptadores:**
- `clean-service/src/main/java/com/example/clean/adapter/inbound/TaskController.java`
- `clean-service/src/main/java/com/example/clean/adapter/outbound/InMemoryTaskRepository.java`

**Pruebas:**
- `clean-service/src/test/java/com/example/clean/usecase/TaskServiceTest.java`
- `clean-service/src/test/java/com/example/clean/adapter/inbound/TaskControllerIntegrationTest.java`

**Infraestructura:**
- `clean-service/Dockerfile`
- `docker-compose.yml`

**Documentación adicional:**
- `GUION_PRESENTACION.md` - Guion detallado para presentación académica de 30 minutos

---

## Secuencia de Comandos para Demostración

A continuación se presenta la secuencia completa de comandos para realizar una demostración del proyecto:

```bash
# 1. Visualización de la estructura del proyecto
tree clean-service/src/main/java -L 3

# 2. Ejecución de pruebas automatizadas
cd clean-service && ./mvnw test

# 3. Despliegue del microservicio
cd .. && docker compose up -d

# 4. Creación de una tarea de ejemplo
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Demostración en vivo","description":"Prueba de funcionalidad del microservicio"}'

# 5. Consulta de todas las tareas
curl -s http://localhost:8080/api/tasks | jq '.'

# 6. Inspección de logs del contenedor
docker compose logs clean-service | tail -20

# 7. Detención del servicio
docker compose down
```

---

## Conclusiones

Este proyecto constituye una implementación práctica de Clean Architecture aplicada a un microservicio de gestión de tareas. La arquitectura propuesta permite mantener el código organizado, testeable y preparado para evolucionar según los requisitos futuros, demostrando los beneficios de aplicar principios de diseño sólidos en el desarrollo de software empresarial.
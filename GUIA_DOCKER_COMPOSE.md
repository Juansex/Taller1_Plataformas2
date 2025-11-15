# Guia Docker Compose - Microservicios + Monitoreo

## Requisitos Previos

Asegúrate de tener instalados:
- Docker (versión 20.10+)
- Docker Compose (versión 1.29+)

Verifica:
```bash
docker --version
docker-compose --version
```

---

## PASO 1: Preparar el Entorno

### Comando 1.1
```bash
cd /ruta/a/tu/proyecto/Taller1_Plataformas2
```

**Explicación**: Navega a la carpeta raíz del proyecto donde está `docker-compose.yml`.

---

### Comando 1.2
```bash
docker system prune -a --volumes
```

**Explicación**: Limpia imágenes y volúmenes antiguos para evitar conflictos.

**Salida esperada**:
```
Deleted Images: 5
Deleted Volumes: 3
Total reclaimed space: 1.2GB
```

---

## PASO 2: Construir las Imágenes Docker

### Comando 2.1
```bash
docker-compose build
```

**Explicación**:
- Lee el `docker-compose.yml`
- Para cada servicio, executa su `Dockerfile`
- Construye imágenes Docker con el código compilado

**¿Qué hace?**
- Auth API → Compila código Go
- Users API → Compila código Java
- TODOs API → Instala dependencias Node.js
- Log Processor → Instala dependencias Python
- Frontend → Compila Vue.js, genera `/dist`, lo sirve con Nginx
- Prometheus y Grafana → Usan imágenes precompiladas

**Duración**: 10-15 minutos (la primera vez descarga muchas dependencias)

**Salida esperada**:
```
Building auth-api
Sending build context to Docker daemon 123.4kB
Step 1/6 : FROM golang:1.21-alpine AS builder
Step 2/6 : WORKDIR /app
...
Successfully built abc123def456

Building users-api
...
Successfully built def456ghi789

Building todos-api
...
Successfully built ghi789jkl012

Building frontend
...
Successfully built jkl012mno345

Building log-processor
...
Successfully built mno345pqr678
```

---

### Comando 2.2
```bash
docker images | grep microservices
```

**Explicación**: Verifica que todas las imágenes se construyeron.

**Salida esperada**:
```
REPOSITORY                TAG         IMAGE ID
taller1_auth-api          latest      abc123def456
taller1_users-api         latest      def456ghi789
taller1_todos-api         latest      ghi789jkl012
taller1_frontend          latest      jkl012mno345
taller1_log-processor     latest      mno345pqr678
```

---

## PASO 3: Iniciar los Servicios

### Comando 3.1
```bash
docker-compose up
```

**Explicación**:
- Crea contenedores basados en las imágenes
- Inicia todos en orden de dependencias:
  1. Redis (base de datos)
  2. Auth API, Users API, TODOs API (en paralelo)
  3. Log Processor (espera a Redis)
  4. Frontend (espera a los APIs)
  5. Prometheus (recolecta métricas)
  6. Grafana (dashboard de monitoreo)

**Espera a ver estas líneas (indicativo de que todo está listo)**:

```
redis       | Ready to accept connections
auth-api    | Listening on :8000
users-api   | Started UsersApiApplication
todos-api   | Server running on port 8082
log-processor | Esperando mensajes en la cola 'logs'...
frontend    | App running at: http://localhost:8080
prometheus  | Server is ready to receive requests
grafana     | HTTP Server Listen at [::]:3000
```

---

### Comando 3.2 (En otra terminal)
```bash
docker-compose ps
```

**Explicación**: Lista el estado de todos los contenedores.

**Salida esperada**:
```
NAME            COMMAND                 STATUS        PORTS
redis           redis-server            Up (healthy)  0.0.0.0:6379->6379/tcp
auth-api        ./auth-api              Up (healthy)  0.0.0.0:8000->8000/tcp
users-api       java -jar app.jar       Up (healthy)  0.0.0.0:8083->8083/tcp
todos-api       npm start               Up (healthy)  0.0.0.0:8082->8082/tcp
log-processor   python3 main.py         Up            
frontend        nginx -g daemon off     Up            0.0.0.0:8080->8080/tcp
prometheus      /bin/prometheus         Up            0.0.0.0:9090->9090/tcp
grafana         /run.sh                 Up            0.0.0.0:3000->3000/tcp
```

---

## PASO 4: Verificar Servicios

### Comando 4.1 (En nueva terminal)
```bash
curl http://localhost:8000/health
```

**Explicación**: Verifica que Auth API está respondiendo.

**Salida esperada**:
```json
{"status":"ok"}
```

---

### Comando 4.2
```bash
curl http://localhost:8083/users
```

**Explicación**: Obtiene lista de usuarios de Users API.

**Salida esperada**:
```json
[
  {
    "id": 1,
    "name": "Admin",
    "email": "admin@example.com"
  },
  ...
]
```

---

### Comando 4.3
```bash
curl -X POST http://localhost:8000/auth \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

**Explicación**: Obtiene un token JWT.

**Salida esperada**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": "admin"
}
```

**Guardar el token en una variable**:
```bash
TOKEN=$(curl -s -X POST http://localhost:8000/auth \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.token')

echo $TOKEN
```

---

### Comando 4.4
```bash
TOKEN="tu_token_aqui"

curl -H "Authorization: Bearer $TOKEN" http://localhost:8082/todos
```

**Explicación**: Obtiene lista de TODOs (requiere token válido).

**Salida esperada**:
```json
[]
```

(Vacío porque no hemos creado TODOs aún)

---

## PASO 5: Acceder a las Aplicaciones

Abre en tu navegador:

### 5.1 Frontend
```
http://localhost:8080
```

**Qué verás**: Página de login

**Login con**:
- Usuario: `admin`
- Contraseña: `admin`

**Después del login**:
- Formulario para crear TODOs
- Lista de TODOs existentes
- Botones para completar/eliminar

---

### 5.2 Prometheus (Métricas)
```
http://localhost:9090
```

**Qué verás**: Dashboard de Prometheus

**Ejemplo de búsqueda**:
1. Busca en "Expression Browser"
2. Escribe: `up`
3. Haz click en "Execute"
4. Ver estado de todos los servicios (1=up, 0=down)

---

### 5.3 Grafana (Monitoreo)
```
http://localhost:3000
```

**Login**:
- Usuario: `admin`
- Contraseña: `admin`

**Navegar**:
1. Haz click en el icono de Grafana (arriba izquierda)
2. Ve a "Dashboards"
3. Crea un nuevo dashboard o importa uno

---

## PASO 6: Crear TODOs y Ver Monitoreo

### En el Frontend (localhost:8080):

#### 6.1 Crear un TODO
```
1. Escribe en "Add a new todo" → "Completar prueba"
2. Haz click en "Add Todo"
3. Verifica que aparece en la lista
```

**¿Qué sucede internamente?**
1. Frontend envía POST a `http://auth-api:8082/todos` con token JWT
2. TODOs API valida el token contra Auth API
3. TODOs API guarda el TODO en Redis
4. Envía un mensaje a la cola de logs
5. Log Processor lee el mensaje y lo imprime

---

#### 6.2 Completar un TODO
```
1. Haz click en el checkbox del TODO
2. El TODO se marca como completado
```

---

#### 6.3 Eliminar un TODO
```
1. Haz click en el botón "Delete"
2. El TODO desaparece
```

---

### En Prometheus (localhost:9090):

#### 6.4 Ver solicitudes HTTP
```
En "Expression Browser" escribe:
rate(http_requests_total[1m])

Haz click en "Execute"
```

**¿Qué verás?** Número de solicitudes por segundo a cada servicio.

---

### En Grafana (localhost:3000):

#### 6.5 Crear un Dashboard
```
1. Click en "+" → "Dashboard"
2. Click en "Add a new panel"
3. Selecciona Prometheus como fuente
4. En "Metrics Browser" busca: http_requests_total
5. Cambia a "Graph" tipo de gráfica
6. Click en "Apply"
```

---

## PASO 7: Ver Logs en Tiempo Real

### Comando 7.1 - Ver logs de todos los servicios
```bash
docker-compose logs -f
```

**Explicación**: Muestra logs en vivo de todos los contenedores.

**Salida esperada**:
```
redis       | Ready to accept connections
auth-api    | Listening on :8000
users-api   | Started UsersApiApplication
log-processor | Operación: CREATE - usuario: admin - tarea: Completar prueba
```

---

### Comando 7.2 - Ver logs de un servicio específico
```bash
docker-compose logs -f log-processor
```

**Explicación**: Ver solo logs del procesador (donde aparecen las operaciones).

**Salida esperada cuando creas un TODO**:
```
log-processor | Operación: CREATE
log-processor | Usuario: admin
log-processor | Tarea: Completar prueba
log-processor | Timestamp: 2025-11-15 10:30:45
```

---

### Comando 7.3 - Ver logs de todos los APIs
```bash
docker-compose logs -f auth-api users-api todos-api
```

**Explicación**: Ver logs de los 3 APIs principales simultáneamente.

---

## 🛑 PASO 8: Detener Todo

### Comando 8.1 - Detener sin eliminar
```bash
docker-compose stop
```

**Explicación**: Pausa todos los contenedores (datos se conservan).

---

### Comando 8.2 - Eliminar todo
```bash
docker-compose down
```

**Explicación**: Detiene y elimina contenedores (pero conserva volúmenes de datos).

---

### Comando 8.3 - Limpiar todo incluyendo datos
```bash
docker-compose down -v
```

**Explicación**: Elimina contenedores, redes Y volúmenes (limpieza completa).

---

## Flujo Completo para el Video

**Tiempo estimado: 5 minutos de demostración**

### Minuto 0:00
```bash
docker-compose build
```
Mostrar compilación

---

### Minuto 1:00
```bash
docker-compose up
```
Mostrar inicio de todos los servicios

---

### Minuto 2:00
Abrir en navegador:
- `http://localhost:8080` → Login → Ver interfaz
- Crear un TODO: escribir "Tarea 1" y agregar
- Otra terminal: `docker-compose logs -f log-processor` → Ver operación

---

### Minuto 3:30
Abrir Prometheus:
- `http://localhost:9090`
- Ejecutar query: `rate(http_requests_total[1m])`
- Mostrar gráfico de solicitudes

---

### Minuto 4:00
Abrir Grafana:
- `http://localhost:3000`
- Crear dashboard simple
- Agregar panel con métrica

---

### Minuto 5:00
Volver a Frontend:
- Crear más TODOs
- Ver cómo cambian las métricas en Grafana en tiempo real

---

## 🆘 Solución de Problemas

### Problema: Puerto 8080 ya está en uso
```bash
# Ver qué está usando el puerto
lsof -i :8080

# O cambiar puerto en docker-compose.yml
# Cambiar "8080:8080" por "8081:8080"
```

---

### Problema: Redis no inicia
```bash
# Ver logs de Redis
docker-compose logs redis

# Reiniciar
docker-compose restart redis
```

---

### Problema: Frontend muestra error de conexión
```bash
# Verificar que los APIs están respondiendo
curl http://localhost:8000/health
curl http://localhost:8083/users
curl http://localhost:8082/todos

# Si alguno falla, ver sus logs
docker-compose logs auth-api
```

---

### Problema: Grafana no tiene datos
```bash
# Esperar 2-3 minutos (Prometheus necesita recolectar métricas)
# Ir a Prometheus y ejecutar: up
# Verificar que todos los servicios están "1" (up)
```

---

## Referencia Rápida de Puertos

| Servicio | Puerto | URL |
|----------|--------|-----|
| Redis | 6379 | redis:6379 (solo local) |
| Auth API | 8000 | http://localhost:8000 |
| TODOs API | 8082 | http://localhost:8082 |
| Users API | 8083 | http://localhost:8083 |
| Frontend | 8080 | http://localhost:8080 |
| Prometheus | 9090 | http://localhost:9090 |
| Grafana | 3000 | http://localhost:3000 |

---

## ✨ Siguiente Paso: Kubernetes

Una vez validado con Docker Compose, puedes desplegar en Kubernetes con Minikube:

```bash
# Minikube inicia una cluster K8s local
minikube start

# Cargar imágenes en Minikube
eval $(minikube docker-env)
docker-compose build

# Desplegar con kubectl
kubectl apply -f kubernetes/
```

(Detalles en archivo separado)


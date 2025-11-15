# REFERENCIA CONSOLIDADA: Variables de Entorno & Configuración

## 1. TABLA DE VARIABLES DE ENTORNO POR SERVICIO

### Auth API (Go)

| Variable            | Valor por Defecto    | Descripción                                      | Requerida |
|---------------------|----------------------|--------------------------------------------------|-----------|
| `AUTH_API_PORT`     | `8000`               | Puerto en el que escucha Auth API                | Sí        |
| `USERS_API_ADDRESS` | `http://users-api:8080` | URL del Users API para validar usuarios       | Sí        |
| `JWT_SECRET`        | `PRFT`               | Clave secreta para firmar JWT (HS256)            | Sí        |
| `ZIPKIN_URL`        | (vacío)              | URL de Zipkin para tracing distribuido          | No        |

**ConfigMap Key**: `auth-api-config`

**Ejemplo ConfigMap**:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: auth-api-config
data:
  AUTH_API_PORT: "8000"
  USERS_API_ADDRESS: "http://users-api:8080"
  JWT_SECRET: "PRFT"
  ZIPKIN_URL: "http://zipkin:9411/api/v2/spans"
```

---

### TODOs API (Node.js)

| Variable            | Valor por Defecto    | Descripción                                      | Requerida |
|---------------------|----------------------|--------------------------------------------------|-----------|
| `PORT`              | `3000`               | Puerto en el que escucha TODOs API               | Sí        |
| `JWT_SECRET`        | `PRFT`               | Clave secreta para validar JWT (debe coincidir)  | Sí        |
| `REDIS_HOST`        | `redis`              | Host del servidor Redis                          | Sí        |
| `REDIS_PORT`        | `6379`               | Puerto del servidor Redis                        | Sí        |
| `REDIS_CHANNEL`     | `log_channel`        | Canal Redis para publicar eventos                | Sí        |
| `ZIPKIN_URL`        | (vacío)              | URL de Zipkin para tracing distribuido          | No        |
| `ZIPKIN_SERVICE_NAME` | `todos-api`        | Nombre del servicio en Zipkin                    | No        |

**ConfigMap Key**: `todos-api-config`

**Variables para Desarrollo**:
```bash
export PORT=3000
export JWT_SECRET=PRFT
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_CHANNEL=log_channel
```

---

### Users API (Java Spring Boot)

| Variable            | Valor por Defecto    | Descripción                                      | Requerida |
|---------------------|----------------------|--------------------------------------------------|-----------|
| `SERVER_PORT`       | `8080`               | Puerto en el que escucha Users API               | Sí        |
| `JWT_SECRET`        | `PRFT`               | Clave secreta para validar JWT                   | Sí        |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:usersdb` | URL de la base de datos H2               | Sí        |
| `SPRING_DATASOURCE_USERNAME` | `sa`  | Usuario de la BD                         | Sí        |
| `SPRING_DATASOURCE_PASSWORD` | (vacío) | Contraseña de la BD                    | Sí        |

**application.properties**:
```properties
server.port=8080
spring.datasource.url=jdbc:h2:mem:usersdb
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
security.jwt.secret=PRFT
```

**Usuarios en BD (data.sql)**:
```sql
INSERT INTO users (username, firstname, lastname, role) VALUES
('admin', 'Admin', 'User', 'ADMIN'),
('johnd', 'John', 'Doe', 'USER'),
('janed', 'Jane', 'Doe', 'USER');
```

---

### Log Message Processor (Python)

| Variable            | Valor por Defecto    | Descripción                                      | Requerida |
|---------------------|----------------------|--------------------------------------------------|-----------|
| `REDIS_HOST`        | `localhost`          | Host del servidor Redis                          | Sí        |
| `REDIS_PORT`        | `6379`               | Puerto del servidor Redis                        | Sí        |
| `REDIS_CHANNEL`     | `log_channel`        | Canal Redis a monitorear                         | Sí        |
| `LOG_PROCESS_DELAY` | `0`                  | Delay en ms antes de loguear (random 0-2000)     | No        |
| `ZIPKIN_URL`        | (vacío)              | URL de Zipkin para tracing distribuido          | No        |

**Ejemplo de uso**:
```bash
export REDIS_HOST=redis
export REDIS_PORT=6379
export REDIS_CHANNEL=log_channel
export LOG_PROCESS_DELAY=0
python main.py
```

---

### Frontend (Vue.js)

| Variable            | Valor por Defecto    | Descripción                                      | Requerida |
|---------------------|----------------------|--------------------------------------------------|-----------|
| `AUTH_API_ADDRESS`  | `http://127.0.0.1:8081` | URL del Auth API (frontend lo proxea)     | Sí        |
| `TODOS_API_ADDRESS` | `http://127.0.0.1:8082` | URL del TODOs API (frontend lo proxea)    | Sí        |
| `ZIPKIN_URL`        | `http://localhost:9411` | URL de Zipkin para tracing en navegador  | No        |

**config/dev.env.js** (Desarrollo):
```javascript
module.exports = merge(prodEnv, {
  AUTH_API_ADDRESS: '"http://127.0.0.1:8081"',
  TODOS_API_ADDRESS: '"http://127.0.0.1:8082"'
})
```

**config/prod.env.js** (Kubernetes):
```javascript
module.exports = {
  AUTH_API_ADDRESS: '"http://auth-api:8000"',
  TODOS_API_ADDRESS: '"http://todos-api:3000"'
}
```

---

### Redis

| Configuración       | Valor por Defecto    | Descripción                                      |
|---------------------|----------------------|--------------------------------------------------|
| Host                | `redis` (K8s) / `localhost` (local) | Dirección del servidor |
| Puerto              | `6379`               | Puerto estándar de Redis                         |
| Canal de Log        | `log_channel`        | Nombre del canal pub/sub para logs               |
| Contraseña          | (ninguna)            | Sin autenticación en este proyecto                |
| Base de Datos       | `0` (defecto)        | Base de datos utilizada                          |

**Comando para conectar (en K8s)**:
```bash
kubectl exec -it <redis-pod> -- redis-cli
```

---

## 2. TABLA DE PUERTOS POR SERVICIO

| Servicio            | Puerto Interno | NodePort (K8s) | Tipo Servicio | Acceso Externo |
|---------------------|-----------------|-----------------|---------------|----------------|
| Frontend (nginx)    | 80              | 30080-32767*    | NodePort      | Sí (UI)        |
| Auth API            | 8000            | No expuesto     | ClusterIP     | Solo intra-K8s |
| TODOs API           | 3000            | No expuesto     | ClusterIP     | Solo intra-K8s |
| Users API           | 8080            | No expuesto     | ClusterIP     | Solo intra-K8s |
| Redis               | 6379            | No expuesto     | ClusterIP     | Solo intra-K8s |
| Log Processor       | N/A (no web)    | N/A             | N/A           | No             |
| Prometheus          | 9090            | 30000           | NodePort      | Sí (métricas)  |
| Grafana             | 3000            | 30001           | NodePort      | Sí (dashboard) |
| Zipkin              | 9411            | 30002*          | NodePort      | Sí (tracing)   |

**Notas:**
- `*` NodePort pueden variar según configuración de Minikube
- Los servicios ClusterIP solo son accesibles dentro del cluster
- Acceso externo requiere NodePort o Ingress

---

## 3. TABLA DE MÉTRICAS PROMETHEUS

| Métrica                          | Tipo   | Descripción                              | Fuente          |
|----------------------------------|--------|------------------------------------------|-----------------|
| `up`                             | Gauge  | ¿Está el target disponible? (1/0)        | Prometheus      |
| `http_requests_total`            | Counter| Total de requests HTTP completadas       | App (opcional)  |
| `http_request_duration_seconds`  | Histogram| Latencia de requests HTTP              | App (opcional)  |
| `container_memory_usage_bytes`   | Gauge  | Memoria usada por contenedor (bytes)     | cAdvisor        |
| `container_cpu_usage_seconds_total` | Counter | CPU acumulada del contenedor          | cAdvisor        |
| `redis_connected_clients`        | Gauge  | Clientes conectados a Redis              | Redis exporter  |
| `redis_used_memory_bytes`        | Gauge  | Memoria utilizada por Redis              | Redis exporter  |
| `kubelet_running_pods`           | Gauge  | Pods corriendo en el nodo                | kubelet         |
| `kubelet_running_containers`     | Gauge  | Contenedores corriendo en el nodo        | kubelet         |

**Query Prometheus comunes**:
```promql
# Status de targets
up

# CPU por pod
rate(container_cpu_usage_seconds_total[5m])

# Memoria por pod (últimas 5 min)
container_memory_usage_bytes

# Requests por segundo
rate(http_requests_total[5m])

# Latencia p95
histogram_quantile(0.95, http_request_duration_seconds_bucket)
```

---

## 4. TABLA DE ENDPOINTS POR SERVICIO

### Auth API (http://auth-api:8000)

| Método | Endpoint    | Body                            | Respuesta                              | Notas                    |
|--------|-------------|--------------------------------|----------------------------------------|--------------------------|
| POST   | `/login`    | `{username, password}`          | `{access_token: "...", ...}`           | Genera JWT                |
| GET    | `/version`  | (vacío)                         | `{version: "..."}`                     | Liveness/Readiness probe |

---

### TODOs API (http://todos-api:3000)

| Método | Endpoint    | Headers          | Body                    | Respuesta                       | Notas                          |
|--------|-------------|------------------|-------------------------|---------------------------------|--------------------------------|
| GET    | `/todos`    | `Authorization: Bearer <JWT>` | (vacío)         | `[{id, content, ...}]`          | Lista TODOs del usuario         |
| POST   | `/todos`    | `Authorization: Bearer <JWT>` | `{content}`     | `{id, content, ...}`            | Crea TODO y publica en Redis    |
| DELETE | `/todos/:id`| `Authorization: Bearer <JWT>` | (vacío)         | `{success: true}`               | Elimina TODO y publica en Redis |

---

### Users API (http://users-api:8080)

| Método | Endpoint           | Headers          | Body | Respuesta                          | Notas              |
|--------|--------------------|--------------------|------|------------------------------------|--------------------|
| GET    | `/users/{username}`| `Authorization: Bearer <JWT>` | (vacío) | `{username, firstname, lastname, role, ...}` | Requiere JWT inter-svc |

---

## 5. TABLA DE FALLOS COMUNES

| Error                    | Causa Probable                                 | Solución                                    |
|--------------------------|------------------------------------------------|---------------------------------------------|
| `CrashLoopBackOff`       | Contenedor crashea al iniciarse                | Revisar logs: `kubectl logs <pod>`          |
| `ImagePullBackOff`       | No puede descargar imagen Docker               | Verificar imagen en Docker Hub/Registry     |
| `Connection refused`     | Servicio no escucha en puerto/dirección        | Revisar variable `_ADDRESS`/puerto          |
| `Invalid JWT`            | JWT_SECRET no coincide entre servicios         | Verificar JWT_SECRET en ConfigMap           |
| `No such host`           | Nombre de servicio incorrecto en dirección     | Usar formato K8s: `<service>:<port>`        |
| `ECONNREFUSED redis`     | Redis no disponible                            | Verificar deployment de Redis               |
| `Module not found`       | Dependencias no instaladas en contenedor       | Revisar Dockerfile: `npm install` / `maven` |
| `Out of memory`          | Límite de memoria excedido                     | Aumentar `memory` en deployment limits      |
| `Readiness probe failed` | Servicio no listo para recibir tráfico         | Esperar o revisar logs                      |

---

## 6. TABLA DE ROLES RBAC (Authorization)

| Usuario   | Rol     | Permisos en Users API                      | Acceso Frontend |
|-----------|---------|---------------------------------------------|-----------------|
| `admin`   | ADMIN   | Leer/escribir información de usuarios       | Todos endpoints |
| `johnd`   | USER    | Leer TODOs, crear/eliminar TODOs propios    | `/todos`        |
| `janed`   | USER    | Leer TODOs, crear/eliminar TODOs propios    | `/todos`        |

**Nota:** Actualmente los roles están definidos pero no se validan en todas partes. El JWT incluye el rol para futura expansión.

---

## 7. TABLA DE KUBECTL COMANDOS ÚTILES

| Comando                                      | Descripción                              |
|----------------------------------------------|------------------------------------------|
| `kubectl get pods`                           | Listar pods actuales                     |
| `kubectl get pods -w`                        | Watch: ver cambios en tiempo real        |
| `kubectl logs <pod-name>`                    | Ver logs del pod                         |
| `kubectl logs <pod-name> -f`                 | Ver logs en vivo                         |
| `kubectl exec -it <pod> -- bash`             | Abrirse shell en el pod                  |
| `kubectl port-forward <pod> 3000:3000`       | Exponer puerto localmente                |
| `kubectl describe pod <pod-name>`            | Ver detalles y eventos del pod           |
| `kubectl delete pod <pod-name>`              | Eliminar pod (Deployment lo recreará)    |
| `kubectl apply -f manifest.yaml`             | Crear/actualizar recurso                 |
| `kubectl delete -f manifest.yaml`            | Eliminar recurso                         |
| `kubectl apply -f k8s-manifests/`            | Desplegar todo                           |
| `kubectl delete -f k8s-manifests/`           | Limpiar todo                             |
| `minikube service <service-name>`            | Abrir servicio NodePort en navegador     |
| `kubectl get configmap <cm-name> -o yaml`    | Ver ConfigMap completo                   |
| `kubectl edit configmap <cm-name>`           | Editar ConfigMap (vim)                   |

---

## 8. TABLA DE DOCKERFILE PATTERNS

| Servicio       | Estrategia             | Imagen Base          | Peso Final  | Notas                          |
|----------------|------------------------|----------------------|-------------|--------------------------------|
| Auth API (Go)  | Multi-stage            | `golang:1.16`→`scratch` | ~10 MB    | Solo binario compilado         |
| Users API      | Single-stage           | `maven:3.6-jdk-11`   | ~600 MB     | Incluye Maven y JDK            |
| TODOs API      | Single-stage           | `node:16-alpine`     | ~150 MB     | Alpine más pequeño que debian  |
| Log Processor  | Single-stage           | `python:3.6`         | ~700 MB     | Basada en debian               |
| Frontend       | Multi-stage            | `node:16`→`nginx`    | ~50 MB      | Webpack build → nginx          |

---

## 9. TABLA DE RUTAS NGINX (Frontend)

| Ruta         | Proxy a                | Puerto | Descripción                              |
|--------------|------------------------|--------|------------------------------------------|
| `/`          | (static files)         | 80     | Archivos estáticos (HTML, JS, CSS)       |
| `/login`     | Auth API               | 8000   | POST /login para autenticación           |
| `/todos`     | TODOs API              | 3000   | GET/POST/DELETE para TODOs               |
| `/zipkin`    | Zipkin HTTP API        | 9411   | Proxy para tracer desde navegador        |

**Config en frontend/config/index.js**:
```javascript
proxyTable: {
  '/login': {
    target: process.env.AUTH_API_ADDRESS,
    secure: false
  },
  '/todos': {
    target: process.env.TODOS_API_ADDRESS,
    secure: false
  },
  '/zipkin': {
    target: 'http://zipkin:9411',
    secure: false
  }
}
```

---

## 10. TABLA DE LIVENESS & READINESS PROBES

| Servicio       | Liveness Check        | Readiness Check       | Timeout | Intervalo |
|----------------|----------------------|------------------------|---------|-----------|
| Auth API       | GET /version (200)    | GET /version (200)     | 5s      | 10s       |
| TODOs API      | GET /health (si existe)| GET /health (si existe)| 5s      | 10s       |
| Users API      | Health endpoint (Spring)| Health endpoint (Spring)| 5s     | 10s       |
| Frontend       | GET / (200)           | GET / (200)            | 5s      | 10s       |
| Redis          | TCP check (6379)      | TCP check (6379)       | 5s      | 10s       |

---

**Última actualización:** 2024
**Proyecto:** microservice-app-example (felipevelasco7)

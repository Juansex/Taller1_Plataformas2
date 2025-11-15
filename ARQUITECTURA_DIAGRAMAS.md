# ARQUITECTURA TÉCNICA: Microservice App Example
## Diagramas y Flujos Detallados

---

## 1. DIAGRAMA DE ARQUITECTURA GENERAL

### Vista de Alto Nivel

```
┌─────────────────────────────────────────────────────────────────────┐
│                          KUBERNETES CLUSTER                         │
│                         (Minikube Environment)                      │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                            NGINX INGRESS                                  │
│                          (Frontend Service)                               │
│                             NodePort                                      │
│                          Port: 80 → 30xxx                                │
└────────────────────────────┬─────────────────────────────────────────────┘
                             │
                    ┌────────▼────────┐
                    │   FRONTEND      │
                    │   Vue.js Pod    │
                    │   Port 80       │
                    └────┬──────┬─────┘
                         │      │
          ┌──────────────┘      └──────────────┐
          │                                    │
    ┌─────▼──────┐                    ┌──────▼─────┐
    │  Auth API  │                    │ TODOs API  │
    │   Go Pod   │                    │ Node Pod   │
    │  Port 8000 │                    │ Port 3000  │
    └─────┬──────┘                    └──────┬─────┘
          │                                  │
    ┌─────▼──────────┐          ┌───────────▼─────┐
    │  Users API     │          │   Redis         │
    │  Java Pod      │          │   Pod           │
    │  Port 8080     │          │   Port 6379     │
    └────────────────┘          └───────┬─────────┘
                                        │
                                   ┌────▼────────────┐
                                   │ Log Message     │
                                   │ Processor Pod   │
                                   │ (Python)        │
                                   └─────────────────┘

┌───────────────────────────────────────────────────────────────────────┐
│                     MONITORING & OBSERVABILITY                        │
├───────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐        ┌─────────────┐        ┌──────────┐         │
│  │ Prometheus  │        │  Grafana    │        │  Zipkin  │         │
│  │ Port 9090   │        │  Port 3000  │        │  Port... │         │
│  │ NodePort    │        │  NodePort   │        │  (opt)   │         │
│  │ 30000       │        │  30001      │        │          │         │
│  └─────────────┘        └─────────────┘        └──────────┘         │
└───────────────────────────────────────────────────────────────────────┘
```

---

## 2. DIAGRAMA DE COMUNICACIÓN ENTRE SERVICIOS

```
                        USER ACTIONS
                             │
                    ┌────────▼────────┐
                    │     FRONTEND    │
                    │   (Vue.js UI)   │
                    └────┬──────┬─────┘
                         │      │
            ┌────────────┘      └──────────────┐
            │                                  │
    REQUEST: POST /login            REQUEST: GET/POST /todos
    BODY: username, password        HEADER: Authorization Bearer JWT
            │                                  │
    ┌───────▼──────────┐            ┌────────▼─────────┐
    │   AUTH API       │            │   TODOS API      │
    │   (Go, Echo)     │            │   (Node, Express)│
    │                  │            │                  │
    │ 1. Validate usr  │            │ 1. Validate JWT  │
    │ 2. Call Users API│            │ 2. Cache todos   │
    │ 3. Generate JWT  │            │ 3. CRUD in memory│
    │ 4. Return token  │            │ 4. Log to Redis  │
    └─────┬────────────┘            └────────┬─────────┘
          │                                  │
    ┌─────▼──────────┐         ┌──────────┐ │
    │  USERS API     │         │  REDIS   │ │
    │  (Java, Spring)│◄────────┤  (Broker)│◄┘
    │                │         │          │
    │ 1. Look up user│         └──────────┘
    │ 2. Return data │              │
    │ 3. Encrypted   │              │
    └────────────────┘              │
                           ┌────────▼────────────┐
                           │  LOG PROCESSOR      │
                           │  (Python, Redis)    │
                           │                     │
                           │ 1. Listen to queue  │
                           │ 2. Get event (JSON) │
                           │ 3. Log to stdout    │
                           │ 4. With Zipkin span │
                           └─────────────────────┘
```

---

## 3. FLUJO DE AUTENTICACIÓN DETALLADO

### Paso 1: Usuario Accede al Frontend

```
┌────────────┐
│   Browser  │
│            │
│ Load URL:  │
│ frontend   │
└──────┬─────┘
       │
       ▼
┌─────────────────────────────┐
│   Frontend (Vue.js)         │
│                             │
│ 1. Load index.html          │
│ 2. Check if logged in       │
│    (sessionStorage.token)   │
│ 3. If not → Redirect to     │
│    /login                   │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│   Login Component           │
│                             │
│ Show form:                  │
│ - Username input            │
│ - Password input            │
│ - Submit button             │
└─────────────────────────────┘
```

### Paso 2: Submiter Form → Auth API

```
┌─────────────────────────────┐
│   Frontend (Login.vue)      │
│                             │
│ doLogin() {                 │
│   POST /login {             │
│     username: "admin",      │
│     password: "admin"       │
│   }                         │
│ }                           │
└──────┬──────────────────────┘
       │
       │ Proxy configurado en config/index.js:
       │ '/login': {
       │   target: AUTH_API_ADDRESS,
       │   secure: false
       │ }
       │
       ▼
┌──────────────────────────────────────┐
│   Auth API (Go, Echo Framework)      │
│                                      │
│ POST /login Handler {                │
│   1. Decode JSON body                │
│   2. username = "admin"              │
│   3. password = "admin"              │
│                                      │
│   4. userService.Login(username,     │
│        password)                     │
│ }                                    │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│   UserService.Login() {              │
│                                      │
│   1. Validate hash:                  │
│      "admin_admin" ∈ allowed?        │
│                                      │
│   2. Call Users API to get user data │
│      GET http://users-api:8080/users │
│      /admin                          │
│                                      │
│      Header: Authorization: Bearer   │
│      <JWT inter-servicio>            │
│ }                                    │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│   Users API (Java, Spring Boot)      │
│                                      │
│ GET /users/{username} {              │
│   1. Validate JWT token              │
│   2. Look up in database             │
│   3. Return User object {            │
│      username: "admin",              │
│      firstname: "Admin",             │
│      lastname: "User",               │
│      role: "ADMIN"                   │
│   }                                  │
│ }                                    │
└──────┬───────────────────────────────┘
       │
       ▼ (retorna a Auth API)
┌──────────────────────────────────────┐
│   Auth API - JWT Generation {        │
│                                      │
│   1. Create JWT token {              │
│      header: {                       │
│        alg: "HS256",                 │
│        typ: "JWT"                    │
│      },                              │
│      payload: {                      │
│        username: "admin",            │
│        firstname: "Admin",           │
│        lastname: "User",             │
│        role: "ADMIN",                │
│        iat: <timestamp>,             │
│        exp: <timestamp + 1h>         │
│      }                               │
│   }                                  │
│                                      │
│   2. Sign with HS256 key (JWT_SECRET)│
│   3. Return token string             │
│ }                                    │
└──────┬───────────────────────────────┘
       │ HTTP 200 OK
       │ Body: { access_token: "...", ... }
       │
       ▼
┌──────────────────────────────────────┐
│   Frontend - Store Token             │
│                                      │
│   1. Token received                  │
│   2. Store in sessionStorage         │
│   3. Set Authorization header for    │
│      future requests                 │
│   4. Redirect to /todos (main page)  │
│ }                                    │
└──────────────────────────────────────┘
```

---

## 4. FLUJO DE OPERACIONES TODO

### GET /todos (Listar)

```
┌───────────────────────┐
│   Frontend            │
│   mounted() {         │
│     GET /todos        │
│     + JWT header      │
│   }                   │
└──────┬────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│   TODOs API (Express) - GET /todos   │
│                                      │
│   1. Middleware JWT:                 │
│      req.user = decoded token        │
│                                      │
│   2. TodoController.list() {         │
│      username = req.user.username    │
│      todos = cache[username]         │
│      return todos                    │
│   }                                  │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│   Memory Cache                       │
│   {                                  │
│     "admin": {                       │
│       items: [                       │
│         { id: 1, content: "Task 1" },│
│         { id: 2, content: "Task 2" } │
│       ],                             │
│       lastInsertedID: 3              │
│     }                                │
│   }                                  │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│   Response: Array of TODOs           │
│   [                                  │
│     { id: 1, content: "Task 1" },    │
│     { id: 2, content: "Task 2" }     │
│   ]                                  │
└──────────────────────────────────────┘
```

### POST /todos (Crear)

```
┌──────────────────────────────────────┐
│   Frontend                           │
│   POST /todos {                      │
│     content: "New task"              │
│   }                                  │
│   + JWT header                       │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│   TODOs API - POST /todos            │
│                                      │
│   1. Validate JWT                    │
│   2. Create TODO {                   │
│      id: nextID++,                   │
│      content: "New task"             │
│   }                                  │
│   3. Store in memory cache           │
│   4. LogOperation(CREATE, ...)       │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│   Redis Publishing                   │
│                                      │
│   redisClient.publish(               │
│     REDIS_CHANNEL,                   │
│     JSON.stringify({                 │
│       opName: "CREATE",              │
│       username: "admin",             │
│       todoId: 3,                     │
│       zipkinSpan: { ... }            │
│     })                               │
│   )                                  │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│   Log Message Processor              │
│   (Python - Redis subscriber)        │
│                                      │
│   1. Listen on redis_channel         │
│   2. Receive message JSON            │
│   3. Parse message                   │
│   4. If has Zipkin span:             │
│      - Continue distributed trace    │
│   5. print() message to stdout       │
│   6. Add random delay (0-2s)         │
└──────────────────────────────────────┘
```

---

## 5. DIAGRAMA DE CONFIGURACIÓN KUBERNETES

### Deployment con ConfigMap

```
┌────────────────────────────────────────────────────┐
│   Deployment: auth-api                             │
│                                                    │
│   ┌────────────────────────────────────────────┐  │
│   │   Pod: auth-api-xyz123                      │  │
│   │                                              │  │
│   │   ┌──────────────────────────────────────┐ │  │
│   │   │  Container: auth-api                 │ │  │
│   │   │                                       │ │  │
│   │   │  Environment Variables (from CM):    │ │  │
│   │   │  - AUTH_API_PORT = 8000              │ │  │
│   │   │  - USERS_API_ADDRESS = http://...   │ │  │
│   │   │  - JWT_SECRET = PRFT                │ │  │
│   │   │  - ZIPKIN_URL = http://...          │ │  │
│   │   │                                       │ │  │
│   │   │  Mounts: (si hay volúmenes)         │ │  │
│   │   │                                       │ │  │
│   │   │  Resources:                          │ │  │
│   │   │  - CPU: 100m-500m                    │ │  │
│   │   │  - Memory: 128Mi-512Mi               │ │  │
│   │   │                                       │ │  │
│   │   │  Probes:                             │ │  │
│   │   │  - Liveness: /version endpoint       │ │  │
│   │   │  - Readiness: /version endpoint      │ │  │
│   │   └──────────────────────────────────────┘ │  │
│   │                                              │  │
│   └────────────────────────────────────────────┘  │
│                                                    │
│   Replicas: 1-3 (HPA)                            │
│   Strategy: RollingUpdate                         │
│            maxSurge: 1                            │
│            maxUnavailable: 0                      │
└────────────────────────────────────────────────────┘
                        │
                        │ Expuesto por
                        │
           ┌────────────▼────────────┐
           │   Service: auth-api     │
           │                         │
           │   Type: ClusterIP       │
           │   Port: 8000            │
           │   TargetPort: 8000      │
           │                         │
           │   Selector:             │
           │   app: auth-api         │
           └─────────────────────────┘
```

### ConfigMap Detallado

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: auth-api-config
  namespace: default
data:
  # Variables para Auth API
  AUTH_API_PORT: "8000"
  USERS_API_ADDRESS: "http://users-api:8080"
  JWT_SECRET: "PRFT"
  ZIPKIN_URL: "http://zipkin:9411/api/v2/spans"
  
  # Variables para Log Message Processor
  REDIS_HOST: "redis"
  REDIS_PORT: "6379"
  REDIS_CHANNEL: "log_channel"
```

---

## 6. DIAGRAMA DE MONITOREO

### Flujo de Métricas

```
┌─────────────────────────────────────────────────────────┐
│              APLICACIONES (microservicios)              │
├─────────────────────────────────────────────────────────┤
│                                                          │
│   ┌──────────────┐   ┌──────────────┐                  │
│   │  Auth API    │   │  TODOs API   │                  │
│   │  Expone:     │   │  Expone:     │                  │
│   │  /metrics    │   │  /metrics    │                  │
│   │  Puerto 9090 │   │  Puerto 9090 │                  │
│   └──────┬───────┘   └──────┬───────┘                  │
│          │                  │                          │
│   ┌──────▼──────────────────▼───┐                      │
│   │   Kubernetes Metrics:       │                      │
│   │   - container_memory_usage  │                      │
│   │   - container_cpu_usage     │                      │
│   │   - kubelet metrics         │                      │
│   └──────┬──────────────────────┘                      │
│          │                                             │
└──────────┼─────────────────────────────────────────────┘
           │
           │ Scrape every 15 seconds
           │
           ▼
┌─────────────────────────────────────────────────────────┐
│         PROMETHEUS (Metrics Database)                   │
│                                                          │
│   - Almacena time-series data                          │
│   - Retention: 15 días (default)                       │
│   - Puerto: 9090 (NodePort 30000)                      │
│                                                          │
│   Queries:                                              │
│   - up (status de targets)                             │
│   - http_requests_total                                │
│   - container_memory_usage_bytes                       │
│   - container_cpu_usage_seconds_total                  │
│                                                          │
└──────────┬──────────────────────────────────────────────┘
           │
           │ Query & Visualize
           │
           ▼
┌─────────────────────────────────────────────────────────┐
│         GRAFANA (Visualization)                         │
│                                                          │
│   Data Source: Prometheus (http://prometheus:9090)     │
│                                                          │
│   Dashboards:                                           │
│   - 315: Kubernetes Cluster Monitoring                 │
│   - CPU por pod                                         │
│   - Memoria por pod                                     │
│   - Network I/O                                         │
│   - HTTP request rate & latency                        │
│                                                          │
│   Puerto: 3000 (NodePort 30001)                        │
│   Usuario: admin / Contraseña: admin                   │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## 7. FLUJO DE DESPLIEGUE EN KUBERNETES

```
┌─────────────────┐
│  Imagen Docker  │
│  (local o Hub)  │
└────────┬────────┘
         │
         ├─ Push a Docker Hub
         │      │
         │      ▼
         │  ┌──────────────┐
         │  │  Docker Hub  │
         │  │  Registry    │
         │  └──────┬───────┘
         │         │
         └─────────┴──► imagePullPolicy
                       IfNotPresent: usa local
                       Always: tira de Hub
                       Never: solo local
         │
         ▼
   ┌──────────────────────────┐
   │  kubectl apply -f        │
   │  k8s-manifests/          │
   │                          │
   │  Lee todos los YAML      │
   │  y crea recursos         │
   └────────┬─────────────────┘
            │
         ┌──┴──┬──────┬─────────┬──────────┐
         │     │      │         │          │
         ▼     ▼      ▼         ▼          ▼
      Deploy Service ConfigMap HPA    NetPolicy
      ments                     Strategies
         │     │      │         │          │
         └──┬──┴──┬───┴─────┬───┴────┬────┘
            │     │         │        │
            ▼     ▼         ▼        ▼
     ┌──────────────────────────────────────┐
     │  Kubernetes API Server               │
     │  (Valida y almacena recursos)        │
     └────────────┬─────────────────────────┘
                  │
     ┌────────────┴─────────────┐
     │                          │
     ▼                          ▼
┌──────────────┐        ┌──────────────┐
│  Scheduler   │        │  Controller  │
│              │        │  Manager     │
│  Asigna pods │        │              │
│  a nodos     │        │  Monitorea   │
│              │        │  estado      │
└────────┬─────┘        └──────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  Kubelet (en cada nodo)          │
│                                  │
│  1. Recibe asignación            │
│  2. Pull image (si no existe)    │
│  3. Crea contenedor              │
│  4. Corre contenedor             │
│  5. Monitorea salud              │
│  6. Reporta estado a API Server  │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  PODS EN EJECUCIÓN               │
│                                  │
│  ☑ auth-api (Running)            │
│  ☑ users-api (Running)           │
│  ☑ todos-api (Running)           │
│  ☑ frontend (Running)            │
│  ☑ log-processor (Running)       │
│  ☑ redis (Running)               │
│  ☑ prometheus (Running)          │
│  ☑ grafana (Running)             │
└──────────────────────────────────┘
```

---

## 8. DIAGRAMA DE RED (Network Policies)

### Sin Network Policies (Permisivo)

```
       Frontend
         ┌┴┐
         │ │
     ┌───┼─┼───┐
     │   │ │   │
     ▼   ▼ ▼   ▼
   Auth Users TODOs  Redis  (puede hablar con todos)
```

### Con Network Policies (Restrictivo)

```
Frontend  ┌──────────────────────────────────┐
  │       │ Puede comunicar con:             │
  │       │ - Auth API                       │
  │       │ - TODOs API                      │
  │       │ - Prometheus                     │
  └──────►│                                  │
          └──────────────────────────────────┘

Auth API
  │       ┌──────────────────────────────────┐
  │       │ Puede comunicar con:             │
  ├──────►│ - Users API (via HTTP)           │
  │       │ - Prometheus                     │
  │       │ - Zipkin (opcional)              │
  │       │ Recibe de: Frontend              │
  │       │ NO puede: Redis, TODOs API       │
  │       └──────────────────────────────────┘
  │

TODOs API
  │       ┌──────────────────────────────────┐
  │       │ Puede comunicar con:             │
  ├──────►│ - Redis (publish messages)       │
  │       │ - Prometheus                     │
  │       │ - Zipkin (opcional)              │
  │       │ Recibe de: Frontend              │
  │       │ NO puede: Users API, Auth API    │
  │       └──────────────────────────────────┘
  │

Log Processor
          ┌──────────────────────────────────┐
          │ Puede comunicar con:             │
         │ - Redis (subscribe)              │
          │ - Prometheus                     │
          │ - Zipkin (opcional)              │
          │ Recibe de: Nadie                 │
          │ NO puede: Frontend, APIs         │
          └──────────────────────────────────┘

Redis
          ┌──────────────────────────────────┐
          │ Puede comunicar con:             │
          │ - TODOs API (recibe)             │
          │ - Log Processor (recibe)         │
          │ Recibe de: TODOs API, Log Proc   │
          │ NO puede: Frontend, Users API    │
          └──────────────────────────────────┘
```

---

## 9. CICLO DE VIDA DE UN POD

```
┌─────────────────────────────────────────────────┐
│   Deployment creado                             │
│   (kubectl apply -f deployment.yaml)            │
└────────────┬────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────┐
│   Pending                                       │
│   - Scheduler asigna pod a nodo                 │
│   - Kubelet recibe asignación                   │
│   - Comienza descargar imagen (pull)            │
└────────────┬────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────┐
│   ContainerCreating                             │
│   - Imagen descargada                           │
│   - Volúmenes montados                          │
│   - Contenedor creado                           │
│   - Contenedor iniciado                         │
└────────────┬────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────┐
│   Running                                       │
│   - Contenedor ejecutándose                     │
│   - Startup probe (si existe)                   │
│   - Readiness probe (si existe)                 │
│   - Liveness probe (monitoreo continuo)         │
│                                                  │
│   Si healthcheck falla:                         │
│   → Kubelet mata y reinicia contenedor          │
│   → Incrementa restart count                    │
│   → Si muchos restarts → CrashLoopBackOff       │
└────────────┬────────────────────────────────────┘
             │
             ▼ (hasta delete)
┌─────────────────────────────────────────────────┐
│   Terminating                                   │
│   - SIGTERM enviado al proceso                  │
│   - Grace period (default 30s)                  │
│   - Si no responde → SIGKILL                    │
│   - Volúmenes desmontados                       │
└────────────┬────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────┐
│   Terminated / Deleted                          │
│   - Pod removido completamente                  │
│   - Si parte de Deployment:                     │
│     → Deployment crea nuevo pod                 │
└─────────────────────────────────────────────────┘
```

---

## 10. TABLA DE DEPENDENCIAS ENTRE SERVICIOS

```
┌──────────────────────────────────────────────────────────────┐
│  SERVICIO        │  DEPENDE DE      │  COMUNICACIÓN           │
├──────────────────┼──────────────────┼─────────────────────────┤
│  Frontend        │  Auth API        │  POST /login            │
│  (Vue.js)        │  TODOs API       │  GET/POST /todos        │
│                  │                  │  Proxy en nginx config  │
├──────────────────┼──────────────────┼─────────────────────────┤
│  Auth API        │  Users API       │  GET /users/{username}  │
│  (Go)            │  Zipkin (opt)    │  Http traced call       │
│                  │                  │  Bearer JWT inter-svc   │
├──────────────────┼──────────────────┼─────────────────────────┤
│  TODOs API       │  Redis           │  PUBLISH logs           │
│  (Node.js)       │  Zipkin (opt)    │  Http traced           │
│                  │  (no otros APIs) │  No habla con Auth API  │
├──────────────────┼──────────────────┼─────────────────────────┤
│  Users API       │  Ninguno         │  Recibe llamadas        │
│  (Java)          │  (excepto config)│  de Auth API            │
│                  │                  │  Se configura via envs  │
├──────────────────┼──────────────────┼─────────────────────────┤
│  Log Processor   │  Redis           │  SUBSCRIBE queue        │
│  (Python)        │  Zipkin (opt)    │  Escucha pasiva         │
│                  │                  │  Logs a stdout          │
├──────────────────┼──────────────────┼─────────────────────────┤
│  Redis           │  Ninguno         │  Recibe:                │
│  (Cache/Broker)  │                  │  - PUBLISH de TODOs     │
│                  │                  │  - SUBSCRIBE de Logs    │
├──────────────────┼──────────────────┼─────────────────────────┤
│  Prometheus      │  Ninguno         │  Scrape /metrics de:    │
│  (Monitoring)    │                  │  - Kubelet (cAdvisor)   │
│                  │                  │  - App endpoints        │
├──────────────────┼──────────────────┼─────────────────────────┤
│  Grafana         │  Prometheus      │  Query metrics via API  │
│  (Dashboards)    │                  │  Port 9090              │
└──────────────────┴──────────────────┴─────────────────────────┘
```

---

## 11. ORDEN DE INICIALIZACIÓN RECOMENDADO

```
1. Redis
   └─► Sin Redis, TODOs API no puede loguear

2. Users API
   └─► Sin Users API, Auth API no puede validar usuarios

3. Auth API
   └─► Sin Auth API, Frontend no puede autenticar

4. TODOs API
   └─► Depende de Redis y JWT de Auth API

5. Frontend
   └─► Depende de Auth API y TODOs API

6. Log Message Processor
   └─► Depende de Redis (independiente de otros)

7. Prometheus
   └─► Monitorea todos (puede estar después)

8. Grafana
   └─► Visualiza métricas de Prometheus (último)

Nota: Kubernetes orquesta esto automáticamente
      Si aplicas todos los manifiestos con:
      kubectl apply -f k8s-manifests/
      El controlador maneja las dependencias
```

---

**Fin de Diagramas y Flujos Técnicos**

Estos diagramas proporcionan una comprensión visual completa de la arquitectura, flujos de comunicación y despliegue de la aplicación microservicios.

# Análisis Detallado del Repositorio: Microservice App Example
## Proyecto: https://github.com/felipevelasco7/microservice-app-example

**Fecha de Análisis:** Noviembre 2025
**Descripción:** Aplicación TODO distribuida basada en microservicios con arquitectura multi-lenguaje escrita en Go, Python, Vue, Java y Node.js

---

## TABLA DE CONTENIDOS
1. [Estructura General del Proyecto](#estructura-general)
2. [Estructura de Kubernetes](#estructura-de-kubernetes)
3. [Monitoreo: Prometheus y Grafana](#monitoreo-prometheus-y-grafana)
4. [Docker: Dockerfiles e Imágenes](#docker-dockerfiles-e-imágenes)
5. [Configuración de Servicios](#configuración-de-servicios)
6. [Archivos Críticos](#archivos-críticos)
7. [Scripts y Herramientas](#scripts-y-herramientas)
8. [Flujo de Despliegue](#flujo-de-despliegue)

---

## ESTRUCTURA GENERAL

### Componentes Principales

El proyecto está compuesto por **5 microservicios principales + 1 componente de infraestructura**:

| Componente | Lenguaje | Descripción | Puerto |
|-----------|----------|-------------|--------|
| **Users API** | Java (Spring Boot) | Proporciona datos de usuarios | 8080 |
| **Auth API** | Go | Autenticación y generación de JWT | 8000 |
| **TODOs API** | Node.js | CRUD de tareas, integrado con Redis | 3000 |
| **Log Message Processor** | Python | Consumidor de colas Redis | N/A |
| **Frontend** | Vue.js + nginx | Interfaz de usuario | 80 |
| **Redis** | Docker | Almacenamiento de colas de mensajes | 6379 |

### Arquitectura de Interacciones

```
Frontend (Vue)
    ├── POST /login → Auth API → Users API
    ├── GET/POST /todos → TODOs API → Redis (colas)
    └── /zipkin → Zipkin (trazas distribuidas)

TODOs API
    └── Publica mensajes en Redis → Log Message Processor

Log Message Processor
    └── Lee de Redis y logs a stdout
```

### Directorio Raíz

```
microservice-app-example/
├── README.md                          # Documentación principal
├── LICENSE                            # MIT License
├── k8s-manifests/                     # Manifiestos de Kubernetes
├── arch-img/                          # Imágenes de arquitectura
├── fotos/                             # Evidencia de despliegue
├── users-api/                         # Spring Boot
├── auth-api/                          # Go
├── todos-api/                         # Node.js
├── log-message-processor/             # Python
└── frontend/                          # Vue.js
```

---

## ESTRUCTURA DE KUBERNETES

### Ubicación de Manifiestos

**Ruta:** `k8s-manifests/`

### Archivos de Manifiestos

#### 1. **Deployments**

| Archivo | Servicio | Puerto | Notas |
|---------|----------|--------|-------|
| `users-api-deployment.yaml` | Users API | 8080 | Spring Boot, acceso a base de datos |
| `auth-api-deployment.yaml` | Auth API | 8000 | Go con Zipkin tracing |
| `todos-api-deployment.yaml` | TODOs API | 3000 | Node.js con Redis |
| `log-message-processor-deployment.yaml` | Log Processor | N/A | Python, consumer de Redis |
| `frontend-deployment.yaml` | Frontend | 80 | nginx, tipo NodePort |
| `redis-deployment.yaml` | Redis | 6379 | Almacenamiento de colas |

#### 2. **Services**

Cada deployment tiene un servicio ClusterIP correspondiente para comunicación interna:
- `users-api` → accesible como `http://users-api:8080` dentro del cluster
- `auth-api` → accesible como `http://auth-api:8000`
- `todos-api` → accesible como `http://todos-api:3000`
- `frontend` → tipo **NodePort** para acceso externo

#### 3. **ConfigMaps**

**Archivos:**
- `auth-api-configmap.yaml`
- `todos-api-configmap.yaml`
- `log-message-processor-configmap.yaml`
- `frontend-configmap.yaml`
- `users-api-configmap.yaml`

**Variables de Entorno Críticas:**

```yaml
# Auth API ConfigMap
AUTH_API_PORT: "8000"
USERS_API_ADDRESS: "http://users-api:8080"
JWT_SECRET: "PRFT"
ZIPKIN_URL: "http://zipkin:9411/api/v2/spans"

# TODOs API ConfigMap
TODO_API_PORT: "3000"
JWT_SECRET: "PRFT"
REDIS_HOST: "redis"
REDIS_PORT: "6379"
REDIS_CHANNEL: "log_channel"

# Log Message Processor ConfigMap
REDIS_HOST: "redis"
REDIS_PORT: "6379"
REDIS_CHANNEL: "log_channel"
ZIPKIN_URL: "http://zipkin:9411/api/v2/spans"

# Frontend ConfigMap
AUTH_API_ADDRESS: "http://auth-api:8000"
TODOS_API_ADDRESS: "http://todos-api:3000"
ZIPKIN_URL: "http://zipkin:9411/api/v2/spans"
```

#### 4. **Network Policies**

**Archivos:**
- `auth-api-network-policy.yaml`
- `todos-api-network-policy.yaml`
- `log-message-processor-network-policy.yaml`
- `frontend-network-policy.yaml`
- `users-api-network-policy.yaml`

**Propósito:** Restringir tráfico de red entre pods (seguridad de red)

#### 5. **Horizontal Pod Autoscaler (HPA)**

**Archivos:**
- `auth-api-hpa.yaml`
- `todos-api-hpa.yaml`
- `log-message-processor-hpa.yaml`
- `frontend-hpa.yaml`
- `users-api-hpa.yaml`

**Función:** Auto-escalado basado en CPU/memoria

#### 6. **Estrategias de Despliegue**

**Archivos:**
- `auth-api-deployment-strategy.yaml`
- `todos-api-deployment-strategy.yaml`
- `log-message-processor-deployment-strategy.yaml`
- `frontend-deployment-strategy.yaml`
- `users-api-deployment-strategy.yaml`

**Rolling Updates:** Despliegues continuos sin downtime

### Aplicar Manifiestos

```bash
# Aplicar todos los manifiestos de una vez
kubectl apply -f k8s-manifests/

# Verificar despliegue
kubectl get pods -o wide
kubectl get svc
kubectl get configmaps
kubectl get networkpolicies
kubectl get hpa
```

### Puertos y Acceso

| Servicio | Tipo | Puerto Interno | Puerto Externo | URL |
|----------|------|----------------|----------------|-----|
| Frontend | NodePort | 80 | 30xxx (variable) | `http://<MINIKUBE_IP>:<NODEPORT>` |
| Prometheus | NodePort | 9090 | 30000 | `http://<MINIKUBE_IP>:30000` |
| Grafana | NodePort | 3000 | 30001 | `http://<MINIKUBE_IP>:30001` |
| Users API | ClusterIP | 8080 | N/A | `http://users-api:8080` |
| Auth API | ClusterIP | 8000 | N/A | `http://auth-api:8000` |
| TODOs API | ClusterIP | 3000 | N/A | `http://todos-api:3000` |
| Redis | ClusterIP | 6379 | N/A | `redis:6379` |

---

## MONITOREO: PROMETHEUS Y GRAFANA

### Despliegue de Prometheus

**Archivo:** `k8s-manifests/prometheus-deployment.yaml`

**Acceso:**
```bash
kubectl apply -f k8s-manifests/prometheus-deployment.yaml
# Puerto NodePort: 30000
# URL: http://<MINIKUBE_IP>:30000
```

**Configuración:**
- **Scrape Interval:** Configurable (típicamente 15s)
- **Retention:** Período de retención de datos
- **Service Monitor:** Detecta métricas de servicios con etiquetas `prometheus=true`

### Despliegue de Grafana

**Archivo:** `k8s-manifests/grafana-deployment.yaml`

**Acceso:**
```bash
kubectl apply -f k8s-manifests/grafana-deployment.yaml
# Puerto NodePort: 30001
# URL: http://<MINIKUBE_IP>:30001
# Usuario: admin
# Contraseña: admin
```

### Configuración de Grafana

#### 1. Agregar Prometheus como Data Source

```
Navegación: Configuration > Data Sources
- Tipo: Prometheus
- URL: http://prometheus:9090
- Click "Save & Test"
```

#### 2. Importar Dashboards

```
Navegación: Dashboards > Import
ID de Dashboard Predeterminado: 315 (Kubernetes cluster monitoring)
```

### Métricas que se Recopilan

Los microservicios con soporte a Prometheus exportan:

| Métrica | Fuente | Ejemplo |
|---------|--------|---------|
| `http_requests_total` | Cada microservicio | Contador de peticiones HTTP |
| `http_request_duration_seconds` | Cada microservicio | Duración de peticiones |
| `redis_connected_clients` | Redis exporter | Clientes conectados a Redis |
| `redis_commands_processed_total` | Redis exporter | Comandos procesados |
| `container_memory_usage_bytes` | Kubelet/cAdvisor | Memoria del contenedor |
| `container_cpu_usage_seconds_total` | Kubelet/cAdvisor | CPU del contenedor |

### Endpoints de Métricas

| Servicio | Endpoint | Puerto |
|----------|----------|--------|
| Prometheus | `/metrics` | 9090 |
| Redis | Exportado vía exporter | 9121 |
| Aplicaciones | Integrado en Zipkin | Varía |

### Verificación

```bash
# Verificar que Prometheus está recolectando métricas
kubectl port-forward svc/prometheus 9090:9090

# En el navegador: http://localhost:9090
# Query: up (muestra todos los targets scrapeados)

# Verificar Grafana
kubectl port-forward svc/grafana 3000:3000
# http://localhost:3000
```

---

## DOCKER: DOCKERFILES E IMÁGENES

### Construcción de Imágenes

**Ubicación Base:** Raíz del repositorio

#### 1. **Users API (Java Spring Boot)**

**Ubicación:** `users-api/`

**Dockerfile:**
```dockerfile
# Multi-stage build típico
FROM maven:3.8.1-jdk-8 AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean install

FROM openjdk:8-jre
WORKDIR /app
COPY --from=builder /app/target/users-api-0.0.1-SNAPSHOT.jar .
EXPOSE 8080
CMD ["java", "-jar", "users-api-0.0.1-SNAPSHOT.jar"]
```

**Construcción:**
```bash
docker build -t felipevelasco7/users-api:latest ./users-api
```

#### 2. **Auth API (Go)**

**Ubicación:** `auth-api/`

**Dockerfile:**
```dockerfile
FROM golang:1.18.2 AS builder
WORKDIR /app
COPY . .
RUN go mod tidy && go build -o auth-api

FROM scratch
COPY --from=builder /app/auth-api .
EXPOSE 8000
CMD ["./auth-api"]
```

**Construcción:**
```bash
cd auth-api
go mod init auth-api || true
go mod tidy
cd ..
docker build -t felipevelasco7/auth-api:latest ./auth-api
```

#### 3. **TODOs API (Node.js)**

**Ubicación:** `todos-api/`

**Dockerfile:**
```dockerfile
FROM node:8.17.0
WORKDIR /app
COPY . .
RUN npm install
EXPOSE 3000
CMD ["npm", "start"]
```

**Construcción:**
```bash
docker build -t felipevelasco7/todos-api:latest ./todos-api
```

#### 4. **Log Message Processor (Python)**

**Ubicación:** `log-message-processor/`

**Dockerfile:**
```dockerfile
FROM python:3.6
WORKDIR /app
COPY . .
RUN pip install -r requirements.txt
CMD ["python", "main.py"]
```

**Construcción:**
```bash
docker build -t felipevelasco7/log-message-processor:latest ./log-message-processor
```

#### 5. **Frontend (Vue.js + nginx)**

**Ubicación:** `frontend/`

**Dockerfile (Multi-stage):**
```dockerfile
# Stage 1: Build
FROM node:16
WORKDIR /app
COPY . .
RUN npm install --legacy-peer-deps && npm run build

# Stage 2: Serve
FROM nginx:alpine
COPY --from=0 /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**Construcción:**
```bash
docker build -t felipevelasco7/frontend:latest ./frontend
```

### Registro de Imágenes

**Opción A: Docker Hub (Recomendado)**
```bash
docker push felipevelasco7/users-api:latest
docker push felipevelasco7/auth-api:latest
docker push felipevelasco7/todos-api:latest
docker push felipevelasco7/log-message-processor:latest
docker push felipevelasco7/frontend:latest
```

**Opción B: Minikube Local**
```bash
minikube image load felipevelasco7/users-api:latest
minikube image load felipevelasco7/auth-api:latest
minikube image load felipevelasco7/todos-api:latest
minikube image load felipevelasco7/log-message-processor:latest
minikube image load felipevelasco7/frontend:latest
```

### docker-compose

**Estado:** No existe archivo `docker-compose.yml` en el repositorio

**Alternativa:** Los manifiestos de Kubernetes (`k8s-manifests/`) son los que orquestan los servicios

---

## CONFIGURACIÓN DE SERVICIOS

### 1. Users API (Java Spring Boot)

**Ruta:** `users-api/`

**Configuración de Ambiente:**
```bash
JWT_SECRET=PRFT
SERVER_PORT=8083
```

**Archivos Clave:**
- `pom.xml` - Gestión de dependencias Maven
- `src/main/java/com/elgris/usersapi/UsersApiApplication.java` - Entrada
- `src/main/java/com/elgris/usersapi/api/UsersController.java` - Endpoints
- `src/main/resources/application.properties` - Configuración Spring
- `src/main/resources/data.sql` - Datos iniciales

**Build:**
```bash
./mvnw clean install
```

**Run:**
```bash
JWT_SECRET=PRFT SERVER_PORT=8083 java -jar target/users-api-0.0.1-SNAPSHOT.jar
```

**Endpoints:**
- `GET /users` - Listar todos los usuarios
- `GET /users/{username}` - Obtener usuario por nombre

---

### 2. Auth API (Go)

**Ruta:** `auth-api/`

**Configuración de Ambiente:**
```bash
AUTH_API_PORT=8000
USERS_API_ADDRESS=http://users-api:8080
JWT_SECRET=PRFT
ZIPKIN_URL=http://zipkin:9411/api/v2/spans (opcional)
```

**Archivos Clave:**
- `main.go` - Punto de entrada, configuración del servidor Echo
- `user.go` - Lógica de usuarios y validación
- `tracing.go` - Integración con Zipkin
- `Gopkg.toml` - Dependencias (legacy)

**Usuarios Hardcodeados:**
```
admin / admin
johnd / foo
janed / ddd
```

**Build:**
```bash
export GO111MODULE=on
go mod init auth-api
go mod tidy
go build
```

**Run:**
```bash
JWT_SECRET=PRFT AUTH_API_PORT=8000 USERS_API_ADDRESS=http://users-api:8080 ./auth-api
```

**Endpoints:**
- `POST /login` - Autenticación y generación de JWT
- `GET /version` - Información de versión

---

### 3. TODOs API (Node.js)

**Ruta:** `todos-api/`

**Configuración de Ambiente:**
```bash
TODO_API_PORT=8082
JWT_SECRET=PRFT
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_CHANNEL=log_channel
ZIPKIN_URL=http://localhost:9411/api/v2/spans (opcional)
```

**Archivos Clave:**
- `server.js` - Servidor Express, Zipkin middleware
- `todoController.js` - Lógica de TODOs
- `routes.js` - Definición de rutas
- `package.json` - Dependencias npm

**Dependencies:**
```json
{
  "express": "^4.16.3",
  "express-jwt": "^5.3.0",
  "body-parser": "^1.18.3",
  "redis": "^2.8.0",
  "zipkin": "^0.13.0",
  "zipkin-transport-http": "^0.13.0"
}
```

**Build:**
```bash
npm install
```

**Run:**
```bash
JWT_SECRET=PRFT TODO_API_PORT=8082 npm start
```

**Endpoints:**
- `GET /todos` - Listar TODOs del usuario
- `POST /todos` - Crear nuevo TODO
- `DELETE /todos/{taskId}` - Eliminar TODO

**Estructura de Datos:**
```json
{
  "id": 1,
  "userId": 1,
  "content": "Create new todo"
}
```

---

### 4. Log Message Processor (Python)

**Ruta:** `log-message-processor/`

**Configuración de Ambiente:**
```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_CHANNEL=log_channel
ZIPKIN_URL=http://localhost:9411/api/v2/spans (opcional)
```

**Archivos Clave:**
- `main.py` - Consumidor de Redis, Zipkin tracing
- `requirements.txt` - Dependencias pip

**requirements.txt:**
```
redis>=2.10.5
requests>=2.19.1
py_zipkin>=0.19.1
```

**Build:**
```bash
pip3 install -r requirements.txt
```

**Run:**
```bash
REDIS_HOST=127.0.0.1 REDIS_PORT=6379 REDIS_CHANNEL=log_channel python3 main.py
```

**Función:** 
- Escucha en Redis por mensajes de creación/eliminación de TODOs
- Imprime en stdout con delay aleatorio (0-2 segundos)
- Integración con Zipkin para trazas distribuidas

---

### 5. Frontend (Vue.js)

**Ruta:** `frontend/`

**Configuración de Ambiente:**
```bash
PORT=8080
AUTH_API_ADDRESS=http://127.0.0.1:8000
TODOS_API_ADDRESS=http://127.0.0.1:8082
ZIPKIN_URL=http://127.0.0.1:9411/api/v2/spans
```

**Archivos Clave:**
- `src/main.js` - Punto de entrada Vue
- `src/router/index.js` - Enrutamiento
- `src/store/index.js` - Vuex store
- `src/auth.js` - Plugin de autenticación
- `src/zipkin.js` - Plugin de tracing Zipkin
- `src/components/` - Componentes Vue
  - `App.vue` - Componente raíz
  - `Login.vue` - Formulario de login
  - `Todos.vue` - Lista de TODOs
  - `TodoItem.vue` - Item individual
  - `AppNav.vue` - Navegación
- `config/index.js` - Configuración Webpack
- `build/` - Scripts y configuraciones de build

**Build Configuration:**
```javascript
// frontend/config/index.js
proxyTable: {
  '/login': { target: process.env.AUTH_API_ADDRESS || 'http://127.0.0.1:8081' },
  '/todos': { target: process.env.TODOS_API_ADDRESS || 'http://127.0.0.1:8082' },
  '/zipkin': { target: process.env.ZIPKIN_URL || 'http://127.0.0.1:9411/api/v2/spans' }
}
```

**Build:**
```bash
npm install --legacy-peer-deps
npm run build
```

**Run:**
```bash
PORT=8080 AUTH_API_ADDRESS=http://127.0.0.1:8000 TODOS_API_ADDRESS=http://127.0.0.1:8082 npm start
```

**Dependencies (Actualizadas):**
- Node: 16.x
- npm: 8.x
- webpack: ^3.12.0
- vue-loader: ^12.1.0
- extract-text-webpack-plugin: ^3.0.2

---

### 6. Redis

**Puerto:** 6379

**Función:** Almacenamiento de colas de mensajes para logging

**Canal:** `log_channel`

**Kubernetes:** `redis-deployment.yaml`

---

## ARCHIVOS CRÍTICOS

### Archivos de Configuración Kubernetes

```
k8s-manifests/
├── auth-api-configmap.yaml              # Variables de entorno Auth API
├── auth-api-deployment.yaml             # Deployment + Service Auth API
├── auth-api-hpa.yaml                    # Auto-escalado Auth API
├── auth-api-network-policy.yaml         # Políticas de red Auth API
├── auth-api-deployment-strategy.yaml    # Rolling updates Auth API
├── todos-api-configmap.yaml
├── todos-api-deployment.yaml
├── todos-api-hpa.yaml
├── todos-api-network-policy.yaml
├── todos-api-deployment-strategy.yaml
├── users-api-configmap.yaml
├── users-api-deployment.yaml
├── users-api-hpa.yaml
├── users-api-network-policy.yaml
├── users-api-deployment-strategy.yaml
├── log-message-processor-configmap.yaml
├── log-message-processor-deployment.yaml
├── log-message-processor-hpa.yaml
├── log-message-processor-network-policy.yaml
├── log-message-processor-deployment-strategy.yaml
├── frontend-configmap.yaml
├── frontend-deployment.yaml
├── frontend-hpa.yaml
├── frontend-network-policy.yaml
├── frontend-deployment-strategy.yaml
├── redis-deployment.yaml
├── prometheus-deployment.yaml           # Despliegue de Prometheus
└── grafana-deployment.yaml              # Despliegue de Grafana
```

### Archivos de Build y Configuración por Servicio

#### Users API
```
users-api/
├── pom.xml                                      # Maven POM
├── README.md                                    # Documentación
├── mvnw                                         # Maven wrapper
├── src/main/java/com/elgris/usersapi/
│   ├── UsersApiApplication.java                # Entrada Spring Boot
│   ├── api/UsersController.java                # REST controller
│   ├── models/User.java, UserRole.java         # Modelos
│   ├── repository/UserRepository.java          # JPA repository
│   ├── security/JwtAuthenticationFilter.java   # Seguridad JWT
│   └── configuration/SecurityConfiguration.java# Config seguridad
├── src/main/resources/
│   ├── application.properties                   # Propiedades Spring
│   └── data.sql                                 # Datos iniciales
└── src/test/java/...                           # Tests
```

#### Auth API
```
auth-api/
├── main.go                              # Punto de entrada
├── user.go                              # Lógica de usuarios
├── tracing.go                           # Integración Zipkin
├── Gopkg.toml                           # Dependencias (legacy)
├── Dockerfile                           # Construcción Docker
└── README.md                            # Documentación
```

#### TODOs API
```
todos-api/
├── server.js                            # Servidor Express
├── routes.js                            # Rutas
├── todoController.js                    # Lógica de TODOs
├── package.json                         # Dependencias npm
├── Dockerfile                           # Construcción Docker
└── README.md                            # Documentación
```

#### Log Message Processor
```
log-message-processor/
├── main.py                              # Consumidor Redis
├── requirements.txt                     # Dependencias pip
├── Dockerfile                           # Construcción Docker
└── README.md                            # Documentación
```

#### Frontend
```
frontend/
├── src/
│   ├── main.js                          # Punto de entrada Vue
│   ├── auth.js                          # Plugin autenticación
│   ├── zipkin.js                        # Plugin Zipkin
│   ├── router/index.js                  # Vue Router
│   ├── store/
│   │   ├── index.js
│   │   ├── state.js                     # Estado Vuex
│   │   └── mutations.js                 # Mutaciones Vuex
│   └── components/
│       ├── App.vue
│       ├── Login.vue
│       ├── Todos.vue
│       └── ...
├── config/
│   ├── index.js                         # Config webpack dev/prod
│   ├── dev.env.js
│   ├── prod.env.js
│   └── dev.env.js
├── build/
│   ├── webpack.base.conf.js             # Config base webpack
│   ├── webpack.dev.conf.js              # Config webpack dev
│   ├── webpack.prod.conf.js             # Config webpack prod
│   ├── dev-server.js                    # Dev server
│   └── ...
├── package.json                         # Dependencias npm
├── Dockerfile                           # Multi-stage build
├── index.html                           # HTML template
└── README.md                            # Documentación
```

### Archivo README Principal

**Ubicación:** `README.md`

**Contiene:**
- Componentes y descripción
- Diagrama de arquitectura
- Instrucciones de build Docker
- Despliegue en Kubernetes (Minikube)
- ConfigMaps, Network Policies, HPA
- Monitoreo con Prometheus y Grafana
- Solución de problemas

---

## SCRIPTS Y HERRAMIENTAS

### Scripts de Construcción Docker

**En `README.md` (sección "Updated Build and Deployment Instructions"):**

```bash
# Auth API (Go)
cd auth-api
go mod init auth-api || true
go mod tidy
cd ..
docker build -t felipevelasco7/auth-api:latest ./auth-api

# TODOs API (Node.js)
docker build -t felipevelasco7/todos-api:latest ./todos-api

# Log Message Processor (Python)
docker build -t felipevelasco7/log-message-processor:latest ./log-message-processor

# Frontend (Vue)
docker build -t felipevelasco7/frontend:latest ./frontend

# Users API (Java)
docker build -t felipevelasco7/users-api:latest ./users-api
```

### Scripts de Despliegue Kubernetes

```bash
# Aplicar todos los manifiestos
kubectl apply -f k8s-manifests/

# Verificar despliegue
kubectl get pods -o wide
kubectl get svc
kubectl get cm (ConfigMaps)
kubectl get networkpolicies
kubectl get hpa

# Acceso al Frontend
minikube service frontend
# O con port-forward
kubectl port-forward svc/frontend 8080:80

# Acceso a Prometheus
kubectl port-forward svc/prometheus 9090:9090

# Acceso a Grafana
kubectl port-forward svc/grafana 3000:3000
```

### Scripts de Limpieza

```bash
# Eliminar solo recursos de Kubernetes
kubectl delete -f k8s-manifests/

# Detener/eliminar Minikube
minikube stop
minikube delete

# Eliminar imágenes Docker locales
docker rmi felipevelasco7/frontend:latest felipevelasco7/auth-api:latest \
          felipevelasco7/todos-api:latest felipevelasco7/log-message-processor:latest \
          felipevelasco7/users-api:latest
```

### Frontend Build Scripts

**En `frontend/package.json`:**

```bash
# Desarrollo
npm run dev

# Build producción
npm run build

# Análisis de bundle
npm run build --report

# Linter
npm run lint
```

---

## FLUJO DE DESPLIEGUE

### 1. Preparación Previa

```bash
# Verificar Docker y Kubernetes
docker --version
kubectl version
minikube version

# Iniciar Minikube (si no está activo)
minikube start --driver=docker
```

### 2. Construcción de Imágenes

**Opción A: Con push a Docker Hub**
```bash
# Construir todas las imágenes
docker build -t felipevelasco7/users-api:latest ./users-api
cd auth-api && go mod init auth-api || true && go mod tidy && cd ..
docker build -t felipevelasco7/auth-api:latest ./auth-api
docker build -t felipevelasco7/todos-api:latest ./todos-api
docker build -t felipevelasco7/log-message-processor:latest ./log-message-processor
docker build -t felipevelasco7/frontend:latest ./frontend

# Hacer push
docker push felipevelasco7/users-api:latest
docker push felipevelasco7/auth-api:latest
docker push felipevelasco7/todos-api:latest
docker push felipevelasco7/log-message-processor:latest
docker push felipevelasco7/frontend:latest
```

**Opción B: Cargar en Minikube Local**
```bash
minikube image load felipevelasco7/users-api:latest
minikube image load felipevelasco7/auth-api:latest
minikube image load felipevelasco7/todos-api:latest
minikube image load felipevelasco7/log-message-processor:latest
minikube image load felipevelasco7/frontend:latest

# Configurar Minikube para usar imágenes locales (imagePullPolicy: IfNotPresent)
```

### 3. Despliegue en Kubernetes

```bash
# Aplicar todos los manifiestos
kubectl apply -f k8s-manifests/

# Verificar que todos los pods están corriendo
kubectl get pods -o wide

# Esperar a que estén en estado "Running"
watch kubectl get pods
```

### 4. Configuración de ConfigMaps (si es necesario)

Los ConfigMaps ya están en los manifiestos, pero se pueden editar:

```bash
# Ver ConfigMaps creados
kubectl get cm

# Editar un ConfigMap (si es necesario)
kubectl edit cm auth-api-config
```

### 5. Acceso a la Aplicación

```bash
# Opción 1: Usar minikube service
minikube service frontend

# Opción 2: Port-forward
kubectl port-forward svc/frontend 8080:80

# Opción 3: NodePort directo
MINIKUBE_IP=$(minikube ip)
NODEPORT=$(kubectl get svc frontend -o jsonpath='{.spec.ports[0].nodePort}')
# Abrir: http://${MINIKUBE_IP}:${NODEPORT}
```

### 6. Despliegue de Monitoreo

```bash
# Prometheus
kubectl apply -f k8s-manifests/prometheus-deployment.yaml
kubectl port-forward svc/prometheus 9090:9090
# http://localhost:9090

# Grafana
kubectl apply -f k8s-manifests/grafana-deployment.yaml
kubectl port-forward svc/grafana 3000:3000
# http://localhost:3000 (admin/admin)
```

### 7. Solución de Problemas Comunes

| Problema | Causa | Solución |
|----------|-------|----------|
| `CrashLoopBackOff` en Auth API | "too many colons in address" | No usar `tcp://` en USERS_API_ADDRESS, debe ser `http://users-api:8080` |
| Module error en TODOs API | Dockerfile no copia correctamente | Verificar WORKDIR es `/app` y CMD es `node server.js` |
| `main.py not found` en Log Processor | Dockerfile no copia archivo | Asegurar que COPY está en el Dockerfile |
| Port-forward inestable | nginx vs http-server | Usar `minikube service frontend` o NodePort en lugar de port-forward |
| ImagePullBackOff | Imagen no encontrada | Usar `minikube image load` para imágenes locales o hacer push a Docker Hub |

### 8. Limpieza

```bash
# Eliminar recursos de Kubernetes
kubectl delete -f k8s-manifests/

# Eliminar Minikube (destructivo)
minikube delete

# Eliminar imágenes Docker locales
docker rmi felipevelasco7/users-api:latest felipevelasco7/auth-api:latest \
          felipevelasco7/todos-api:latest felipevelasco7/log-message-processor:latest \
          felipevelasco7/frontend:latest
```

---

## RESUMEN TÉCNICO

### Flujo de Autenticación

1. **Usuario ingresa credenciales en Frontend**
2. **Frontend envía POST a `/login`** (Auth API)
3. **Auth API valida contra Users API** (con JWT inter-servicio)
4. **Auth API retorna JWT token**
5. **Frontend almacena token en sessionStorage**
6. **Peticiones futuras incluyen token en header `Authorization: Bearer <token>`**

### Flujo de TODOs

1. **Frontend solicita GET `/todos`** (incluye JWT)
2. **TODOs API valida JWT y retorna TODOs del usuario**
3. **Al crear/eliminar, TODOs API publica en Redis**
4. **Log Message Processor escucha Redis y registra eventos**
5. **Con Zipkin: trazas distribuidas de toda la operación**

### Stack Tecnológico

| Aspecto | Tecnología |
|--------|-----------|
| Orquestación | Kubernetes (Minikube) |
| Contenedorización | Docker |
| Monitoreo | Prometheus + Grafana |
| Trazas Distribuidas | Zipkin |
| Cola de Mensajes | Redis |
| API Gateway/Proxy | nginx (Frontend) |
| Autenticación | JWT (HS256) |
| Persistencia | En-memoria (TODOs) + SQL (Users) |

---

## REFERENCIAS RÁPIDAS

### Puertos Clave

```
8000  - Auth API (Go)
8080  - Users API (Java)
3000  - TODOs API (Node.js)
80    - Frontend (nginx)
6379  - Redis
9090  - Prometheus
3000  - Grafana (dentro del cluster)
30000 - Prometheus NodePort
30001 - Grafana NodePort
```

### Rutas de Logs

```bash
# Logs del Pod
kubectl logs <pod-name>

# Logs en tiempo real
kubectl logs -f <pod-name>

# Logs de todos los Pods del servicio
kubectl logs -l app=auth-api
```

### Comandos Útiles

```bash
# Ejecutar comando en un Pod
kubectl exec -it <pod-name> -- /bin/bash

# Obtener detalles del Pod
kubectl describe pod <pod-name>

# Obtener recursos del nodo
kubectl top nodes
kubectl top pods

# Port-forward simple
kubectl port-forward pod/<pod-name> <local-port>:<remote-port>
```

---

**Fin del Análisis**

Este reporte proporciona una visión completa de la arquitectura, configuración y despliegue del repositorio microservice-app-example. Para más información, consulta el README principal del proyecto.

# Resumen Ejecutivo: Microservice App Example
## Análisis Rápido del Repositorio

---

##  DESCRIPCIÓN GENERAL

**Proyecto:** Aplicación TODO distribuida con arquitectura de microservicios  
**Lenguajes:** Go, Python, Vue.js, Java, Node.js  
**Orquestación:** Kubernetes (Minikube)  
**Monitoreo:** Prometheus + Grafana  
**Trazas Distribuidas:** Zipkin  

---

##  COMPONENTES PRINCIPALES

```
┌─────────────────────────────────────────────────────────────┐
│                     FRONTEND (Vue.js)                        │
│                      nginx | Puerto 80                       │
└────────────┬────────────────────────────────────┬──────────┘
             │                                    │
             ▼                                    ▼
    ┌─────────────────┐           ┌────────────────────┐
    │  Auth API (Go)  │           │  TODOs API (Node)  │
    │  Puerto 8000    │           │  Puerto 3000       │
    └────────┬────────┘           └──────────┬─────────┘
             │                               │
             ▼                               ▼
    ┌─────────────────┐           ┌──────────────────┐
    │ Users API (Java)│           │  Redis (Broker)  │
    │  Puerto 8080    │           │  Puerto 6379     │
    └─────────────────┘           └────────┬─────────┘
                                           │
                                           ▼
                                ┌────────────────────────┐
                                │ Log Message Processor  │
                                │ (Python Consumer)      │
                                └────────────────────────┘
```

---

##  DOCKER

### Imágenes Generadas

| Servicio | Base | Comando |
|----------|------|---------|
| Users API | Java 8 | `docker build -t felipevelasco7/users-api:latest ./users-api` |
| Auth API | Go 1.18 | `docker build -t felipevelasco7/auth-api:latest ./auth-api` |
| TODOs API | Node 8 | `docker build -t felipevelasco7/todos-api:latest ./todos-api` |
| Log Processor | Python 3.6 | `docker build -t felipevelasco7/log-message-processor:latest ./log-message-processor` |
| Frontend | Node 16 → nginx | `docker build -t felipevelasco7/frontend:latest ./frontend` |

### Dockerfiles Clave

- **Frontend:** Multi-stage (build en Node 16, serve en nginx)
- **Auth API:** Multi-stage (builder en Go 1.18, scratch runtime)
- **TODOs API:** Simple (Node 8 + npm install)
- **Log Processor:** Simple (Python 3.6 + pip)
- **Users API:** Multi-stage (Maven build, Java 8 runtime)

---

## ☸️  KUBERNETES

### Manifiestos en `k8s-manifests/`

#### 1. Deployments + Services
- `{service}-deployment.yaml` - Define Pod, container, recursos
- Ejemplo: `auth-api-deployment.yaml` crea Deployment + Service ClusterIP

#### 2. ConfigMaps
- Variables de entorno por servicio
- Ej: `JWT_SECRET`, `REDIS_HOST`, `ZIPKIN_URL`

#### 3. Network Policies
- Restricción de tráfico inter-pod
- Ej: Solo TODOs API puede comunicar con Redis

#### 4. HPA (Horizontal Pod Autoscaler)
- Auto-escalado basado en CPU/memoria
- Rango: 1-3 replicas típicamente

#### 5. Deployment Strategies
- Rolling Updates para despliegues sin downtime

### Aplicar Todos los Manifiestos

```bash
kubectl apply -f k8s-manifests/
```

### Acceder a la Aplicación

```bash
# Opción 1
minikube service frontend

# Opción 2
kubectl port-forward svc/frontend 8080:80
```

---

## 📈 PROMETHEUS Y GRAFANA

### Despliegue

```bash
# Prometheus (NodePort 30000)
kubectl apply -f k8s-manifests/prometheus-deployment.yaml
# URL: http://<MINIKUBE_IP>:30000

# Grafana (NodePort 30001)
kubectl apply -f k8s-manifests/grafana-deployment.yaml
# URL: http://<MINIKUBE_IP>:30001
# Credenciales: admin / admin
```

### Configuración en Grafana

1. **Data Source:** Configuration > Data Sources
   - URL: `http://prometheus:9090`

2. **Dashboard:** Dashboards > Import
   - ID: 315 (Kubernetes Cluster Monitoring)

### Métricas Recopiladas

- `http_requests_total` - Contador de peticiones
- `http_request_duration_seconds` - Duración de peticiones
- `container_memory_usage_bytes` - Memoria del contenedor
- `container_cpu_usage_seconds_total` - CPU del contenedor
- `redis_connected_clients` - Clientes Redis

---

## ⚙️  CONFIGURACIÓN POR SERVICIO

### Auth API (Go) - Puerto 8000

```bash
# Environment Variables
AUTH_API_PORT=8000
USERS_API_ADDRESS=http://users-api:8080
JWT_SECRET=PRFT
ZIPKIN_URL=http://zipkin:9411/api/v2/spans

# Build
cd auth-api && go mod tidy && go build && cd ..

# Run
./auth-api
```

**Usuarios Hardcodeados:** admin/admin, johnd/foo, janed/ddd

---

### Users API (Java) - Puerto 8080

```bash
# Environment Variables
JWT_SECRET=PRFT
SERVER_PORT=8083

# Build
./mvnw clean install

# Run
java -jar target/users-api-0.0.1-SNAPSHOT.jar
```

**Endpoints:** `GET /users`, `GET /users/{username}`

---

### TODOs API (Node.js) - Puerto 3000

```bash
# Environment Variables
TODO_API_PORT=3000
JWT_SECRET=PRFT
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_CHANNEL=log_channel

# Build & Run
npm install && npm start
```

**Endpoints:** `GET /todos`, `POST /todos`, `DELETE /todos/{taskId}`

**Flujo:** Crea/elimina → Publica en Redis → Log Processor escucha

---

### Log Message Processor (Python)

```bash
# Environment Variables
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_CHANNEL=log_channel

# Build
pip install -r requirements.txt

# Run
python main.py
```

**Función:** Consumer de Redis que registra eventos de TODOs

---

### Frontend (Vue.js) - Puerto 80

```bash
# Environment Variables
PORT=8080
AUTH_API_ADDRESS=http://auth-api:8000
TODOS_API_ADDRESS=http://todos-api:3000
ZIPKIN_URL=http://zipkin:9411/api/v2/spans

# Build
npm install --legacy-peer-deps && npm run build

# Run (development)
npm start

# Served (production)
nginx -g 'daemon off;'
```

**Flujo:**
1. Login → Auth API → obtiene JWT
2. GET/POST/DELETE TODOs → TODOs API (con JWT header)
3. Trazas distribuidas → Zipkin

---

## 🔑 ARCHIVOS CRÍTICOS

### Raíz del Proyecto

```
microservice-app-example/
├── k8s-manifests/               # Manifiestos Kubernetes
│   ├── {service}-deployment.yaml
│   ├── {service}-configmap.yaml
│   ├── {service}-hpa.yaml
│   ├── {service}-network-policy.yaml
│   ├── prometheus-deployment.yaml
│   └── grafana-deployment.yaml
├── README.md                    # Documentación principal
├── users-api/                   # Java Spring Boot
├── auth-api/                    # Go + Zipkin
├── todos-api/                   # Node.js + Redis
├── log-message-processor/       # Python + Redis consumer
└── frontend/                    # Vue.js + nginx
```

### Por Servicio

**Users API:**
- `pom.xml` - Maven POM
- `src/main/java/com/elgris/usersapi/` - Controllers, Models, Security
- `src/main/resources/application.properties` - Configuración Spring

**Auth API:**
- `main.go` - Servidor Echo
- `user.go` - Lógica de usuarios
- `tracing.go` - Zipkin middleware

**TODOs API:**
- `server.js` - Express + middleware
- `todoController.js` - Lógica CRUD
- `routes.js` - Enrutamiento

**Log Processor:**
- `main.py` - Consumer Redis + Zipkin

**Frontend:**
- `src/main.js` - Entrada Vue
- `src/router/index.js` - Vue Router
- `src/components/` - Componentes Vue
- `config/index.js` - Webpack dev/prod
- `build/` - Scripts build

---

##  FLUJO DE DESPLIEGUE RÁPIDO

### 1. Build & Push de Imágenes

```bash
# Build
docker build -t felipevelasco7/users-api:latest ./users-api
cd auth-api && go mod init auth-api || true && go mod tidy && cd ..
docker build -t felipevelasco7/auth-api:latest ./auth-api
docker build -t felipevelasco7/todos-api:latest ./todos-api
docker build -t felipevelasco7/log-message-processor:latest ./log-message-processor
docker build -t felipevelasco7/frontend:latest ./frontend

# Push a Docker Hub
docker push felipevelasco7/users-api:latest
docker push felipevelasco7/auth-api:latest
docker push felipevelasco7/todos-api:latest
docker push felipevelasco7/log-message-processor:latest
docker push felipevelasco7/frontend:latest
```

### 2. Desplegar en Kubernetes

```bash
kubectl apply -f k8s-manifests/
```

### 3. Verificar Despliegue

```bash
kubectl get pods -o wide
kubectl get svc
```

### 4. Acceder a la App

```bash
minikube service frontend
```

### 5. Monitoreo

```bash
# Prometheus
kubectl port-forward svc/prometheus 9090:9090

# Grafana
kubectl port-forward svc/grafana 3000:3000
```

---

## 🔍 VARIABLES DE ENTORNO CRÍTICAS

### JWT_SECRET
- Debe ser **igual en todos los servicios**
- Usado para validación de tokens
- Default: "PRFT"

### ZIPKIN_URL
- Para trazas distribuidas
- Default: `http://zipkin:9411/api/v2/spans`
- Opcional (si no se configura, no hay tracing)

### REDIS_*
- `REDIS_HOST`: nombre del servicio Redis (ej: "redis")
- `REDIS_PORT`: puerto (6379)
- `REDIS_CHANNEL`: canal de suscripción (ej: "log_channel")

### Direcciones Inter-Servicio (Kubernetes)
- `http://users-api:8080`
- `http://auth-api:8000`
- `http://todos-api:3000`
- `redis:6379`

** NOTA:** No usar `tcp://` en direcciones, debe ser `http://` o nombre del servicio

---

##  SOLUCIÓN DE PROBLEMAS

| Error | Causa | Solución |
|-------|-------|----------|
| `CrashLoopBackOff` | Dirección con `tcp://` | Usar `http://users-api:8080` sin prefijo `tcp://` |
| `Error: Cannot find module 'server.js'` | Dockerfile WORKDIR incorrecto | Cambiar a `WORKDIR /app` y `CMD ["node", "server.js"]` |
| `main.py not found` | COPY en Dockerfile falta | Agregar `COPY main.py /app/` en Dockerfile |
| `ImagePullBackOff` | Imagen no disponible | Usar `minikube image load` o push a Docker Hub |
| Port-forward inestable | nginx servicio incorrecto | Usar `minikube service frontend` |

---

##  REFERENCIAS RÁPIDAS

### Puertos Principales

| Servicio | Puerto | Tipo |
|----------|--------|------|
| Frontend | 80 | Service |
| Auth API | 8000 | ClusterIP |
| Users API | 8080 | ClusterIP |
| TODOs API | 3000 | ClusterIP |
| Redis | 6379 | ClusterIP |
| Prometheus | 9090 | NodePort (30000) |
| Grafana | 3000 | NodePort (30001) |

### Comandos Kubernetes Útiles

```bash
kubectl get pods -o wide          # Ver pods
kubectl logs <pod-name>           # Ver logs
kubectl exec -it <pod-name> /bin/bash  # Entrar a pod
kubectl port-forward <svc-name> <local>:<remote>  # Port forward
kubectl describe pod <pod-name>   # Detalles del pod
kubectl apply -f <archivo.yaml>   # Aplicar manifiesto
kubectl delete -f <archivo.yaml>  # Eliminar manifiesto
kubectl edit cm <nombre-configmap> # Editar ConfigMap
```

---

##  CHECKLIST DE DESPLIEGUE

- [ ] Docker instalado y corriendo
- [ ] kubectl y Minikube instalados
- [ ] Minikube iniciado: `minikube start --driver=docker`
- [ ] Imágenes buildadas o en Docker Hub
- [ ] Manifiestos de Kubernetes listos
- [ ] Variables de entorno configuradas en ConfigMaps
- [ ] `kubectl apply -f k8s-manifests/` ejecutado
- [ ] Todos los pods en estado "Running"
- [ ] Frontend accesible: `minikube service frontend`
- [ ] Prometheus en `http://<IP>:30000`
- [ ] Grafana en `http://<IP>:30001`
- [ ] Trazas visibles en Zipkin (si está configurado)

---

**Documento de Referencia Rápida**  
Consulta `ANALISIS_MICROSERVICE_APP_EXAMPLE.md` para detalles completos

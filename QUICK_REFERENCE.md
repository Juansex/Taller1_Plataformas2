# Quick Reference: Microservice App Example
## Tabla de Referencia Rápida

---

## COMPONENTES DEL SISTEMA

| Componente | Lenguaje | Puerto | Tipo Build | Key Files | Función |
|-----------|----------|--------|-----------|-----------|---------|
| **Frontend** | Vue.js | 80 | Multi-stage (Node→nginx) | `src/main.js`, `config/index.js` | UI, enrutamiento, autenticación |
| **Auth API** | Go | 8000 | Multi-stage (go→scratch) | `main.go`, `user.go`, `tracing.go` | JWT token generation |
| **Users API** | Java | 8080 | Multi-stage (maven→java) | `pom.xml`, `UsersController.java` | User profiles, GET endpoints |
| **TODOs API** | Node.js | 3000 | Single-stage | `server.js`, `todoController.js` | CRUD operations, Redis logging |
| **Log Processor** | Python | N/A | Single-stage | `main.py`, `requirements.txt` | Redis consumer, event logging |
| **Redis** | Docker | 6379 | Official image | - | Message queue for logging |
| **Prometheus** | Docker | 9090 | Official image | `prometheus-deployment.yaml` | Metrics collection |
| **Grafana** | Docker | 3000 | Official image | `grafana-deployment.yaml` | Metrics visualization |

---

## VARIABLES DE ENTORNO CRÍTICAS

```
AUTH_API_PORT                    = 8000
USERS_API_ADDRESS               = http://users-api:8080          NO tcp://
JWT_SECRET                       = PRFT                           IGUAL en todos los servicios
ZIPKIN_URL                       = http://zipkin:9411/api/v2/spans

TODO_API_PORT                    = 3000
REDIS_HOST                       = redis
REDIS_PORT                       = 6379
REDIS_CHANNEL                    = log_channel

SERVER_PORT                      = 8083                          (Users API)
TODOS_API_ADDRESS               = http://todos-api:3000          (Frontend)
AUTH_API_ADDRESS                = http://auth-api:8000           (Frontend)
```

---

## ESTRUCTURA DE MANIFIESTOS KUBERNETES

```
k8s-manifests/
├── {service}-deployment.yaml           → Deployment + Service ClusterIP
├── {service}-configmap.yaml            → Variables de entorno
├── {service}-hpa.yaml                  → Auto-escalado (1-3 replicas)
├── {service}-network-policy.yaml       → Restricción de tráfico
├── {service}-deployment-strategy.yaml  → Rolling updates
├── redis-deployment.yaml
├── prometheus-deployment.yaml          → NodePort 30000
└── grafana-deployment.yaml             → NodePort 30001
```

**Servicios:** auth-api, users-api, todos-api, log-message-processor, frontend

---

##  COMANDOS DOCKER

| Tarea | Comando |
|-------|---------|
| Build Auth API | `cd auth-api && go mod tidy && cd .. && docker build -t felipevelasco7/auth-api:latest ./auth-api` |
| Build Users API | `docker build -t felipevelasco7/users-api:latest ./users-api` |
| Build TODOs API | `docker build -t felipevelasco7/todos-api:latest ./todos-api` |
| Build Log Processor | `docker build -t felipevelasco7/log-message-processor:latest ./log-message-processor` |
| Build Frontend | `docker build -t felipevelasco7/frontend:latest ./frontend` |
| Push image | `docker push felipevelasco7/{service}:latest` |
| List images | `docker images \| grep felipevelasco7` |
| Remove image | `docker rmi felipevelasco7/{service}:latest` |
| Run container | `docker run -e VAR=valor -p 8000:8000 felipevelasco7/{service}:latest` |

---

## ☸️ COMANDOS KUBERNETES

| Tarea | Comando |
|-------|---------|
| Iniciar Minikube | `minikube start --driver=docker` |
| Obtener IP | `minikube ip` |
| Aplicar manifiestos | `kubectl apply -f k8s-manifests/` |
| Ver pods | `kubectl get pods -o wide` |
| Ver servicios | `kubectl get svc` |
| Ver ConfigMaps | `kubectl get cm` |
| Ver eventos | `kubectl get events --sort-by='.lastTimestamp'` |
| Ver logs | `kubectl logs pod/{pod-name}` |
| Logs en tiempo real | `kubectl logs -f pod/{pod-name}` |
| Entrar a pod | `kubectl exec -it pod/{pod-name} -- /bin/bash` |
| Port-forward | `kubectl port-forward svc/{service} {local}:{remote}` |
| Editar ConfigMap | `kubectl edit cm/{name}` |
| Restart deployment | `kubectl rollout restart deployment/{name}` |
| Describe pod | `kubectl describe pod/{pod-name}` |
| Delete resource | `kubectl delete -f k8s-manifests/` |
| Top nodos | `kubectl top nodes` |
| Top pods | `kubectl top pods` |

---

## 🔗 ACCESO A SERVICIOS

### Dentro del Cluster (Kubernetes)
```
Frontend    → http://frontend:80
Auth API    → http://auth-api:8000
Users API   → http://users-api:8080
TODOs API   → http://todos-api:3000
Redis       → redis:6379
Prometheus  → http://prometheus:9090
```

### Desde Host (Port-forward)
```bash
minikube service frontend                              # Frontend (NodePort)
kubectl port-forward svc/auth-api 8000:8000           # Auth API
kubectl port-forward svc/users-api 8080:8080          # Users API
kubectl port-forward svc/todos-api 3000:3000          # TODOs API
kubectl port-forward svc/prometheus 9090:9090         # Prometheus
kubectl port-forward svc/grafana 3000:3000            # Grafana
```

### NodePort Directo
```bash
MINIKUBE_IP=$(minikube ip)
NODEPORT=$(kubectl get svc frontend -o jsonpath='{.spec.ports[0].nodePort}')
echo "http://${MINIKUBE_IP}:${NODEPORT}"              # Frontend
echo "http://${MINIKUBE_IP}:30000"                     # Prometheus
echo "http://${MINIKUBE_IP}:30001"                     # Grafana
```

---

##  USUARIOS HARDCODEADOS (Auth API)

| Username | Password | Role |
|----------|----------|------|
| admin | admin | Admin |
| johnd | foo | User |
| janed | ddd | User |

---

##  ENDPOINTS HTTP

### Auth API (POST)
```
POST /login
Content-Type: application/json
Body: {"username":"admin","password":"admin"}
Response: JWT Token
```

### Users API (GET)
```
GET /users                                    # Listar todos
GET /users/{username}                        # Obtener uno
Authorization: Bearer <JWT_TOKEN>
```

### TODOs API (CRUD)
```
GET /todos                                    # Listar
POST /todos                                   # Crear
Body: {"content":"Task description"}
DELETE /todos/{taskId}                        # Eliminar
Authorization: Bearer <JWT_TOKEN> (requerido)
```

---

##  TROUBLESHOOTING RÁPIDO

| Problema | Causa | Solución |
|----------|-------|----------|
| `CrashLoopBackOff` | Dirección con `tcp://` | Cambiar a `http://users-api:8080` |
| `Module not found` | WORKDIR incorrecto en Dockerfile | Agregar `WORKDIR /app` |
| `ImagePullBackOff` | Imagen no existe | `minikube image load` o `docker push` |
| `Connection refused` | Puerto incorrecto | Verificar puerto en ConfigMap |
| `Can't connect to Redis` | REDIS_HOST incorrecto | Usar `redis` como nombre del servicio |
| Pod pending | Recursos insuficientes | `kubectl describe pod` o aumentar recursos |
| Port-forward inestable | nginx servicio incorrecto | Usar `minikube service frontend` |

---

## 📈 MÉTRICAS PROMETHEUS CLAVE

```
up{job="..."}                               # Status del servicio (1=up, 0=down)
http_requests_total                        # Total de peticiones HTTP
http_request_duration_seconds               # Duración promedio de peticiones
container_memory_usage_bytes                # Uso de memoria (bytes)
container_cpu_usage_seconds_total          # Uso de CPU (segundos)
redis_connected_clients                     # Clientes conectados a Redis
redis_commands_processed_total              # Comandos procesados por Redis
```

---

## 🔄 FLUJOS PRINCIPALES

### Flujo de Autenticación
```
Frontend Login Page
    ↓
POST /login → Auth API
    ↓
Auth API valida contra Users API
    ↓
JWT Token retornado
    ↓
Frontend almacena en sessionStorage
```

### Flujo de TODOs
```
GET /todos (con JWT header)
    ↓
TODOs API valida JWT
    ↓
TODOs API retorna lista del usuario
    ↓
Al crear/eliminar:
    → TODOs API publica en Redis
    ↓
Log Message Processor escucha
    ↓
Log message a stdout
```

### Flujo de Tracing (con Zipkin)
```
Frontend request
    ↓
Tracing middleware agrega headers
    ↓
Auth API/TODOs API continúan traza
    ↓
Spans enviados a Zipkin
    ↓
Trazas visibles en Zipkin UI
```

---

## 🛠️ SOLUCIÓN RÁPIDA DE PROBLEMAS

### Ver qué está fallando
```bash
kubectl describe pod/{pod-name}              # Detalles del pod
kubectl logs pod/{pod-name}                  # Logs del pod
kubectl get events --sort-by='.lastTimestamp'  # Eventos del cluster
```

### Verificar conectividad entre servicios
```bash
# Desde un pod de debug
kubectl run -it --image=busybox --restart=Never -- sh

# Dentro del pod:
nslookup redis                               # Resolver DNS
curl http://auth-api:8000/version            # Test conectividad
ping users-api                               # Ping a otro servicio
```

### Editar configuración sin reconstruir
```bash
kubectl edit cm/auth-api-config              # Editar ConfigMap
kubectl rollout restart deployment/auth-api  # Reiniciar pod con nueva config
```

### Resetear todo
```bash
kubectl delete -f k8s-manifests/             # Eliminar recursos
minikube delete                              # Eliminar cluster
minikube start --driver=docker               # Recrear cluster
```

---

##  ARCHIVOS MÁS IMPORTANTES

```
k8s-manifests/               → Todos los manifiestos Kubernetes
README.md                    → Documentación principal
users-api/pom.xml          → Dependencias Java
auth-api/main.go           → Servidor Auth API
todos-api/server.js        → Servidor TODOs API
frontend/src/main.js       → Entrada Vue.js
frontend/config/index.js   → Configuración Webpack
```

---

##  CHECKLIST DE DESPLIEGUE

```
☐ Docker instalado
☐ kubectl instalado
☐ Minikube instalado
☐ Minikube iniciado (minikube start --driver=docker)
☐ Imágenes buildadas (docker build)
☐ Imágenes en Minikube (minikube image load)
☐ kubectl apply -f k8s-manifests/ ejecutado
☐ kubectl get pods -o wide → todos Running
☐ minikube service frontend → Frontend accesible
☐ kubectl port-forward svc/prometheus 9090:9090 → Prometheus activo
☐ kubectl port-forward svc/grafana 3000:3000 → Grafana activo
☐ Login con admin/admin en frontend → funciona
☐ Crear/eliminar TODO → funciona
☐ Ver logs en Log Processor → funciona
```

---

##  REFERENCIAS RÁPIDAS

- **Oficial Kubernetes:** https://kubernetes.io/docs
- **Docker Hub (felipevelasco7):** https://hub.docker.com/u/felipevelasco7
- **Repositorio GitHub:** https://github.com/felipevelasco7/microservice-app-example
- **Prometheus:** http://localhost:9090 (localhost con port-forward)
- **Grafana:** http://localhost:3000 (admin/admin)
- **Documentación del Proyecto:** Ver README.md en raíz del repositorio

---

**Última actualización:** Noviembre 2025  
**Versión:** 1.0

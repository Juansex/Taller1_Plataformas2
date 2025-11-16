# Microservice App - Taller 1 Plataformas 2

Aplicación de microservicios con 9 servicios containerizados, escalado automático con Kubernetes HPA, CI/CD automático y monitoreo en tiempo real.

## Quick Start

### Docker Compose (Desarrollo)
```bash
cp .env.example .env
docker-compose up -d
docker-compose ps  # Verificar 9 servicios UP
```

URLs: Frontend (8080), Prometheus (9090), Grafana (3000)

### Kubernetes con HPA (Producción)
```bash
cd k8s-manifests
./deploy.sh
kubectl get hpa -w  # Monitorear escalado automático
```

## Arquitectura

**9 Servicios:**
- Frontend (Vue.js, 8080) - Interfaz web
- Auth API (Go, 8000) - Autenticación JWT
- Users API (Java/Spring Boot, 8083) - Gestión de usuarios
- Todos API (Node.js, 8082) - CRUD de tareas
- Log Processor (Python) - Procesamiento de logs desde Redis
- Redis (6379) - Broker de mensajes
- Redis Exporter (9121) - Métricas de Redis
- Prometheus (9090) - Recolección de métricas
- Grafana (3000) - Visualización de dashboards

**Networking:** Todos los servicios se comunican por service names en red interna.

## 8 Criterios de Evaluación

| Criterio | Implementación |
|----------|---|
| **Docker** | 9 servicios containerizados con docker-compose.yml |
| **Networking** | Comunicación por service names |
| **HPA** | 5 Horizontal Pod Autoscalers (CPU 70%, Memory 80%, 2-10 replicas) |
| **Secrets** | Variables externalizadas en .env, protegidas en .gitignore |
| **CI/CD** | GitHub Actions: Build, Lint, Test, Summary en cada push |
| **Monitoring** | Prometheus + Grafana + Redis Exporter + Spring Boot Actuator + Zipkin |
| **Documentación** | README completo con instrucciones |
| **Demostración** | Sistema completamente funcional y testeable |

## Deployment

### Estructura Kubernetes
```
k8s-manifests/
├── deployments/     (5 servicios)
├── services/        (ClusterIP + NodePort)
├── hpa/             (5 HPAs configurados)
├── configmaps/      (variables públicas)
├── secrets/         (datos sensibles)
└── deploy.sh        (script automático)
```

### Configuración HPA
- Min Replicas: 2 | Max Replicas: 10
- CPU Threshold: 70% | Memory Threshold: 80%
- Scale Up: Duplica cada 30 segundos
- Scale Down: Reduce 50% después de 5 minutos

## Requisitos Previos

**Docker Compose:**
- Docker >= 20.10
- Docker Compose >= 1.29

**Kubernetes:**
- kubectl configurado
- Kubernetes cluster (Minikube, Docker Desktop, etc.)
- Metrics Server instalado

## Variables de Entorno

Copiar `.env.example` a `.env`:
```bash
REDIS_PASSWORD=RedisSecure2025!
JWT_SECRET=PRFT
AUTH_API_PORT=8000
SERVER_PORT=8083
TODO_API_PORT=8082
REDIS_CHANNEL=log_channel
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
```

## Comandos Útiles

**Docker Compose:**
```bash
docker-compose up -d              # Levantar
docker-compose ps                 # Ver estado
docker-compose logs -f            # Ver logs
docker-compose down -v            # Detener y limpiar
docker-compose up -d --scale users-api=5  # Escalar manual
```

**Kubernetes:**
```bash
kubectl apply -f k8s-manifests/   # Aplicar manifiestos
kubectl get pods                  # Listar pods
kubectl get hpa                   # Ver HPAs
kubectl logs <pod>                # Ver logs
kubectl describe hpa <nombre>     # Detalles HPA
kubectl top pods                  # Uso de recursos
```

**Prometheus Queries:**
```
rate(http_requests_total[5m])     # Requests por segundo
process_cpu_seconds_total         # CPU usage
jvm_memory_usage_bytes            # Memoria JVM
redis_connected_clients           # Clientes Redis
```

## Verificación de Funcionamiento

1. Frontend accesible: `http://localhost:8080`
2. Login: `admin / admin`
3. Prometheus: `http://localhost:9090`
4. Grafana: `http://localhost:3000` (admin/admin)
5. Todos los 9 servicios en estado UP

## Troubleshooting

**Docker: Puerto ya en uso**
```bash
lsof -i :8080
kill -9 <PID>
```

**Redis: Connection refused**
```bash
docker ps | grep redis
docker logs <container-id>
```

**Kubernetes: Pods no inician**
```bash
kubectl describe pod <pod-name>
kubectl logs <pod-name>
```

**Metrics Server no disponible**
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

## Licencia

MIT License - Ver archivo [LICENSE](LICENSE)

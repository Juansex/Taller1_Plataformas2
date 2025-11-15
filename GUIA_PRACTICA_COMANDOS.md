# Guía Práctica: Comandos y Configuraciones
## Microservice App Example - Ejemplos Ejecutables

---

## TABLA DE CONTENIDOS
1. [Construcción de Imágenes Docker](#construcción-de-imágenes-docker)
2. [Despliegue en Kubernetes](#despliegue-en-kubernetes)
3. [Acceso y Debugging](#acceso-y-debugging)
4. [Monitoreo y Métricas](#monitoreo-y-métricas)
5. [Variables de Entorno](#variables-de-entorno)
6. [Tests y Validación](#tests-y-validación)

---

## CONSTRUCCIÓN DE IMÁGENES DOCKER

### Build de Todas las Imágenes

```bash
#!/bin/bash
# build-all-images.sh

echo "=== Building Users API (Java) ==="
docker build -t felipevelasco7/users-api:latest ./users-api

echo "=== Building Auth API (Go) ==="
cd auth-api
go mod init auth-api || true
go mod tidy
cd ..
docker build -t felipevelasco7/auth-api:latest ./auth-api

echo "=== Building TODOs API (Node.js) ==="
docker build -t felipevelasco7/todos-api:latest ./todos-api

echo "=== Building Log Message Processor (Python) ==="
docker build -t felipevelasco7/log-message-processor:latest ./log-message-processor

echo "=== Building Frontend (Vue.js + nginx) ==="
docker build -t felipevelasco7/frontend:latest ./frontend

echo "=== All images built successfully ==="
docker images | grep felipevelasco7
```

### Build Individual por Servicio

#### Users API (Java)

```bash
# Opción 1: Build directo
docker build -t felipevelasco7/users-api:latest ./users-api

# Opción 2: Con argumentos BUILD_ARG
docker build \
  --build-arg MAVEN_VERSION=3.8.1 \
  -t felipevelasco7/users-api:latest \
  ./users-api

# Verificar
docker run -p 8083:8080 felipevelasco7/users-api:latest
# Test: curl http://localhost:8083/users
```

#### Auth API (Go)

```bash
# Setup módulo Go
cd auth-api
go mod init auth-api || true
go mod tidy
cd ..

# Build
docker build -t felipevelasco7/auth-api:latest ./auth-api

# Verificar
docker run -e AUTH_API_PORT=8000 \
           -e USERS_API_ADDRESS=http://host.docker.internal:8083 \
           -p 8000:8000 \
           felipevelasco7/auth-api:latest
# Test: curl -X POST http://localhost:8000/login -d '{"username":"admin","password":"admin"}'
```

#### TODOs API (Node.js)

```bash
# Build
docker build -t felipevelasco7/todos-api:latest ./todos-api

# Run con variables de entorno
docker run -e JWT_SECRET=PRFT \
           -e TODO_API_PORT=3000 \
           -e REDIS_HOST=redis \
           -e REDIS_PORT=6379 \
           -e REDIS_CHANNEL=log_channel \
           -p 3000:3000 \
           --link redis:redis \
           felipevelasco7/todos-api:latest

# Test: curl http://localhost:3000/todos
```

#### Log Message Processor (Python)

```bash
# Build
docker build -t felipevelasco7/log-message-processor:latest ./log-message-processor

# Run
docker run -e REDIS_HOST=redis \
           -e REDIS_PORT=6379 \
           -e REDIS_CHANNEL=log_channel \
           --link redis:redis \
           felipevelasco7/log-message-processor:latest
```

#### Frontend (Vue.js)

```bash
# Build
docker build -t felipevelasco7/frontend:latest ./frontend

# Run
docker run -p 8080:80 felipevelasco7/frontend:latest

# Build con tag específico para nginx
docker build -t felipevelasco7/frontend:nginx-80 ./frontend
```

### Push a Docker Hub

```bash
#!/bin/bash
# push-all-images.sh

echo "Pushing images to Docker Hub..."

docker push felipevelasco7/users-api:latest
docker push felipevelasco7/auth-api:latest
docker push felipevelasco7/todos-api:latest
docker push felipevelasco7/log-message-processor:latest
docker push felipevelasco7/frontend:latest

echo "All images pushed successfully!"
```

### Limpiar Imágenes Locales

```bash
# Ver imágenes
docker images | grep felipevelasco7

# Eliminar una imagen
docker rmi felipevelasco7/frontend:latest

# Eliminar todas las imágenes del proyecto
docker rmi \
  felipevelasco7/users-api:latest \
  felipevelasco7/auth-api:latest \
  felipevelasco7/todos-api:latest \
  felipevelasco7/log-message-processor:latest \
  felipevelasco7/frontend:latest
```

---

## DESPLIEGUE EN KUBERNETES

### Iniciar Minikube

```bash
# Iniciar Minikube
minikube start --driver=docker

# Verificar estado
minikube status

# Obtener IP
minikube ip
# Ej: 192.168.49.2

# Detener Minikube
minikube stop

# Eliminar Minikube (destructivo)
minikube delete
```

### Cargar Imágenes en Minikube

```bash
#!/bin/bash
# load-images-minikube.sh

echo "Loading images into Minikube..."

minikube image load felipevelasco7/users-api:latest
minikube image load felipevelasco7/auth-api:latest
minikube image load felipevelasco7/todos-api:latest
minikube image load felipevelasco7/log-message-processor:latest
minikube image load felipevelasco7/frontend:latest

# Verificar imágenes cargadas
minikube image ls | grep felipevelasco7

echo "All images loaded successfully!"
```

### Aplicar Manifiestos de Kubernetes

```bash
# Aplicar todos los manifiestos
kubectl apply -f k8s-manifests/

# Aplicar por componente (en orden de dependencias)
kubectl apply -f k8s-manifests/redis-deployment.yaml
kubectl apply -f k8s-manifests/users-api-deployment.yaml
kubectl apply -f k8s-manifests/auth-api-deployment.yaml
kubectl apply -f k8s-manifests/todos-api-deployment.yaml
kubectl apply -f k8s-manifests/log-message-processor-deployment.yaml
kubectl apply -f k8s-manifests/frontend-deployment.yaml

# Aplicar monitoreo
kubectl apply -f k8s-manifests/prometheus-deployment.yaml
kubectl apply -f k8s-manifests/grafana-deployment.yaml

# Aplicar políticas de red
kubectl apply -f k8s-manifests/*-network-policy.yaml

# Aplicar HPA
kubectl apply -f k8s-manifests/*-hpa.yaml

# Aplicar estrategias de despliegue
kubectl apply -f k8s-manifests/*-deployment-strategy.yaml
```

### Verificar Despliegue

```bash
# Ver todos los pods
kubectl get pods -o wide

# Ver servicios
kubectl get svc

# Ver ConfigMaps
kubectl get cm

# Ver NetworkPolicies
kubectl get networkpolicies

# Ver HPA
kubectl get hpa

# Ver eventos
kubectl get events --sort-by='.lastTimestamp'

# Esperar a que los pods estén listos (watch)
watch kubectl get pods
```

### Eliminar Manifiestos

```bash
# Eliminar todos los recursos
kubectl delete -f k8s-manifests/

# Eliminar por tipo
kubectl delete deployments --all
kubectl delete services --all
kubectl delete cm --all
kubectl delete networkpolicies --all
kubectl delete hpa --all
```

---

## ACCESO Y DEBUGGING

### Acceso al Frontend

```bash
# Opción 1: minikube service (recomendado)
minikube service frontend

# Opción 2: Port-forward del servicio
kubectl port-forward svc/frontend 8080:80
# Abrir: http://localhost:8080

# Opción 3: NodePort directo
MINIKUBE_IP=$(minikube ip)
NODEPORT=$(kubectl get svc frontend -o jsonpath='{.spec.ports[0].nodePort}')
echo "Frontend: http://${MINIKUBE_IP}:${NODEPORT}"

# Opción 4: Port-forward del deployment
kubectl port-forward deployment/frontend 8080:80
```

### Logs de Pods

```bash
# Ver logs de un pod específico
kubectl logs pod/auth-api-xyz123

# Ver logs en tiempo real
kubectl logs -f pod/auth-api-xyz123

# Ver logs de todos los pods de un servicio
kubectl logs -l app=auth-api

# Ver logs de los últimos 100 líneas
kubectl logs pod/auth-api-xyz123 --tail=100

# Ver logs del contenedor anterior (si crasheó)
kubectl logs pod/auth-api-xyz123 --previous

# Logs de múltiples contenedores
kubectl logs pod/todos-api-xyz123 -c todos-api
```

### Ejecutar Comandos en Pods

```bash
# Entrar a un pod (bash)
kubectl exec -it pod/auth-api-xyz123 -- /bin/bash

# Entrar a un pod (sh, para imágenes Alpine)
kubectl exec -it pod/log-message-processor-xyz123 -- /bin/sh

# Ejecutar comando específico
kubectl exec pod/auth-api-xyz123 -- curl http://users-api:8080/users

# Ejecutar en otro contenedor si hay múltiples
kubectl exec -it pod/todos-api-xyz123 -c todos-api -- /bin/bash
```

### Describir Recursos

```bash
# Detalles de un pod
kubectl describe pod/auth-api-xyz123

# Detalles de un servicio
kubectl describe svc/frontend

# Detalles de un deployment
kubectl describe deployment/auth-api

# Detalles de un ConfigMap
kubectl describe cm/auth-api-config
```

### Port-Forward

```bash
# Port-forward a servicio
kubectl port-forward svc/frontend 8080:80

# Port-forward a pod específico
kubectl port-forward pod/frontend-xyz123 8080:80

# Port-forward a deployment
kubectl port-forward deployment/frontend 8080:80

# Port-forward múltiples puertos
kubectl port-forward svc/prometheus 9090:9090

# Ejecutar en background
kubectl port-forward svc/frontend 8080:80 &
```

### Editar Recursos

```bash
# Editar un ConfigMap
kubectl edit cm/auth-api-config

# Editar un Deployment
kubectl edit deployment/auth-api

# Editar un Service
kubectl edit svc/frontend

# Ver YAML actual de un recurso
kubectl get cm/auth-api-config -o yaml
kubectl get deployment/auth-api -o yaml
```

### Copiar Archivos

```bash
# Copiar archivo del pod a local
kubectl cp pod/frontend-xyz123:/usr/share/nginx/html/index.html ./index.html

# Copiar archivo de local a pod
kubectl cp ./config.json pod/auth-api-xyz123:/app/config.json

# Copiar con especificación de contenedor
kubectl cp pod/todos-api-xyz123:/app/server.js ./server.js -c todos-api
```

---

## MONITOREO Y MÉTRICAS

### Prometheus

```bash
# Port-forward a Prometheus
kubectl port-forward svc/prometheus 9090:9090

# Abrir en navegador
# http://localhost:9090

# Queries útiles en Prometheus:
# - up                           # Status de todos los targets
# - http_requests_total          # Total de peticiones HTTP
# - container_memory_usage_bytes # Memoria de contenedores
# - container_cpu_usage_seconds_total  # CPU de contenedores

# Ver targets scrapeados
# Ir a http://localhost:9090/targets

# Ver service discovery
# Ir a http://localhost:9090/service-discovery
```

### Grafana

```bash
# Port-forward a Grafana
kubectl port-forward svc/grafana 3000:3000

# Abrir en navegador
# http://localhost:3000
# Usuario: admin
# Contraseña: admin

# Paso 1: Agregar Prometheus como Data Source
# Configuration > Data Sources > Prometheus
# URL: http://prometheus:9090

# Paso 2: Importar Dashboard
# Dashboards > Import
# ID: 315 (Kubernetes Cluster Monitoring)

# Paso 3: Ver métricas en tiempo real
# Seleccionar dashboard y observar gráficos
```

### Métricas Disponibles

```bash
# Ver todas las métricas disponibles
# En Prometheus: http://localhost:9090/metrics

# Métricas importantes:
# - up{job="kubernetes-nodes"}                    # Nodos disponibles
# - container_memory_usage_bytes{pod_name="..."}  # Memoria por pod
# - container_cpu_usage_seconds_total             # CPU por pod
# - http_requests_total{endpoint="/todos"}        # Peticiones HTTP
# - http_request_duration_seconds                 # Latencia HTTP
# - redis_connected_clients                       # Clientes Redis
```

### Monitoreo Manual

```bash
# Top de nodos
kubectl top nodes

# Top de pods
kubectl top pods

# Top de pods por namespace
kubectl top pods -n kube-system

# Uso de recursos en tiempo real
watch kubectl top pods

# Describir nodos
kubectl describe nodes

# Estrés test (usar con cuidado)
kubectl run -i --tty --image=busybox --restart=Never -- sh
# Dentro del pod: curl -s http://frontend/
```

---

## VARIABLES DE ENTORNO

### Editar ConfigMaps

```bash
# Ver ConfigMap actual
kubectl get cm/auth-api-config -o yaml

# Editar ConfigMap
kubectl edit cm/auth-api-config

# Recargar pods después de cambiar ConfigMap
kubectl rollout restart deployment/auth-api
```

### Configuración Completa en Kubernetes

#### Auth API ConfigMap

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

#### TODOs API ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: todos-api-config
data:
  TODO_API_PORT: "3000"
  JWT_SECRET: "PRFT"
  REDIS_HOST: "redis"
  REDIS_PORT: "6379"
  REDIS_CHANNEL: "log_channel"
  ZIPKIN_URL: "http://zipkin:9411/api/v2/spans"
```

#### Log Message Processor ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: log-message-processor-config
data:
  REDIS_HOST: "redis"
  REDIS_PORT: "6379"
  REDIS_CHANNEL: "log_channel"
  ZIPKIN_URL: "http://zipkin:9411/api/v2/spans"
```

#### Frontend ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: frontend-config
data:
  AUTH_API_ADDRESS: "http://auth-api:8000"
  TODOS_API_ADDRESS: "http://todos-api:3000"
  ZIPKIN_URL: "http://zipkin:9411/api/v2/spans"
```

---

## TESTS Y VALIDACIÓN

### Test Manual de Endpoints

```bash
# Test Auth API (login)
curl -X POST http://localhost:8000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# Capturar el token
TOKEN=$(curl -s -X POST http://localhost:8000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.token')

echo "Token: $TOKEN"

# Test Users API
curl http://localhost:8080/users
curl http://localhost:8080/users/admin

# Test TODOs API (sin token)
curl http://localhost:3000/todos
# Debería dar 401 Unauthorized

# Test TODOs API (con token)
curl -H "Authorization: Bearer $TOKEN" http://localhost:3000/todos

# Crear TODO
curl -X POST http://localhost:3000/todos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Test TODO"}'

# Eliminar TODO
curl -X DELETE http://localhost:3000/todos/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Health Checks

```bash
# Version endpoint (Auth API)
curl http://localhost:8000/version

# Comprobar que pod está ready
kubectl get pod <pod-name> -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}'
# Debería retornar: True

# Liveness probe (si está configurado)
kubectl describe pod <pod-name> | grep -A 5 "Liveness"

# Readiness probe (si está configurado)
kubectl describe pod <pod-name> | grep -A 5 "Readiness"
```

### Tests de Carga

```bash
# Con Apache Bench (ab)
ab -n 1000 -c 10 http://localhost:8080/users

# Con hey (Google)
hey -n 1000 -c 10 http://localhost:8080/users

# Con curl loop
for i in {1..100}; do
  curl -s http://localhost:8080/users > /dev/null
  echo "Request $i completed"
done
```

### Validación de ConfigMaps

```bash
# Verificar que ConfigMap está en el pod
kubectl exec <pod-name> -- env | grep JWT_SECRET

# Verificar variables en un pod
kubectl exec <pod-name> -- printenv | sort

# Ejecutar un pod de prueba
kubectl run debug-pod --image=busybox -it --rm -- /bin/sh
# Dentro: nslookup redis (debe resolver)
# Dentro: curl http://auth-api:8000/version (debe responder)
```

---

## LIMPIEZA Y MANTENIMIENTO

### Limpiar Kubernetes

```bash
# Eliminar todos los recursos
kubectl delete -f k8s-manifests/

# Eliminar namespace específico
kubectl delete namespace default

# Limpiar recursos orfandos
kubectl delete pods --field-selector status.phase=Failed
kubectl delete pods --field-selector status.phase=Succeeded

# Ver consumo de almacenamiento
du -sh k8s-manifests/
```

### Limpiar Docker

```bash
# Eliminar imágenes no usadas
docker image prune

# Eliminar contenedores parados
docker container prune

# Eliminar volúmenes no usados
docker volume prune

# Limpieza completa
docker system prune -a
```

### Monitoreo de Recursos

```bash
# Espacio en disco
df -h

# Uso de memoria
free -h

# Procesos
ps aux | grep docker

# Conexiones de red
netstat -an | grep ESTABLISHED | wc -l
```

---

## TROUBLESHOOTING RÁPIDO

### Pod en CrashLoopBackOff

```bash
# Ver logs
kubectl logs <pod-name>

# Ver descripción
kubectl describe pod/<pod-name>

# Ver eventos
kubectl get events --sort-by='.lastTimestamp'

# Verificar variable USERS_API_ADDRESS
kubectl exec <pod-name> -- env | grep USERS_API_ADDRESS
# Debe ser: http://users-api:8080 (NO tcp://...)
```

### Pod en ImagePullBackOff

```bash
# Verificar imagen está disponible
docker images | grep felipevelasco7

# Cargar en Minikube
minikube image load felipevelasco7/auth-api:latest

# O hacer push a Docker Hub
docker push felipevelasco7/auth-api:latest

# Reiniciar pod
kubectl rollout restart deployment/auth-api
```

### Pod Pending

```bash
# Ver detalles
kubectl describe pod/<pod-name>

# Verificar que hay recursos disponibles
kubectl describe nodes

# Ver limpieza de espacio
kubectl get pvc
```

### Conexión Rechazada Entre Servicios

```bash
# Verificar NetworkPolicy
kubectl get networkpolicies

# Ver reglas
kubectl describe networkpolicy/<policy-name>

# Eliminar temporalmente para debug
kubectl delete networkpolicies --all

# Verificar DNS
kubectl run -it --image=busybox --restart=Never -- nslookup redis

# Test de conectividad
kubectl run -it --image=curlimages/curl --restart=Never -- curl http://auth-api:8000/version
```

---

**Fin de Guía Práctica**

Estos comandos cubren los escenarios más comunes. Para casos específicos, consulta la documentación oficial de Kubernetes y Docker.

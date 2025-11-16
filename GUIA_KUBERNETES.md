# Guía de Implementación Kubernetes con HPA

## Descripción

Este documento describe cómo desplegar la arquitectura de microservicios en Kubernetes con escalado automático mediante Horizontal Pod Autoscaler (HPA).

## Componentes Implementados

### 1. Deployments
Cada microservicio tiene su propio deployment:
- **auth-api**: Autenticación y generación de JWT (2-10 replicas)
- **users-api**: Gestión de usuarios con Actuator (2-10 replicas)
- **todos-api**: Gestión de tareas (2-10 replicas)
- **log-processor**: Procesamiento de logs desde Redis (2-10 replicas)
- **frontend**: Interfaz Vue.js (2-10 replicas)

### 2. Services
Exponen los pods de manera interna o externa:
- **auth-api**: ClusterIP puerto 8000 (acceso interno)
- **users-api**: ClusterIP puerto 8083 (acceso interno)
- **todos-api**: ClusterIP puerto 8082 (acceso interno)
- **frontend**: NodePort 30080 (acceso externo)

### 3. HPA (Horizontal Pod Autoscaler)
Escala automáticamente los pods basándose en:
- **CPU Threshold**: 70% de utilización
- **Memory Threshold**: 80% de utilización
- **Min Replicas**: 2 pods mínimo
- **Max Replicas**: 10 pods máximo

### 4. ConfigMaps y Secrets
Gestiona variables de entorno de forma segura:
- **ConfigMap**: Datos públicos (puertos, URLs, nombres de servicios)
- **Secret**: Datos sensibles (JWT_SECRET, REDIS_PASSWORD)

## Requisitos Previos

1. Kubernetes cluster activo (Minikube, Docker Desktop, o cloud)
2. kubectl instalado y configurado
3. Metrics Server instalado (para HPA)

### Verificar Metrics Server

```bash
kubectl get deployment metrics-server -n kube-system
```

Si no está instalado:
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

## Estructura de Archivos

```
k8s-manifests/
├── deployments/
│   ├── auth-api.yaml
│   ├── users-api.yaml
│   ├── todos-api.yaml
│   ├── log-processor.yaml
│   └── frontend.yaml
├── services/
│   └── services.yaml
├── hpa/
│   └── hpa.yaml
├── configmaps/
│   └── configmap.yaml
├── secrets/
│   └── secret.yaml
└── deploy.sh
```

## Instalación

### Opción 1: Script Automático (Recomendado)

```bash
cd k8s-manifests
./deploy.sh
```

El script ejecuta los siguientes pasos automáticamente:
1. Crea ConfigMaps
2. Crea Secrets
3. Crea Services
4. Crea Deployments
5. Crea HPAs

### Opción 2: Pasos Manuales

```bash
# 1. Crear ConfigMaps
kubectl apply -f k8s-manifests/configmaps/configmap.yaml

# 2. Crear Secrets
kubectl apply -f k8s-manifests/secrets/secret.yaml

# 3. Crear Services
kubectl apply -f k8s-manifests/services/services.yaml

# 4. Crear Deployments
kubectl apply -f k8s-manifests/deployments/

# 5. Crear HPAs
kubectl apply -f k8s-manifests/hpa/hpa.yaml
```

## Verificación del Deployment

### Ver estado de los pods
```bash
kubectl get pods
kubectl get pods -o wide
```

### Ver services
```bash
kubectl get svc
```

### Ver HPAs
```bash
kubectl get hpa
```

### Ver detalles de un HPA
```bash
kubectl describe hpa users-api-hpa
```

### Monitorear HPAs en tiempo real
```bash
kubectl get hpa -w
```

### Ver uso de recursos
```bash
kubectl top pods
kubectl top nodes
```

## Verificar Logging

### Logs de un pod
```bash
kubectl logs <pod-name>
kubectl logs -f <pod-name>  # Follow mode
```

### Logs de un deployment
```bash
kubectl logs -l app=users-api --all-containers=true
```

## Acceso a Aplicaciones

### Frontend (NodePort)
```bash
kubectl port-forward svc/frontend 8080:8080
# Acceder a: http://localhost:8080
```

### Users API (ClusterIP)
```bash
kubectl port-forward svc/users-api 8083:8083
# Acceder a: http://localhost:8083/actuator/health
```

### Acceso directo en Minikube
```bash
minikube service frontend
```

## Testing de HPA

### 1. Generar carga para disparar escalado
```bash
kubectl run -it --rm load-generator --image=busybox /bin/sh

# Dentro del contenedor:
while sleep 0.01; do wget -q -O- http://users-api:8083/actuator/health; done
```

### 2. Monitorear escalado en tiempo real (otra terminal)
```bash
kubectl get hpa -w
```

### 3. Ver nuevos pods creados
```bash
kubectl get pods
```

### 4. Detener el generador de carga
```bash
# Ctrl+C en la terminal del load-generator
exit
```

## Escalado Manual

### Escalar manualmente
```bash
kubectl scale deployment users-api --replicas=5
```

### Editar deployment
```bash
kubectl edit deployment users-api
```

## Eliminación de Recursos

### Eliminar todo en orden inverso
```bash
# Eliminar HPAs
kubectl delete hpa --all

# Eliminar Deployments
kubectl delete deployments --all

# Eliminar Services
kubectl delete svc --all

# Eliminar ConfigMaps
kubectl delete configmap app-config

# Eliminar Secrets
kubectl delete secret app-secrets
```

### O eliminar todo de una vez
```bash
kubectl delete all -l app=auth-api
kubectl delete all -l app=users-api
kubectl delete all -l app=todos-api
kubectl delete all -l app=log-processor
kubectl delete all -l app=frontend
kubectl delete configmap app-config
kubectl delete secret app-secrets
```

## Configuración de HPA Explicada

### Parámetros Clave

```yaml
minReplicas: 2              # Mínimo 2 pods siempre activos
maxReplicas: 10             # Máximo 10 pods permitidos
averageUtilization: 70      # Escala cuando CPU > 70%
```

### Comportamiento de Escalado

**Scale Up (Aumentar replicas):**
- Se activa cuando CPU > 70% O Memory > 80%
- Duplica el número de pods cada 30 segundos (máximo)
- Sin período de espera (scaleUp.stabilizationWindowSeconds: 0)

**Scale Down (Reducir replicas):**
- Se activa cuando CPU < 70% Y Memory < 80%
- Reduce 50% del número de pods
- Espera 5 minutos antes de otro scale down (300 segundos)

## Monitoreo con Prometheus (Opcional)

### Conectar Prometheus a Kubernetes
1. Users API expone métricas en `/actuator/prometheus`
2. Agregar target a prometheus.yml:

```yaml
- job_name: 'users-api-k8s'
  static_configs:
    - targets: ['users-api:8083']
  metrics_path: '/actuator/prometheus'
```

### Queries útiles
```promql
# Uso de CPU
rate(process_cpu_seconds_total[5m]) * 100

# Uso de memoria
process_resident_memory_bytes / 1024 / 1024

# Requests por segundo
rate(http_requests_total[1m])
```

## Troubleshooting

### HPAs no escalan
```bash
# Verificar Metrics Server
kubectl get deployment metrics-server -n kube-system

# Verificar métricas
kubectl top pods

# Ver eventos
kubectl describe hpa users-api-hpa
```

### Pods no inician
```bash
# Ver logs
kubectl logs <pod-name>

# Ver eventos
kubectl describe pod <pod-name>
```

### ConfigMap o Secret no se aplica
```bash
# Eliminar y recrear
kubectl delete configmap app-config
kubectl apply -f k8s-manifests/configmaps/configmap.yaml
```

## Limpieza Rápida

```bash
# Eliminar y recrear todo
./deploy.sh  # Crea nuevamente
```

## Referencia Rápida

| Comando | Descripción |
|---------|-------------|
| `kubectl apply -f <file>` | Aplicar manifesto |
| `kubectl delete -f <file>` | Eliminar recurso |
| `kubectl get pods` | Listar pods |
| `kubectl get hpa` | Listar HPAs |
| `kubectl logs <pod>` | Ver logs |
| `kubectl describe pod <pod>` | Detalles del pod |
| `kubectl exec -it <pod> /bin/sh` | Acceder a pod |
| `kubectl port-forward <pod> 8080:8080` | Tunnel local |
| `kubectl top pods` | Uso de recursos |
| `kubectl scale deployment <name> --replicas=N` | Escalar manual |

## Notas Importantes

1. Los manifiestos asumen que las imágenes Docker ya están construidas
2. Si usas Minikube, asegúrate de estar en el mismo contexto de Docker
3. Las replicas iniciales son 2, puedes modificar en los deployments
4. Los CPU/Memory requests y limits están optimizados para desarrollo
5. Para producción, ajusta los valores según tu infraestructura

## Próximos Pasos

1. Construir imágenes Docker
2. Cargar imágenes en el registro de Kubernetes
3. Ejecutar `./deploy.sh`
4. Monitorear con `kubectl get hpa -w`
5. Generar carga para ver escalado automático

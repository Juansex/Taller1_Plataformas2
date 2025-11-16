# GUÍA COMPLETA PARA WINDOWS

## INICIO RÁPIDO - OPCIÓN FÁCIL (Docker Compose)

### Paso 1: Abre PowerShell en la carpeta del proyecto
```powershell
# Te paras en: C:\Users\user\OneDrive\...\Taller1_Plataformas2
# Botón derecho en la carpeta → Open in Terminal / Open PowerShell here
```

### Paso 2: Prepara variables de entorno
```powershell
copy .env.example .env
```

### Paso 3: Levanta todos los servicios
```powershell
docker-compose up -d
```

### Paso 4: Espera ~1 minuto y verifica
```powershell
docker-compose ps
```

### Paso 5: Accede a las aplicaciones
- **Frontend:** http://localhost:8080 (usuario: admin/admin)
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3000 (admin/admin)
- **Zipkin:** http://localhost:9411

### Paso 6: Cuando termines
```powershell
docker-compose down -v
```

---

## OPCIÓN AVANZADA - Kubernetes en Docker Desktop

### Requisito: Tener Docker Desktop instalado
1. Descargar desde: https://www.docker.com/products/docker-desktop
2. Instalar normalmente
3. **IMPORTANTE:** Durante instalación, marcar "Install Kubernetes"

### Paso 1: Habilitar Kubernetes en Docker Desktop
```
1. Abre Docker Desktop
2. Haz clic en la rueda de engranaje (Settings)
3. Ve a: Resources → Kubernetes
4. Marca: Enable Kubernetes
5. Espera ~2 minutos a que se inicie
```

### Paso 2: Verificar que Kubernetes está activo
Abre PowerShell en la carpeta del proyecto y ejecuta:
```powershell
kubectl cluster-info
```

**Resultado esperado:** Mostrará URL del control plane

### Paso 3: Construir imágenes Docker
```powershell
# Copiar variables primero
copy .env.example .env

# Luego construir cada imagen (esto toma tiempo)
docker build -t auth-api:latest ./auth-api
docker build -t users-api:latest ./users-api
docker build -t todos-api:latest ./todos-api
docker build -t log-processor:latest ./log-message-processor
docker build -t frontend:latest ./frontend
```

### Paso 4: Desplegar en Kubernetes
```powershell
# Entrar a la carpeta
cd k8s-manifests

# Aplicar ConfigMap
kubectl apply -f configmaps/configmap.yaml

# Aplicar Secrets
kubectl apply -f secrets/secret.yaml

# Aplicar Services
kubectl apply -f services/services.yaml

# Aplicar Deployments (todos de una vez)
kubectl apply -f deployments/

# Aplicar HPAs
kubectl apply -f hpa/hpa.yaml
```

### Paso 5: Verificar que se desplegó correctamente
```powershell
# Ver todos los pods
kubectl get pods

# Resultado esperado: 10 pods en Running (2 de cada servicio)
```

### Paso 6: Monitorear el escalado automático
Abre otra terminal PowerShell en la carpeta del proyecto:
```powershell
kubectl get hpa -w

# Verás cómo los HPAs monitorean CPU y Memory
# CPU target: 70%
# Memory target: 80%
```

### Paso 7: Acceder a los servicios
Abre otra terminal PowerShell:
```powershell
kubectl port-forward svc/frontend 8080:8080
```

Luego abre: http://localhost:8080

### Paso 8: Ver logs de un pod
```powershell
# Primero, obtén el nombre exacto del pod
kubectl get pods

# Luego ve sus logs
kubectl logs auth-api-abc123 -f
# (reemplaza "auth-api-abc123" por el nombre real)
```

### Paso 9: Limpiar Kubernetes cuando termines
```powershell
# Elimina todos los pods, servicios, deployments, HPAs
kubectl delete all --all
```

---

## SOLUCIÓN DE PROBLEMAS EN WINDOWS

### ❌ "docker-compose: command not found"
**Solución:** Docker no está instalado o no en el PATH
```powershell
# Verifica:
docker --version

# Si no funciona, reinstala Docker Desktop desde:
# https://www.docker.com/products/docker-desktop
```

### ❌ "chmod: command not found"
**Problema:** Intentaste usar comando Linux en Windows
**Solución:** Los scripts de Windows no necesitan `chmod`
```powershell
# NO hagas esto en Windows:
chmod +x deploy.sh

# En Windows, solo usa:
kubectl apply -f deployments/
```

### ❌ "'.' is not recognized"
**Problema:** Intentaste ejecutar el script con `./`
**Solución:** En Windows PowerShell, debes usar `powershell -File` o aplicar directamente
```powershell
# NO hagas esto:
./deploy.sh

# Haz esto en su lugar:
kubectl apply -f configmaps/configmap.yaml
kubectl apply -f secrets/secret.yaml
kubectl apply -f services/services.yaml
kubectl apply -f deployments/
kubectl apply -f hpa/hpa.yaml
```

### ❌ "minikube: command not found"
**Problema:** Minikube no funciona en Windows
**Solución:** Usa Docker Desktop Kubernetes en su lugar
```powershell
# En Windows, Docker Desktop incluye Kubernetes integrado
# NO necesitas minikube

# Verifica que Kubernetes está activo:
kubectl cluster-info
```

### ❌ "Port already in use"
**Problema:** El puerto 8080 (o similar) ya está ocupado
```powershell
# Opción 1: Detener Docker Compose
docker-compose down -v

# Opción 2: Cambiar puerto en .env
# Edita el archivo .env y cambia:
# AUTH_API_PORT=8001  (en lugar de 8000)
# SERVER_PORT=8084    (en lugar de 8083)
```

### ❌ Kubernetes muestra "Pending" en kubectl get pods
**Problema:** Las imágenes Docker no existen localmente
**Solución:** Primero construye las imágenes
```powershell
# Verifica que están construidas:
docker images | findstr "auth-api\|users-api\|todos-api\|log-processor\|frontend"

# Si no aparecen, construye:
docker build -t auth-api:latest ./auth-api
docker build -t users-api:latest ./users-api
docker build -t todos-api:latest ./todos-api
docker build -t log-processor:latest ./log-message-processor
docker build -t frontend:latest ./frontend
```

### ❌ "HPA always shows 0% CPU"
**Problema:** metrics-server no está instalado
```powershell
# Verifica:
kubectl get deployment metrics-server -n kube-system

# Si no existe, instala:
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Espera 30 segundos y verifica:
kubectl get hpa
```

---

## COMANDOS ÚTILES EN WINDOWS

```powershell
# Ver todos los contenedores Docker
docker ps -a

# Ver todas las imágenes
docker images

# Ver logs en vivo
docker-compose logs -f

# Ver específicamente los logs de un servicio
docker-compose logs -f users-api

# Detener un contenedor específico
docker-compose stop auth-api

# Reiniciar un contenedor
docker-compose restart auth-api

# Eliminar volúmenes (CUIDADO: pierde datos)
docker-compose down -v

# Ver pods de Kubernetes
kubectl get pods

# Ver pods con más detalles
kubectl get pods -o wide

# Ver servicios
kubectl get svc

# Ver HPAs
kubectl get hpa

# Describir un pod (ver detalles de error)
kubectl describe pod <nombre-pod>

# Ver logs de un pod
kubectl logs <nombre-pod>

# Ver logs en vivo
kubectl logs -f <nombre-pod>

# Obtener acceso a un servicio
kubectl port-forward svc/frontend 8080:8080
```

---

## PRÓXIMOS PASOS DESPUÉS DE LEVANTAR

1. **Accede al Frontend:** http://localhost:8080
   - Usuario: `admin`
   - Contraseña: `admin`

2. **Prueba las funcionalidades:**
   - Crea una tarea
   - Ve a Prometheus: http://localhost:9090
   - Consulta métricas

3. **Monitorea en Grafana:** http://localhost:3000
   - Usuario: `admin`
   - Contraseña: `admin`

4. **Ve las trazas en Zipkin:** http://localhost:9411
   - Busca servicios como "auth-api" o "users-api"

---

## DOCUMENTACIÓN COMPLETA

Para más detalles, ve a:
- **README.md** - Visión general del proyecto
- **GUIA_RAPIDA.md** - Comandos para todos los SO
- **GitHub:** https://github.com/Juansex/Taller1_Plataformas2

---

## RESUMEN SUPER RÁPIDO

```powershell
# 1. Preparar
copy .env.example .env

# 2. Levantar (OPCIÓN 1: Docker Compose)
docker-compose up -d

# O (OPCIÓN 2: Kubernetes)
docker build -t auth-api:latest ./auth-api
docker build -t users-api:latest ./users-api
docker build -t todos-api:latest ./todos-api
docker build -t log-processor:latest ./log-message-processor
docker build -t frontend:latest ./frontend
cd k8s-manifests
kubectl apply -f configmaps/
kubectl apply -f secrets/
kubectl apply -f services/
kubectl apply -f deployments/
kubectl apply -f hpa/

# 3. Ver estado
docker-compose ps              # Docker Compose
kubectl get pods               # Kubernetes

# 4. Acceder
# http://localhost:8080
```

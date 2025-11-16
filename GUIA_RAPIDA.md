# GUÍA RÁPIDA - PASO A PASO

## ¿QUÉ SE HIZO?

### 1. Kubernetes con HPA
- Creé 5 Deployments (uno por cada servicio)
- Creé 5 HPAs que escalan automáticamente cuando CPU > 70% o Memory > 80%
- Rango de replicas: 2 mínimo, 10 máximo por servicio

### 2. Limpieza de Documentación
- Eliminé 17 archivos .md redundantes
- Consolidé todo en un único README.md limpio (159 líneas)

### 3. GitHub Actions CI/CD
- Build automático de imágenes Docker
- Validación de secrets hardcodeados
- Testing de docker-compose

### 4. Gestión de Secretos
- Variables en .env (no committed)
- Template en .env.example (committed)

---

## COMANDOS PARA EJECUTAR

### OPCIÓN 1: DOCKER COMPOSE (Desarrollo Local) - Windows / Mac / Linux

```powershell
# 1. Preparar variables de entorno
copy .env.example .env
# O en Mac/Linux: cp .env.example .env

# 2. Levantar todos los servicios
docker-compose up -d

# 3. Ver estado de servicios
docker-compose ps

# 4. Ver logs en vivo
docker-compose logs -f

# 5. Detener y limpiar
docker-compose down -v
```

**Explicación:**
- `copy .env.example .env` (Windows) / `cp .env.example .env` (Mac/Linux) → Copia template
- `docker-compose up -d` → Levanta 9 servicios en background
- `docker-compose ps` → Muestra estado de cada contenedor
- `docker-compose logs -f` → Ve logs en tiempo real
- `docker-compose down -v` → Detiene y borra volúmenes

**URLs después de levantar:**
- Frontend: http://localhost:8080 (usuario: admin/admin)
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- Zipkin: http://localhost:9411

---

### OPCIÓN 2: KUBERNETES (Producción) - Windows

> ⚠️ **REQUISITOS en Windows:**
> - Docker Desktop instalado con Kubernetes habilitado (Settings → Kubernetes → Enable)
> - kubectl instalado (viene con Docker Desktop)
> - NO funcionará `minikube` en CMD/PowerShell, usa Docker Desktop

```powershell
# 1. HABILITAR Kubernetes en Docker Desktop (interfaz gráfica)
# Settings → Resources → Kubernetes → Enable Kubernetes
# Esperar a que esté listo (~2 minutos)

# 2. Verificar que Kubernetes está listo
kubectl cluster-info
# Debe mostrar: Kubernetes control plane is running at https://...

# 3. Construir imágenes Docker locales (en la carpeta raíz del proyecto)
docker build -t auth-api:latest ./auth-api
docker build -t users-api:latest ./users-api
docker build -t todos-api:latest ./todos-api
docker build -t log-processor:latest ./log-message-processor
docker build -t frontend:latest ./frontend

# 4. Desplegar todo (ejecuta script)
cd k8s-manifests
# En Windows PowerShell:
powershell -ExecutionPolicy Bypass -File deploy.ps1
# O manualmente:
kubectl apply -f configmaps/configmap.yaml
kubectl apply -f secrets/secret.yaml
kubectl apply -f services/services.yaml
kubectl apply -f deployments/auth-api.yaml
kubectl apply -f deployments/users-api.yaml
kubectl apply -f deployments/todos-api.yaml
kubectl apply -f deployments/log-processor.yaml
kubectl apply -f deployments/frontend.yaml
kubectl apply -f hpa/hpa.yaml

# 5. Monitorear pods (ve cómo se crean)
kubectl get pods -w

# 6. Monitorear HPAs (ve cómo se escala automáticamente)
kubectl get hpa -w

# 7. Ver logs de un pod
kubectl logs <nombre-del-pod>
# Ejemplo:
# kubectl logs auth-api-xyz123

# 8. Acceder a servicios (en otra terminal)
kubectl port-forward svc/frontend 8080:8080
# Luego: http://localhost:8080

# 9. Limpiar todo
kubectl delete all --all
```

**Explicación:**
- Docker Desktop Kubernetes → Ya incluye kubectl
- `docker build` → Construye imágenes con tag específico
- `kubectl apply` → Aplica manifiestos YAML
- `kubectl get pods -w` → Observa pods en tiempo real (se actualiza automáticamente)
- `kubectl get hpa -w` → Observa escalado automático
- `kubectl port-forward` → Accede a servicio desde localhost
- `kubectl delete all --all` → Limpia todo (¡cuidado!)

---

## ARCHIVOS IMPORTANTES

### Kubernetes (k8s-manifests/)
```
k8s-manifests/
├── deployments/      # 5 YAML (auth-api, users-api, todos-api, log-processor, frontend)
├── services/         # 4 servicios (3 ClusterIP + 1 NodePort)
├── hpa/              # 5 autoscalers (2-10 replicas, CPU 70%, Memory 80%)
├── configmaps/       # Variables públicas
├── secrets/          # Datos sensibles (JWT, Redis password)
├── deploy.sh         # Script para Linux/Mac
└── deploy.ps1        # Script para Windows (opcional)
```

### Configuración
```
.env.example         # Template de variables (committed)
.env                 # Tus variables (NO committed, en .gitignore)
docker-compose.yml   # 9 servicios orquestados
```

### GitHub Actions
```
.github/workflows/ci.yml
- Build de imágenes
- Validación de secrets
- Testing automático
```

---

## FLUJO DE TRABAJO RECOMENDADO

### Para Desarrollo Local (Windows/Mac/Linux):
```powershell
# Windows
copy .env.example .env

# O Mac/Linux
cp .env.example .env

# Todos:
docker-compose up -d
# Desarrolla y prueba
docker-compose logs -f  # En otra terminal para ver errores
docker-compose down -v  # Cuando termines
```

### Para Producción - Docker Desktop Kubernetes (Windows):
```powershell
# 1. Habilitar en Docker Desktop (GUI)
# Settings → Resources → Kubernetes → Enable Kubernetes

# 2. Esperar a que esté listo
kubectl cluster-info

# 3. Construir imágenes
docker build -t auth-api:latest ./auth-api
docker build -t users-api:latest ./users-api
docker build -t todos-api:latest ./todos-api
docker build -t log-processor:latest ./log-message-processor
docker build -t frontend:latest ./frontend

# 4. Desplegar
cd k8s-manifests
kubectl apply -f configmaps/configmap.yaml
kubectl apply -f secrets/secret.yaml
kubectl apply -f services/services.yaml
kubectl apply -f deployments/
kubectl apply -f hpa/hpa.yaml

# 5. Monitorear
kubectl get hpa -w
```

### Integración Continua (Git):
```powershell
git add .
git commit -m "Cambios"
git push origin main
# GitHub Actions automáticamente:
# 1. Construye imágenes
# 2. Valida secrets
# 3. Prueba docker-compose
# 4. Reporta resultados
```

---

## VERIFICACIÓN RÁPIDA

### Docker Compose (todos los SO)
```powershell
docker-compose ps  # Todos deben estar "Up"
# Espera ~1 minuto a que levanten los servicios

# Verificar que responden
curl http://localhost:8080   # Frontend
curl http://localhost:9090   # Prometheus
curl http://localhost:8083/actuator/health  # Users API
```

### Kubernetes (Windows con Docker Desktop)
```powershell
kubectl get pods      # Deben estar "Running"
kubectl get hpa       # HPAs creados
kubectl get svc       # Services creados

# Verificar que responden
kubectl port-forward svc/frontend 8080:8080
# Abre: http://localhost:8080
```

---

## PROBLEMAS COMUNES Y SOLUCIONES

### ❌ "Port already in use"
**Problema:** Puerto 8080 (o similar) ya está en uso
```powershell
# Opción 1: Detener y limpiar Docker Compose
docker-compose down -v
docker-compose up -d

# Opción 2: Cambiar puerto en .env
# Edita .env y cambia: AUTH_API_PORT=8001 (o el que necesites)
```

### ❌ "Kubernetes command not found"
**Problema:** kubectl no está instalado
```powershell
# Solución: Docker Desktop debe incluirlo automáticamente
# Verifica:
docker --version    # Debe mostrar versión
kubectl version     # Debe mostrar versión

# Si no:
# 1. Reinstala Docker Desktop
# 2. Durante instalación, marca "Install Kubernetes"
```

### ❌ "Kubernetes is not running"
**Problema:** Kubernetes no está habilitado en Docker Desktop
```powershell
# Solución: Habilitarlo manualmente
# 1. Abre Docker Desktop
# 2. Settings (rueda de engranaje) → Resources → Kubernetes
# 3. Marca "Enable Kubernetes"
# 4. Espera ~2 minutos a que se inicie
# 5. Verifica: kubectl cluster-info
```

### ❌ "Failed to build Docker image"
**Problema:** Dockerfile tiene errores
```powershell
# Solución: Intenta build manualmente para ver el error
docker build -t auth-api:latest ./auth-api
# Lee el error completo que muestra

# Si dice "file not found":
# Verifica que estés en la carpeta correcta (raíz del proyecto)
# Los Dockerfiles están en: auth-api/, users-api/, etc.
```

### ❌ "Pods stuck in 'Pending' state"
**Problema:** Kubernetes intenta descargar imágenes, pero no existen
```powershell
# Solución: Asegúrate de construir localmente primero
docker build -t auth-api:latest ./auth-api
docker build -t users-api:latest ./users-api
docker build -t todos-api:latest ./todos-api
docker build -t log-processor:latest ./log-message-processor
docker build -t frontend:latest ./frontend

# Luego:
kubectl apply -f k8s-manifests/deployments/
```

### ❌ "HPA no escala (siempre muestra 0% utilización)"
**Problema:** metrics-server no está instalado
```powershell
# Verifica:
kubectl get deployment metrics-server -n kube-system

# Si no existe, instalar:
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Esperar ~30 segundos y luego:
kubectl get hpa -w
```

### ❌ Ver errores detallados de un pod
```powershell
# Ver logs:
kubectl logs <nombre-del-pod>

# Ejemplo:
kubectl logs auth-api-abc123

# Ver descripción detallada:
kubectl describe pod <nombre-del-pod>

# Ejemplo:
kubectl describe pod auth-api-abc123
```

### ❌ Docker Compose: un servicio no levanta
```powershell
# Ver logs específicos:
docker-compose logs users-api

# Reintentar:
docker-compose restart users-api

# Reconstruir:
docker-compose down -v
docker-compose build --no-cache users-api
docker-compose up -d
```

---

## RESUMEN - QUICK START

| Acción | Windows | Mac/Linux |
|--------|---------|-----------|
| **1. Preparar** | `copy .env.example .env` | `cp .env.example .env` |
| **2. Levantar servicios** | `docker-compose up -d` | `docker-compose up -d` |
| **3. Ver estado** | `docker-compose ps` | `docker-compose ps` |
| **4. Ver logs** | `docker-compose logs -f` | `docker-compose logs -f` |
| **5. Detener** | `docker-compose down -v` | `docker-compose down -v` |

---

## KUBERNETES SOLO WINDOWS (Docker Desktop)

| Acción | Comando |
|--------|---------|
| **Verificar K8s activo** | `kubectl cluster-info` |
| **Construir imágenes** | `docker build -t auth-api:latest ./auth-api` |
| **Desplegar** | `kubectl apply -f k8s-manifests/deployments/` |
| **Ver pods** | `kubectl get pods -w` |
| **Ver HPAs** | `kubectl get hpa -w` |
| **Ver logs** | `kubectl logs <pod-name> -f` |
| **Limpiar** | `kubectl delete all --all` |

---

## PUNTOS CLAVE

✅ **Docker Compose funciona en Windows/Mac/Linux**
✅ **Kubernetes requiere Docker Desktop (Windows/Mac) o Minikube (Linux)**
✅ **Los comandos `chmod` y `./script.sh` NO funcionan en Windows**
✅ **En Windows, usa PowerShell o CMD, no Bash**
✅ **GitHub Actions se ejecuta automáticamente en cada push**

---

## REPOSITORIO

📦 **GitHub:** https://github.com/Juansex/Taller1_Plataformas2
🔐 **Rama:** main
📝 **Documentación:** README.md (159 líneas)


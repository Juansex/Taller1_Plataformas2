# Referencia Rápida - Comandos Esenciales

## Iniciar TODO (La forma más rápida)

```bash
# Opción 1: Automática (Recomendada)
./demo.sh

# Opción 2: Manual
docker-compose build
docker-compose up
```

---

## 🌐 URLs de Acceso

| Aplicación | URL | Usuario | Contraseña |
|-----------|-----|---------|------------|
| Frontend | http://localhost:8080 | admin | admin |
| Prometheus | http://localhost:9090 | - | - |
| Grafana | http://localhost:3000 | admin | admin |

---

## 📊 APIs Internas

```bash
# Auth API
curl http://localhost:8000/health

# Users API
curl http://localhost:8083/users

# TODOs API (requiere auth)
TOKEN=$(curl -s -X POST http://localhost:8000/auth \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.token')

curl -H "Authorization: Bearer $TOKEN" http://localhost:8082/todos
```

---

## 🔍 Ver Logs

```bash
# Todos los servicios
docker-compose logs -f

# Log Processor (operaciones en tiempo real)
docker-compose logs -f log-processor

# APIs específicos
docker-compose logs -f auth-api users-api todos-api

# Frontend
docker-compose logs -f frontend
```

---

## Detener & Limpiar

```bash
# Pausar servicios (mantiene datos)
docker-compose stop

# Detener y eliminar contenedores
docker-compose down

# Limpiar todo incluyendo volúmenes
docker-compose down -v

# Limpiar sistema Docker completamente
docker system prune -a --volumes
```

---

## 🔧 Troubleshooting Rápido

```bash
# Ver estado de contenedores
docker-compose ps

# Rebuild sin cache
docker-compose build --no-cache

# Reiniciar servicio específico
docker-compose restart auth-api

# Ver errores en detalle
docker-compose logs auth-api

# Entrar a un contenedor
docker-compose exec auth-api sh
```

---

## 📝 Documentación Por Tema

| Necesito... | Lee... |
|----------|--------|
| Empezar | RESUMEN_INTEGRACION.md |
| Entender Docker | GUIA_DOCKER_COMPOSE.md |
| Ver ejemplos | GUIA_PRACTICA_COMANDOS.md |
| Kubernetes | INTEGRACION_KUBERNETES_PROMETHEUS_GRAFANA.md |
| Diagramas | ARQUITECTURA_DIAGRAMAS.md |
| Referencia rápida | Este archivo |

---

## 🎬 Flujo Video (5 minutos)

```bash
# Terminal 1: Ejecutar
./demo.sh

# Terminal 2: Ver logs
docker-compose logs -f log-processor

# Navegador:
# 1. http://localhost:8080 → Login (admin/admin) → Crear TODOs
# 2. http://localhost:9090 → Ejecutar: rate(http_requests_total[1m])
# 3. http://localhost:3000 → Crear dashboard simple
```

---

## ✅ Checklist Pre-Video

```bash
# 1. Verificar Docker
docker --version
docker-compose --version

# 2. Puertos disponibles
lsof -i :8080
lsof -i :9090
lsof -i :3000

# 3. Ejecutar demo
./demo.sh

# 4. Abrir en navegador
# http://localhost:8080

# 5. Crear algunos TODOs
# Ver logs en otra terminal: docker-compose logs -f log-processor
```

---

## 🐳 Estructura Docker

```
docker-compose.yml
├── redis (6379)
├── auth-api (8000)
├── users-api (8083)
├── todos-api (8082)
├── log-processor
├── frontend (8080)
├── prometheus (9090)
└── grafana (3000)
```

---

## 📊 Métricas Importantes

En Prometheus (http://localhost:9090):

```
# Solicitudes HTTP por segundo
rate(http_requests_total[1m])

# Errores HTTP
rate(http_requests_total{status=~"5.."}[1m])

# Latencia (percentil 95)
histogram_quantile(0.95, http_request_duration_seconds_bucket)

# Estado de servicios
up
```

---

## Variables de Entorno

```bash
# Auth API
REDIS_HOST=redis
REDIS_PORT=6379

# Users API
SPRING_APPLICATION_NAME=users-api
SERVER_PORT=8083

# TODOs API
PORT=8082
NODE_ENV=production

# Log Processor
REDIS_HOST=redis
REDIS_PORT=6379
```

---

¡Listo! Usa esta referencia rápida durante la presentación.


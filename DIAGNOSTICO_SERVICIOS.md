# Diagnóstico y Solución: Servicios No Funcionando

## Problema Actual

Los servicios siguen sin funcionar después de hacer pull y rebuild. Los errores específicos son:

1. **auth-api**: 404 en `/metrics`
2. **redis**: Prometheus aún intenta scrapear `redis:6379` en lugar de `redis-exporter:9121`
3. **todos-api**: "no such host" - el servicio no existe en la red
4. **users-api**: "connection refused" - el servicio existe pero no responde

## Diagnóstico Paso a Paso

### Paso 1: Verificar que tienes los últimos cambios

```bash
cd /ruta/a/Taller1_Plataformas2

# Ver el branch actual
git branch

# Debe mostrar: * copilot/fix-prometheus-and-redis-setup

# Ver los últimos commits
git log --oneline -5

# Debes ver estos commits:
# 9c50f8b Fix users-api Prometheus metrics and add troubleshooting guide
# 3339ff3 Add comprehensive testing and video guide
# d5ded8a Add frontend service to docker-compose.yml
# a7b6292 Add redis-exporter, fix Redis authentication, add health and metrics endpoints
```

### Paso 2: Detener y limpiar completamente

```bash
# Detener todos los contenedores
docker compose down

# Verificar que no queden contenedores corriendo
docker compose ps
# Debe mostrar: no containers

# OPCIONAL pero recomendado: Limpiar volúmenes para empezar fresco
docker compose down -v

# OPCIONAL: Eliminar imágenes antiguas para forzar rebuild completo
docker rmi $(docker images | grep taller1 | awk '{print $3}')
```

### Paso 3: Verificar archivos clave

Antes de rebuild, verifica que los archivos tienen los cambios correctos:

```bash
# 1. Verificar prometheus.yml - debe usar redis-exporter
grep "redis-exporter:9121" config/prometheus.yml
# Debe devolver: - targets: ['redis-exporter:9121']

# 2. Verificar que auth-api tiene el endpoint de métricas
grep "GET.*metrics" auth-api/main.go
# Debe devolver algo como: e.GET("/metrics", echo.WrapHandler(promhttp.Handler()))

# 3. Verificar que todos-api tiene el endpoint de métricas
grep "'/metrics'" todos-api/server.js
# Debe devolver: app.get('/metrics', async (req, res) => {

# 4. Verificar que users-api tiene PrometheusConfiguration
ls users-api/src/main/java/com/elgris/usersapi/configuration/PrometheusConfiguration.java
# Debe existir
```

**SI ALGÚN ARCHIVO NO TIENE LOS CAMBIOS CORRECTOS:**
```bash
# Hacer pull de nuevo
git pull origin copilot/fix-prometheus-and-redis-setup

# Y volver al Paso 2
```

### Paso 4: Rebuild completo sin caché

Este es el paso más importante. Debes reconstruir SIN usar caché:

```bash
# Rebuild TODAS las imágenes sin caché
docker compose build --no-cache --progress=plain 2>&1 | tee build.log

# Esto tomará 10-20 minutos. Es normal.
# El archivo build.log guardará toda la salida para debugging
```

**IMPORTANTE**: Espera a que el build termine completamente. Si hay errores:

```bash
# Ver errores específicos
grep -i "error" build.log | head -20

# Ver qué servicio falló
grep -i "failed" build.log
```

**Errores comunes durante el build:**

1. **users-api falla descargando dependencias Maven**:
   ```bash
   # Solución: Reintentar, puede ser problema de red
   docker compose build --no-cache users-api
   ```

2. **frontend falla con node-sass**:
   ```bash
   # Verificar que el Dockerfile usa node:10-alpine
   head -1 frontend/Dockerfile
   # Debe ser: FROM node:10-alpine AS builder
   ```

3. **todos-api falla con dependencias**:
   ```bash
   # Ver logs específicos
   docker compose build todos-api 2>&1 | tee todos-build.log
   ```

### Paso 5: Iniciar servicios y esperar a que estén healthy

```bash
# Iniciar en modo detached
docker compose up -d

# Ver el estado inmediatamente
docker compose ps

# Esperar 30-60 segundos para que los servicios inicien
# y luego verificar de nuevo
sleep 60
docker compose ps
```

**Salida esperada de `docker compose ps`:**

```
NAME              IMAGE                           STATUS              PORTS
auth-api          ...                             Up (healthy)        0.0.0.0:8000->8000/tcp
frontend          ...                             Up                  0.0.0.0:8080->8080/tcp
grafana           grafana/grafana:latest          Up                  0.0.0.0:3000->3000/tcp
log-processor     ...                             Up
prometheus        prom/prometheus:latest          Up                  0.0.0.0:9090->9090/tcp
redis             redis:7.0-alpine                Up (healthy)
redis-exporter    oliver006/redis_exporter:...    Up                  0.0.0.0:9121->9121/tcp
todos-api         ...                             Up (healthy)        0.0.0.0:8082->8082/tcp
users-api         ...                             Up (healthy)        0.0.0.0:8083->8083/tcp
```

**SI algún servicio no está "Up" o "(healthy)":**

```bash
# Ver logs del servicio problemático
docker compose logs [nombre-servicio]

# Ejemplos:
docker compose logs auth-api
docker compose logs users-api
docker compose logs todos-api
```

### Paso 6: Verificar endpoints individualmente

Una vez que todos los servicios estén UP, verifica cada endpoint:

```bash
# 1. Auth API health
curl http://localhost:8000/health
# Esperado: {"status":"healthy"}

# 2. Auth API metrics
curl http://localhost:8000/metrics | head -10
# Esperado: # HELP go_goroutines Number of goroutines...

# 3. Todos API health
curl http://localhost:8082/health
# Esperado: {"status":"healthy"}

# 4. Todos API metrics
curl http://localhost:8082/metrics | head -10
# Esperado: # HELP process_cpu_user_seconds_total...

# 5. Users API health
curl http://localhost:8083/users
# Esperado: [] (array vacío o con usuarios)

# 6. Users API metrics
curl http://localhost:8083/actuator/prometheus | head -10
# Esperado: # HELP jvm_memory_used_bytes...

# 7. Redis Exporter metrics
curl http://localhost:9121/metrics | grep redis_up
# Esperado: redis_up 1
```

**SI algún curl falla:**

- **Connection refused**: El servicio no está corriendo
  ```bash
  docker compose ps [servicio]
  docker compose logs [servicio]
  ```

- **404**: El endpoint no existe (el servicio no se reconstruyó correctamente)
  ```bash
  # Reconstruir ese servicio específico
  docker compose down
  docker compose build --no-cache [servicio]
  docker compose up -d
  ```

- **500**: Error en el servicio
  ```bash
  docker compose logs [servicio] | tail -50
  ```

### Paso 7: Verificar Prometheus Targets

Ahora sí, verifica Prometheus:

```bash
# Abrir en el navegador
open http://localhost:9090/targets

# O desde terminal
curl http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | {job: .labels.job, health: .health}'
```

**Todos los targets deben mostrar:**
- ✅ **auth-api** (auth-api:8000) - UP
- ✅ **users-api** (users-api:8083) - UP
- ✅ **todos-api** (todos-api:8082) - UP
- ✅ **redis** (redis-exporter:9121) - UP
- ✅ **prometheus** (localhost:9090) - UP

**SI aún ves `redis:6379` en lugar de `redis-exporter:9121`:**

```bash
# Prometheus no recargó la configuración
# Reiniciar Prometheus
docker compose restart prometheus

# Esperar 10 segundos
sleep 10

# Verificar de nuevo
open http://localhost:9090/targets
```

## Solución Rápida (Resumen)

Si todo lo demás falla, ejecuta esto en secuencia:

```bash
# 1. Limpiar todo
docker compose down -v
docker system prune -f

# 2. Verificar cambios
git status
git log --oneline -1
# Debe ser: 9c50f8b Fix users-api Prometheus metrics...

# 3. Rebuild completo
docker compose build --no-cache

# 4. Iniciar
docker compose up -d

# 5. Esperar 60 segundos
sleep 60

# 6. Verificar
docker compose ps
curl http://localhost:8000/health
curl http://localhost:8082/health
curl http://localhost:8083/actuator/prometheus | head -5
curl http://localhost:9121/metrics | grep redis_up

# 7. Ver Prometheus
open http://localhost:9090/targets
```

## Checklist de Verificación Final

- [ ] `git log` muestra commit 9c50f8b como el más reciente
- [ ] `docker compose ps` muestra 9 servicios corriendo
- [ ] `curl http://localhost:8000/health` devuelve `{"status":"healthy"}`
- [ ] `curl http://localhost:8082/health` devuelve `{"status":"healthy"}`
- [ ] `curl http://localhost:8083/actuator/prometheus` devuelve métricas
- [ ] `curl http://localhost:9121/metrics` devuelve métricas de Redis
- [ ] http://localhost:9090/targets muestra todos los targets en verde (UP)
- [ ] El target "redis" apunta a `redis-exporter:9121` (NO a `redis:6379`)

## Si Todo Falla

Si después de seguir todos estos pasos aún no funciona, comparte:

1. Output de `git log --oneline -5`
2. Output de `docker compose ps`
3. Output de `docker compose logs auth-api | tail -50`
4. Output de `docker compose logs users-api | tail -50`
5. Output de `docker compose logs todos-api | tail -50`
6. Output de `docker compose logs prometheus | tail -50`
7. Screenshot de http://localhost:9090/targets

Esto ayudará a diagnosticar exactamente dónde está el problema.

## Tiempo Estimado

- Limpieza: 1-2 minutos
- Rebuild: 15-20 minutos (primera vez)
- Inicio de servicios: 1-2 minutos
- Verificación: 2-3 minutos

**Total: ~20-25 minutos**

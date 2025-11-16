# Guía de Pruebas y Video - Docker Compose con Prometheus y Redis

Esta guía te ayudará a verificar que todos los cambios funcionan correctamente y a crear un video de demostración.

## 📋 Pre-requisitos

- Docker Desktop instalado y corriendo
- Git instalado
- Terminal (PowerShell, CMD, o Bash)
- Navegador web
- Al menos 4 GB de RAM disponible
- 10 GB de espacio en disco

## 🚀 Paso 1: Preparar el Entorno

### 1.1 Clonar o actualizar el repositorio

```bash
# Si ya tienes el repo, actualiza la rama
git checkout copilot/fix-prometheus-and-redis-setup
git pull origin copilot/fix-prometheus-and-redis-setup

# O clona desde cero
git clone https://github.com/Juansex/Taller1_Plataformas2.git
cd Taller1_Plataformas2
git checkout copilot/fix-prometheus-and-redis-setup
```

### 1.2 Limpiar contenedores y volúmenes anteriores (opcional)

```bash
# Detener todos los contenedores del proyecto
docker compose down -v

# Limpiar imágenes antiguas (opcional, toma tiempo reconstruir)
docker system prune -a --volumes
```

## 🔨 Paso 2: Construir las Imágenes

```bash
# Construir todas las imágenes (toma 10-15 minutos la primera vez)
docker compose build

# Ver las imágenes creadas
docker images | grep taller1
```

**Salida esperada**: Deberías ver imágenes para auth-api, users-api, todos-api, log-processor, y frontend.

## ▶️ Paso 3: Levantar los Servicios

```bash
# Iniciar todos los servicios en segundo plano
docker compose up -d

# Ver el estado de los contenedores
docker compose ps

# Ver los logs en tiempo real (útil para debugging)
docker compose logs -f
```

**Salida esperada**: Todos los servicios deberían estar en estado "running" o "healthy".

### 3.1 Verificar que todos los servicios están corriendo

```bash
# Ver estado de salud
docker compose ps

# Deberías ver 9 servicios:
# - redis (healthy)
# - redis-exporter (running)
# - auth-api (healthy)
# - users-api (healthy)
# - todos-api (healthy)
# - log-processor (running)
# - prometheus (running)
# - grafana (running)
# - frontend (running)
```

## ✅ Paso 4: Verificar Funcionamiento de Cada Servicio

### 4.1 Verificar Health Endpoints

```bash
# Auth API health
curl http://localhost:8000/health
# Esperado: {"status":"healthy"}

# Todos API health
curl http://localhost:8082/health
# Esperado: {"status":"healthy"}

# Users API health
curl http://localhost:8083/users
# Esperado: Lista de usuarios (puede estar vacía)
```

### 4.2 Verificar Métricas de Prometheus

```bash
# Métricas de Auth API
curl http://localhost:8000/metrics
# Esperado: Métricas en formato Prometheus

# Métricas de Todos API
curl http://localhost:8082/metrics
# Esperado: Métricas en formato Prometheus

# Métricas de Users API (Spring Boot Actuator)
curl http://localhost:8083/actuator/prometheus
# Esperado: Métricas en formato Prometheus

# Métricas de Redis (via exporter)
curl http://localhost:9121/metrics
# Esperado: Métricas de Redis
```

### 4.3 Verificar Redis (Interno, no expuesto)

```bash
# Conectarse al contenedor de Redis y verificar autenticación
docker exec -it redis redis-cli -a RedisSecure2025! ping
# Esperado: PONG

# Ver keys en Redis
docker exec -it redis redis-cli -a RedisSecure2025! keys "*"
# Esperado: Lista de keys o (empty array)
```

## 🎯 Paso 5: Pruebas Funcionales Completas

### 5.1 Prueba de Autenticación

```bash
# Obtener un token JWT
curl -X POST http://localhost:8000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# Esperado: {"accessToken":"eyJhbGc..."}
# Guarda el token para usarlo en las siguientes pruebas
```

### 5.2 Prueba de Todos API

```bash
# Crear un TODO (reemplaza TOKEN con el token obtenido)
TOKEN="eyJhbGc..."
curl -X POST http://localhost:8082/todos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"task":"Verificar que Prometheus funciona","completed":false}'

# Listar TODOs
curl http://localhost:8082/todos \
  -H "Authorization: Bearer $TOKEN"

# Esperado: Lista de TODOs
```

### 5.3 Verificar Log Processor

```bash
# Ver logs del procesador de mensajes
docker compose logs log-processor

# Esperado: Deberías ver mensajes procesados desde Redis
# Ejemplo: "message received after waiting for XXXms: {...}"
```

## 📊 Paso 6: Verificar Prometheus y Grafana

### 6.1 Prometheus UI

1. Abre tu navegador en: http://localhost:9090
2. Ve a **Status → Targets**
3. Verifica que todos los targets estén en estado "UP":
   - prometheus (localhost:9090)
   - auth-api (auth-api:8000)
   - users-api (users-api:8083)
   - todos-api (todos-api:8082)
   - redis (redis-exporter:9121)

4. Ejecuta una query en **Graph**:
   ```
   redis_uptime_in_seconds
   ```
   Deberías ver el tiempo de actividad de Redis.

### 6.2 Grafana UI

1. Abre tu navegador en: http://localhost:3000
2. Login: `admin` / `admin`
3. Ve a **Configuration → Data Sources**
4. Verifica que Prometheus esté configurado como data source

## 🎥 Paso 7: Guía para el Video de Demostración

### Estructura del Video (10-15 minutos)

#### **Parte 1: Introducción (1 min)**
- Presentación del proyecto
- Explicar la arquitectura de microservicios
- Mencionar los problemas que se resolvieron

#### **Parte 2: Levantar el Sistema (3 min)**
```bash
# Mostrar en terminal
docker compose down -v
docker compose build
docker compose up -d
docker compose ps
```
- Explicar cada comando
- Mostrar el output de `docker compose ps`

#### **Parte 3: Verificar Servicios (3 min)**

**Terminal 1: Health Checks**
```bash
curl http://localhost:8000/health
curl http://localhost:8082/health
curl http://localhost:8083/users
```

**Terminal 2: Redis (seguro, no expuesto)**
```bash
# Mostrar que Redis NO responde desde el host
# (debería fallar porque no está expuesto)
curl http://localhost:6379
# Error de conexión - ¡correcto!

# Pero SÍ funciona internamente
docker exec -it redis redis-cli -a RedisSecure2025! ping
```

#### **Parte 4: Prometheus (4 min)**

1. Abrir http://localhost:9090 en el navegador
2. Ir a **Status → Targets**
3. Mostrar todos los targets en verde (UP)
4. Explicar cada target:
   - `auth-api`: Servicio de autenticación (Go)
   - `users-api`: Servicio de usuarios (Java/Spring Boot)
   - `todos-api`: Servicio de TODOs (Node.js)
   - `redis`: Via redis-exporter (no scraping directo)
   - `prometheus`: Auto-monitoreo

5. Ejecutar queries de ejemplo:
   ```
   # Ver uptime de Redis
   redis_uptime_in_seconds
   
   # Ver métricas de HTTP requests (si hay tráfico)
   http_requests_total
   
   # Ver uso de memoria de los procesos
   process_resident_memory_bytes
   ```

#### **Parte 5: Prueba Funcional (3 min)**

**En la terminal:**
```bash
# 1. Obtener token
TOKEN=$(curl -s -X POST http://localhost:8000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r .accessToken)

echo "Token: $TOKEN"

# 2. Crear TODO
curl -X POST http://localhost:8082/todos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"task":"Demo para video","completed":false}'

# 3. Listar TODOs
curl http://localhost:8082/todos \
  -H "Authorization: Bearer $TOKEN" | jq .

# 4. Ver logs del procesador
docker compose logs --tail=20 log-processor
```

#### **Parte 6: Frontend (2 min)**

1. Abrir http://localhost:8080 en el navegador
2. Hacer login con: `admin` / `admin`
3. Crear algunos TODOs desde la UI
4. Mostrar que se reflejan en tiempo real

#### **Parte 7: Grafana (1 min)**

1. Abrir http://localhost:3000
2. Login: `admin` / `admin`
3. Mostrar que Prometheus está configurado como data source
4. (Opcional) Crear un dashboard simple

#### **Parte 8: Conclusión (1 min)**
- Resumen de lo logrado
- Mencionar mejoras implementadas:
  - ✅ Redis protegido con password
  - ✅ Redis-exporter para métricas
  - ✅ Prometheus scraping correctamente
  - ✅ Todos los servicios con health checks
  - ✅ Build issues resueltos (Node.js, mvnw)

## 🐛 Troubleshooting

### Si un servicio no inicia

```bash
# Ver logs del servicio específico
docker compose logs [nombre-servicio]

# Ejemplo:
docker compose logs auth-api
docker compose logs todos-api
```

### Si el build falla

```bash
# Build individual con output detallado
docker compose build --no-cache [nombre-servicio]

# Ejemplo:
docker compose build --no-cache frontend
```

### Si Prometheus no puede scrapear

1. Verificar que los servicios estén en la misma red:
   ```bash
   docker network inspect taller1_plataformas2_microservices
   ```

2. Verificar conectividad:
   ```bash
   docker exec -it prometheus wget -O- http://auth-api:8000/metrics
   ```

### Si Redis no acepta conexiones

```bash
# Verificar que el password es correcto
docker exec -it redis redis-cli -a RedisSecure2025! ping

# Ver logs de Redis
docker compose logs redis
```

## 📝 Checklist para el Video

- [ ] Terminal con buen tamaño de fuente (mínimo 16pt)
- [ ] Navegador con zoom al 125% o 150%
- [ ] Cerrar pestañas innecesarias
- [ ] Preparar comandos en un script o archivo de texto
- [ ] Tener agua cerca 💧
- [ ] Hablar claro y pausado
- [ ] Mencionar cada comando antes de ejecutarlo
- [ ] Explicar qué se espera ver en cada paso
- [ ] Mostrar el resultado de cada comando/acción

## 🎬 Tips para Grabar

1. **Software de grabación**: OBS Studio, Loom, o Zoom
2. **Resolución**: 1920x1080 (Full HD)
3. **Audio**: Usar micrófono decente, reducir ruido de fondo
4. **Iluminación**: Buena luz si te grabas a ti mismo
5. **Duración**: 10-15 minutos máximo
6. **Edición**: Cortar pausas largas, acelerar builds si es necesario

## 📞 Comandos Rápidos de Referencia

```bash
# Levantar todo
docker compose up -d

# Ver estado
docker compose ps

# Ver logs
docker compose logs -f

# Reiniciar un servicio
docker compose restart [servicio]

# Detener todo
docker compose down

# Detener y limpiar volúmenes
docker compose down -v

# Reconstruir una imagen
docker compose build --no-cache [servicio]

# Acceder a un contenedor
docker exec -it [contenedor] /bin/sh
```

## ✨ Resumen de URLs

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| Frontend | http://localhost:8080 | admin/admin |
| Auth API | http://localhost:8000 | - |
| Users API | http://localhost:8083 | - |
| Todos API | http://localhost:8082 | - |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin/admin |
| Redis Exporter | http://localhost:9121/metrics | - |

## 🎉 ¡Éxito!

Si todos los pasos funcionaron correctamente, tu sistema está completamente operativo con:
- ✅ 9 servicios corriendo
- ✅ Redis protegido y monitoreable
- ✅ Prometheus scraping todos los endpoints
- ✅ Grafana listo para dashboards
- ✅ Health checks funcionando
- ✅ Sistema completo de TODOs operativo

¡Ya estás listo para hacer el video! 🎥

# Solución de Problemas: Prometheus Targets Fallando

## Problema Reportado

Al verificar los targets en Prometheus (http://localhost:9090/targets), varios servicios aparecen como DOWN:

- **auth-api**: 404 Not Found en `/metrics`
- **redis**: EOF error (intentando scrapear redis:6379 directamente)
- **todos-api**: "no such host" error
- **users-api**: HTTP 500 en `/actuator/prometheus`

## Causa del Problema

1. **Servicios no reconstruidos**: Los cambios en el código (añadir endpoints `/metrics` y `/health`) requieren reconstruir las imágenes Docker
2. **Prometheus no reiniciado**: El contenedor de Prometheus no ha recargado el archivo `prometheus.yml` actualizado
3. **users-api sin Actuator**: La API de usuarios no tenía las dependencias de Spring Boot Actuator y Prometheus configuradas

## Solución Paso a Paso

### 1. Detener todos los servicios

```bash
docker compose down
```

### 2. Reconstruir las imágenes (especialmente las que cambiaron)

```bash
# Reconstruir todo sin usar caché
docker compose build --no-cache

# O reconstruir servicios específicos si prefieres ser más rápido:
docker compose build --no-cache auth-api
docker compose build --no-cache users-api
docker compose build --no-cache todos-api
```

**Nota**: El build de `users-api` puede tardar varios minutos la primera vez porque Maven debe descargar todas las dependencias nuevas (Actuator y Prometheus client).

### 3. Levantar los servicios de nuevo

```bash
docker compose up -d
```

### 4. Esperar a que todos los servicios estén healthy

```bash
# Ver el estado de los contenedores
docker compose ps

# Deberías ver todos los servicios en estado "healthy" después de 10-20 segundos
```

### 5. Verificar los endpoints de métricas directamente

Antes de revisar Prometheus, verifica que cada servicio responda correctamente:

```bash
# Auth API metrics
curl http://localhost:8000/metrics
# Debe devolver métricas en formato Prometheus (texto plano)

# Users API metrics (ahora con Actuator configurado)
curl http://localhost:8083/actuator/prometheus
# Debe devolver métricas en formato Prometheus

# Todos API metrics
curl http://localhost:8082/metrics
# Debe devolver métricas en formato Prometheus

# Redis metrics (via exporter)
curl http://localhost:9121/metrics
# Debe devolver métricas de Redis
```

### 6. Verificar Prometheus Targets

Abre http://localhost:9090/targets en tu navegador. Deberías ver:

✅ **prometheus** (localhost:9090) - UP
✅ **auth-api** (auth-api:8000) - UP  
✅ **users-api** (users-api:8083) - UP
✅ **todos-api** (todos-api:8082) - UP
✅ **redis** (redis-exporter:9121) - UP

## Cambios Realizados para Solucionar

### 1. users-api/pom.xml
Se añadieron las siguientes dependencias:

```xml
<!-- Actuator para monitoring -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Prometheus metrics para Spring Boot 1.5.x -->
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>simpleclient_spring_boot</artifactId>
    <version>0.0.26</version>
</dependency>
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>simpleclient_hotspot</artifactId>
    <version>0.0.26</version>
</dependency>
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>simpleclient_servlet</artifactId>
    <version>0.0.26</version>
</dependency>
```

### 2. users-api/src/main/resources/application.properties
Se añadió configuración de actuator:

```properties
# Actuator endpoints configuration
management.security.enabled=false
endpoints.prometheus.enabled=true
endpoints.metrics.enabled=true
endpoints.health.enabled=true
```

### 3. users-api PrometheusConfiguration.java
Se creó una clase de configuración que registra el endpoint `/actuator/prometheus` y configura la recolección de métricas.

## Verificación de Éxito

### Test 1: Health Checks
```bash
curl http://localhost:8000/health   # {"status":"healthy"}
curl http://localhost:8082/health   # {"status":"healthy"}
curl http://localhost:8083/users    # [lista de usuarios]
```

### Test 2: Métricas disponibles
```bash
curl http://localhost:8000/metrics | grep "^# HELP"
curl http://localhost:8082/metrics | grep "^# HELP"
curl http://localhost:8083/actuator/prometheus | grep "^# HELP"
curl http://localhost:9121/metrics | grep "^# HELP redis"
```

### Test 3: Prometheus Targets
Todos los targets deben estar en estado **UP** (verde) en http://localhost:9090/targets

### Test 4: Queries en Prometheus
Prueba estas queries en http://localhost:9090/graph:

```promql
# Ver uptime de Redis
redis_uptime_in_seconds

# Ver métricas de proceso (memoria, CPU)
process_resident_memory_bytes

# Ver métricas HTTP de usuarios-api
http_server_requests_seconds_count
```

## Problemas Comunes y Soluciones

### Problema: "no such host" para todos-api o users-api

**Causa**: Los servicios no están corriendo o hay un problema de red Docker.

**Solución**:
```bash
# Ver logs del servicio
docker compose logs todos-api
docker compose logs users-api

# Ver si los contenedores están corriendo
docker compose ps

# Verificar la red
docker network inspect taller1_plataformas2_microservices
```

### Problema: Auth API devuelve 404 en /metrics

**Causa**: El servicio no se reconstruyó con los cambios.

**Solución**:
```bash
docker compose build --no-cache auth-api
docker compose up -d auth-api
```

### Problema: Users API devuelve 500 en /actuator/prometheus

**Causa**: Las dependencias de Actuator no se compilaron correctamente o hay un error en el código.

**Solución**:
```bash
# Ver logs detallados
docker compose logs users-api

# Reconstruir forzando descarga de dependencias
docker compose build --no-cache users-api

# Si el problema persiste, verificar que el archivo PrometheusConfiguration.java se creó correctamente
```

### Problema: Redis target sigue mostrando redis:6379 en lugar de redis-exporter:9121

**Causa**: Prometheus no ha recargado la configuración.

**Solución**:
```bash
# Reiniciar Prometheus
docker compose restart prometheus

# O detener y volver a levantar todo
docker compose down
docker compose up -d
```

### Problema: Build de users-api muy lento o falla por timeout

**Causa**: Maven está descargando muchas dependencias y puede haber problemas de red.

**Solución**:
```bash
# Aumentar timeout y reintentar
docker compose build users-api

# Si falla, verificar conectividad a Maven Central
docker compose run --rm users-api mvn dependency:resolve
```

## Tiempo Estimado para Resolver

- **Detener servicios**: 10 segundos
- **Rebuild completo**: 10-15 minutos (primera vez con caché limpio)
- **Rebuild incremental**: 3-5 minutos (si solo cambiaron algunos servicios)
- **Levantar servicios**: 30 segundos
- **Verificación**: 2-3 minutos

**Total**: ~15-20 minutos para solución completa

## Próximos Pasos Después de Solucionar

1. ✅ Verificar que todos los targets estén UP
2. ✅ Ejecutar pruebas funcionales (login, crear TODO)
3. ✅ Ver logs del log-processor para confirmar que procesa mensajes
4. ✅ Crear dashboards en Grafana usando las métricas de Prometheus
5. ✅ Proceder con la grabación del video de demostración

## Comandos de Referencia Rápida

```bash
# Estado completo del sistema
docker compose ps && echo "---" && docker compose logs --tail=5 --no-log-prefix prometheus | grep "targets"

# Verificar todos los endpoints de métricas
for port in 8000 8082 8083 9121; do echo "=== Port $port ===" && curl -s http://localhost:$port/metrics | head -3; done

# Reinicio limpio completo
docker compose down -v && docker compose build --no-cache && docker compose up -d && docker compose ps
```

## Resumen

El problema principal era que:
1. Los servicios necesitaban ser **reconstruidos** después de añadir los endpoints de métricas
2. **users-api no tenía las dependencias** de Spring Boot Actuator y Prometheus
3. Prometheus necesitaba **reiniciarse** para recargar la configuración

Con los cambios aplicados y siguiendo los pasos de esta guía, todos los targets deberían aparecer en verde (UP) en Prometheus.

# 🎥 GUION TÉCNICO CORREGIDO PARA GRABACIÓN DE VIDEO

**Duración total estimada:** 25-30 minutos  
**Formato:** Demostración en vivo con comandos ejecutables

---

## 📍 PARTE 1: INTRODUCCIÓN (1 min)

**Guion:**
"En este video vamos a demostrar una arquitectura de microservicios completa ejecutándose localmente con Docker Compose. 

Tenemos cinco servicios:
- Auth API (Go): autenticación con JWT
- Users API (Java/Spring Boot): gestión de usuarios con métricas
- Todos API (Node.js): gestión de tareas
- Redis: cola de mensajes
- Log Processor (Python): procesa eventos de Redis

Todo esto está monitoreado con Prometheus y visualizado en Grafana."

---

## 📍 PARTE 2: STARTUP (5 min)

**Acciones en pantalla:**

```bash
cd C:\Users\user\OneDrive\Documentos\Juanse\Taller1_Plataformas2\Taller1_Plataformas2

git pull origin main

docker-compose down

docker-compose build --no-cache

docker-compose up
```

**Guion mientras esperan a que compile:**
"Docker Compose está descargando las imágenes base, compilando el código Java con Maven, instalando dependencias de Node y Python, y compilando el binario Go. Esto toma unos 15-20 minutos en la primera ejecución."

**Señales de éxito esperadas en los logs:**
```
✓ redis Started
✓ redis-exporter Started
✓ auth-api Started
✓ users-api Started
✓ todos-api Started
✓ frontend Started
✓ prometheus Started
✓ grafana Started
✓ log-processor Started
```

---

## 📍 PARTE 3: VERIFICACIÓN DE SERVICIOS (2 min)

**Abrir en navegador: http://localhost:8080**

**Guion:**
"El frontend está corriendo. Vemos la pantalla de login. Vamos a autenticarnos con las credenciales de prueba."

**Acciones:**
1. Username: `admin`
2. Password: `admin`
3. Click en Login

**Guion después de login:**
"Acceso exitoso. Vemos el dashboard de tareas. Ahora vamos a navegar a Prometheus para ver el monitoreo."

---

## 📍 PARTE 4: PROMETHEUS - MONITOREO (8-10 min)

### 4.1 - Targets Status

**Abrir: http://localhost:9090/targets**

**Guion:**
"Ingresamos a Prometheus. En la sección Status → Targets, vemos los endpoints que estamos monitoreando.

Observamos que:
- **users-api** (en puerto 8083) reporta **UP**: esto significa que Spring Boot Actuator está exponiendo métricas en `/actuator/prometheus`
- **redis-exporter** reporta **UP**: es un servicio que traduce las métricas de Redis al formato que Prometheus entiende
- **prometheus** (a sí mismo) reporta **UP**

Los otros servicios como auth-api y todos-api son funcionales para la aplicación, pero no exponen un endpoint de métricas. Por lo tanto, no los monitoreamos, y esto es completamente normal para esta implementación."

---

### 4.2 - Consulta 1: Health Check

**En el Query box de Prometheus, ejecutar:**

```promql
up
```

**Click: Execute**

**Guion:**
"La métrica `up` es un health check binario. Un valor de 1 significa que el servicio está UP. Un valor de 0 significaría que está DOWN.

Como vemos, tanto users-api como redis-exporter reportan 1, confirmando que están accesibles y exponiendo métricas correctamente."

---

### 4.3 - Consulta 2: CPU Usage

**En el Query box, ejecutar:**

```promql
process_cpu_seconds_total{job="users-api"}
```

**Click: Execute → Graph**

**Guion:**
"Aquí vemos `process_cpu_seconds_total`, una métrica de la JVM de Java que acumula el tiempo total de CPU utilizado por el proceso. Este es un contador que solo aumenta.

Esto proviene directamente del Spring Boot Actuator, que expone métricas de Micrometer."

---

### 4.4 - Consulta 3: HTTP Requests

**En el Query box, ejecutar:**

```promql
http_requests_total{job="users-api"}
```

**Click: Execute → Graph**

**Guion:**
"Aquí vemos `http_requests_total`, que cuenta las peticiones HTTP que recibe la API de Usuarios, segmentadas por método y endpoint.

Este es el contador que vamos a ver incrementar cuando hagamos peticiones reales en la Parte 6."

---

### 4.5 - Consulta 4: Request Rate

**En el Query box, ejecutar:**

```promql
rate(http_requests_total{job="users-api"}[1m])
```

**Click: Execute → Graph**

**Guion:**
"Usando la función `rate()`, calculamos la tasa de cambio del contador en la última 1 minuto. Esto nos da el throughput en peticiones por segundo.

Ahorita está en cero porque no hemos hecho peticiones. En la Parte 6, veremos este gráfico moverse."

---

### 4.6 - Redis Metrics

**En el Query box, ejecutar:**

```promql
redis_connected_clients
```

**Click: Execute**

**Guino:**
"Finalmente, aquí vemos métricas de Redis exportadas por redis-exporter: número de clientes conectados, memoria usada, etc.

Todos estos datos están siendo recolectados cada 10 segundos (tal como configuramos en prometheus.yml)."

---

### 4.7 - Mostrar Configuración

**Abrir en VSCode o editor de texto: `config/prometheus.yml`**

**Guion:**
"En el archivo de configuración de Prometheus, vemos dos jobs principales:

1. **users-api**: apunta a `users-api:8083` con `metrics_path: '/actuator/prometheus'`
2. **redis**: apunta a `redis-exporter:9121` con el path de métricas de redis

Esto es lo que permite que Prometheus sepa dónde y cómo recolectar las métricas."

---

## 📍 PARTE 5: PREPARACIÓN PARA DEMO (1 min)

**Abrir una nueva terminal/PowerShell:**

```bash
# Terminal 1: Logs de log-processor
docker-compose logs -f log-processor

# Terminal 2: Ready para hacer curls
# (mantenla abierta pero no hacer nada aún)
```

**Guion:**
"Abrimos una terminal adicional para monitorear el log-processor en tiempo real. Esto nos permitirá ver cómo el servicio consume eventos de Redis cuando creemos una tarea."

---

## 📍 PARTE 6: DEMOSTRACIÓN FUNCIONAL (12-14 min)

### 6.1 - Obtener Token JWT

**En PowerShell/Terminal 2, ejecutar:**

```powershell
$response = curl -X POST http://localhost:8000/login `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","password":"admin"}' `
  -UseBasicParsing

$token = ($response.Content | ConvertFrom-Json).token
Write-Host "Token: $token"
```

**Guion:**
"Primero, generamos un token JWT contra el auth-api. Este token será válido para autenticar peticiones a users-api y todos-api."

**Copiar el token que aparece en pantalla.**

---

### 6.2 - Petición autenticada a Users API

**En la misma terminal, ejecutar (reemplazando `<TOKEN>`):**

```powershell
curl -X GET http://localhost:8083/users `
  -H "Authorization: Bearer <TOKEN>" `
  -UseBasicParsing | ConvertFrom-Json | Format-Table
```

**Guion:**
"Ahora hacemos una petición GET a `/users` de la API de Usuarios, autenticada con nuestro token.

Esta petición es IMPORTANTE porque users-api está siendo monitoreada. Prometheus va a registrar que ocurrió esta petición HTTP."

---

### 6.3 - Mostrar Logs en Tiempo Real

**En Terminal 1 (donde están los logs de log-processor):**

**Guion:**
"En la otra terminal, vemos los logs del log-processor. Aquí se procesa cada evento que ocurre en la aplicación a través de Redis."

**Esperar 5 segundos para que se actualicen los logs.**

---

### 6.4 - Crear una Tarea (Opcional pero Recomendado)

**En Terminal 2, ejecutar (reemplazando `<TOKEN>`):**

```powershell
curl -X POST http://localhost:8082/todos `
  -H "Authorization: Bearer <TOKEN>" `
  -H "Content-Type: application/json" `
  -d '{"content":"Demo completada exitosamente"}' `
  -UseBasicParsing | ConvertFrom-Json | Format-Table
```

**Guion:**
"Aunque todos-api no está monitoreada directamente en Prometheus, la aplicación es completamente funcional. Creamos una tarea de demostración.

Si observamos el log-processor en Terminal 1, deberíamos ver el evento registrado en Redis inmediatamente."

---

### 6.5 - Verificar Actualizacion de Métricas en Prometheus

**Volver a http://localhost:9090**

**Ejecutar:**

```promql
rate(http_requests_total{job="users-api"}[1m])
```

**Guion:**
"Volvemos a Prometheus. Si ejecutamos nuevamente la consulta de rate de peticiones HTTP, deberíamos ver que el gráfico ha cambiado.

La petición que hicimos en el paso 6.2 fue registrada por Spring Boot Actuator y Prometheus la recolectó."

---

### 6.6 - Verificar en Grafana

**Abrir: http://localhost:3000**

**Login:**
- Username: `admin`
- Password: `admin`

**Navegar a:** Home → Dashboards

**Guion:**
"De vuelta en Grafana, vemos los dashboards que conectan con Prometheus.

Aquí podemos ver visualizaciones en tiempo real de:
- Peticiones HTTP a users-api
- Memoria utilizada
- Conexiones a Redis
- Y cualquier otra métrica que hayamos expuesto

Esto completa el ciclo de monitoreo: la métrica se genera en el servicio → Prometheus la recolecta → Grafana la visualiza."

---

## 📍 PARTE 7: RESUMEN ARQUITECTÓNICO (2 min)

**Mostrar en pantalla: `ARQUITECTURA_DIAGRAMAS.md`**

**Guion:**
"Para resumir, nuestro sistema tiene esta arquitectura:

**Frontend (Vue.js)** → Autentica con **Auth API (Go)** → Obtiene token JWT

**Frontend** → Usa token para acceder a **Users API (Java)** y **Todos API (Node.js)**

**Users API, Todos API, Auth API** → Publican eventos en **Redis**

**Log Processor (Python)** → Consume eventos de Redis

**Prometheus** → Recolecta métricas de:
  - **Users API** (Spring Boot Actuator)
  - **Redis** (vía redis-exporter)

**Grafana** → Visualiza datos de Prometheus

Este es un ejemplo completamente funcional de:
✓ Microservicios en múltiples lenguajes
✓ Orquestación con Docker Compose
✓ Autenticación con JWT
✓ Monitoreo y observabilidad
✓ Logging centralizado con Redis

El proyecto está completamente deployable y escalable."

---

## 🎬 COMANDOS PARA COPIAR-PEGAR DURANTE LA GRABACIÓN

### Token Generation (PowerShell)
```powershell
$response = curl -X POST http://localhost:8000/login `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","password":"admin"}' `
  -UseBasicParsing

$token = ($response.Content | ConvertFrom-Json).token
Write-Host "Token: $token"
```

### Users API Request (PowerShell)
```powershell
curl -X GET http://localhost:8083/users `
  -H "Authorization: Bearer <REEMPLAZA_CON_TU_TOKEN>" `
  -UseBasicParsing
```

### Create Todo (PowerShell)
```powershell
curl -X POST http://localhost:8082/todos `
  -H "Authorization: Bearer <REEMPLAZA_CON_TU_TOKEN>" `
  -H "Content-Type: application/json" `
  -d '{"content":"Demo completada exitosamente"}' `
  -UseBasicParsing
```

### Prometheus Queries
```promql
up
process_cpu_seconds_total{job="users-api"}
http_requests_total{job="users-api"}
rate(http_requests_total{job="users-api"}[1m])
redis_connected_clients
```

---

## ✅ CHECKLIST PRE-GRABACIÓN

- [ ] Todos los contenedores están UP (docker-compose up)
- [ ] Frontend accesible en http://localhost:8080
- [ ] Prometheus accesible en http://localhost:9090
- [ ] Grafana accesible en http://localhost:3000
- [ ] Token PowerShell script listo para copiar-pegar
- [ ] URLs del navegador en marcadores
- [ ] Terminal 1 abierta mostrando logs
- [ ] Micrófono y pantalla configurados
- [ ] OBS o software de grabación listo

---

## 🎯 PUNTOS CRÍTICOS

1. **No intentes monitorear todos-api o auth-api** - No exponen `/metrics`
2. **La petición a users-api es CRUCIAL** - Es la que vamos a ver en Prometheus
3. **Déjate tiempo entre pasos** - Prometheus recolecta cada 10 segundos
4. **Ten el token a mano** - Cópialo en un editor antes de usar en curls
5. **Muestra los gráficos actualizándose** - Ese es el "wow moment" de la demo

---

## 📺 ESTRUCTURA DE GRABACIÓN RECOMENDADA

| Sección | Duración | Qué grabas |
|---------|----------|-----------|
| Intro + Startup | 6 min | Terminal con docker-compose, explicación |
| Verificación | 2 min | Frontend login |
| Prometheus | 10 min | Targets, queries, gráficos |
| Demo funcional | 12 min | Curls, logs, Grafana |
| Resumen | 2 min | Diagramas de arquitectura |
| **Total** | **32 min** | ✅ Listo para entregar |

---

**¡Buena suerte con la grabación! 🚀**

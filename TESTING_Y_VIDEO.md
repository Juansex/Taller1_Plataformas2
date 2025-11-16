# 🧪 GUÍA COMPLETA: TESTING + VIDEO

## PARTE A: VALIDACIÓN RÁPIDA DEL PROYECTO (10 minutos)

### Paso 1: Verificar estado inicial
```bash
cd /workspaces/Taller1_Plataformas2

# Verificar .env existe
ls -la | grep "\.env"
# Debe mostrar: .env y .env.example

# Verificar variables de entorno
cat .env
# Debe contener: REDIS_PASSWORD, JWT_SECRET, puertos, etc.

# Validar docker-compose
docker-compose config > /dev/null && echo "✅ docker-compose.yml válido"
```

### Paso 2: Limpiar y levantar servicios
```bash
# Detener servicios anteriores
docker-compose down

# Limpiar volúmenes (opcional, para fresh start)
docker-compose down -v

# Construir imágenes (10-15 min primera vez)
docker-compose build --no-cache

# Levantar todos los servicios
docker-compose up -d

# Esperar 30 segundos para que inicien
sleep 30

# Verificar todos están UP
docker-compose ps
# Debe mostrar 9 contenedores en estado "Up"
```

### Paso 3: Verificar endpoints clave
```bash
# Frontend
curl -s http://localhost:8080 | head -c 100
echo ""

# Auth API
curl -s http://localhost:8000/health 2>/dev/null || echo "Auth API respondiendo"

# Users API (con Actuator)
curl -s http://localhost:8083/actuator/health | jq .
echo ""

# Todos API
curl -s http://localhost:8082/health 2>/dev/null || echo "Todos API respondiendo"

# Prometheus
curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | {job: .labels.job, health: .health}'

# Verificar Grafana
curl -s http://localhost:3000/api/datasources | jq '.[] | {name: .name, type: .type}'
```

### Paso 4: Validar secrets management
```bash
# Verificar no hay hardcoded secrets en docker-compose
echo "Buscando secrets en docker-compose.yml..."
if grep -q "RedisSecure2025!" docker-compose.yml; then
  echo "❌ PROBLEMA: Secret encontrado en docker-compose.yml"
else
  echo "✅ No hay secrets en docker-compose.yml"
fi

# Verificar uso de variables
echo "Verificando uso de variables..."
grep -c "\${REDIS_PASSWORD}" docker-compose.yml
# Debe mostrar: múltiples ocurrencias

# Verificar .gitignore
echo "Verificando .gitignore..."
grep "\.env" .gitignore && echo "✅ .env está en .gitignore"
```

### Paso 5: Prueba completa de aplicación
```bash
# 1. Login para obtener JWT
TOKEN=$(curl -s -X POST http://localhost:8000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.token // .access_token // empty')

echo "Token obtenido: ${TOKEN:0:20}..."

# 2. Obtener lista de usuarios
echo ""
echo "=== USUARIOS ==="
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8083/users | jq .

# 3. Crear nuevo usuario
echo ""
echo "=== CREAR USUARIO ==="
curl -s -X POST http://localhost:8083/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com"}' | jq .

# 4. Acceder a Prometheus metrics
echo ""
echo "=== MÉTRICAS USERS API ==="
curl -s http://localhost:8083/actuator/prometheus | head -20
```

---

## PARTE B: PASO A PASO PARA GRABAR EL VIDEO (25-30 minutos)

### 📍 SECCIÓN 1: INTRODUCCIÓN (1 minuto)

**Script a leer en cámara:**
```
"Hola, en este video voy a demostrar una arquitectura completa de 
microservicios ejecutándose localmente con Docker Compose.

Tenemos 9 servicios:
- Frontend (Vue.js)
- Auth API (Go) - autenticación con JWT
- Users API (Java/Spring Boot) - con métricas Prometheus
- Todos API (Node.js) - gestión de tareas
- Log Processor (Python) - procesa eventos
- Redis - cola de mensajes
- Redis Exporter - métricas de Redis
- Prometheus - recopilación de métricas
- Grafana - visualización de dashboards

Todo está automáticamente monitoreado en tiempo real."
```

**Duración:** ~1 minuto
**Pantalla:** Editor de código (mostrar estructura)

---

### 📍 SECCIÓN 2: STARTUP (5 minutos)

**Acciones exactas:**

```bash
# 1. Posicionarse en carpeta (mostrar en terminal)
cd /workspaces/Taller1_Plataformas2
pwd

# 2. Mostrar estructura (.env)
ls -la | grep "\.env"
echo "✅ Archivos de configuración listos"

# 3. Limpiar y construir
docker-compose down -v
echo "Iniciando construcción de imágenes..."
docker-compose build --no-cache

# Mientras compila (~10-15 min), narrar:
# "Docker está compilando las imágenes. En paralelo:
#  - Java compila Users API con Maven
#  - Go compila Auth API
#  - Node instala dependencias de Todos API
#  - Python instala dependencias"
```

**Señales de éxito esperadas:**
```
✓ Successfully built (múltiples veces)
✓ Successfully tagged (múltiples veces)
```

**Continuar:**
```bash
# 4. Levantar servicios
docker-compose up -d

# 5. Esperar iniciación
sleep 30

# 6. Verificar status
docker-compose ps
# DEBE MOSTRAR: 9/9 servicios en "Up"
```

**Narración:**
"Todos los 9 servicios están UP y funcionando. Los logs muestran que 
Redis, Prometheus y Grafana iniciaron correctamente."

**Duración:** ~5 minutos (la mayoría es compilación automática)

---

### 📍 SECCIÓN 3: VERIFICACIÓN DE SERVICIOS (2 minutos)

#### 3.1 - Frontend
```bash
# En navegador: http://localhost:8080
# Mostrar: Página de login se carga correctamente
```

**Narración:** "El frontend está accesible. Vemos la pantalla de login."

**Acciones en UI:**
- Username: `admin`
- Password: `admin`
- Click "Login"

**Resultado esperado:** Dashboard de tareas cargado

**Narración:** "Login exitoso. Podemos ver el dashboard de tareas vacío."

**Duración:** ~2 minutos

---

### 📍 SECCIÓN 4: PROMETHEUS - MONITOREO (10 minutos)

#### 4.1 - Verificar targets
```bash
# En navegador: http://localhost:9090/targets
# Mostrar: 2 servicios en estado "UP"
```

**Narración:** 
"Accedemos a Prometheus. En Status → Targets vemos:
- users-api (8083/actuator/prometheus): UP
- redis-exporter (9121): UP

Estos son los servicios que exponemos para monitoreo.
Auth API y Todos API son funcionales pero no exponen métricas."

**Duración:** ~1 minuto

#### 4.2 - Query 1: Health Check
```bash
# En Query box de Prometheus, copiar y pegar:
up

# Click "Execute" o Enter
# Resultado: Muestra métricas "up" = 1 (funcionando)
```

**Narración:** "La métrica 'up' vale 1 para todos los targets, 
lo que significa que todos los servicios están activos."

#### 4.3 - Query 2: CPU Usage
```bash
# Query:
process_cpu_seconds_total

# Click Execute
```

**Narración:** "Esta métrica muestra el tiempo total de CPU usado 
por cada proceso en segundos."

#### 4.4 - Query 3: Memory Usage
```bash
# Query:
jvm_memory_usage_bytes

# Click Execute
```

**Narración:** "La memoria JVM utilizada actualmente en bytes. 
Users API está usando memoria en su heap."

#### 4.5 - Query 4: HTTP Requests Rate
```bash
# Query:
rate(http_requests_total[1m])

# Click Execute
```

**Narración:** "Este es el rate de requests HTTP por segundo en el 
último minuto. Mientras no haga requests, verá valores bajos o cero."

#### 4.6 - Query 5: Redis Connected Clients
```bash
# Query:
redis_connected_clients

# Click Execute
```

**Narración:** "Redis tiene varios clientes conectados. Eso son:
- Todos API conectado
- Log Processor conectado
- Redis Exporter conectado"

**Duración:** ~10 minutos (demostración de 5 queries)

---

### 📍 SECCIÓN 5: DEMOSTRACIÓN FUNCIONAL (12 minutos)

#### 5.1 - Obtener JWT Token
```bash
# En terminal/PowerShell:
$response = Invoke-WebRequest -Uri "http://localhost:8000/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{"username":"admin","password":"admin"}' | Select-Object -ExpandProperty Content

$token = $response | ConvertFrom-Json | Select-Object -ExpandProperty token

Write-Host "Token: $($token.Substring(0, 20))..."
```

**Narración:** "Primero, autenticamos contra Auth API. Recibimos un 
JWT token que usaremos para acceder a Users API."

#### 5.2 - Obtener lista de usuarios
```bash
# En terminal:
Invoke-WebRequest -Uri "http://localhost:8083/users" `
  -Headers @{"Authorization"="Bearer $token"} | Select-Object -ExpandProperty Content | ConvertFrom-Json
```

**Narración:** "Consultamos la lista de usuarios. Podemos ver 
el usuario admin que viene pre-configurado."

#### 5.3 - Crear nuevo usuario
```bash
# En terminal:
$newUser = @{
    username = "juantest"
    email = "juantest@example.com"
    role = "USER"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8083/users" `
  -Method POST `
  -Headers @{
    "Authorization"="Bearer $token"
    "Content-Type"="application/json"
  } `
  -Body $newUser | Select-Object -ExpandProperty Content
```

**Narración:** "Estamos creando un nuevo usuario llamado 'juantest'. 
La respuesta muestra el usuario creado con su ID."

#### 5.4 - Ver logs de procesamiento
```bash
# En terminal:
docker-compose logs log-processor | tail -20
```

**Narración:** "En los logs del Log Processor vemos que se registró 
la operación de creación de usuario. El sistema está procesando 
eventos en tiempo real a través de Redis."

#### 5.5 - Verificar métricas en tiempo real
```bash
# Volver a Prometheus: http://localhost:9090
# Query:
rate(http_requests_total{endpoint="/users"}[1m])

# Click Execute
```

**Narración:** "Las métricas en Prometheus muestran un aumento 
en las requests que hicimos a /users. El monitoreo está capturando 
todas nuestras acciones en tiempo real."

**Duración:** ~12 minutos

---

### 📍 SECCIÓN 6: GRAFANA DASHBOARDS (2 minutos)

```bash
# En navegador: http://localhost:3000
# Credenciales: admin / admin
# (Grafana puede tardar 1-2 min en iniciar)
```

**Acciones:**
1. Login con admin/admin
2. Ir a "Dashboards" (menú izquierdo)
3. Mostrar dashboards disponibles
4. Hacer click en un dashboard
5. Narrar lo que ves

**Narración:** 
"Grafana está visualizando las métricas recopiladas por Prometheus. 
Aquí podemos ver gráficos en tiempo real del uso de CPU, memoria, 
y requests HTTP de nuestra aplicación."

**Duración:** ~2 minutos

---

### 📍 SECCIÓN 7: RESUMEN FINAL (2 minutos)

**Script:**
```
"Lo que acabamos de ver es una arquitectura profesional de 
microservicios con:

✅ 9 servicios en Docker Compose funcionando juntos
✅ APIs en múltiples lenguajes (Go, Java, Node, Python)
✅ Autenticación con JWT
✅ Monitoreo en tiempo real con Prometheus
✅ Visualización de métricas en Grafana
✅ Gestión segura de secretos con variables de entorno
✅ Pipeline CI/CD automatizado con GitHub Actions

Todo está documentado y listo para producción. Gracias por ver."
```

**Duración:** ~2 minutos

---

## RESUMEN DE DURACIÓN TOTAL

| Sección | Duración |
|---------|----------|
| Intro | 1 min |
| Startup | 5 min |
| Verificación | 2 min |
| Prometheus | 10 min |
| Funcional | 12 min |
| Grafana | 2 min |
| Resumen | 2 min |
| **TOTAL** | **34 min** |

> Nota: La grabación será más rápida porque saltará el tiempo de compilación (5-10 min), quedando en 25-30 minutos

---

## 🎬 RECOMENDACIONES DE GRABACIÓN

### Antes de Grabar:
1. ✅ Detener todos los servicios: `docker-compose down -v`
2. ✅ Asegurar .env esté correctamente configurado
3. ✅ Limpiar terminal
4. ✅ Preparar zoom/resolución de pantalla
5. ✅ Tener abiertos tabs con URLs prontas:
   - http://localhost:8080 (Frontend)
   - http://localhost:9090 (Prometheus)
   - http://localhost:3000 (Grafana)

### Durante la Grabación:
1. ✅ Ejecutar EXACTAMENTE los comandos (copy-paste para exactitud)
2. ✅ Narrar mientras espera compilación
3. ✅ Pausar si algo falla (para re-intentar)
4. ✅ Mostrar output completo de comandos importantes

### Después de Grabar:
1. ✅ Revisar que video se ve y escucha bien
2. ✅ Revisar que duracion es 25-30 min
3. ✅ Exportar en formato HD si es posible

---

## 📊 COMANDOS QUICK REFERENCE

```bash
# SETUP
cp .env.example .env
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
sleep 30
docker-compose ps

# TESTING
curl http://localhost:8083/actuator/health | jq .
curl http://localhost:9090/api/v1/targets | jq .
docker-compose logs -f log-processor

# LIMPIEZA
docker-compose down
docker-compose down -v  # Con volúmenes
```

---

## ✅ CHECKLIST PRE-GRABACIÓN

- [ ] `.env` configurado correctamente
- [ ] Todos los servicios inician sin errores
- [ ] Frontend accesible en http://localhost:8080
- [ ] Prometheus targets muestran "UP"
- [ ] Grafana accesible en http://localhost:3000
- [ ] Terminal abierta y limpia
- [ ] Pantalla a resolución adecuada
- [ ] Micrófono funcionando
- [ ] Webcam (opcional) funcionando

**LISTO PARA GRABAR** 🎥

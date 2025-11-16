# 🚀 EJECUTAR TESTING - GUÍA VISUAL PASO A PASO

## ⏱️ TIEMPO TOTAL: 30-40 minutos (primera ejecución)

---

## PASO 1️⃣: PREPARAR ENTORNO (2 minutos)

### Abre una terminal y ejecuta:

```bash
cd /workspaces/Taller1_Plataformas2
```

**Verifica que estés en la carpeta correcta:**
```bash
pwd
# Debe mostrar: /workspaces/Taller1_Plataformas2

ls -la | grep -E "docker-compose|\.env"
# Debe mostrar: docker-compose.yml, .env, .env.example
```

---

## PASO 2️⃣: LIMPIAR AMBIENTE (2 minutos)

### Detén servicios anteriores:

```bash
docker-compose down -v
```

**Esperado:**
```
Stopping services...
Removing containers...
Removing volumes...
✅ Done
```

---

## PASO 3️⃣: CONSTRUIR IMÁGENES (10-15 minutos) ⏳

### Ejecuta:

```bash
docker-compose build
```

**Qué pasa mientras compila:**
- Docker descarga imágenes base
- Java compila con Maven (usuarios-api) → ~3 minutos
- Go compila el binario (auth-api) → ~2 minutos
- Node instala dependencias (todos-api, frontend) → ~2 minutos
- Python instala requirements (log-processor) → ~1 minuto

**Puedes ver el progreso en tiempo real**

**Señal de éxito al final:**
```
Successfully built [hash]
Successfully tagged...
```

---

## PASO 4️⃣: LEVANTAR SERVICIOS (1 minuto)

### Una vez termine la construcción, ejecuta:

```bash
docker-compose up -d
```

**Esperado:**
```
Creating redis          ... done
Creating redis-exporter ... done
Creating auth-api       ... done
Creating users-api      ... done
Creating todos-api      ... done
Creating frontend       ... done
Creating log-processor  ... done
Creating prometheus     ... done
Creating grafana        ... done
```

---

## PASO 5️⃣: ESPERAR INICIALIZACIÓN (1-2 minutos)

### Espera a que todo arranque:

```bash
sleep 30
```

### Luego verifica que todos estén UP:

```bash
docker-compose ps
```

**Esperado: Verás una tabla así:**
```
NAME              STATUS
redis             Up 1 minute
redis-exporter    Up 1 minute  
auth-api          Up 1 minute
users-api         Up 1 minute
todos-api         Up 1 minute
frontend          Up 1 minute
log-processor     Up 1 minute
prometheus        Up 1 minute
grafana           Up 1 minute

✅ 9/9 servicios UP
```

**⚠️ Si NO ves 9 servicios UP:**
```bash
# Ver qué pasó
docker-compose logs | head -100

# Ver logs de un servicio específico
docker-compose logs users-api
```

---

## PASO 6️⃣: VALIDAR FUNCIONAMIENTO (5 minutos)

### 6.1 Verificar Frontend (en navegador)

Abre: **http://localhost:8080**

**Esperado:** Verás la pantalla de login de la aplicación

Login con:
- **Usuario:** `admin`
- **Contraseña:** `admin`

**Esperado después:** Verás el dashboard de tareas

---

### 6.2 Verificar Prometheus (en terminal)

Ejecuta:
```bash
curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | {job: .labels.job, health: .health}'
```

**Esperado:**
```json
{
  "job": "users-api",
  "health": "up"
}
{
  "job": "redis-exporter",
  "health": "up"
}
{
  "job": "prometheus",
  "health": "up"
}
```

**✅ SI VES ESTO: TODO FUNCIONA CORRECTAMENTE**

---

### 6.3 Verificar API Auth (en terminal)

Ejecuta:
```bash
# Obtener token
curl -X POST http://localhost:8000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq .
```

**Esperado:** Recibirás un token JWT
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## PASO 7️⃣: TESTING COMPLETO DE FUNCIONALIDAD (5 minutos)

### Ejecuta este bloque en terminal:

```bash
# Obtener token
TOKEN=$(curl -s -X POST http://localhost:8000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.token // .access_token // empty')

echo "✅ Token obtenido: ${TOKEN:0:30}..."

# Listar usuarios
echo ""
echo "=== USUARIOS ACTUALES ==="
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8083/users | jq '.[] | {id, username, email}'

# Crear nuevo usuario
echo ""
echo "=== CREAR NUEVO USUARIO ==="
curl -s -X POST http://localhost:8083/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"testuser@example.com"}' | jq .

# Listar usuarios nuevamente
echo ""
echo "=== USUARIOS DESPUÉS DE CREAR ==="
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8083/users | jq '.[] | {id, username, email}'
```

**Esperado:** 
- Verás el usuario 'admin' existente
- Verás el nuevo usuario 'testuser' creado
- Sin errores de autenticación

---

## PASO 8️⃣: VALIDAR MONITOREO EN PROMETHEUS (3 minutos)

Abre en navegador: **http://localhost:9090**

### Ejecuta estas queries (copiar en el query box):

**Query 1:** Health Check
```promql
up
```
**Resultado:** Métrica UP para todos los targets = 1

**Query 2:** CPU Usage
```promql
process_cpu_seconds_total
```
**Resultado:** Muestra CPU utilizado

**Query 3:** Memory
```promql
jvm_memory_usage_bytes
```
**Resultado:** Bytes de memoria JVM

**Query 4:** Requests Rate
```promql
rate(http_requests_total[1m])
```
**Resultado:** Requests por segundo en el último minuto

---

## PASO 9️⃣: VERIFICAR GRAFANA (2 minutos)

Abre en navegador: **http://localhost:3000**

Login:
- **Usuario:** `admin`
- **Password:** `admin`

Navega a: **Dashboards → Manage**

**Verás:**
- Prometheus como datasource
- Dashboards pre-configurados
- Métricas en tiempo real

---

## PASO 🔟: VALIDAR SECRETS MANAGEMENT (2 minutos)

En terminal, verifica seguridad:

```bash
# 1. Verificar .env está protegido
echo "1. ¿.env está en .gitignore?"
grep "\.env" .gitignore && echo "✅ Sí" || echo "❌ No"

# 2. Verificar sin secrets en docker-compose
echo ""
echo "2. ¿docker-compose.yml sin secrets hardcodeados?"
if grep -q "RedisSecure2025!" docker-compose.yml; then
  echo "❌ PROBLEMA"
else
  echo "✅ Sí (usando variables)"
fi

# 3. Verificar GitHub Actions existe
echo ""
echo "3. ¿GitHub Actions CI/CD setup?"
ls .github/workflows/ci.yml > /dev/null && echo "✅ Sí" || echo "❌ No"
```

---

## ✅ CHECKLIST FINAL

Marca cada item cuando valides:

- [ ] 9 servicios en estado "Up" (docker-compose ps)
- [ ] Frontend cargado en http://localhost:8080
- [ ] Login exitoso en Frontend
- [ ] Prometheus targets muestran "UP"
- [ ] Auth API devuelve JWT token
- [ ] Puedes crear usuarios vía API
- [ ] Prometheus acepta queries
- [ ] Grafana accesible en http://localhost:3000
- [ ] .env está en .gitignore
- [ ] docker-compose.yml usa ${VARIABLES}
- [ ] GitHub Actions workflow existe

**SI TODO TIENE ✅: READY PARA GRABAR VIDEO**

---

## 🎥 SIGUIENTES PASOS: GRABAR VIDEO

Una vez validado todo, consulta: **TESTING_Y_VIDEO.md**

Contiene:
- Script exacto para cada sección
- Duraciones
- Comandos copy-paste
- Tips de grabación

---

## 🚨 PROBLEMAS COMUNES

### "Puertos ya están en uso"
```bash
# Cambiar puertos en .env:
# AUTH_API_PORT=8001
# SERVER_PORT=8084
# TODO_API_PORT=8085

# Luego:
docker-compose down -v
docker-compose up -d
```

### "Un servicio dice 'Exited (1)'"
```bash
# Ver qué pasó:
docker-compose logs <servicio>

# Ejemplo:
docker-compose logs users-api

# Reintentar:
docker-compose down -v
docker-compose up -d
```

### "Prometheus targets en DOWN"
```bash
# Esperar más:
sleep 60

# Luego verificar:
curl -s http://localhost:9090/api/v1/targets | jq .
```

### "No puedo autenticar"
```bash
# Verificar Auth API está corriendo:
docker-compose logs auth-api | tail -20

# Reiniciar:
docker-compose restart auth-api
```

---

## 📞 RESUMEN RÁPIDO

```bash
# Todo en una línea (después del primer setup):
cd /workspaces/Taller1_Plataformas2 && \
docker-compose down -v && \
docker-compose build && \
docker-compose up -d && \
sleep 30 && \
docker-compose ps
```

---

**¡LISTO PARA EMPEZAR!** 🚀

Ejecuta **PASO 1** y sigue de ahí.

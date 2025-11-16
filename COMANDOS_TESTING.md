# 🎯 COMANDOS EXACTOS PARA TESTING Y VIDEO

## BLOQUE 1: SETUP INICIAL (Copiar-Pegar)

```bash
cd /workspaces/Taller1_Plataformas2
cp .env.example .env
docker-compose down -v
docker-compose build
```

**Esperar:** 10-15 minutos para que compile

---

## BLOQUE 2: LEVANTAR SERVICIOS

```bash
docker-compose up -d
sleep 30
docker-compose ps
```

**Esperado:** Ver 9 servicios en estado "Up"

---

## BLOQUE 3: PRUEBAS BÁSICAS

### 3.1 Verificar Frontend
```bash
curl -s http://localhost:8080 | head -c 100
```

### 3.2 Verificar Users API
```bash
curl -s http://localhost:8083/actuator/health | jq .
```

### 3.3 Verificar Prometheus Targets
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

---

## BLOQUE 4: PRUEBA DE AUTENTICACIÓN Y API

```bash
# 4.1 Login (obtener token)
TOKEN=$(curl -s -X POST http://localhost:8000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.token // .access_token // empty')

echo "Token: ${TOKEN:0:30}..."

# 4.2 Obtener usuarios
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8083/users | jq .

# 4.3 Crear nuevo usuario
curl -s -X POST http://localhost:8083/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com"}' | jq .

# 4.4 Obtener usuarios nuevamente
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8083/users | jq .
```

---

## BLOQUE 5: VERIFICAR LOGS

```bash
# Ver últimos logs de log-processor
docker-compose logs -f log-processor --tail=20

# Ver logs de usuarios-api
docker-compose logs users-api --tail=10

# Ver logs de todos los servicios
docker-compose logs | head -50
```

---

## BLOQUE 6: VALIDAR SECRETS MANAGEMENT

```bash
# 6.1 Verificar que .env está en .gitignore
grep ".env" .gitignore

# 6.2 Verificar que .env NO está en docker-compose.yml
if grep -q "RedisSecure2025!" docker-compose.yml; then
  echo "❌ PROBLEMA: Secret en docker-compose.yml"
else
  echo "✅ OK: No hay secrets en docker-compose.yml"
fi

# 6.3 Verificar que docker-compose usa variables
grep -c "REDIS_PASSWORD" docker-compose.yml
# Debe mostrar: 2 o más
```

---

## BLOQUE 7: VALIDAR GITHUB ACTIONS

```bash
# Ver workflow
cat .github/workflows/ci.yml | head -30

# Verificar que existe
ls -la .github/workflows/ci.yml
```

---

## 📱 COMANDOS PARA EL VIDEO

### Parte 1: Introducción (sin comandos, solo narrar)

### Parte 2: Startup
```bash
cd /workspaces/Taller1_Plataformas2
ls -la | grep .env
docker-compose down -v
docker-compose build
# [Esperar compilación]
docker-compose up -d
sleep 30
docker-compose ps
```

### Parte 3: Frontend (en navegador)
```
http://localhost:8080
Login: admin / admin
```

### Parte 4: Prometheus Queries
Copiar cada query en: `http://localhost:9090`

**Query 1:**
```promql
up
```

**Query 2:**
```promql
process_cpu_seconds_total
```

**Query 3:**
```promql
jvm_memory_usage_bytes
```

**Query 4:**
```promql
rate(http_requests_total[1m])
```

**Query 5:**
```promql
redis_connected_clients
```

### Parte 5: Demo Funcional

```bash
# Obtener token
TOKEN=$(curl -s -X POST http://localhost:8000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.token // .access_token // empty')

# Crear usuario
curl -s -X POST http://localhost:8083/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"juantest","email":"juantest@example.com"}' | jq .

# Ver logs
docker-compose logs log-processor | tail -10
```

### Parte 6: Grafana (en navegador)
```
http://localhost:3000
Login: admin / admin
```

### Parte 7: Resumen (solo narración)

---

## ✅ CHECKLIST DE EJECUCIÓN

```bash
# Ejecutar este script completo para validar todo

echo "=== CHECKLIST DE TESTING ==="
echo ""

echo "1. Servicios UP?"
docker-compose ps | grep -c "Up"
echo "   (Debe mostrar 9)"
echo ""

echo "2. Frontend accesible?"
curl -s http://localhost:8080 > /dev/null && echo "✅ Sí" || echo "❌ No"
echo ""

echo "3. Users API accesible?"
curl -s http://localhost:8083/actuator/health > /dev/null && echo "✅ Sí" || echo "❌ No"
echo ""

echo "4. Prometheus accesible?"
curl -s http://localhost:9090 > /dev/null && echo "✅ Sí" || echo "❌ No"
echo ""

echo "5. Prometheus targets UP?"
curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets | length'
echo "   (Debe mostrar 3 o más)"
echo ""

echo "6. Secrets protegidos?"
grep "\.env" .gitignore > /dev/null && echo "✅ .env en .gitignore" || echo "❌ PROBLEMA"
echo ""

echo "7. GitHub Actions setup?"
ls .github/workflows/ci.yml > /dev/null && echo "✅ Workflow creado" || echo "❌ PROBLEMA"
echo ""

echo "=== FIN CHECKLIST ==="
```

---

## 🚨 TROUBLESHOOTING

### Si un servicio no inicia:
```bash
# Ver logs específicos
docker-compose logs <servicio>

# Ej:
docker-compose logs users-api
docker-compose logs auth-api
docker-compose logs todos-api
```

### Si Prometheus targets están DOWN:
```bash
# Esperar 30 segundos más
sleep 30

# Verificar que servicios estén corriendo
docker-compose ps

# Ver logs de prometheus
docker-compose logs prometheus | tail -20
```

### Si no puedo autenticar:
```bash
# Verificar Auth API
curl -v http://localhost:8000/login

# Ver logs
docker-compose logs auth-api
```

### Si Redis no está accesible:
```bash
# Verificar Redis
docker-compose logs redis

# Probar conexión
docker-compose exec redis redis-cli -a RedisSecure2025! PING
```

---

## 📊 TIEMPOS ESTIMADOS

| Paso | Duración | Notas |
|------|----------|-------|
| Setup .env | 1 min | Copy-paste |
| docker-compose build | 10-15 min | Primera vez (después es más rápido) |
| docker-compose up -d | 1 min | Lanzar servicios |
| Esperar inicialización | 1 min | sleep 30 + verificación |
| Testing básico | 2 min | Curl commands |
| Video (si está todo UP) | 25-30 min | Narración + queries |
| **TOTAL PRIMERA VEZ** | **40-50 min** | Incluye compilación |
| **TOTAL EJECUCIONES SIGUIENTES** | **25-30 min** | Solo video |

---

## 🎬 TIPS PARA LA GRABACIÓN

1. **Ejecuta todo en orden** - Copiar-pega los bloques en orden
2. **Pausa mientras espera** - Narrar mientras compila/inicia
3. **Muestra el output** - Importante ver los comandos ejecutándose
4. **Mantén terminal visible** - Muestra que todo funciona
5. **Alterna entre terminal y navegador** - Muestra Frontend, Prometheus, Grafana

---

**READY TO TEST & RECORD** 🎥

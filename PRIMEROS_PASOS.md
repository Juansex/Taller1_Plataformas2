# Primeros Pasos Después de Clonar

## 1️⃣ Configurar Variables de Entorno

```bash
# Copiar template a .env
cp .env.example .env

# Revisar y ajustar si es necesario (opcional)
cat .env
```

> **Nota:** `.env` **NO se comitea al repo** (está en .gitignore). Cada usuario debe tener su propia copia con sus valores.

## 2️⃣ Levantar Servicios

```bash
# Opción A: Automática (recomendada)
./demo.sh

# Opción B: Manual
docker-compose build
docker-compose up -d

# Verificar que todos levantaron correctamente
docker-compose ps
# Debe mostrar 9 contenedores en estado "Up"
```

## 3️⃣ Acceder a los Servicios

### Frontend (Vue.js)
- **URL:** http://localhost:8080
- **User:** `admin`
- **Pass:** `admin`

### Prometheus (Métricas)
- **URL:** http://localhost:9090
- **Verifica:** http://localhost:9090/api/v1/targets
- Debe mostrar `users-api` y `redis-exporter` como "UP"

### Grafana (Dashboards)
- **URL:** http://localhost:3000
- **User:** `admin`
- **Pass:** `admin` (desde .env)
- Datasource pre-configurado: Prometheus

## 4️⃣ Pruebas Rápidas

```bash
# Health check de Users API
curl http://localhost:8083/actuator/health

# Ver métricas en Prometheus
curl http://localhost:9090/api/v1/query?query=up

# Ver logs de servicios específicos
docker-compose logs -f log-processor
docker-compose logs -f users-api
```

## 5️⃣ Detener Servicios

```bash
# Pausar sin eliminar
docker-compose stop

# Detener y eliminar contenedores
docker-compose down

# Limpiar TODO (volúmenes incluidos)
docker-compose down -v
```

---

## 📖 Documentación Recomendada

| Archivo | Propósito |
|---------|-----------|
| **REFERENCIA_RAPIDA.md** | Comandos más usados |
| **EVALUACION_CRITERIOS.md** | Cómo proyecto cumple criterios profesor |
| **GUION_VIDEO_CORREGIDO.md** | Demo en vivo (25-30 min) |
| **ARQUITECTURA_DIAGRAMAS.md** | Diagrama de servicios |
| **GUIA_DOCKER_COMPOSE.md** | Detalles técnicos cada servicio |
| **README.md** | Overview general |

---

## ✅ Validación Rápida

```bash
# Ejecutar esta secuencia para validar todo funciona:

echo "1. Limpiando..."
docker-compose down -v

echo "2. Preparando .env..."
cp .env.example .env

echo "3. Levantando servicios..."
docker-compose up -d

echo "4. Esperando inicio..."
sleep 30

echo "5. Verificando servicios..."
docker-compose ps

echo "6. Testeando endpoints..."
echo "Frontend:"
curl -s http://localhost:8080 | head -c 100

echo -e "\n\nUsers API:"
curl -s http://localhost:8083/actuator/health | jq .

echo -e "\n\nPrometheus targets:"
curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | {job: .labels.job, endpoint: .scrapeUrl, health: .health}'

echo -e "\n\n✅ Validación completada"
```

---

## 🐛 Troubleshooting

### Problema: "Error response from daemon: Ports are allocated"
```bash
# Cambiar puertos en .env:
# AUTH_API_PORT=8001
# SERVER_PORT=8084
# TODO_API_PORT=8085
```

### Problema: Servicios no se conectan a Redis
```bash
# Verificar que Redis está UP
docker-compose logs redis

# Verificar password en .env
grep REDIS_PASSWORD .env

# Resetear volúmenes
docker-compose down -v
docker-compose up -d
```

### Problema: Prometheus targets están "DOWN"
```bash
# Esperar 30 segundos a que servicios inicien
sleep 30

# Verificar que users-api está UP
curl http://localhost:8083/actuator/health

# Ver logs de prometheus
docker-compose logs prometheus
```

### Problema: Grafana no muestra datos
```bash
# Esperar a que Prometheus haya scrapeado al menos 1 vez
# Puede tardar hasta 2 minutos en la primera ejecución

# Verificar datasource en Grafana:
# Settings → Data Sources → Prometheus
# URL debe ser: http://prometheus:9090
```

---

## 📞 Contacto / Ayuda

Si algo no funciona:
1. Revisar logs: `docker-compose logs -f`
2. Verificar .env existe y tiene variables
3. Ver sección Troubleshooting arriba
4. Revisar EVALUACION_CRITERIOS.md para detalles técnicos

# Índice del Proyecto

# Índice de Documentación

## 📋 Documentos Prioritarios

### 🎓 **SI ERES EL PROFESOR**
→ **[PARA_EL_PROFESOR.md](PARA_EL_PROFESOR.md)** ⭐⭐⭐
   - Resumen de criterios cumplidos (todos 8)
   - Cómo ejecutar y evaluar
   - Verificación rápida (15 minutos)
   - Score final del proyecto

### 👨‍💻 **SI QUIERES EJECUTAR EL PROYECTO**
→ **[PRIMEROS_PASOS.md](PRIMEROS_PASOS.md)** ⭐⭐
   - Setup inicial (configurar .env)
   - Cómo levantar servicios (2 minutos)
   - URLs de acceso
   - Troubleshooting

### 📚 **REFERENCIAS TÉCNICAS**
1. **[EVALUACION_CRITERIOS.md](EVALUACION_CRITERIOS.md)** 📊
   - Mapeo detallado de criterios profesor
   - Ejemplos de código para cada criterio
   - Explicación técnica completa

2. **[REFERENCIA_RAPIDA.md](REFERENCIA_RAPIDA.md)**
   - Comandos Docker más usados
   - URLs de servicios
   - Casos de uso comunes

## 📚 Guías Técnicas

| Documento | Propósito | Contenido |
|-----------|-----------|-----------|
| **README.md** | Inicio rápido | Requisitos, instalación, ejecución básica |
| **GUION_VIDEO.md** | Presentación | Script completo paso a paso para grabación |
| **GUIA_DOCKER_COMPOSE.md** | Ejecución | Instrucciones detalladas para Docker Compose |
| **REFERENCIA_RAPIDA.md** | Consulta rápida | Comandos esenciales y URLs |
| **ARQUITECTURA_DIAGRAMAS.md** | Diseño técnico | Diagramas ASCII de arquitectura |

## Estructura del Proyecto

### Microservicios
- `auth-api/` - Servicio de autenticación en Go
- `users-api/` - API de usuarios en Java/Spring Boot
- `todos-api/` - API de tareas en Node.js
- `log-message-processor/` - Procesador de logs en Python
- `frontend/` - Interfaz gráfica en Vue.js

### Configuración
- `config/` - Archivos de configuración centralizados
  - `prometheus.yml` - Configuración de métricas
  - `grafana/` - Dashboards y datasources

### Otros
- `docker-compose.yml` - Definición de servicios Docker
- `demo.sh` - Script de demostración automática
- `LICENSE` - Licencia del proyecto
- `.gitignore` - Archivo de exclusiones Git

## Cómo Usar Este Proyecto

### Para Ejecución Local
1. Lee `README.md` para requisitos
2. Sigue `GUIA_DOCKER_COMPOSE.md` con `docker-compose up`
3. Consulta `REFERENCIA_RAPIDA.md` para comandos rápidos

### Para Presentación en Video
1. Lee `GUION_VIDEO.md` para el script
2. Practica con `demo.sh` o pasos en `GUIA_DOCKER_COMPOSE.md`
3. Consulta `REFERENCIA_RAPIDA.md` durante la grabación

### Para Entender la Arquitectura
1. Lee `README.md` para conceptos
2. Consulta `ARQUITECTURA_DIAGRAMAS.md` para visualización
3. Revisa `REFERENCIA_RAPIDA.md` para detalles técnicos

## Información Rápida

### Puertos
- Frontend: 8080
- Auth API: 8000
- Users API: 8083
- TODOs API: 8082
- Redis: 6379
- Prometheus: 9090
- Grafana: 3000

### Credenciales
- Frontend: admin / admin
- Grafana: admin / admin

### Tecnologías
- **Frontend**: Vue.js
- **Auth**: Go
- **Users**: Java/Spring Boot
- **TODOs**: Node.js/Express
- **Logs**: Python
- **Queue**: Redis
- **Monitoring**: Prometheus + Grafana

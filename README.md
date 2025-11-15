# Microservice App - Taller 1 Plataformas 2

Aplicación de ejemplo basada en arquitectura de microservicios para el curso de Plataformas 2. Este proyecto implementa una aplicación TODO utilizando 5 microservicios diferentes, cada uno construido con tecnologías distintas (Go, Java, Node.js, Python, Vue.js).

> 🎬 **Para demostración en video**: Consulta el archivo **[GUION_VIDEO.md](GUION_VIDEO.md)** que contiene un guion completo paso a paso para grabar tu presentación.

## 📋 Tabla de Contenidos
- [Componentes](#componentes)
- [Arquitectura](#arquitectura)
- [Requisitos](#requisitos)
- [Instalación y Configuración](#instalación-y-configuración)
- [Ejecución de los Servicios](#ejecución-de-los-servicios)
- [Pruebas](#pruebas)
- [Demostración Completa](#demostración-completa)
- [Solución de Problemas](#solución-de-problemas)

## 🏗️ Componentes

### 1. [Auth API](/auth-api) (Go)
Servicio de autenticación que genera tokens JWT.
- **Puerto**: 8000
- **Tecnología**: Go
- **Función**: Autenticación de usuarios y generación de tokens JWT

### 2. [Users API](/users-api) (Java/Spring Boot)
API de perfiles de usuario.
- **Puerto**: 8083
- **Tecnología**: Java con Spring Boot
- **Función**: Gestión de información de usuarios

### 3. [TODOs API](/todos-api) (Node.js)
API para operaciones CRUD sobre tareas TODO.
- **Puerto**: 8082
- **Tecnología**: Node.js con Express
- **Función**: Crear, leer, actualizar y eliminar tareas

### 4. [Log Message Processor](/log-message-processor) (Python)
Procesador de mensajes de cola Redis.
- **Tecnología**: Python
- **Función**: Procesar y mostrar logs de operaciones

### 5. [Frontend](/frontend) (Vue.js)
Interfaz de usuario web.
- **Puerto**: 8080
- **Tecnología**: Vue.js
- **Función**: Interfaz gráfica para la aplicación

## 🎯 Arquitectura

La aplicación sigue una arquitectura de microservicios donde cada componente es independiente y se comunica mediante APIs REST y colas de mensajes.

![microservice-app-example](/arch-img/Microservices.png)

**Flujo de datos:**
1. Usuario se autentica en **Auth API**
2. Obtiene token JWT para acceder a otros servicios
3. **Frontend** consume **TODOs API** y **Users API**
4. Las operaciones se registran en Redis
5. **Log Message Processor** procesa los mensajes de Redis

## 📦 Requisitos

### Herramientas Necesarias
- **Go** >= 1.18.2
- **Java** (OpenJDK 8 o superior)
- **Maven** (incluido en mvnw)
- **Node.js** >= 8.17.0
- **NPM** >= 6.13.4
- **Python** >= 3.6
- **pip3**
- **Redis** >= 7.0
- **Docker** (opcional, para ejecutar Redis)

## 🚀 Instalación y Configuración

### Paso 1: Clonar el Repositorio
```bash
git clone https://github.com/Juansex/Taller1_Plataformas2.git
cd Taller1_Plataformas2
```

### Paso 2: Iniciar Redis
Redis es necesario para **todos-api** y **log-message-processor**.

```bash
# Usando Docker (recomendado)
docker run -d -p 6379:6379 --name redis redis:7.0

# O instalar Redis localmente según tu sistema operativo
```

### Paso 3: Construir cada Microservicio

#### Auth API (Go)
```bash
cd auth-api
export GO111MODULE=on
go mod init github.com/bortizf/microservice-app-example/tree/master/auth-api
go mod tidy
go build
cd ..
```

#### Users API (Java)
```bash
cd users-api
./mvnw clean package -DskipTests
cd ..
```

#### TODOs API (Node.js)
```bash
cd todos-api
npm install
cd ..
```

#### Log Message Processor (Python)
```bash
cd log-message-processor
pip3 install -r requirements.txt
cd ..
```

#### Frontend (Vue.js)
```bash
cd frontend
npm install --legacy-peer-deps
cd ..
```

## ▶️ Ejecución de los Servicios

### Orden de Inicio Recomendado

#### 1. Users API (Puerto 8083)
```bash
cd users-api
JWT_SECRET=PRFT SERVER_PORT=8083 java -jar target/users-api-0.0.1-SNAPSHOT.jar
```

#### 2. Auth API (Puerto 8000)
```bash
cd auth-api
JWT_SECRET=PRFT AUTH_API_PORT=8000 USERS_API_ADDRESS=http://127.0.0.1:8083 ./auth-api
```

#### 3. TODOs API (Puerto 8082)
```bash
cd todos-api
JWT_SECRET=PRFT TODO_API_PORT=8082 REDIS_HOST=127.0.0.1 REDIS_PORT=6379 REDIS_CHANNEL=log_channel npm start
```

#### 4. Log Message Processor
```bash
cd log-message-processor
REDIS_HOST=127.0.0.1 REDIS_PORT=6379 REDIS_CHANNEL=log_channel python3 main.py
```

#### 5. Frontend (Puerto 8080)
```bash
cd frontend
PORT=8080 AUTH_API_ADDRESS=http://127.0.0.1:8000 TODOS_API_ADDRESS=http://127.0.0.1:8082 npm start
```

## 🧪 Pruebas

### Probar Auth API
```bash
# Login (devuelve un token JWT)
curl -X POST http://127.0.0.1:8000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

**Usuarios disponibles:**
- admin / admin
- johnd / foo
- janed / ddd

### Probar Users API
```bash
# Listar todos los usuarios
curl -X GET http://127.0.0.1:8083/users

# Obtener usuario específico (requiere token)
curl -X GET http://127.0.0.1:8083/users/admin \
  -H "Authorization: Bearer TU_TOKEN_JWT_AQUI"
```

### Probar TODOs API
```bash
# Crear un TODO (requiere token)
curl -X POST http://127.0.0.1:8082/todos \
  -H "Authorization: Bearer TU_TOKEN_JWT_AQUI" \
  -H "Content-Type: application/json" \
  -d '{"content":"Mi primera tarea"}'

# Listar TODOs (requiere token)
curl -X GET http://127.0.0.1:8082/todos \
  -H "Authorization: Bearer TU_TOKEN_JWT_AQUI"
```

### Probar Frontend
Abrir en el navegador: http://127.0.0.1:8080

## 🎯 Demostración Completa

### Flujo Completo de Trabajo

Para una demostración completa del sistema funcionando:

**1. Autenticarse y obtener token:**
```bash
TOKEN=$(curl -s -X POST http://127.0.0.1:8000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.token')
```

**2. Verificar usuarios disponibles:**
```bash
curl -X GET http://127.0.0.1:8083/users
```

**3. Crear varias tareas:**
```bash
# Tarea 1
curl -X POST http://127.0.0.1:8082/todos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Preparar presentación del proyecto"}'

# Tarea 2
curl -X POST http://127.0.0.1:8082/todos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Revisar documentación"}'

# Tarea 3
curl -X POST http://127.0.0.1:8082/todos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Hacer pruebas de integración"}'
```

**4. Ver todas las tareas creadas:**
```bash
curl -X GET http://127.0.0.1:8082/todos \
  -H "Authorization: Bearer $TOKEN"
```

**5. Observar los logs:**
- En la terminal del **Log Message Processor** verás cada operación CREATE que realizaste
- Cada mensaje incluye el tipo de operación, usuario y ID de la tarea

**6. Probar la interfaz web:**
- Abre http://127.0.0.1:8080 en tu navegador
- Inicia sesión con: **admin** / **admin**
- Verás las 3 tareas que creaste con curl
- Crea una nueva tarea desde la interfaz
- Marca una tarea como completada
- Elimina una tarea
- Observa cómo el Log Processor muestra cada operación en tiempo real

### Verificar que Todo Funciona

**Checklist de verificación:**
- [ ] Redis está corriendo (`docker ps`)
- [ ] Users API responde en puerto 8083
- [ ] Auth API responde en puerto 8000 y genera tokens
- [ ] TODOs API responde en puerto 8082 con operaciones CRUD
- [ ] Log Processor muestra logs de operaciones en consola
- [ ] Frontend carga en puerto 8080 y permite login
- [ ] Las operaciones desde el navegador aparecen en los logs

## 🔧 Solución de Problemas

### Frontend no compila (node-sass error)
**Problema**: Error de Python con node-sass
**Solución**: Usar el flag `--legacy-peer-deps`
```bash
cd frontend
npm install --legacy-peer-deps
```

### Error de conexión entre servicios
**Verificar**:
1. Todos los servicios están ejecutándose
2. Los puertos no están ocupados por otras aplicaciones
3. Las variables de entorno están correctamente configuradas
4. El JWT_SECRET es el mismo en todos los servicios (PRFT)

### Redis connection refused
**Verificar**:
```bash
# Verificar que Redis está corriendo
docker ps | grep redis

# O si está instalado localmente
redis-cli ping
# Debe responder: PONG
```

### Puerto ya en uso
```bash
# Ver qué proceso está usando el puerto
lsof -i :8080  # Cambiar por el puerto que necesites

# Detener el proceso
kill -9 PID
```

## 📚 Documentación Adicional

- **[Guion para Video Explicativo](GUION_VIDEO.md)** - Script completo paso a paso para grabar presentación
- [Documentación detallada de pruebas](VERIFICATION_RESULTS.md)
- [Auth API README](auth-api/README.md)
- [Users API README](users-api/README.md)
- [TODOs API README](todos-api/README.md)
- [Log Message Processor README](log-message-processor/README.md)
- [Frontend README](frontend/README.md)

## 👥 Créditos

- Proyecto base: [bortizf/microservice-app-example](https://github.com/bortizf/microservice-app-example)
- Referencia de implementación: [felipevelasco7/microservice-app-example](https://github.com/felipevelasco7/microservice-app-example)

## 📝 Licencia

MIT License - Ver archivo [LICENSE](LICENSE)
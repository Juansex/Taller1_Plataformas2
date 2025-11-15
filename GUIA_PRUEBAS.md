# 🧪 Guía de Pruebas - Taller 1 Plataformas 2

## ⚡ Inicio Rápido (Quick Start)

### Paso 1: Verificar Requisitos
```bash
# Go
go version

# Java
java -version

# Node.js y npm
node --version
npm --version

# Python
python3 --version

# Docker
docker --version
```

---

### Paso 2: Iniciar Redis (Requerido)
```bash
# Opción 1: Con Docker (Recomendado)
docker run -d -p 6379:6379 --name redis redis:7.0

# Verificar que está corriendo
docker ps | grep redis
```

---

## 🔨 Compilación de Servicios

### Paso 3: Compilar Users API (Java/Spring Boot)
```bash
cd users-api
./mvnw clean package -DskipTests
cd ..

# Verificar compilación
ls -lh users-api/target/users-api-0.0.1-SNAPSHOT.jar
```

---

### Paso 4: Compilar Auth API (Go)
```bash
cd auth-api
export GO111MODULE=on
go mod init github.com/bortizf/microservice-app-example/tree/master/auth-api
go mod tidy
go build
cd ..

# Verificar compilación
file auth-api/auth-api
```

---

### Paso 5: Instalar TODOs API (Node.js)
```bash
cd todos-api
npm install
cd ..

# Verificar instalación
ls -la todos-api/node_modules | head -10
```

---

### Paso 6: Preparar Log Message Processor (Python)
```bash
cd log-message-processor
pip3 install -r requirements.txt
cd ..

# Verificar instalación
pip3 list | grep redis
```

---

### Paso 7: Instalar Frontend (Vue.js)
```bash
cd frontend
npm install
cd ..

# Verificar instalación
ls -la frontend/node_modules | head -10
```

---

## 🚀 Ejecución de Servicios (Abre 5 terminales)

### Terminal 1: Auth API (Go - Puerto 8000)
```bash
cd auth-api
./auth-api
# Debería mostrar: "Listening on :8000"
```

---

### Terminal 2: Users API (Java - Puerto 8083)
```bash
cd users-api
java -jar target/users-api-0.0.1-SNAPSHOT.jar
# Debería mostrar: "Started UsersApiApplication in X seconds"
```

---

### Terminal 3: TODOs API (Node.js - Puerto 8082)
```bash
cd todos-api
npm start
# Debería mostrar: "Server running on port 8082"
```

---

### Terminal 4: Log Message Processor (Python)
```bash
cd log-message-processor
python3 main.py
# Debería mostrar: "Conectado a Redis y esperando mensajes..."
```

---

### Terminal 5: Frontend (Vue.js - Puerto 8080)
```bash
cd frontend
npm run dev
# Debería mostrar: "App running at: http://localhost:8080"
```

---

## ✅ Pruebas Funcionales

### Test 1: Verificar que todos los servicios están corriendo
```bash
# En una nueva terminal, ejecuta:

# Auth API
curl -s http://localhost:8000/health || echo "Auth API: NO RESPONDE"

# Users API
curl -s http://localhost:8083/health || echo "Users API: Revisar con curl -s http://localhost:8083/users"

# TODOs API
curl -s http://localhost:8082/health || echo "TODOs API: Revisar con curl -s http://localhost:8082/todos"

# Frontend
curl -s http://localhost:8080 | head -20
```

---

### Test 2: Autenticación (Auth API)
```bash
# Obtener token JWT para usuario admin
curl -X POST http://localhost:8000/auth \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# Debería retornar algo como:
# {"token":"eyJhbGc...","user":"admin"}

# Guardar el token en una variable para pruebas posteriores:
TOKEN="tu_token_aqui"
```

---

### Test 3: Usuarios (Users API - Puerto 8083)
```bash
# Obtener lista de usuarios
curl -s http://localhost:8083/users | jq '.'

# Debería retornar un JSON con usuarios como:
# [{"id":1,"name":"Admin","email":"admin@example.com",...}]
```

---

### Test 4: TODOs - Crear Tarea
```bash
TOKEN="tu_token_aqui"

# Crear una nueva tarea TODO
curl -X POST http://localhost:8082/todos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Completar proyecto","description":"Terminar pruebas"}'

# Debería retornar la tarea creada con un ID
```

---

### Test 5: TODOs - Listar Tareas
```bash
TOKEN="tu_token_aqui"

# Listar todas las tareas
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8082/todos | jq '.'

# Debería mostrar un array con todas las tareas
```

---

### Test 6: TODOs - Actualizar Tarea
```bash
TOKEN="tu_token_aqui"
TASK_ID="1"  # Reemplazar con un ID real

# Marcar tarea como completada
curl -X PUT http://localhost:8082/todos/$TASK_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"completed":true}'
```

---

### Test 7: TODOs - Eliminar Tarea
```bash
TOKEN="tu_token_aqui"
TASK_ID="1"  # Reemplazar con un ID real

# Eliminar una tarea
curl -X DELETE http://localhost:8082/todos/$TASK_ID \
  -H "Authorization: Bearer $TOKEN"
```

---

### Test 8: Verificar Logs en Tiempo Real
```bash
# En la Terminal 4 (Log Message Processor) deberías ver
# mensajes como:
# Operación: CREATE - usuario: admin - tarea: Completar proyecto
# Operación: UPDATE - usuario: admin - tarea_id: 1
# Operación: DELETE - usuario: admin - tarea_id: 1
```

---

## 🌐 Prueba en Frontend (Interfaz Gráfica)

1. Abre navegador: **http://localhost:8080**
2. **Login con**: 
   - Usuario: `admin`
   - Password: `admin`
3. Debería mostrar un formulario para crear TODOs
4. **Crear nueva tarea**: Escribe en el campo y presiona "Add Todo"
5. **Completar tarea**: Haz click en el checkbox de una tarea
6. **Eliminar tarea**: Haz click en el botón "Delete"
7. Verifica en Terminal 4 que aparecen los logs de cada operación

---

## 🐛 Solución de Problemas

### Redis no inicia
```bash
# Verificar si el puerto 6379 está en uso
lsof -i :6379
# O eliminar contenedor anterior
docker rm -f redis
# Y reiniciar
docker run -d -p 6379:6379 --name redis redis:7.0
```

### Auth API no compila
```bash
cd auth-api
rm -rf go.mod go.sum
go mod init github.com/bortizf/microservice-app-example/tree/master/auth-api
go mod tidy
go build
```

### Users API falla al iniciar
```bash
cd users-api
# Limpiar build anterior
./mvnw clean
# Recompilar
./mvnw clean package -DskipTests
# Ejecutar
java -jar target/users-api-0.0.1-SNAPSHOT.jar
```

### TODOs API: Error de conexión a Redis
```bash
cd todos-api
# Verificar que Redis está corriendo
redis-cli ping
# Debería responder: PONG
```

### Frontend: Puertos ocupados
```bash
# Cambiar puerto en frontend/config/index.js
# Buscar "port: 8080" y cambiar a otro puerto
```

---

## 📊 Checklist de Validación para el Video

- [ ] Redis corriendo (`docker ps`)
- [ ] Auth API respondiendo (`curl localhost:8000/health`)
- [ ] Users API respondiendo (`curl localhost:8083/users`)
- [ ] TODOs API respondiendo (`curl localhost:8082/health`)
- [ ] Frontend cargando (`http://localhost:8080`)
- [ ] Login funcionando (admin/admin)
- [ ] Crear TODO funciona
- [ ] Actualizar TODO funciona
- [ ] Eliminar TODO funciona
- [ ] Logs aparecen en Terminal 4
- [ ] Token JWT válido de Auth API
- [ ] Todos los servicios inician sin errores

---

## 🎬 Orden para el Video

1. **Mostrar requisitos**: `go version`, `java -version`, etc.
2. **Iniciar Redis**: `docker run ...`
3. **Abrir 5 terminales lado a lado**
4. **Iniciar servicios en este orden**: Auth → Users → TODOs → Logs → Frontend
5. **Esperar 5-10 segundos entre cada servicio** para que inicien correctamente
6. **Probar en Frontend**: Login → Create → Update → Delete
7. **Mostrar logs** en Terminal 4
8. **Hacer curl requests** si quieres mostrar backend directo
9. **Explicar arquitectura** mientras ves todo funcionando

---

## 📝 Notas Importantes

- **No inicies todos los servicios a la vez**: Espera a que cada uno esté listo
- **Guarda el TOKEN JWT**: Lo necesitarás para los curl requests
- **Verifica Redis antes de anything**: Es crítico para TODOs API
- **El Frontend puede tardar 30-60 segundos** la primera vez en iniciar
- **Los logs en Terminal 4 mostrarán** cada operación que hagas en tiempo real


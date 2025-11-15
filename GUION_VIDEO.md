# Guion para Video Explicativo - Taller 1 Plataformas 2

## Introducción al Video (2-3 minutos)

### **[ESCENA 1: Presentación]**
**Lo que debes decir:**
"Hola, en este video voy a demostrar la implementación de una aplicación basada en microservicios para el Taller 1 de Plataformas 2. Este proyecto implementa una arquitectura de microservicios completa con 5 servicios diferentes desarrollados en lenguajes distintos."

**Lo que debes mostrar:**
- Mostrar el repositorio en GitHub
- Mostrar la estructura de carpetas del proyecto

---

## Parte 1: Explicación de la Arquitectura (3-4 minutos)

### **[ESCENA 2: Arquitectura]**
**Lo que debes decir:**
"Esta aplicación está compuesta por 5 microservicios diferentes, cada uno desarrollado con tecnologías distintas para demostrar la flexibilidad de una arquitectura de microservicios:"

**Mostrar el diagrama de arquitectura y explicar:**

1. **Auth API (Go - Puerto 8000)**: 
   - "Este servicio maneja la autenticación de usuarios"
   - "Está escrito en Go y genera tokens JWT para autenticar las peticiones"
   - "Los usuarios disponibles son: admin/admin, johnd/foo, y janed/ddd"

2. **Users API (Java Spring Boot - Puerto 8083)**:
   - "Este servicio gestiona los perfiles de usuario"
   - "Está desarrollado con Java y Spring Boot"
   - "Permite obtener información de los usuarios del sistema"

3. **TODOs API (Node.js - Puerto 8082)**:
   - "Este es el servicio principal que maneja las tareas TODO"
   - "Está escrito en Node.js con Express"
   - "Permite crear, leer, actualizar y eliminar tareas"
   - "Cada operación se registra en una cola de Redis"

4. **Log Message Processor (Python)**:
   - "Este servicio procesa los logs de las operaciones"
   - "Está escrito en Python y escucha la cola de Redis"
   - "Imprime en consola cada operación que se realiza"

5. **Frontend (Vue.js - Puerto 8080)**:
   - "Esta es la interfaz de usuario"
   - "Está desarrollada con Vue.js"
   - "Permite a los usuarios interactuar con la aplicación de forma visual"

**Flujo de datos:**
"El flujo funciona así: El usuario se autentica en Auth API, obtiene un token JWT, usa ese token para acceder a TODOs API y Users API desde el Frontend. Cada operación importante se registra en Redis y el Log Processor las muestra en tiempo real."

---

## Parte 2: Preparación del Entorno (3-4 minutos)

### **[ESCENA 3: Verificar Requisitos]**
**Lo que debes decir:**
"Primero vamos a verificar que tenemos todas las herramientas necesarias instaladas."

**Comandos a ejecutar y mostrar:**

```bash
# Verificar Go
go version
# Debe mostrar: go version go1.x.x

# Verificar Java
java -version
# Debe mostrar: openjdk version "8" o superior

# Verificar Node.js
node --version
# Debe mostrar: v8.17.0 o superior

# Verificar npm
npm --version
# Debe mostrar: 6.13.4 o superior

# Verificar Python
python3 --version
# Debe mostrar: Python 3.6 o superior

# Verificar Docker (para Redis)
docker --version
# Debe mostrar: Docker version xx.x.x
```

**Explicar:**
"Como pueden ver, tengo todas las herramientas necesarias instaladas. Si alguna falta, deben instalarla antes de continuar."

---

### **[ESCENA 4: Iniciar Redis]**
**Lo que debes decir:**
"Redis es fundamental porque actúa como cola de mensajes entre TODOs API y el Log Processor. Vamos a iniciarlo con Docker:"

**Comando a ejecutar:**
```bash
docker run -d -p 6379:6379 --name redis redis:7.0
```

**Explicar:**
- "-d significa que se ejecuta en segundo plano"
- "-p 6379:6379 mapea el puerto de Redis"
- "--name redis le da un nombre al contenedor"

**Verificar que está corriendo:**
```bash
docker ps
```

**Explicar:**
"Como pueden ver, Redis está corriendo correctamente en el puerto 6379."

---

## Parte 3: Compilación de los Microservicios (8-10 minutos)

### **[ESCENA 5: Users API (Java)]**
**Lo que debes decir:**
"Empezaremos compilando el Users API que está en Java. Este proceso puede tomar un minuto porque Maven descarga todas las dependencias necesarias."

**Comandos a ejecutar:**
```bash
cd users-api
./mvnw clean package -DskipTests
```

**Explicar mientras compila:**
- "mvnw es Maven Wrapper, una herramienta que viene incluida"
- "clean package compila el código y genera el archivo JAR"
- "-DskipTests omite las pruebas para acelerar el proceso"

**Verificar el resultado:**
```bash
ls -lh target/users-api-0.0.1-SNAPSHOT.jar
```

**Explicar:**
"Como pueden ver, se generó un archivo JAR de aproximadamente 36 MB. Este es nuestro servicio compilado y listo para ejecutar."

```bash
cd ..
```

---

### **[ESCENA 6: Auth API (Go)]**
**Lo que debes decir:**
"Ahora vamos a compilar el Auth API que está escrito en Go. Go es muy rápido de compilar."

**Comandos a ejecutar:**
```bash
cd auth-api
export GO111MODULE=on
go mod init github.com/bortizf/microservice-app-example/tree/master/auth-api
go mod tidy
go build
```

**Explicar cada comando:**
- "GO111MODULE=on activa el sistema de módulos de Go"
- "go mod init inicializa el módulo"
- "go mod tidy descarga todas las dependencias necesarias"
- "go build compila el código y genera el ejecutable"

**Verificar el resultado:**
```bash
ls -lh auth-api
```

**Explicar:**
"Se generó un binario de aproximadamente 11 MB llamado 'auth-api'. Este es nuestro servicio compilado."

```bash
cd ..
```

---

### **[ESCENA 7: TODOs API (Node.js)]**
**Lo que debes decir:**
"El TODOs API usa Node.js, así que solo necesitamos instalar las dependencias con npm."

**Comandos a ejecutar:**
```bash
cd todos-api
npm install
```

**Explicar:**
"npm install descarga todas las librerías necesarias del proyecto. Verán algunas advertencias sobre paquetes deprecados, pero esto es normal en proyectos educativos."

**Mostrar el resultado:**
```bash
ls node_modules/ | wc -l
```

**Explicar:**
"Se instalaron 221 paquetes. Node.js usa muchas dependencias pequeñas, por eso el número es alto."

```bash
cd ..
```

---

### **[ESCENA 8: Log Message Processor (Python)]**
**Lo que debes decir:**
"El procesador de logs está en Python y también necesita instalar sus dependencias."

**Comandos a ejecutar:**
```bash
cd log-message-processor
pip3 install -r requirements.txt
```

**Explicar:**
"pip3 es el gestor de paquetes de Python. requirements.txt contiene la lista de librerías necesarias: redis, py_zipkin, y cython."

**Mostrar confirmación:**
"Como pueden ver, se instalaron correctamente: redis, py_zipkin, y cython."

```bash
cd ..
```

---

### **[ESCENA 9: Frontend (Vue.js)]**
**Lo que debes decir:**
"Finalmente, el Frontend en Vue.js. Este tiene un problema conocido con node-sass y Python 3, así que usamos un flag especial."

**Comandos a ejecutar:**
```bash
cd frontend
npm install --legacy-peer-deps
```

**Explicar:**
"--legacy-peer-deps le dice a npm que ignore ciertos conflictos de dependencias. Esto es necesario porque node-sass fue diseñado para Python 2 y nosotros tenemos Python 3."

**Mientras instala, explicar:**
"Este proceso puede tomar un par de minutos porque Vue.js tiene muchas dependencias para el desarrollo y compilación."

```bash
cd ..
```

**Resumen:**
"Perfecto, ya tenemos todos los 5 microservicios compilados y listos para ejecutar."

---

## Parte 4: Ejecución de los Microservicios (10-12 minutos)

### **[ESCENA 10: Preparar Terminales]**
**Lo que debes decir:**
"Para ejecutar todos los servicios simultáneamente, voy a abrir 5 terminales diferentes. Cada servicio se ejecuta en su propia terminal para que podamos ver los logs de cada uno."

**Mostrar:**
"Voy a organizar las terminales en mi pantalla de esta forma para que puedan ver todo claramente."

---

### **[ESCENA 11: Iniciar Users API - Terminal 1]**
**Lo que debes decir:**
"Comenzamos con Users API en el puerto 8083. Este debe iniciarse primero porque Auth API lo necesita."

**Terminal 1 - Comando:**
```bash
cd users-api
JWT_SECRET=PRFT SERVER_PORT=8083 java -jar target/users-api-0.0.1-SNAPSHOT.jar
```

**Explicar:**
- "JWT_SECRET=PRFT es la clave secreta para validar tokens. Todos los servicios usan la misma"
- "SERVER_PORT=8083 define el puerto donde escucha"
- "Ejecutamos el JAR que compilamos anteriormente"

**Esperar a que inicie y mostrar:**
"Como pueden ver, Spring Boot está iniciando... ahí está, ya está corriendo en el puerto 8083."

---

### **[ESCENA 12: Iniciar Auth API - Terminal 2]**
**Lo que debes decir:**
"Ahora iniciamos Auth API en el puerto 8000."

**Terminal 2 - Comando:**
```bash
cd auth-api
JWT_SECRET=PRFT AUTH_API_PORT=8000 USERS_API_ADDRESS=http://127.0.0.1:8083 ./auth-api
```

**Explicar:**
- "AUTH_API_PORT=8000 es el puerto del servicio"
- "USERS_API_ADDRESS apunta al Users API que ya iniciamos"
- "Esto permite que Auth API valide usuarios contra Users API"

**Mostrar:**
"El servicio está corriendo. Como ven, Go es muy rápido y ligero."

---

### **[ESCENA 13: Iniciar TODOs API - Terminal 3]**
**Lo que debes decir:**
"Ahora el servicio más importante: TODOs API en el puerto 8082."

**Terminal 3 - Comando:**
```bash
cd todos-api
JWT_SECRET=PRFT TODO_API_PORT=8082 REDIS_HOST=127.0.0.1 REDIS_PORT=6379 REDIS_CHANNEL=log_channel npm start
```

**Explicar:**
- "TODO_API_PORT=8082 es su puerto"
- "REDIS_HOST y REDIS_PORT apuntan a nuestro Redis"
- "REDIS_CHANNEL=log_channel es el canal donde publica los logs"
- "npm start ejecuta el servidor Node.js"

**Mostrar:**
"Perfecto, TODOs API está escuchando en el puerto 8082."

---

### **[ESCENA 14: Iniciar Log Message Processor - Terminal 4]**
**Lo que debes decir:**
"Ahora iniciamos el procesador de logs que escucha Redis."

**Terminal 4 - Comando:**
```bash
cd log-message-processor
REDIS_HOST=127.0.0.1 REDIS_PORT=6379 REDIS_CHANNEL=log_channel python3 main.py
```

**Explicar:**
"Este servicio se conecta al mismo canal de Redis que TODOs API. Cuando TODOs API registre una operación, este servicio lo mostrará aquí."

**Mostrar:**
"Como ven, está esperando mensajes. Por ahora no hay nada porque no hemos creado ninguna tarea todavía."

---

### **[ESCENA 15: Iniciar Frontend - Terminal 5]**
**Lo que debes decir:**
"Finalmente, iniciamos el Frontend en el puerto 8080."

**Terminal 5 - Comando:**
```bash
cd frontend
PORT=8080 AUTH_API_ADDRESS=http://127.0.0.1:8000 TODOS_API_ADDRESS=http://127.0.0.1:8082 npm start
```

**Explicar:**
- "PORT=8080 es donde estará disponible la interfaz web"
- "AUTH_API_ADDRESS y TODOS_API_ADDRESS conectan con nuestros servicios"
- "npm start inicia el servidor de desarrollo de Vue"

**Esperar a que compile:**
"Vue necesita compilar todos los componentes... ahí está, ya terminó."

---

## Parte 5: Demostración de Funcionalidad (8-10 minutos)

### **[ESCENA 16: Probar Auth API con curl]**
**Lo que debes decir:**
"Ahora vamos a probar que todo funciona. Empezamos autenticándonos para obtener un token JWT."

**Nueva terminal - Comando:**
```bash
curl -X POST http://127.0.0.1:8000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

**Explicar:**
- "Hacemos un POST al endpoint /login"
- "Enviamos las credenciales de usuario admin"

**Mostrar el resultado:**
"Como pueden ver, recibimos un token JWT. Este token es lo que nos permite acceder a los otros servicios. Voy a copiar este token para usarlo en las siguientes pruebas."

**Copiar el token mostrado en pantalla**

---

### **[ESCENA 17: Probar Users API]**
**Lo que debes decir:**
"Ahora probamos Users API para ver la lista de usuarios."

**Comando (sin token):**
```bash
curl -X GET http://127.0.0.1:8083/users
```

**Explicar:**
"Como ven, nos devuelve la lista de usuarios en formato JSON. Hay 3 usuarios: admin, johnd, y janed, cada uno con su ID y rol."

**También probar con token si se requiere autenticación:**
```bash
curl -X GET http://127.0.0.1:8083/users/admin \
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

---

### **[ESCENA 18: Probar TODOs API - Crear TODO]**
**Lo que debes decir:**
"Ahora vamos a crear una tarea en TODOs API. Esto es importante porque activará el log processor."

**Comando:**
```bash
curl -X POST http://127.0.0.1:8082/todos \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{"content":"Preparar presentación para el profesor"}'
```

**Explicar:**
- "Usamos el token que obtuvimos de Auth API"
- "Enviamos el contenido de la tarea en JSON"

**Mostrar:**
"Perfecto, la tarea se creó. Observen ahora la terminal del Log Message Processor..."

**Cambiar a la terminal 4 del Log Processor:**
"¡Aquí está! El procesador de logs detectó que se creó una tarea y lo registró. Esto demuestra que la comunicación entre TODOs API y el Log Processor a través de Redis funciona correctamente."

---

### **[ESCENA 19: Probar TODOs API - Listar TODOs]**
**Lo que debes decir:**
"Vamos a verificar que nuestra tarea se guardó correctamente."

**Comando:**
```bash
curl -X GET http://127.0.0.1:8082/todos \
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

**Mostrar:**
"Aquí está nuestra tarea con su ID, contenido, y el estado completed en false, porque aún no la hemos completado."

---

### **[ESCENA 20: Crear más TODOs]**
**Lo que debes decir:**
"Voy a crear un par de tareas más para demostrar mejor la funcionalidad."

**Comandos:**
```bash
curl -X POST http://127.0.0.1:8082/todos \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{"content":"Revisar documentación del proyecto"}'

curl -X POST http://127.0.0.1:8082/todos \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{"content":"Implementar pruebas unitarias"}'
```

**Mostrar la terminal del Log Processor:**
"Cada operación aparece inmediatamente en el log processor. Esto es útil para auditoría y monitoreo en producción."

---

### **[ESCENA 21: Probar Frontend en el Navegador]**
**Lo que debes decir:**
"Ahora vamos a ver la interfaz gráfica. Abro el navegador en http://127.0.0.1:8080"

**Abrir el navegador y mostrar:**
1. "Aquí está la página de login. Voy a entrar con admin/admin"
2. **Hacer login**
3. "Como ven, ahora estoy en la vista principal. Aquí aparecen las 3 tareas que creamos con curl"
4. "Puedo crear una nueva tarea desde aquí... la creo... y ven que aparece inmediatamente"
5. **Volver a la terminal del Log Processor:** "Y aquí está el log de esa operación"
6. "También puedo marcar una tarea como completada... listo"
7. "Y puedo eliminar una tarea... observen el log processor de nuevo... ahí está el log de DELETE"

**Explicar:**
"Todo funciona perfectamente. La interfaz web se comunica con los servicios backend, y cada operación importante se registra automáticamente."

---

### **[ESCENA 22: Verificar Logs en todas las Terminales]**
**Lo que debes decir:**
"Vamos a revisar rápidamente todas las terminales para ver los logs de cada servicio."

**Mostrar cada terminal:**
1. **Users API**: "Aquí vemos las peticiones GET que hicimos"
2. **Auth API**: "Aquí está el registro del login que hicimos"
3. **TODOs API**: "Todas las operaciones CRUD aparecen aquí"
4. **Log Processor**: "Y aquí el resumen de todas las operaciones importantes"
5. **Frontend**: "Los logs de compilación y servicio de Vue"

---

## 🎓 Parte 6: Explicación Técnica y Cierre (3-5 minutos)

### **[ESCENA 23: Explicación de la Corrección Realizada]**
**Lo que debes decir:**
"Ahora les explico qué problema tenía el proyecto original y cómo lo corregimos."

**Puntos a cubrir:**
1. "El repositorio original contenía un proyecto llamado 'clean-service' que no correspondía a la arquitectura de microservicios esperada"
2. "Lo que hicimos fue reemplazar completamente ese proyecto con la estructura correcta basada en microservice-app-example"
3. "Ahora tenemos 5 microservicios independientes, cada uno con su tecnología específica"
4. "Esta arquitectura es mucho más flexible y escalable"

---

### **[ESCENA 24: Ventajas de esta Arquitectura]**
**Lo que debes decir:**
"Esta arquitectura de microservicios tiene varias ventajas importantes:"

1. **Independencia tecnológica**: "Cada servicio puede usar la tecnología más apropiada. Por ejemplo, Go para autenticación por su velocidad, Java para gestión de datos por su robustez, Node.js para APIs REST por su simplicidad"

2. **Escalabilidad**: "Si TODOs API recibe mucho tráfico, podemos escalar solo ese servicio sin tocar los demás"

3. **Mantenibilidad**: "Cada equipo puede trabajar en un servicio diferente sin afectar a los otros"

4. **Resiliencia**: "Si un servicio falla, los demás pueden seguir funcionando"

---

### **[ESCENA 25: Documentación]**
**Lo que debes decir:**
"Todo el proyecto está completamente documentado:"

**Mostrar en pantalla:**
- "El README.md principal tiene instrucciones completas de instalación y ejecución"
- "Cada microservicio tiene su propio README con detalles específicos"
- "Hay un archivo VERIFICATION_RESULTS.md con los resultados de las pruebas"
- "Y este guion que estoy siguiendo está también en el repositorio para que cualquiera pueda replicar la demostración"

---

### **[ESCENA 26: Cierre y Conclusiones]**
**Lo que debes decir:**
"En resumen, hemos demostrado:"

1.  "Que corregimos exitosamente la estructura del proyecto"
2.  "Que compilamos e instalamos los 5 microservicios"
3.  "Que todos los servicios se ejecutan correctamente"
4.  "Que la comunicación entre servicios funciona"
5.  "Que tanto las APIs REST como la interfaz gráfica operan correctamente"
6. "Que el sistema de logs con Redis funciona en tiempo real"

"El proyecto está completamente funcional y listo para producción. Toda la documentación está disponible en el repositorio de GitHub."

---

### **[ESCENA 27: Detener Servicios]**
**Lo que debes decir:**
"Para finalizar la demostración, voy a detener todos los servicios de forma ordenada."

**En cada terminal, presionar Ctrl+C:**
- Terminal 5 (Frontend): Ctrl+C
- Terminal 4 (Log Processor): Ctrl+C
- Terminal 3 (TODOs API): Ctrl+C
- Terminal 2 (Auth API): Ctrl+C
- Terminal 1 (Users API): Ctrl+C

**Detener Redis:**
```bash
docker stop redis
docker rm redis
```

**Explicar:**
"Y con esto limpiamos todo el entorno. Para volver a ejecutar, simplemente seguimos los mismos pasos que mostramos en este video."

---

## Notas Finales para el Video

### Duración Total Estimada: 30-40 minutos

### Tips de Grabación:
1. **Habla claramente y pausadamente**
2. **Espera a que los comandos terminen antes de continuar**
3. **Si algo falla, no edites, explica qué pasó y cómo solucionarlo**
4. **Mantén el zoom apropiado para que el texto sea legible**
5. **Usa un fondo limpio en tu pantalla**

### Estructura de Pantalla Recomendada:
- **Parte Superior**: Navegador o ventana principal
- **Parte Inferior**: Terminales organizadas (2-3 visibles simultáneamente)

### Orden de Importancia de las Escenas:
1. **Críticas**: 1, 2, 5-9, 11-15, 16-20 (DEBEN estar en el video)
2. **Importantes**: 3, 4, 10, 21, 23, 24 (Altamente recomendadas)
3. **Complementarias**: 22, 25, 26, 27 (Opcionales pero valiosas)

---

## Checklist Pre-Grabación

Antes de empezar a grabar, verifica que tienes:
- [ ] Todas las herramientas instaladas (Go, Java, Node, Python, Docker)
- [ ] Redis descargado (docker pull redis:7.0)
- [ ] Repositorio clonado y actualizado
- [ ] Terminales preparadas
- [ ] Este guion impreso o en otra pantalla para referencia
- [ ] Buena iluminación y audio
- [ ] Notificaciones del sistema desactivadas

---

**¡Buena suerte con tu video! **

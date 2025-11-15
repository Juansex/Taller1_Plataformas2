#  Integración Kubernetes + Prometheus + Grafana

##  Análisis del Repositorio Original

El repositorio de referencia (`felipevelasco7/microservice-app-example`) incluye:

###  Estructura Implementada
```
microservice-app-example/
├── kubernetes/              # Configuración K8s
│   ├── auth-api.yaml
│   ├── users-api.yaml
│   ├── todos-api.yaml
│   ├── log-processor.yaml
│   ├── frontend.yaml
│   ├── redis.yaml
│   ├── prometheus.yaml
│   └── grafana.yaml
├── docker/                  # Dockerfiles
│   ├── Dockerfile.auth
│   ├── Dockerfile.users
│   ├── Dockerfile.todos
│   ├── Dockerfile.log-processor
│   └── Dockerfile.frontend
├── prometheus/              # Configuración Prometheus
│   └── prometheus.yml
├── grafana/                 # Dashboards Grafana
│   ├── provisioning/
│   └── dashboards/
└── docker-compose.yml       # Orquestación local
```

---

##  Recomendación: Híbrido (Local + Kubernetes)

Dado que tu proyecto necesita ser presentado en video, te recomiendo un **enfoque híbrido**:

### Fase 1: Prueba Local (Docker Compose)
- Ejecutar todo con `docker-compose` para validar funcionalidad
- Más rápido, sin configuración K8s compleja
- Incluir Prometheus + Grafana en mismo compose

### Fase 2: Kubernetes (Minikube)
- Desplegar en Kubernetes para demostrar scalability
- Mostrar monitoreo en Grafana
- Mejor para la presentación final

---

##  Plan de Acción Necesario

### 1. Crear Dockerfiles (para cada servicio)
### 2. Crear docker-compose.yml (con Prometheus + Grafana)
### 3. Crear manifiestos Kubernetes
### 4. Configurar Prometheus
### 5. Configurar Grafana con dashboards

---

##  PASO 1: Crear Dockerfiles

### 1.1 Dockerfile para Auth API (Go)

Crea: `auth-api/Dockerfile`

```dockerfile
FROM golang:1.21-alpine AS builder

WORKDIR /app
COPY . .

RUN export GO111MODULE=on && \
    go mod init github.com/bortizf/microservice-app-example/tree/master/auth-api && \
    go mod tidy && \
    go build -o auth-api .

FROM alpine:latest

WORKDIR /root/
COPY --from=builder /app/auth-api .

EXPOSE 8000

CMD ["./auth-api"]
```

**Explicación:**
- `FROM golang:1.21-alpine AS builder` → Imagen base para compilar (builder stage)
- `COPY` → Copia el código fuente
- `go build` → Compila el binario
- `FROM alpine:latest` → Imagen final (más pequeña)
- `COPY --from=builder` → Copia el binario compilado a la imagen final
- `EXPOSE 8000` → Expone el puerto 8000
- `CMD` → Comando para ejecutar

---

### 1.2 Dockerfile para Users API (Java)

Crea: `users-api/Dockerfile`

```dockerfile
FROM maven:3.8-openjdk-11-slim AS builder

WORKDIR /app
COPY . .

RUN ./mvnw clean package -DskipTests

FROM openjdk:11-jre-slim

WORKDIR /app
COPY --from=builder /app/target/users-api-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Explicación:**
- Stage 1: Compila con Maven
- Stage 2: Solo incluye JRE (más pequeño que JDK) + JAR compilado
- `ENTRYPOINT` vs `CMD`: ENTRYPOINT es la forma recomendada para aplicaciones Java

---

### 1.3 Dockerfile para TODOs API (Node.js)

Crea: `todos-api/Dockerfile`

```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .

EXPOSE 8082

CMD ["npm", "start"]
```

**Explicación:**
- `npm ci` → Clean Install (más seguro que npm install para producción)
- `--only=production` → Solo instala dependencias de producción
- Node.js en Alpine es más pequeño

---

### 1.4 Dockerfile para Log Processor (Python)

Crea: `log-message-processor/Dockerfile`

```dockerfile
FROM python:3.10-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["python3", "main.py"]
```

**Explicación:**
- `--no-cache-dir` → No cachea los .whl descargados (más pequeño)
- Copia requirements.txt antes del código para aprovechar capas de Docker

---

### 1.5 Dockerfile para Frontend (Vue.js)

Crea: `frontend/Dockerfile`

```dockerfile
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
```

**Explicación:**
- Stage 1: Compila Vue.js
- Stage 2: Sirve archivos estáticos con Nginx
- `npm run build` → Genera archivos optimizados en `/dist`

Crea: `frontend/nginx.conf`

```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    client_max_body_size 20M;

    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript 
               application/json application/javascript application/xml+rss;

    server {
        listen 8080;
        server_name _;

        location / {
            root /usr/share/nginx/html;
            try_files $uri $uri/ /index.html;
        }

        location ~ \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            root /usr/share/nginx/html;
            expires 30d;
            add_header Cache-Control "public, immutable";
        }
    }
}
```

---

##  PASO 2: Crear docker-compose.yml (Con Prometheus + Grafana)

Crea: `docker-compose.yml` en la raíz

```yaml
version: '3.8'

services:
  # Redis - Cola de mensajes
  redis:
    image: redis:7.0-alpine
    container_name: redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 5
    networks:
      - microservices

  # Auth API (Go)
  auth-api:
    build:
      context: ./auth-api
      dockerfile: Dockerfile
    container_name: auth-api
    ports:
      - "8000:8000"
    depends_on:
      - redis
    environment:
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - microservices
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8000/health"]
      interval: 10s
      timeout: 5s
      retries: 3

  # Users API (Java)
  users-api:
    build:
      context: ./users-api
      dockerfile: Dockerfile
    container_name: users-api
    ports:
      - "8083:8083"
    depends_on:
      redis:
        condition: service_healthy
    environment:
      - SPRING_APPLICATION_NAME=users-api
      - SERVER_PORT=8083
      - REDIS_HOST=redis
    networks:
      - microservices
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8083/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 3

  # TODOs API (Node.js)
  todos-api:
    build:
      context: ./todos-api
      dockerfile: Dockerfile
    container_name: todos-api
    ports:
      - "8082:8082"
    depends_on:
      redis:
        condition: service_healthy
    environment:
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - PORT=8082
      - NODE_ENV=production
    networks:
      - microservices
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8082/health"]
      interval: 10s
      timeout: 5s
      retries: 3

  # Log Message Processor (Python)
  log-processor:
    build:
      context: ./log-message-processor
      dockerfile: Dockerfile
    container_name: log-processor
    depends_on:
      redis:
        condition: service_healthy
    environment:
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - microservices

  # Frontend (Vue.js)
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: frontend
    ports:
      - "8080:8080"
    depends_on:
      - auth-api
      - users-api
      - todos-api
    networks:
      - microservices

  # Prometheus - Recolector de métricas
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    networks:
      - microservices
    depends_on:
      - auth-api
      - users-api
      - todos-api

  # Grafana - Dashboard de monitoreo
  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_SECURITY_ADMIN_USER=admin
    volumes:
      - grafana_data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning
    depends_on:
      - prometheus
    networks:
      - microservices

volumes:
  redis_data:
  prometheus_data:
  grafana_data:

networks:
  microservices:
    driver: bridge
```

**Explicación:**
- `healthcheck` → Docker verifica que los servicios están saludables antes de iniciar dependientes
- `depends_on` → Define el orden de inicialización
- `volumes` → Persiste datos entre reinicios
- `networks` → Permite comunicación entre contenedores por nombre (redis, auth-api, etc)

---

##  PASO 3: Configurar Prometheus

Crea: `prometheus/prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    monitor: 'microservices-monitor'

alerting:
  alertmanagers:
    - static_configs:
        - targets: []

rule_files: []

scrape_configs:
  # Auth API (Go)
  - job_name: 'auth-api'
    static_configs:
      - targets: ['auth-api:8000']
    metrics_path: '/metrics'
    scrape_interval: 10s

  # Users API (Java/Spring Boot)
  - job_name: 'users-api'
    static_configs:
      - targets: ['users-api:8083']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  # TODOs API (Node.js)
  - job_name: 'todos-api'
    static_configs:
      - targets: ['todos-api:8082']
    metrics_path: '/metrics'
    scrape_interval: 10s

  # Prometheus mismo
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
```

**Explicación:**
- `scrape_interval` → Cada cuánto recolecta métricas (15s globalmente)
- `job_name` → Nombre del servicio a monitorear
- `targets` → Dirección del servicio
- `metrics_path` → Endpoint donde expone las métricas
- Nota: Cada servicio expone métricas en diferente endpoint según tecnología

---

## 📈 PASO 4: Configurar Grafana

Crea: `grafana/provisioning/datasources/prometheus.yml`

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
```

**Explicación:**
- Define Prometheus como fuente de datos automáticamente
- Grafana lo carga al iniciar sin configuración manual

---

Crea: `grafana/provisioning/dashboards/main.json`

Este es un dashboard complejo. Aquí una versión simplificada:

```json
{
  "dashboard": {
    "title": "Microservices Monitor",
    "tags": ["microservices"],
    "timezone": "browser",
    "panels": [
      {
        "title": "HTTP Requests per Second",
        "targets": [
          {
            "expr": "rate(http_requests_total[1m])",
            "legendFormat": "{{job}} - {{method}}"
          }
        ],
        "type": "graph"
      },
      {
        "title": "Error Rate",
        "targets": [
          {
            "expr": "rate(http_requests_total{status=~\"5..\"}[1m])",
            "legendFormat": "{{job}}"
          }
        ],
        "type": "graph"
      },
      {
        "title": "Response Time (p95)",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
            "legendFormat": "{{job}}"
          }
        ],
        "type": "graph"
      },
      {
        "title": "Container Memory Usage",
        "targets": [
          {
            "expr": "container_memory_usage_bytes",
            "legendFormat": "{{name}}"
          }
        ],
        "type": "graph"
      }
    ]
  }
}
```

---

##  PASO 5: Construir e Iniciar con Docker Compose

### Comando 5.1: Construir todas las imágenes
```bash
docker-compose build
```

**Explicación**: Construye las imágenes Docker para todos los servicios basados en sus Dockerfiles.

**Duración**: 5-10 minutos (descarga dependencias)

**Salida esperada**:
```
Building auth-api
Building users-api
Building todos-api
Building log-processor
Building frontend
```

---

### Comando 5.2: Iniciar todos los servicios
```bash
docker-compose up
```

**Explicación**: Levanta todos los contenedores en orden de dependencias.

**Esperar a que muestre**: 
```
prometheus | msg="Server is ready to receive requests" 
grafana | logger=server msg="HTTP Server Listen"
```

---

### Comando 5.3: Verificar que todo está corriendo
```bash
docker-compose ps
```

**Salida esperada**:
```
NAME                COMMAND                  STATE
redis               "redis-server"           Up (healthy)
auth-api            "./auth-api"             Up (healthy)
users-api           "java -jar app.jar"      Up (healthy)
todos-api           "npm start"              Up (healthy)
log-processor       "python3 main.py"        Up
frontend            "nginx -g daemon off"    Up
prometheus          "/bin/prometheus"        Up
grafana             "/run.sh"                Up
```

---

##  Acceso a Servicios

Una vez levantado con `docker-compose up`:

| Servicio | URL | Usuario | Contraseña |
|----------|-----|---------|------------|
| Frontend | http://localhost:8080 | admin | admin |
| Prometheus | http://localhost:9090 | - | - |
| Grafana | http://localhost:3000 | admin | admin |
| Auth API | http://localhost:8000 | - | - |
| Users API | http://localhost:8083 | - | - |
| TODOs API | http://localhost:8082 | - | - |
| Redis | localhost:6379 | - | - |

---

## ☸️ PASO 6: Desplegar en Kubernetes (Opcional - Para Presentación)

Una vez validado con Docker Compose, puedes desplegar en Kubernetes:

### Requisito: Instalar Minikube
```bash
curl -LO https://github.com/kubernetes/minikube/releases/latest/download/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube
minikube start
```

### Crear manifiestos Kubernetes

Crea: `kubernetes/auth-api-deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-api
  namespace: microservices
spec:
  replicas: 2
  selector:
    matchLabels:
      app: auth-api
  template:
    metadata:
      labels:
        app: auth-api
    spec:
      containers:
      - name: auth-api
        image: microservices/auth-api:latest
        imagePullPolicy: Never
        ports:
        - containerPort: 8000
        env:
        - name: REDIS_HOST
          value: redis-service
        - name: REDIS_PORT
          value: "6379"
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /health
            port: 8000
          initialDelaySeconds: 10
          periodSeconds: 10

---
apiVersion: v1
kind: Service
metadata:
  name: auth-api-service
  namespace: microservices
spec:
  type: ClusterIP
  selector:
    app: auth-api
  ports:
  - port: 8000
    targetPort: 8000
```

**Explicación:**
- `replicas: 2` → Ejecuta 2 instancias (alta disponibilidad)
- `resources` → Limita memoria y CPU
- `livenessProbe` → Kubernetes reinicia el pod si falla
- `Service` → Expone el Deployment internamente

---

##  Métricas en Grafana

Una vez en Grafana (http://localhost:3000):

1. Ve a **Configuration** → **Data Sources**
2. Prometheus ya está configurado automáticamente
3. Ve a **+** → **Import**
4. Importa dashboards públicos:
   - ID: `3662` → Prometheus Overview
   - ID: `1860` → Node Exporter Full
   - ID: `6417` → Kubernetes Cluster Monitoring

---

##  Flujo para el Video

1. **Mostrar docker-compose.yml** - Explicar arquitectura
2. **`docker-compose build`** - Compilar imágenes
3. **`docker-compose up`** - Levantar servicios
4. **Login en Frontend** - Crear TODOs
5. **Abrir Prometheus** (localhost:9090) - Mostrar métricas en tiempo real
6. **Abrir Grafana** (localhost:3000) - Mostrar dashboards de monitoreo
7. **Hacer operaciones en Frontend** - Ver cómo cambian las gráficas en Grafana

---

## 🔗 Próximos Pasos

1. **Agregar expositores de métricas** a cada servicio:
   - Go: `prometheus/promhttp`
   - Java: Spring Boot Actuator (ya incluido)
   - Node.js: `prom-client`
   - Python: `prometheus_client`

2. **Crear alertas** en Prometheus/Grafana

3. **Desplegar en Kubernetes** con manifiestos

4. **Usar Helm** para templating de K8s

---

¿Necesitas que detalle alguno de estos pasos?


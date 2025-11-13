# Etapa 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (se cachea esta capa)
RUN mvn dependency:go-offline -B || true

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=builder /app/target/clean-service-1.0.0.jar app.jar

# Exponer puerto
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/tasks || exit 1

# Punto de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM maven:3.8-openjdk-11-slim AS builder

WORKDIR /app
COPY . .

RUN ./mvnw clean package -DskipTests

FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=builder /app/target/users-api-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]

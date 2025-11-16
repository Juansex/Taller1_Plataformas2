FROM maven:3.8-eclipse-temurin-11 AS builder

WORKDIR /app
COPY . .

RUN bash mvnw clean package -DskipTests

FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/users-api-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]

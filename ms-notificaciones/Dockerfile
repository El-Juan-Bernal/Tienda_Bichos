# --- Etapa 1: Construcción ---
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# --- Etapa 2: Ejecución ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]


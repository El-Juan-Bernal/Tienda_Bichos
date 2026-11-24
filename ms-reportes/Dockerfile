# --- Etapa 1: Construcción ---
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# --- Etapa 2: Ejecución ---
# Imagen basada en Ubuntu (no Alpine): Kafka Streams usa RocksDB, una librería nativa
# que se lleva mejor con glibc que con la musl de Alpine.
FROM eclipse-temurin:25-jre
WORKDIR /app

# /data guarda el estado de Kafka Streams (se monta como volumen en el compose)
RUN groupadd -r spring && useradd -r -g spring spring \
 && mkdir -p /data/estado-streams \
 && chown -R spring:spring /data
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]

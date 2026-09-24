# --- Etapa 1: Construcción (Build) ---
# Usamos una imagen con Maven y Java (asumiendo Java 17, si usas 21, cámbialo aquí)
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copiamos el pom.xml y descargamos dependencias (mejora el caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y compilamos el .jar
COPY src ./src
RUN mvn clean package -DskipTests

# --- Etapa 2: Ejecución (Run) ---
# Usamos una imagen más ligera solo con el entorno de ejecución de Java
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copiamos el .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto de Spring Boot
EXPOSE 8080

# Comando para arrancar el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]

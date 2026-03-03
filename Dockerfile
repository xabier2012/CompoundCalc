# ---- Stage 1: Build (Usando imagen oficial de Maven) ----
FROM maven:3.9.6-eclipse-temurin-22 AS build
WORKDIR /app

# Copiamos el pom.xml para descargar dependencias y aprovechar la caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Stage 2: Runtime (Imagen ligera para ejecutar) ----
FROM eclipse-temurin:22-jre-alpine
WORKDIR /app

# Crear un usuario sin privilegios por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiamos el jar desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
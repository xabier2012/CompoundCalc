# ---- Stage 1: Build ----
FROM eclipse-temurin:22-jdk AS build
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
# If mvnw is not present, use Maven from the image directly
RUN if [ -f mvnw ]; then chmod +x mvnw && ./mvnw dependency:go-offline -B; \
    else apt-get update && apt-get install -y maven && mvn dependency:go-offline -B; fi

# Copy source and build
COPY src src
RUN if [ -f mvnw ]; then ./mvnw package -DskipTests -B; \
    else mvn package -DskipTests -B; fi

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:22-jre
WORKDIR /app

# Non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY --from=build /app/target/*.jar app.jar

RUN chown appuser:appuser app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

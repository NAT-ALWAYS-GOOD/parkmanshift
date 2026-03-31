FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
# Pre-copy Maven files to leverage Docker cache
COPY backend/.mvn/ .mvn/
COPY backend/mvnw . 
COPY backend/pom.xml .
# Copy OpenAPI contract BEFORE running package so it's available for the generator
COPY openapi.yaml /openapi.yaml
# Adjusting pom for Docker file path if needed: the pom says ../openapi.yaml, 
# so we place it at /openapi.yaml and run in /app/
RUN ./mvnw dependency:go-offline -B
# Copy whole backend directory
COPY backend/ .
# Ensure we place openapi.yaml at the expected relative path for the build
COPY openapi.yaml ../openapi.yaml
RUN ./mvnw clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# --- Stage 1: Build the application ---
FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app

# Copy the Maven Wrapper files and pom.xml first to leverage Docker caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy the source code
COPY src ./src

# Make the Maven Wrapper script executable
RUN chmod +x mvnw

# Build the Spring Boot application
# Use -Dmaven.test.skip=true to skip tests during the Docker build
RUN ./mvnw clean package -Dmaven.test.skip=true

# --- Stage 2: Create the final, smaller runtime image ---
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your Spring Boot application runs on (default is 8080)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

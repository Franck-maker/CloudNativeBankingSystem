# ================================
# STAGE 1 : Build (The 'Build' Stage)
# ================================
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml first (for dependency caching)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Give execution rights to the Maven wrapper
RUN chmod +x mvnw

# Download dependencies (this step gets cached to speed up future builds)
RUN ./mvnw dependency:go-offline

# Copy the actual source code
COPY src ./src

# Build the application (this will create the JAR file in the target directory)
RUN ./mvnw clean package -DskipTests

# ==========================================
# STAGE 2: Run (The 'Run' Stage)
# ==========================================

FROM eclipse-temurin:21-jre-alpine 

WORKDIR /app

# Create a non-root user for DevSecOps best practices
RUN addgroup -S bankgroup && adduser -S bankuser -G bankgroup
USER bankuser

# Copy ONLY the compiled .jar file from Stage 1
COPY --from=builder /app/target/*.jar app.jar

# 15-Factor : Export services via port binding
EXPOSE 8080

# Run the application : Execute the app as a stateless process (15-Factor)
ENTRYPOINT ["java", "-jar", "app.jar"]

####################################
# Multi-stage Dockerfile for MarketLens Backend
# - Build stage: Compile Java application with Maven
# - Runtime stage: Lightweight JRE with production optimizations
####################################

# ============================================
# Stage 1: Build
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copy pom.xml first (better layer caching)
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (skip tests for faster builds)
# Run tests separately in CI/CD pipeline
RUN mvn clean package -DskipTests -B

# ============================================
# Stage 2: Runtime
# ============================================
FROM eclipse-temurin:17-jre-jammy

# Create non-root user for security
RUN groupadd -r marketlens && useradd -r -g marketlens marketlens

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /build/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R marketlens:marketlens /app

# Switch to non-root user
USER marketlens

# Expose application port
EXPOSE 8001

# Health check (using actuator endpoint)
# Note: Render and most cloud platforms use HTTP health checks instead of Docker HEALTHCHECK
# Uncomment if using Docker Swarm or Kubernetes:
# HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
#   CMD wget --no-verbose --tries=1 --spider http://localhost:8001/actuator/health || exit 1

# JVM Options for production:
# -XX:+UseContainerSupport: Auto-detect container memory limits
# -XX:MaxRAMPercentage=75.0: Use up to 75% of container memory
# -XX:InitialRAMPercentage=50.0: Start with 50% of container memory
# -XX:+ExitOnOutOfMemoryError: Exit on OOM (container orchestrator will restart)
# -Djava.security.egd=file:/dev/./urandom: Faster startup (non-blocking entropy)
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+ExitOnOutOfMemoryError \
               -Djava.security.egd=file:/dev/./urandom"

# Run application with prod profile
# SPRING_PROFILES_ACTIVE can be overridden via environment variable
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

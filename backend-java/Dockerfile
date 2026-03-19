####################################
# Multi-stage Dockerfile for MarketLens Backend
####################################

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-jammy

RUN groupadd -r marketlens && useradd -r -g marketlens marketlens
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar
RUN chown -R marketlens:marketlens /app

USER marketlens

EXPOSE 10000

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+ExitOnOutOfMemoryError \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-10000} -jar app.jar"]
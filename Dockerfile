# Multi-stage build: Maven package → slim JRE runtime
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN useradd --system --create-home --uid 10001 campus \
    && mkdir -p /app/media /app/logs \
    && chown -R campus:campus /app
COPY --from=build /workspace/target/campus-share-1.0.0.jar /app/app.jar
USER campus
EXPOSE 8085
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

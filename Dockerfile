FROM maven:3.9-eclipse-temurin-21 AS backend-build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN apk add --no-cache curl \
    && addgroup -S astore \
    && adduser -S astore -G astore \
    && mkdir -p /app/logs/archive \
    && chown -R astore:astore /app

COPY --chown=astore:astore --from=backend-build /workspace/target/*.jar app.jar

EXPOSE 8080

USER astore

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS "http://localhost:8080/actuator/health" | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]

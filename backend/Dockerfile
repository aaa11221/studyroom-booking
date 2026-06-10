ARG MAVEN_IMAGE=maven:3.9-eclipse-temurin-11-alpine
ARG JRE_IMAGE=bellsoft/liberica-openjre-alpine:11

FROM ${MAVEN_IMAGE} AS builder
WORKDIR /workspace

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
COPY tests ./tests
RUN mvn -q -DskipTests package

FROM ${MAVEN_IMAGE} AS dev
WORKDIR /workspace

RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /home/app/.m2 \
    && chown -R app:app /home/app
USER app

ENV SERVER_PORT=9099
EXPOSE 9099

CMD ["mvn", "spring-boot:run"]

FROM ${JRE_IMAGE}
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=builder /workspace/target/*.jar /app/app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

RUN chmod +x /app/docker-entrypoint.sh && chown -R app:app /app
USER app

ENV SERVER_PORT=9099
EXPOSE 9099

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:${SERVER_PORT}/health || exit 1

ENTRYPOINT ["/app/docker-entrypoint.sh"]

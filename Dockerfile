# syntax=docker/dockerfile:1

# ---- Build stage: compile and package with the Maven Wrapper ------------------
FROM eclipse-temurin:24-jdk-noble AS build
WORKDIR /workspace
# Copy the build definition first so the dependency layer is cached between builds.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp package -DskipTests \
    && java -Djarmode=tools -jar target/app.jar extract --layers --launcher --destination target/extracted

# ---- Runtime stage: JRE only, non-root, layered for fast rebuilds -------------
FROM eclipse-temurin:24-jre-noble
WORKDIR /app
RUN groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app
COPY --from=build /workspace/target/extracted/dependencies/ ./
COPY --from=build /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/target/extracted/application/ ./
USER 10001:10001
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080"]
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

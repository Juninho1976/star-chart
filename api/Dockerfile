FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Show versions + run build with full output (no -q), and list target dir even if build fails
RUN bash -lc 'mvn -v && \
  mvn -U -DskipTests package || (echo "==== MAVEN FAILED; listing /app and /app/target ====" && ls -la /app && ls -la /app/target || true && exit 1)'

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/api.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/api.jar"]
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn --batch-mode --no-transfer-progress \
    -pl participant -am package \
    -Dmaven.test.skip=true -Dcheckstyle.skip=true

FROM gcr.io/distroless/java21-debian12
WORKDIR /app
COPY --from=build /app/participant/target/participant-*.jar app.jar
EXPOSE 8444
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jdk-alpine AS build

ARG APP_MODULE=local-app
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY core-domain/build.gradle ./core-domain/build.gradle
COPY sync-module/build.gradle ./sync-module/build.gradle
COPY shared-infra/build.gradle ./shared-infra/build.gradle
COPY local-app/build.gradle ./local-app/build.gradle
COPY online-app/build.gradle ./online-app/build.gradle

COPY core-domain/src ./core-domain/src
COPY sync-module/src ./sync-module/src
COPY shared-infra/src ./shared-infra/src
COPY local-app/src ./local-app/src
COPY online-app/src ./online-app/src

RUN ./gradlew ":${APP_MODULE}:bootJar" --no-daemon

FROM eclipse-temurin:21-jre-alpine

ARG APP_MODULE=local-app
WORKDIR /app

COPY --from=build "/workspace/${APP_MODULE}/build/libs/"*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

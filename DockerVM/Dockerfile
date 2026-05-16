FROM node:22.12-slim AS frontend
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

RUN apt-get update && apt-get install -y dos2unix findutils && rm -rf /var/lib/apt/lists/*

COPY BoggleSpringBoot/ ./

RUN dos2unix gradlew && chmod +x gradlew

RUN rm -rf /app/src/main/resources/static && mkdir -p /app/src/main/resources/static
COPY --from=frontend /frontend/dist/ /app/src/main/resources/static/

RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
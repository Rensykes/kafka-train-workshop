FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-focal

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# We connect to Kafka using the service name 'kafka' from docker-compose
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.kafka.bootstrap-servers=kafka:29092"]
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY avro-schemas /app/avro-schemas
COPY workshop-kafka-train-avro-producer/pom.xml .
RUN mvn dependency:go-offline
COPY workshop-kafka-train-avro-producer/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]

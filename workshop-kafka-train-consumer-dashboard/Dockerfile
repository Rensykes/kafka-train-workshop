    FROM maven:3.9-eclipse-temurin-17 AS build
    WORKDIR /build
    COPY pom.xml .
    RUN mvn dependency:go-offline
    COPY src ./src
    RUN mvn package -DskipTests

    FROM eclipse-temurin:17-jre-focal
    
    WORKDIR /app
        COPY --from=build /build/target/*.jar app.jar

    ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.kafka.bootstrap-servers=kafka:29092"]
    FROM maven:3.9-eclipse-temurin-17 AS build
    WORKDIR /build
    COPY pom.xml .
    RUN mvn dependency:go-offline
    COPY src ./src
    RUN mvn package -DskipTests

    FROM eclipse-temurin:17-jre-focal
    
    WORKDIR /app
        COPY --from=build /build/target/*.jar app.jar
    
    COPY --chmod=755 entrypoint.sh .
    
    ENTRYPOINT ["./entrypoint.sh"]
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/FrudokoGame-1.0-SNAPSHOT-launcher.jar app.jar
COPY --from=build /app/target/FrudokoGame.war ./target/FrudokoGame.war
EXPOSE 8080
CMD ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "app.jar"]
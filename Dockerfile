FROM eclipse-temurin:21 AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine-3.23
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

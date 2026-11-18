# ---------- Etapa 1: compilar el .jar con Maven (Java 17) ----------
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Etapa 2: imagen de ejecución (la misma base que en clase) ----------
FROM amazoncorretto:17
WORKDIR /app
COPY --from=build /app/target/java-api-demo.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

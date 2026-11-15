FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/store-orders-api-1.0.0.jar app.jar
ENV PORT=8080
EXPOSE 8080
CMD ["sh", "-c", "java -Xmx300m -jar app.jar --server.port=${PORT}"]

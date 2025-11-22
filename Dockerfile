# syntax=docker/dockerfile:1.7

FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace/app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17.0.11_9-jre
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m" \
    TZ=Asia/Shanghai
WORKDIR /app
COPY --from=builder /workspace/app/target/medical-booking-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

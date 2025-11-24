FROM maven:3.8-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=builder /app/target/VulnTodoApp-1.0.war /usr/local/tomcat/webapps/ROOT.war

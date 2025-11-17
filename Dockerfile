# --- Stage 1: Build Stage ---
FROM maven:3.8-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Lệnh này sẽ tạo ra một file .war chứa cả postgresql-driver.jar bên trong WEB-INF/lib
RUN mvn clean package


# --- Stage 2: Runtime Stage ---
FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*

# Chỉ cần copy file WAR. Tomcat sẽ tự xử lý các thư viện bên trong nó.
COPY --from=builder /app/target/VulnTodoApp-1.0.war /usr/local/tomcat/webapps/ROOT.war
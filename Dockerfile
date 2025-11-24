FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    mysql-server \
    maven \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/local
RUN wget https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.19/bin/apache-tomcat-10.1.19.tar.gz \
    && tar xvf apache-tomcat-10.1.19.tar.gz \
    && mv apache-tomcat-10.1.19 tomcat \
    && rm apache-tomcat-10.1.19.tar.gz

RUN echo "[mysqld]\nsecure-file-priv=\"\"\ndefault-authentication-plugin=mysql_native_password" >> /etc/mysql/my.cnf

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

RUN rm -rf /usr/local/tomcat/webapps/ROOT
RUN cp target/VulnTodoApp-1.0.war /usr/local/tomcat/webapps/ROOT.war

RUN chmod 755 /usr/local/tomcat/webapps
RUN mkdir -p /usr/local/tomcat/webapps/ROOT && \
    cd /usr/local/tomcat/webapps/ROOT && \
    jar -xvf ../ROOT.war && \
    chmod -R 777 /usr/local/tomcat/webapps/ROOT

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8080

CMD ["/entrypoint.sh"]
# ---- Build stage (opcjonalnie, jeśli chcesz multi-stage build) ----
# FROM maven:3.9.6-eclipse-temurin-17 AS build
# WORKDIR /app
# COPY pom.xml .
# COPY src ./src
# RUN mvn clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/ftp-server-1.0-SNAPSHOT-jar-with-dependencies.jar /app/ftp_fs.jar
COPY keystore.jks /app/keystore.jks
EXPOSE 2121
# Pozwól na przekazanie zmiennych środowiskowych do aplikacji
ENV SERVER_PORT=2121
ENV SERVER_FILES_DIR=server_files
ENV DB_URL=""
ENV DB_USER=""
ENV DB_PASSWORD=""
ENV DB_FILES_DIRECTORY=server_files
ENV DB_URL_NO_DB=""
ENV SSL_KEYSTORE=/app/keystore.jks
ENV SSL_KEYSTORE_PASS=dysmex-wyBdod-nydfe7
ENTRYPOINT ["java", "-jar", "/app/ftp_fs.jar"] 


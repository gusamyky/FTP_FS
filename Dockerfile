# Use Java 17 as the base image with platform specification
FROM --platform=linux/amd64 openjdk:17-slim

# Set working directory
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Build the application
RUN mvn clean package -DskipTests

# Create directory for server files
RUN mkdir -p /app/server_files

# Expose the port the app runs on
EXPOSE 2121

# Set environment variables
ENV SERVER_PORT=2121
ENV SERVER_HOST=0.0.0.0
ENV SERVER_FILES_DIR=/app/server_files

# Command to run the application
CMD ["java", "-jar", "target/ftp-server-1.0-SNAPSHOT.jar"] 
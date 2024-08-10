FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the application's JAR file to the container
COPY target/money-transfer-service-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 to the outside world
EXPOSE 8080

# Define the command to run your app using java
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

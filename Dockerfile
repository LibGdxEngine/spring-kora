# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/*.jar /app/app.jar

# Define the command to run your Spring Boot application
CMD ["java", "-jar", "app.jar"]

# 1. Use an official Java Runtime as the base image for building
FROM maven:3.8.7-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project code into the container
COPY . .

# Package the application (runs 'mvn clean install')
RUN mvn clean install -DskipTests

# 2. Use a smaller, production-ready JRE base image for the final stage
FROM eclipse-temurin:17-jre-alpine

# Set the environment variable for the port (Render will override this, but it's a good default)
ENV PORT 10000

# Set the working directory in the final image
WORKDIR /usr/app

# Copy the built JAR file from the 'build' stage
# CRITICAL: Replace 'DNDapp-1.0-SNAPSHOT.jar' with the actual name of your compiled JAR file
COPY --from=build /app/target/DNDapp-1.0-SNAPSHOT.jar ./app.jar

# Expose the port where the Java application is listening (Render handles this via a proxy)
EXPOSE ${PORT}

# Define the command to run the application
# This executes the main method in your GameServer.java via the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
